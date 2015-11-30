package br.unb.cic.bionimbus.services;

import br.unb.cic.bionimbus.persistence.EntityManagerProducer;
import javax.servlet.http.HttpServlet;

import com.google.inject.Inject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;

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
                        // Starts the Server
                        server.start();
                        
                        // Join to main Thread
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

    @Inject
    public HttpServer(MetricsServletContextListener contextListener) {
        this(8181, null, contextListener);
    }

    public HttpServer(int port, HttpServlet servlet, MetricsServletContextListener contextListener) {
        try {
            // Initialize EntityManager to prevent lazy creation
            EntityManagerProducer.initialize();

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        this.port = port;
        this.proxyServlet = servlet;

        server = new Server(port);
        
        WebAppContext context = new WebAppContext();

        // context.setDescriptor("./src/main/WEB-INF/web.xml");
        context.setResourceBase("/home/zoonimbus/zoonimbusProject/src/main/java/br/unb/cic/bionimbus/rest/resource");
        // context.setContextPath("/");

        context.setClassLoader(Thread.currentThread().getContextClassLoader());
        context.setParentLoaderPriority(true);

        ServletHolder restServlet = context.addServlet(HttpServletDispatcher.class,  "/*");
        restServlet.setInitOrder(0);
        // restServlet.setInitParameter("javax.ws.rs.Application", "br.unb.cic.bionimbus.rest.application.RestApplication");
        
        server.setHandler(context);
        
        /*
        Context context = new Context();
        ServletHolder sh = new ServletHolder(ServletContainer.class);
        
        sh.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
        sh.setInitParameter("com.sun.jersey.config.property.packages", "br.unb.cic.bionimbus.p2p.plugin.proxy");
        
        ServletContextHandler context = new ServletContextHandler();
        ServletHolder sh = new ServletHolder(HttpServletDispatcher.class);
        
        
        ServletHolder sh = new ServletHolder(HttpServletDispatcher.class);
        ServletHandler handler = new ServletHandler();
        
        ContextHandler context = new ContextHandler();
        
        context.setContextPath("/*");
        context.setResourceBase(".");
        context.setClassLoader(Thread.currentThread().getContextClassLoader());
        context.setHandler(handler);
        
        server.setHandler(handler);
        
        // Sets the InitParameter telling what is the REST application
        sh.setInitParameter("javax.ws.rs.Application", "br.unb.cic.bionimbus.rest.application.RestApplication");        
        
        handler.addServletWithMapping(sh, "/*");
        
        context.addEventListener(contextListener);

        if (servlet != null) {
           context.addServlet(sh, "/*");
           context.addServlet(new ServletHolder(servlet), "/file");
        }
           
        // Coda Hale Metrics
        context.addServlet(new ServletHolder(new AdminServlet()), "/admin/*");
         */
        

    }

    public static void main(String[] args) throws Exception {
        new HttpServer(8181, null, null).start();
    }
}
