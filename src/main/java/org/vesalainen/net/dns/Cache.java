/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net.dns;

import org.vesalainen.net.IllegalNetMaskException;
import org.vesalainen.net.SubNet;
import org.vesalainen.net.dns.jaxb.dns.Dns;
import org.vesalainen.net.dns.jaxb.dns.Dns.Master;
import org.vesalainen.net.dns.jaxb.dns.Dns.NameServer;
import org.vesalainen.net.dns.jaxb.dns.Dns.Slave;
import org.vesalainen.net.dns.jaxb.dns.Dns.Zone;
import org.vesalainen.net.dns.jaxb.dns.ObjectFactory;
import org.vesalainen.net.dns.jaxb.dns.ResourceRecord.ARr;
import org.vesalainen.net.dns.jaxb.dns.ResourceRecord.CnameRr;
import org.vesalainen.net.dns.jaxb.dns.ResourceRecord.MxRr;
import org.vesalainen.net.dns.jaxb.dns.ResourceRecord.NsRr;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.vesalainen.util.ConcurrentHashMapSet;
import org.vesalainen.util.HashMapSet;
import org.vesalainen.util.MapSet;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author tkv
 */
public class Cache implements Runnable
{
    public static final String PACKAGE = "org.vesalainen.net.dns.jaxb.dns";
    
    public static final JavaLogging log = new JavaLogging(Cache.class);

    private static final Map<DomainName,MapSet<Question,ResourceRecord>> outerZones = new HashMap<>();
    private static final Map<DomainName,ResponseMessage> zoneTransferMessage = new HashMap<>();
    private static final Map<DomainName,MapSet<Question,ResourceRecord>> innerZones = new HashMap<>();
    private static final Map<SubNet,MapSet<Question,ResourceRecord>> innerNets = new HashMap<>();
    private static MapSet<Question,ResourceRecord> cache = new ConcurrentHashMapSet<>();
    private static final List<InetAddress> nameServers = new ArrayList<>();
    private static final List<InetAddress> slaves = new ArrayList<>();
    private static final Map<DomainName,InetAddress> masters = new HashMap<>();
    private static File config;
    private static Dns dns;
    private static final ObjectFactory factory = new ObjectFactory();
    private static Clock clock = Clock.systemUTC();
    private static long cleanupInterval;

    public Cache(File conf) throws JAXBException, UnknownHostException, IllegalNetMaskException, IOException, RCodeException
    {
        config = conf;
        JAXBContext jaxbCtx = JAXBContext.newInstance(PACKAGE);
        Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
        dns = (Dns) unmarshaller.unmarshal(config); //NOI18N
        BigInteger cui = dns.getCleanupInterval();
        cleanupInterval = cui == null ? 1000 : cui.longValue();
        init();
        initZones();
        initInnerZones();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        if (cleanupInterval > 0)
        {
            scheduler.scheduleAtFixedRate(this, 0, cleanupInterval, TimeUnit.SECONDS);
        }
    }

