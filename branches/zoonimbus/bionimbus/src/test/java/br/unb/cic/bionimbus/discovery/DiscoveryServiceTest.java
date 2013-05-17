package br.unb.cic.bionimbus.discovery;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: edward
 * Date: 5/12/12
 * Time: 9:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class DiscoveryServiceTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test public void testCache() throws InterruptedException {
        Cache<Object, Object> cache = CacheBuilder.newBuilder().initialCapacity(1000).weakKeys().expireAfterWrite(2, TimeUnit.SECONDS).build();
        cache.put("10", 1);
        
        TimeUnit.SECONDS.sleep(3);

        System.out.println("Resultado:" + cache.getIfPresent("10"));

    }
}
