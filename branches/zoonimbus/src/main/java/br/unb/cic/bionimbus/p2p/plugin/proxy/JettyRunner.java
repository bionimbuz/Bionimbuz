package br.unb.cic.bionimbus.p2p.plugin.proxy;

import javax.servlet.http.HttpServlet;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.json.HealthCheckModule;
import com.codahale.metrics.servlets.AdminServlet;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import com.sun.jersey.spi.container.servlet.ServletContainer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JettyRunner {

    //    private static final Counter pendingJobs = Metrics.newCounter(MetricsTest.class, "counter");
    private Server server;
    private static JettyRunner REF;
    private volatile boolean running;
    private final ExecutorService service = Executors.newSingleThreadExecutor();
    private final int port;
    private final HttpServlet proxyServlet;

    public static synchronized JettyRunner getInstance(int port, HttpServlet proxy) {
        if (REF == null) {
            REF = new JettyRunner(port, proxy);
        }
        return REF;
    }

    public void start() throws Exception {
        if (!running) {

            service.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        server.start();
                        server.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        running = false;
                    }
                }
            });
        }
    }

    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        running = false;
    }

    private JettyRunner(int port, HttpServlet servlet) {
        this.port = port;
        this.proxyServlet = servlet;

        server = new Server(port);
        Context context = new Context(server, "/", Context.SESSIONS);
        ServletHolder sh = new ServletHolder(ServletContainer.class);
        sh.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
        sh.setInitParameter("com.sun.jersey.config.property.packages", "br.unb.cic.bionimbus.p2p.plugin.proxy");

        context.addServlet(sh, "/*");

        if (servlet != null)
            context.addServlet(new ServletHolder(servlet), "/file");

/*        final HealthCheckRegistry hr = new HealthCheckRegistry();
        hr.register("a", new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                return Result.healthy();
            }
        });

        // Coda Hale Metrics
        context.addServlet(new ServletHolder(new AdminServlet()), "/admin*//*");*/

//            for (int i=0; i < 10; i++)
//                pendingJobs.inc();
    }

    public static void main(String[] args) throws Exception {
        new JettyRunner(9191, null).start();
    }

}
