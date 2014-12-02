package br.unb.cic.bionimbus.services;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServlet;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.AdminServlet;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.MetricsServlet;
import com.google.inject.Inject;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import com.sun.jersey.spi.container.servlet.ServletContainer;

import java.util.EventListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {

    private Server server;
    private static HttpServer REF;
    private volatile boolean running;
    private final ExecutorService service = Executors.newSingleThreadExecutor();
    private final int port;
    private final HttpServlet proxyServlet;

    private MetricsServletContextListener contextListener;

    public void start() throws Exception {
        if (!running) {

            service.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.println("starting http server on port " + port);
                        server.start();
//                        server.join();
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

    @Inject
    public HttpServer(MetricsServletContextListener contextListener) {
        this(9191, null, contextListener);
    }

    public HttpServer(int port, HttpServlet servlet, MetricsServletContextListener contextListener) {
        this.port = port;
        this.proxyServlet = servlet;

        server = new Server(port);
        Context context = new Context(server, "/", Context.SESSIONS);
        ServletHolder sh = new ServletHolder(ServletContainer.class);
        sh.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
        sh.setInitParameter("com.sun.jersey.config.property.packages", "br.unb.cic.bionimbus.p2p.plugin.proxy");

        context.addEventListener(contextListener);

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
        */

        // Coda Hale Metrics
        context.addServlet(new ServletHolder(new AdminServlet()), "/admin/*");

//            for (int i=0; i < 10; i++)
//                pendingJobs.inc();
    }

    public static void main(String[] args) throws Exception {
        new HttpServer(9191, null, null).start();
    }
}