    public static void save() throws JAXBException, FileNotFoundException, IOException
    {
        try (FileOutputStream out = new FileOutputStream(config))
        {
            JAXBContext jaxbCtx = JAXBContext.newInstance(PACKAGE);
            Marshaller marshaller = jaxbCtx.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "utf-8");
            //NOI18N
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(dns, out);
        }
    }

    public static void storeCache(File file) throws FileNotFoundException, IOException
    {
        log.config("saving %d query answers sets", cache.size());
        try (ObjectOutputStream ois = new ObjectOutputStream(new FileOutputStream(file)))
        {
            ois.writeObject(cache);
        }
    }

    public static void restoreCache(File file) throws FileNotFoundException, IOException, ClassNotFoundException
    {
        try (ObjectInputStream iis = new ObjectInputStream(new FileInputStream(file)))
        {
            cache = (ConcurrentHashMapSet<Question, ResourceRecord>) iis.readObject();
            log.config("restoring %d query answers sets", cache);
        }
    }

    private static void init() throws UnknownHostException
    {
        for (NameServer nameServer : dns.getNameServer())
        {
            nameServers.add(InetAddress.getByName(nameServer.getAddress()));
        }
        for (Master master : dns.getMaster())
        {
            masters.put(new DomainName(master.getDomain()), InetAddress.getByName(master.getAddress()));
        }
        for (Slave slave : dns.getSlave())
        {
            slaves.add(InetAddress.getByName(slave.getAddress()));
        }
    }
    private static void initZones() throws UnknownHostException, IllegalNetMaskException
    {
        DomainName arpa = new DomainName("in-addr.arpa");
        MapSet<Question,ResourceRecord> arpaMapSet = new HashMapSet<Question,ResourceRecord>();
        for (Zone zone : dns.getZone())
        {
            DomainName domain = new DomainName(zone.getName());
            SOA soa = new SOA(
                    zone.getMname(),
                    zone.getRname(),
                    zone.getSerial().intValue(),
                    zone.getRefresh().intValue(),
                    zone.getRetry().intValue(),
                    zone.getExpire().intValue(),
                    zone.getMinimum().intValue()
                    );
            MapSet<Question,ResourceRecord> outerMapSet = new HashMapSet<Question,ResourceRecord>();
            ResourceRecord soaRR = new ResourceRecord(domain, Constants.SOA, soa.getMinimum(), soa);
            outerMapSet.add(soaRR.getQuestion(), soaRR);
            ResourceRecord[] zt = new ResourceRecord[zone.getARrOrMxRrOrNsRr().size()+2];
            int zti = 0;
            zt[zti++] = soaRR;
            for (Object oo : zone.getARrOrMxRrOrNsRr())
            {
                ResourceRecord rr = addRR(domain, outerMapSet, oo);
                zt[zti++] = rr;
                rr = rr.getPTR();
                if (rr != null)
                {
                    arpaMapSet.add(rr.getQuestion(), rr);
                }
            }
            zt[zti++] = soaRR;
            ResponseMessage zoneTransfer = new ResponseMessage(
                    0,
                    null,
                    true,
                    soaRR.getQuestion(),
                    zt,
                    null,
                    null
                    );
            zoneTransferMessage.put(domain, zoneTransfer);
            outerZones.put(domain, outerMapSet);
            outerZones.put(arpa, arpaMapSet);
        }
    }
    private static void initInnerZones() throws UnknownHostException, IllegalNetMaskException
    {
        for (Zone zone : dns.getZone())
        {
            DomainName domain = new DomainName(zone.getName());
            SOA soa = new SOA(
                    zone.getMname(),
                    zone.getRname(),
                    zone.getSerial().intValue(),
                    zone.getRefresh().intValue(),
                    zone.getRetry().intValue(),
                    zone.getExpire().intValue(),
                    zone.getMinimum().intValue()
                    );
            MapSet<Question,ResourceRecord> innerMapSet = innerZones.get(domain);
            if (innerMapSet == null)
            {
                innerMapSet = new HashMapSet<Question,ResourceRecord>();
                innerZones.put(domain, innerMapSet);
            }
            SubNet subnet = new SubNet(dns.getInside().getNet());
            innerNets.put(subnet, innerMapSet);
            for (Object oo : dns.getInside().getARrOrMxRrOrNsRr())
            {
                addRR(domain, innerMapSet, oo);
            }
        }
    }

    private static ResourceRecord addRR(DomainName dn, MapSet<Question,ResourceRecord> map, Object oo) throws UnknownHostException
    {
        if (oo instanceof ARr)
        {
            ARr a = (ARr) oo;
            Inet4Address ad = (Inet4Address) Inet4Address.getByName(a.getAddress());
            A aa = new A(ad);
            ResourceRecord rr = new ResourceRecord(new DomainName(a.getName()), Constants.A, a.getTtl().intValue(), aa);
            map.add(rr.getQuestion(), rr);
            return rr;
        }
        if (oo instanceof MxRr)
        {
            MxRr mxr = (MxRr) oo;
            MX mx = new MX(mxr.getExchange(), mxr.getPreference().intValue());
            ResourceRecord rr = new ResourceRecord(dn, Constants.MX, mxr.getTtl().intValue(), mx);
            map.add(rr.getQuestion(), rr);
            return rr;
        }
        if (oo instanceof NsRr)
        {
            NsRr nsr = (NsRr) oo;
            NS ns = new NS(nsr.getNsdname());
            ResourceRecord rr = new ResourceRecord(dn, Constants.NS, nsr.getTtl().intValue(), ns);
            map.add(rr.getQuestion(), rr);
            return rr;
        }
        if (oo instanceof CnameRr)
        {
            CnameRr cnamerr = (CnameRr) oo;
            CName cname = new CName(new DomainName(cnamerr.getCname()));
            ResourceRecord rr = new ResourceRecord(new DomainName(cnamerr.getName()), Constants.CNAME, cnamerr.getTtl().intValue(), cname);
            map.add(rr.getQuestion(), rr);
            return rr;
        }
        throw new IllegalArgumentException(oo.toString()+" unknown");
    }

    public static void refreshZones() throws IOException, RCodeException, UnknownHostException, IllegalNetMaskException, JAXBException
    {
        for (DomainName name : masters.keySet())
        {
            InetAddress address = masters.get(name);
            Message msg = null;
            try
            {
                msg = ZoneTransfer.getZone(name, new InetSocketAddress(address, 53));
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
                continue;
            }
            catch (RCodeException ex)
            {
                ex.printStackTrace();
                continue;
            }
            ResourceRecord[] ar = msg.getAnswers();
            if (ar.length > 0)
            {
                Zone target = null;
                for (Zone zone : dns.getZone())
                {
                    if (name.toString().equalsIgnoreCase(zone.getName()))
                    {
                        target = zone;
                        break;
                    }
                }
                if (target != null)
                {
                    dns.getZone().remove(target);
                }
                target = factory.createDnsZone();
                dns.getZone().add(target);
                for (int ii=0;ii<ar.length-1;ii++)
                {
                    ResourceRecord rr = ar[ii];
                    switch (rr.getType())
                    {
                        case Constants.SOA:
                        {
                            SOA soa = (SOA) rr.getRData();
                            target.setName(rr.getName().toString());
                            target.setTtl(BigInteger.valueOf(rr.getTtl()));
                            target.setMname(soa.getMName().toString());
                            target.setRname(soa.getRName().toString());
                            target.setSerial(BigInteger.valueOf(soa.getSerial()));
                            target.setRefresh(BigInteger.valueOf(soa.getRefresh()));
                            target.setRetry(BigInteger.valueOf(soa.getRetry()));
                            target.setExpire(BigInteger.valueOf(soa.getExpire()));
                            target.setMinimum(BigInteger.valueOf(soa.getMinimum()));
                        }
                        break;
                        case Constants.A:
                        {
                            A a = (A) rr.getRData();
                            ARr arr = factory.createResourceRecordARr();
                            target.getARrOrMxRrOrNsRr().add(arr);
                            arr.setName(rr.getName().toString());
                            arr.setTtl(BigInteger.valueOf(rr.getTtl()));
                            arr.setAddress(a.getAddress().getHostAddress());
                        }
                        break;
                        case Constants.MX:
                        {
                            MX mx = (MX) rr.getRData();
                            MxRr mxrr = factory.createResourceRecordMxRr();
                            target.getARrOrMxRrOrNsRr().add(mxrr);
                            mxrr.setName(rr.getName().toString());
                            mxrr.setTtl(BigInteger.valueOf(rr.getTtl()));
                            mxrr.setPreference(BigInteger.valueOf(mx.getPreference()));
                            mxrr.setExchange(mx.getExchange().toString());
                        }
                        break;
                        case Constants.NS:
                        {
                            NS ns = (NS) rr.getRData();
                            NsRr nsrr = factory.createResourceRecordNsRr();
                            target.getARrOrMxRrOrNsRr().add(nsrr);
                            nsrr.setName(rr.getName().toString());
                            nsrr.setTtl(BigInteger.valueOf(rr.getTtl()));
                            nsrr.setNsdname(ns.getNsDName().toString());
                        }
                        break;
                    }
                }
            }
        }
        initZones();
        save();
    }

    public static boolean resolveAuthorative(Question question, InetSocketAddress sender, Answer answer)
    {
        return resolveAuthorative(question.getQName(), question.getQType(), sender, answer);
    }

    public static boolean resolveAuthorative(DomainName dn, int type, InetSocketAddress sender, Answer answer)
    {
        Question question = new Question(dn, type);
        SubNet subnet = inner(sender);
        if (subnet != null)
        {
            return getInner(question, subnet, answer);
        }
        else
        {
            return getZone(question, answer);
        }
    }

    public static SubNet inner(InetSocketAddress sender)
    {
        for (SubNet subnet : innerNets.keySet())
        {
            if (subnet.includes(sender.getAddress()))
            {
                return subnet;
            }
        }
        return null;
    }
    public static boolean getInner(Question question, SubNet subnet, Answer answer)
    {
        boolean found = false;
        MapSet<Question,ResourceRecord> map = innerNets.get(subnet);
        Set<ResourceRecord> set = map.get(question);
        if (set != null)
        {
            answer.getAnswers().addAll(set);
            found = true;
        }
        set = map.get(question.getCName());
        if (set != null)
        {
            answer.getAnswers().addAll(set);
            found = true;
        }
        return found;
    }
    public static boolean getZone(Question question, Answer answer)
    {
        boolean found = false;
        DomainName qName = question.getQName();
        for (DomainName dn : outerZones.keySet())
        {
            if (qName.isSubDomainOf(dn))
            {
                MapSet<Question,ResourceRecord> map = outerZones.get(dn);
                if (question.getQType() != Constants.ANY)
                {
                    Set<ResourceRecord> set = map.get(question);
                    if (set != null)
                    {
                        answer.getAnswers().addAll(set);
                        found = true;
                    }
                    set = map.get(question.getCName());
                    if (set != null)
                    {
                        answer.getAnswers().addAll(set);
                        found = true;
                    }
                    set = map.get(new Question(dn, Constants.NS));
                    if (set != null)
                    {
                        answer.getAuthorities().addAll(set);
                        for (ResourceRecord rr : set)
                        {
                            NS ns = (NS) rr.getRData();
                            DomainName nsdn = ns.getNsDName();
                            Set<ResourceRecord> nsset = map.get(new Question(nsdn, Constants.A));
                            if (nsset != null)
                            {
                                answer.getAdditionals().addAll(nsset);
                            }
                            nsset = map.get(new Question(nsdn, Constants.AAAA));
                            if (nsset != null)
                            {
                                answer.getAdditionals().addAll(nsset);
                            }
                        }
                    }
                }
                else
                {
                    for (Question q : map.keySet())
                    {
                        answer.getAnswers().addAll(map.get(q));
                        found = true;
                    }
                }
            }
        }
        return found;
    }
    public static void getFromCache(Question question, Answer answer)
    {
        boolean found = false;
        long current = System.currentTimeMillis();
        Set<ResourceRecord> set = cache.get(question);
        if (set != null)
        {
            answer.getAnswers().addAll(set);
        }
        set = cache.get(question.getCName());
        if (set != null)
        {
            answer.getAnswers().addAll(set);
        }
    }

    public static Set<InetAddress> getNameServerFor(DomainName domain)
    {

        Set<InetAddress> res = new HashSet<>();
        DomainName baseDomain = domain;
        while (baseDomain.getLevel() > 1)
        {
            // NS
            Question qNS = new Question(baseDomain, Constants.NS);
            Answer answer = new Answer();
            getFromCache(qNS, answer);
            if (answer.hasAnswer())
            {
                Answer answer2 = new Answer();
                for (ResourceRecord rrNS : answer.getAnswers())
                {
                    RData rData = rrNS.getRData();
                    if (rData instanceof NS)
                    {
                        NS ns = (NS) rData;
                        Question qA = new Question(ns.getNsDName(), Constants.A);
                        getFromCache(qA, answer2);
                        Question qAAAA = new Question(ns.getNsDName(), Constants.AAAA);
                        getFromCache(qAAAA, answer2);
                        for (ResourceRecord rrA : answer2.getAnswers())
                        {
                            RData rd = rrA.getRData();
                            if (rd instanceof A)
                            {
                                A a = (A) rd;
                                res.add(a.getAddress());
                            }
                        }
                    }
                }
            }
            // SOA
            Question qSOA = new Question(baseDomain, Constants.SOA);
            Answer answerSOA = new Answer();
            getFromCache(qSOA, answerSOA);
            if (answerSOA.hasAnswer())
            {
                Answer answer2 = new Answer();
                for (ResourceRecord rrNS : answerSOA.getAnswers())
                {
                    RData rData = rrNS.getRData();
                    if (rData instanceof SOA)
                    {
                        SOA soa = (SOA) rData;
                        Question qA = new Question(soa.getMName(), Constants.A);
                        getFromCache(qA, answer2);
                        Question qAAAA = new Question(soa.getMName(), Constants.AAAA);
                        getFromCache(qAAAA, answer2);
                        for (ResourceRecord rrA : answer2.getAnswers())
                        {
                            RData rd = rrA.getRData();
                            if (rd instanceof A)
                            {
                                A a = (A) rd;
                                res.add(a.getAddress());
                            }
                        }
                    }
                }
            }
            baseDomain = baseDomain.getBaseDomain();
        }
        return res;
    }
    private void cleanup()
    {
        cache.entrySet().stream().forEach((e) ->
        {
            e.getValue().removeIf(ResourceRecord::isStale);
        });
    }

    public static void add(MapSet<Question, ResourceRecord> map)
    {
        map.entrySet().stream().forEach((e) ->
        {
            Question q = e.getKey();
            Set<ResourceRecord> set = e.getValue();
            log.finest("add %s count=%d", q, set.size());
            set.forEach(ResourceRecord::setExpires);
            Set<ResourceRecord> oldSet = cache.get(q);
            if (oldSet != null)
            {
                oldSet.removeIf(ResourceRecord::isStale);
            }
            cache.addAll(q, set);
        });
    }

    /**
     * @return the nameServers
     */
    public static List<InetAddress> getNameServers()
    {
        return nameServers;
    }

    /**
     * @return the slaves
     */
    public static List<InetAddress> getSlaves()
    {
        return slaves;
    }

    /**
     * @return the masters
     */
    public static Map<DomainName, InetAddress> getMasters()
    {
        return masters;
    }

    public static ResponseMessage getZoneTransfer(DomainName name)
    {
        return zoneTransferMessage.get(name);
    }

    public static Clock getClock()
    {
        return clock;
    }

    @Override
    public void run()
    {
        try
        {
            if (cleanupInterval != 0)
            {
                cleanup();
            }
            refreshZones();
        }
        catch (IOException | RCodeException | IllegalNetMaskException | JAXBException ex)
        {
            log.log(Level.SEVERE, ex, "%s", ex.getMessage());
        }
    }
}
