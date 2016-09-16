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
package br.unb.cic.bionimbus.services.discovery;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Created by IntelliJ IDEA. User: edward Date: 5/12/12 Time: 9:37 PM To change
 * this template use File | Settings | File Templates.
 */
public class DiscoveryServiceTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testCache() throws InterruptedException {
        Cache<Object, Object> cache = CacheBuilder.newBuilder().initialCapacity(1000).weakKeys().expireAfterWrite(2, TimeUnit.SECONDS).build();
        cache.put("10", 1);

        TimeUnit.SECONDS.sleep(3);

        System.out.println("Resultado:" + cache.getIfPresent("10"));

    }
}
