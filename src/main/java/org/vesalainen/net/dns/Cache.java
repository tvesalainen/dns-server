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
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.vesalainen.util.ConcurrentHashMapSet;
import org.vesalainen.util.HashMapSet;
import org.vesalainen.util.MapSet;

/**
 *
 * @author tkv
 */
public class Cache implements Runnable
{
    public static final String PACKAGE = "org.vesalainen.net.dns.jaxb.dns";

    private static Map<DomainName,MapSet<Question,ResourceRecord>> outerZones = new HashMap<DomainName,MapSet<Question,ResourceRecord>>();
    private static Map<DomainName,ResponseMessage> zoneTransferMessage = new HashMap<DomainName,ResponseMessage>();
    private static Map<DomainName,MapSet<Question,ResourceRecord>> innerZones = new HashMap<DomainName,MapSet<Question,ResourceRecord>>();
    private static Map<SubNet,MapSet<Question,ResourceRecord>> innerNets = new HashMap<SubNet,MapSet<Question,ResourceRecord>>();
    private static MapSet<Question,ResourceRecord> cache = new ConcurrentHashMapSet<Question,ResourceRecord>();
    private static List<InetSocketAddress> nameServers = new ArrayList<InetSocketAddress>();
    private static List<InetSocketAddress> slaves = new ArrayList<InetSocketAddress>();
    private static Map<DomainName,InetSocketAddress> masters = new HashMap<DomainName,InetSocketAddress>();
    private static File config;
    private static Dns dns;
    private static ObjectFactory factory = new ObjectFactory();

