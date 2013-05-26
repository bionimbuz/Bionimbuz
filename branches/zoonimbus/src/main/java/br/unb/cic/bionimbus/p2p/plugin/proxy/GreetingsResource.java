package br.unb.cic.bionimbus.p2p.plugin.proxy;


import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class GreetingsResource {

    //private static final Counter pendingJobs = Metrics.newCounter(MetricsTest.class, "hitCounter");

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String get() {
        return "http proxy";
    }
}
