package com.toby.crplus;

import org.apache.commons.net.util.SubnetUtils;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * CRoutesPlus - An (slightly better) alternative to "CHNRoutes"
 * Generate Windows Batch Scripts for adding/removing routes from CIDR lists
 *
 * CRoutesPlus - "CHNRoutes"的一个（稍微好点的）替代品
 * 从无类别域间路由表生成给Windows添加/删除路由设置的批处理
 *
 * By Toby 2014.11 tobyxdd@gmail.com
 * http://toby.so/
 */

public class CRPMain {

    public static SimpleDateFormat dateFormat=new SimpleDateFormat("HH:mm:ss");
    private static String chinaCIDR="http://www.ipdeny.com/ipblocks/data/aggregated/cn-aggregated.zone";

    public static void main(String[] args) throws IOException {
        log("CRoutesPlus - By TobyXDD");
        if(args.length==0)
        {
            log("No argument! Maybe you need some help.");
            showHelp();
            return;
        }
        boolean genBat=false; String customCIDR=null;
        for(int i=0;i<args.length;i++)
        {
            if(args[i].equalsIgnoreCase("-g"))genBat=true;
            if(args[i].equalsIgnoreCase("-c"))
            {
                if((i+1)<args.length)customCIDR=args[i+1];else
                {
                    log("Bad argument(s)!");
                    showHelp();
                }
            }
            if(args[i].equalsIgnoreCase("-t"))genBat=false;
        }
        ArrayList<String> CIDRList=new ArrayList<String>();
        InputStream is;
        if(customCIDR==null)
        {
            log("Fetching China CIDR from IPDeny...");
            is=new URL(chinaCIDR).openStream();
        }else
        {
            log("Reading list from "+customCIDR);
            is=new FileInputStream(customCIDR);
        }
        BufferedReader reader=new BufferedReader(new InputStreamReader(is));
        String tl;
        while((tl=reader.readLine())!=null)
        {
            if(!tl.isEmpty())CIDRList.add(tl);
        }
        reader.close();
        //for(String s:CIDRList)log(s);
        ArrayList<IPRP> iprps=new ArrayList<IPRP>();
        log("Generating IPRPs...");
        for(String subnet:CIDRList)
        {
            SubnetUtils.SubnetInfo info=new SubnetUtils(subnet).getInfo();
            iprps.add(new IPRP(info.getAddress(),info.getNetmask()));
        }
        //log("Performing aggregation...");
        //iprps=aggregate(iprps);
        if(genBat)
        {
            log("Generating routes...");
            BufferedWriter vpnUpWriter=new BufferedWriter(new FileWriter("VPN_UP.bat"));
            BufferedWriter vpnDownWriter=new BufferedWriter(new FileWriter("VPN_Down.bat"));
            vpnUpWriter.write("@echo off\n" +
                    "for /F \"tokens=3\" %%* in ('route print ^| findstr \"\\<0.0.0.0\\>\"') do set \"gw=%%*\"\n" +
                    "ipconfig /flushdns\n");
            vpnDownWriter.write("@echo off\n");
            for(IPRP iprp:iprps)
            {
                vpnUpWriter.write("route add "+iprp.IP+" mask "+iprp.netmask+" %gw% metric 5\n");
                vpnDownWriter.write("route delete "+iprp.IP+"\n");
            }
            vpnUpWriter.close();vpnDownWriter.close();
            log("All done. :)");
        }

        /*if(genBat)
        {
            log("Generating routes...");
            BufferedWriter vpnUpWriter=new BufferedWriter(new FileWriter("VPN_UP.bat"));
            BufferedWriter vpnDownWriter=new BufferedWriter(new FileWriter("VPN_Down.bat"));
            vpnUpWriter.write("@echo off\n" +
                    "for /F \"tokens=3\" %%* in ('route print ^| findstr \"\\<0.0.0.0\\>\"') do set \"gw=%%*\"\n" +
                    "ipconfig /flushdns\n");
            vpnDownWriter.write("@echo off\n");
            for(String subnet:CIDRList)
            {
                SubnetUtils.SubnetInfo info=new SubnetUtils(subnet).getInfo();
                vpnUpWriter.write("route add "+info.getAddress()+" mask "+info.getNetmask()+" %gw% metric 5\n");
                vpnDownWriter.write("route delete "+info.getAddress()+"\n");
            }
            vpnUpWriter.close();vpnDownWriter.close();
            log("All done. :)");
        }*/
        return;
    }
    private static ArrayList<IPRP> aggregate(ArrayList<IPRP> argIPRPs)
    {
        ArrayList<IPRP> nl=new ArrayList<IPRP>();
        for(int i=0;i<argIPRPs.size();i++)
        {
            String[] tIP=argIPRPs.get(i).IP.split("\\.");
            String[] tMask=argIPRPs.get(i).netmask.split("\\.");
            //0~3 - a.b.c.d
            String[] tNIP=new String[4];
            for(int j=0;j<4;j++)
            {
                tNIP[j]=String.valueOf(Integer.parseInt(tIP[j])+(255-Integer.parseInt(tMask[j])));
            }
            for(int j=3;j>=1;j--)
            {
                if(Integer.parseInt(tNIP[j])>=255)
                {
                    tNIP[j-1]=String.valueOf(Integer.parseInt(tNIP[j-1])+1);
                    tNIP[j]=String.valueOf(Integer.parseInt(tNIP[j])-255);
                }
            }
        }
        return nl;
    }
    private static void log(String argStr)
    {
        Calendar calendar=Calendar.getInstance();
        System.out.println("["+dateFormat.format(new Date())+"] "+argStr);
    }
    private static void showHelp()
    {
        System.out.println("-Arguments for CRoutesPlus-\n" +
                "-g : Generate normal Windows BATs with Chinese CIDR\n" +
                "-c [Path] : Custom CIDR file\n" +
                "-t : Only generate IPRange-Netmask list\n" +
                "EXAMPLE:\n" +
                "java -jar crp.jar -g -c Syria.txt");
    }
}
