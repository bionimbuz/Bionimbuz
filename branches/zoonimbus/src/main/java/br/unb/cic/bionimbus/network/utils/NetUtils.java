package br.unb.cic.bionimbus.network.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created with IntelliJ IDEA.
 * User: edward
 * Date: 5/15/13
 * Time: 1:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class NetUtils {

    private NetUtils() {
    }

    public static String getAddress(String NIC) throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface current = interfaces.nextElement();

            if (!current.isUp() || current.isLoopback() || current.isVirtual()) continue;
            Enumeration<InetAddress> addresses = current.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress current_addr = addresses.nextElement();
                if (current_addr.isLoopbackAddress()) continue;

                if (current_addr instanceof Inet4Address && current.getName().contains(NIC)) {

                    String eth0Address = current_addr.getHostAddress();
                    return eth0Address;
//                    System.out.println(current_addr.getCanonicalHostName());
//                    System.out.println(current_addr.getHostAddress());
                }
            }
        }
        return null;
    }
}