    public Cache(File conf) throws JAXBException, UnknownHostException, IllegalNetMaskException, IOException, RCodeException
    {
        config = conf;
        JAXBContext jaxbCtx = JAXBContext.newInstance(PACKAGE);
        Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
        dns = (Dns) unmarshaller.unmarshal(config); //NOI18N
        init();
        initZones();
        initInnerZones();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this, 0, 1, TimeUnit.HOURS);
    }

    public static void save() throws JAXBException, FileNotFoundException, IOException
    {
        FileOutputStream out = new FileOutputStream(config);
        JAXBContext jaxbCtx = JAXBContext.newInstance(PACKAGE);
        Marshaller marshaller = jaxbCtx.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "utf-8");
        //NOI18N
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(dns, out);
        out.close();
    }

    public static void storeCache(File file) throws FileNotFoundException, IOException
    {
        ObjectOutputStream ois = new ObjectOutputStream(new FileOutputStream(file));
        ois.writeObject(cache);
        ois.close();
    }

    public static void restoreCache(File file) throws FileNotFoundException, IOException, ClassNotFoundException
    {
        ObjectInputStream iis = new ObjectInputStream(new FileInputStream(file));
        cache = (ConcurrentHashMapSet<Question, ResourceRecord>) iis.readObject();
        iis.close();
    }

    private static void init()
    {
        for (NameServer nameServer : dns.getNameServer())
        {
            nameServers.add(new InetSocketAddress(nameServer.getAddress(), 53));
        }
        for (Master master : dns.getMaster())
        {
            masters.put(new DomainName(master.getDomain()), new InetSocketAddress(master.getAddress(), 53));
        }
        for (Slave slave : dns.getSlave())
        {
            slaves.add(new InetSocketAddress(slave.getAddress(), 53));
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
            InetSocketAddress address = masters.get(name);
            Message msg = null;
            try
            {
                msg = ZoneTransfer.getZone(name, address);
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
                            arr.setAddress(a.getAddress().getAddress().getHostAddress());
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
    public static boolean getCache(Question question, Answer answer)
    {
        boolean found = false;
        Set<ResourceRecord> set = cache.get(question);
        if (set != null)
        {
            Set removables = new ConcurrentSkipListSet<ResourceRecord>();
            long current = System.currentTimeMillis();
            for (ResourceRecord rr : set)
            {
                if (rr.expired(current))
                {
                    removables.add(rr);
                }
            }
            set.removeAll(removables);
            found = !set.isEmpty() || found;
            answer.getAnswers().addAll(set);
        }
        set = cache.get(question.getCName());
        if (set != null)
        {
            Set removables = new ConcurrentSkipListSet<ResourceRecord>();
            long current = System.currentTimeMillis();
            for (ResourceRecord rr : set)
            {
                if (rr.expired(current))
                {
                    removables.add(rr);
                }
            }
            set.removeAll(removables);
            found = !set.isEmpty() || found;
            answer.getAnswers().addAll(set);
        }
        return found;
    }

    public static List<InetSocketAddress> getNameServerFor(DomainName domain)
    {

        List<InetSocketAddress> res = new ArrayList<InetSocketAddress>();
        DomainName baseDomain = domain;
        while (baseDomain.getLevel() > 1)
        {
            // NS
            Question qNS = new Question(baseDomain, Constants.NS);
            Answer answer = new Answer();
            if (getCache(qNS, answer))
            {
                Answer answer2 = new Answer();
                for (ResourceRecord rrNS : answer.getAnswers())
                {
                    NS ns = (NS) rrNS.getRData();
                    Question qA = new Question(ns.getNsDName(), Constants.A);
                    getCache(qA, answer2);
                    Question qAAAA = new Question(ns.getNsDName(), Constants.AAAA);
                    getCache(qAAAA, answer2);
                    for (ResourceRecord rrA : answer2.getAnswers())
                    {
                        A a = (A) rrA.getRData();
                        res.add(a.address);
                    }
                }
            }
            // SOA
            Question qSOA = new Question(baseDomain, Constants.SOA);
            Answer answerSOA = new Answer();
            if (getCache(qSOA, answerSOA))
            {
                Answer answer2 = new Answer();
                for (ResourceRecord rrNS : answerSOA.getAnswers())
                {
                    SOA soa = (SOA) rrNS.getRData();
                    Question qA = new Question(soa.getMName(), Constants.A);
                    getCache(qA, answer2);
                    Question qAAAA = new Question(soa.getMName(), Constants.AAAA);
                    getCache(qAAAA, answer2);
                    for (ResourceRecord rrA : answer2.getAnswers())
                    {
                        A a = (A) rrA.getRData();
                        res.add(a.address);
                    }
                }
            }
            baseDomain = baseDomain.getBaseDomain();
        }
        return res;
    }
    private void cleanup()
    {
        Set qqRemovables = new ConcurrentSkipListSet<Question>();
        for (Question qq : cache.keySet())
        {
            Set<ResourceRecord> set = cache.get(qq);
            Set rrRemovables = new ConcurrentSkipListSet<ResourceRecord>();
            long current = System.currentTimeMillis();
            for (ResourceRecord rr : set)
            {
                if (rr.expired(current))
                {
                    rrRemovables.add(rr);
                }
            }
            set.removeAll(rrRemovables);
            if (set.isEmpty())
            {
                qqRemovables.add(qq);
            }
        }
        cache.remove(qqRemovables);
    }

    public static void add(ResourceRecord rr)
    {
        if (rr.getTtl() > 0)
        {
            rr.setExpires(new Date());
            cache.add(rr.getQuestion(), rr);
        }
    }

    /**
     * @return the nameServers
     */
    public static List<InetSocketAddress> getNameServers()
    {
        return nameServers;
    }

    /**
     * @return the slaves
     */
    public static List<InetSocketAddress> getSlaves()
    {
        return slaves;
    }

    /**
     * @return the masters
     */
    public static Map<DomainName, InetSocketAddress> getMasters()
    {
        return masters;
    }

    public static ResponseMessage getZoneTransfer(DomainName name)
    {
        return zoneTransferMessage.get(name);
    }

    public static void main(String... args)
    {
        try
        {
            File ff = new File("C:\\Users\\tkv\\Documents\\NetBeansProjects\\DNSServer\\src\\fi\\sw_nets\\net\\dns\\dns.xml");
            Cache cc = new Cache(ff);
            Answer answer = new Answer();
            Question question = new Question("valpuri.org", Constants.MX);
            System.err.println(cc.getZone(question, answer));
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void run()
    {
        try
        {
            cleanup();
            refreshZones();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        catch (RCodeException ex)
        {
            ex.printStackTrace();
        }
        catch (IllegalNetMaskException ex)
        {
            ex.printStackTrace();
        }
        catch (JAXBException ex)
        {
            ex.printStackTrace();
        }
    }
}
