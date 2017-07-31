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
package br.unb.cic.bionimbuz.services;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

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

//        try {
//            // Initialize EntityManager to prevent lazy creation
//            EntityManagerProducer.initialize();
//        } catch (Exception e) {
//            LOGGER.error("[Exception] " + e.getMessage());
//        }
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
