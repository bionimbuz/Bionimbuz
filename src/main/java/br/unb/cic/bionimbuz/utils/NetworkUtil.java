/*
 * BioNimbuZ is a federated cloud platform.
 * Copyright (C) 2012-2015 Laboratory of Bioinformatics and Data (LaBiD),
 * Department of Computer Science, University of Brasilia, Brazil
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package br.unb.cic.bionimbuz.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * @edward | 15 de mai de 2013 - 13:11:00
 * @jgomes | 15 de ago de 2017 - 23:52:59
 */
public final class NetworkUtil {

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Constructors.
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    private NetworkUtil() {
        super();
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // statics methods.
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    public static boolean isLocalhost(String address) {

        if (address == null || address.isEmpty()) {
            return false;
        }
        try {
            final int indexOf = address.indexOf(":");
            if (indexOf > -1) {
                address = address.substring(0, indexOf);
            }
            return isLocalhost(InetAddress.getByName(address));
        } catch (final UnknownHostException e) {
            return false;
        }
    }

    public static boolean isLocalhost(InetAddress address) {

        if (address.isAnyLocalAddress() || address.isLoopbackAddress()) {
            return true;
        }
        try {
            return NetworkInterface.getByInetAddress(address) != null;
        } catch (final SocketException e) {
            return false;
        }
    }

    public static String getAddress(String NIC) throws SocketException {

        final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            final NetworkInterface current = interfaces.nextElement();

            if (!current.isUp() || current.isLoopback() || current.isVirtual()) {
                continue;
            }
            final Enumeration<InetAddress> addresses = current.getInetAddresses();
            while (addresses.hasMoreElements()) {
                final InetAddress current_addr = addresses.nextElement();
                if (current_addr.isLoopbackAddress()) {
                    continue;
                }

                if (current_addr instanceof Inet4Address && current.getName().contains(NIC)) {
                    return current_addr.getHostAddress();
                }
            }
        }
        return null;
    }
}
