package br.unb.cic.bionimbus.services;

import br.unb.cic.bionimbus.persistence.EntityManagerProducer;
import javax.servlet.http.HttpServlet;

import com.google.inject.Inject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP server that will handle REST requests and responses on port 8181
 * 
 * @author Vinicius
 */
public class HttpServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServer.class);
    private Server server;
    private static HttpServer REF;
    private volatile boolean running;
    private final ExecutorService service = Executors.newSingleThreadExecutor();
    private final int port;
    private final HttpServlet proxyServlet;

    /**
     * Starts server
     * @throws Exception 
     */
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
                        LOGGER.error("[InterruptedException] " + e.getMessage());
                    } catch (Exception e) {
                        LOGGER.error("[Exception] " + e.getMessage());
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
            LOGGER.error("[Exception] " + e.getMessage());
        }
        running = false;
    }

    @Inject
    public HttpServer(MetricsServletContextListener contextListener) {
        this(8181, null, contextListener);
    }

    public HttpServer(int port, HttpServlet servlet, MetricsServletContextListener contextListener) {
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

        server.setHandler(context);

        try {
            // Initialize EntityManager to prevent lazy creation
            EntityManagerProducer.initialize();

        } catch (Exception e) {
            LOGGER.error("[Exception] " + e.getMessage());
        }
    }

    /**
     * Starts HttpServer on port 8181
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        new HttpServer(8181, null, null).start();
    }
}
