/*
    BioNimbuZ is a federated cloud platform.
    Copyright (C) 2012-2015 Laboratory of Bioinformatics and Data (LaBiD), 
    Department of Computer Science, University of Brasilia, Brazil

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package br.unb.cic.bionimbus.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created with IntelliJ IDEA. User: edward Date: 5/15/13 Time: 1:11 PM To
 * change this template use File | Settings | File Templates.
 */
public class NetUtils {

    private NetUtils() {
    }

    public static String getAddress(String NIC) throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface current = interfaces.nextElement();

            if (!current.isUp() || current.isLoopback() || current.isVirtual()) {
                continue;
            }
            Enumeration<InetAddress> addresses = current.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress current_addr = addresses.nextElement();
                if (current_addr.isLoopbackAddress()) {
                    continue;
                }

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
