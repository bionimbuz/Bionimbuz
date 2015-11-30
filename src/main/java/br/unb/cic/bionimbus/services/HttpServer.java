package br.unb.cic.bionimbus.services;

import br.unb.cic.bionimbus.persistence.EntityManagerProducer;
import javax.servlet.http.HttpServlet;

import com.google.inject.Inject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

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

        // Instantiate a new Server on int port
        server = new Server(port);

        // Creates the Context used in the Web application
        WebAppContext context = new WebAppContext();

        // Configures the http server context
        context.setDescriptor("./src/main/webapp/web.xml");
        context.setResourceBase("./src/main/webapp");
        context.setContextPath("/");
        context.setParentLoaderPriority(true);

        ServletHolder restServlet = context.addServlet(HttpServletDispatcher.class,  "/*");
        restServlet.setInitOrder(0);
        // restServlet.setInitParameter("javax.ws.rs.Application", "br.unb.cic.bionimbus.rest.application.RestApplication");
        
        server.setHandler(context);

        try {
            // Initialize EntityManager to prevent lazy creation
            EntityManagerProducer.initialize();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        new HttpServer(8181, null, null).start();
    }
}
