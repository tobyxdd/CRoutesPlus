package com.toby.crplus;

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

public class IPRP {
    public String IP;
    public String netmask;
    public IPRP(String argIP,String argNetmask)
    {
        IP=argIP;
        netmask=argNetmask;
    }
}
