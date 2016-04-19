/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net.dns;

/**
 *
 * @author tkv
 */
public class Constants
{
    // TYPE
    public enum Type {NONE, A, NS, MD, MF, CNAME, SOA, MB, MG, MR, NULL, WKS, PTR, HINFO, MINFO, MX, TXT,
                        RP, AFSDB, X25, ISDN, RT, NSAP, NSAP_PTR, SIG, KEY, GPOS, AAAA, LOC};
    public static final int A = 1;  // a host address
    public static final int NS = 2; // an authoritative name server
    public static final int MD = 3; // a mail destination (Obsolete - use MX)
    public static final int MF = 4; // a mail forwarder (Obsolete - use MX)
    public static final int CNAME = 5; // the canonical name for an alias
    public static final int SOA = 6; // marks the start of a zone of authority
    public static final int MB = 7; // a mailbox domain name (EXPERIMENTAL)
    public static final int MG = 8; // a mail group member (EXPERIMENTAL)
    public static final int MR = 9; // a mail rename domain name (EXPERIMENTAL)
    public static final int NULL = 10; // a null RR (EXPERIMENTAL)
    public static final int WKS = 11; // a well known service description
    public static final int PTR = 12; // a domain name pointer
    public static final int HINFO = 13; // host information
    public static final int MINFO = 14; // mailbox or mail list information
    public static final int MX = 15; // mail exchange
    public static final int TXT = 16; // text strings
    public static final int RP = 17;    // for Responsible Person
    public static final int AFSDB = 18;    // for AFS Data Base location
    public static final int X25 = 19;    // for X.25 PSDN address
    public static final int ISDN = 20;    // for ISDN address
    public static final int RT = 21;    // for Route Through
    public static final int NSAP = 22;    // for NSAP address, NSAP style A record
    public static final int NSAP_PTR = 23;    // for domain name pointer, NSAP style
    public static final int SIG = 24;    // for security signature
    public static final int KEY = 25;    // for security key
    public static final int PX = 26;    // X.400 mail mapping information
    public static final int GPOS = 27;    // Geographical Position
    public static final int AAAA = 28;    // IP6 Address
    public static final int LOC = 29;    // Location Information

    public static final int OPT = 41;    // OPT

    // QTYPE
    public static final int AXFR = 252; // A request for a transfer of an entire zone
    public static final int MAILB = 253; // A request for mailbox-related records (MB, MG or MR)
    public static final int MAILA = 254; // A request for mail agent RRs (Obsolete - see MX)
    public static final int ANY = 255; // A request for all records (*)
    // CLASS
    public static final int IN = 1; // the Internet
    public static final int CS = 2; // the CSNET class (Obsolete - used only for examples in some obsolete RFCs)
    public static final int CH = 3; // the CHAOS class
    public static final int HS = 4; // Hesiod [Dyer 87]
    // QR
    public static final int QR_QUERY = 0;
    public static final int QR_RESPONSE = 1;
    // OPCODE
    public static final int OPCODE_QUERY = 0;
    public static final int OPCODE_IQUERY = 1;
    public static final int OPCODE_STATUS = 2;
    // RCODE
    public static final int RCODE_NO_ERROR = 0;
    public static final int RCODE_FORMAT_ERROR = 1;
    public static final int RCODE_SERVER_FAILURE = 2;
    public static final int RCODE_NAME_ERROR = 3;
    public static final int RCODE_NOT_IMPLEMENTED = 4;
    public static final int RCODE_REFUSED = 5;

    public static final String type(int type)
    {
        switch (type)
        {
            case A:
                return "A";
            case NS:
                return "NS";
            case MD:
                return "MD";
            case MF:
                return "MF";
            case CNAME:
                return "CNAME";
            case SOA:
                return "SOA";
            case MB:
                return "MB";
            case MG:
                return "MG";
            case MR:
                return "MR";
            case NULL:
                return "NULL";
            case WKS:
                return "WKS";
            case PTR:
                return "PTR";
            case HINFO:
                return "HINFO";
            case MINFO:
                return "MINFO";
            case MX:
                return "MX";
            case TXT:
                return "TXT";
            case AAAA:
                return "AAAA";
            case LOC:
                return "LOC";
            case AXFR:
                return "AXFR";
            case MAILB:
                return "MAILB";
            case MAILA:
                return "MAILA";
            case ANY:
                return "ANY";
            default:
                return "?";
        }
    }

    public static final String clazz(int clazz)
    {
        switch (clazz)
        {
            case IN:
                return "IN";
            case CS:
                return "CS";
            case CH:
                return "CH";
            case HS:
                return "HS";
            default:
                return "?";
        }
    }
    public static final String qr(int qr)
    {
        switch (qr)
        {
            case QR_QUERY:
                return "QUERY";
            case QR_RESPONSE:
                return "RESPONSE";
            default:
                return "?";
        }
    }
    public static final String opCode(int opCode)
    {
        switch (opCode)
        {
            case OPCODE_QUERY:
                return "QUERY";
            case OPCODE_IQUERY:
                return "IQUERY";
            case OPCODE_STATUS:
                return "STATUS";
            default:
                return "?";
        }
    }
    public static final String rCode(int rCode)
    {
        switch (rCode)
        {
            case RCODE_NO_ERROR:
                return "OK";
            case RCODE_FORMAT_ERROR:
                return "FORMAT ERROR";
            case RCODE_SERVER_FAILURE:
                return "SERVER FAILURE";
            case RCODE_NAME_ERROR:
                return "NAME ERROR";
            case RCODE_NOT_IMPLEMENTED:
                return "NOT IMPLEMENTED";
            case RCODE_REFUSED:
                return "REFUSED";
            default:
                return "?";
        }
    }
}
