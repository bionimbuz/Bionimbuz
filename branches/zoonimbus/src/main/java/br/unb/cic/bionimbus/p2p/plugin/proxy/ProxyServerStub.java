package br.unb.cic.bionimbus.p2p.plugin.proxy;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;

import javax.servlet.http.HttpServlet;

import br.unb.cic.bionimbus.services.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyServerStub {

//    private volatile boolean running;
////    private final HttpServer jetty;
////    private final ExecutorService executorService;
//    private static final Random random = new Random();
////    private final Queue<RequestMessage> outgoingQueue = Queues.newConcurrentLinkedQueue(); // external
////    // -->
////    // proxy
////    // -->
////    // client
////    private final ConcurrentMap<Long, LinkedBlockingQueue<ResponseMessage>> incomingQueue = Maps.newConcurrentMap();
//
//    // client
//    // -->
//    // proxy
//    // -->
//    // external
//    private static final Logger LOG = LoggerFactory.getLogger(ProxyServerStub.class);
//    private final int port;
//    private File file;
//
//    private static ProxyServerStub REF;
//
//    private String filePath;
//    private int maxFileSize = 50 * 1024;
//    private int maxMemSize = 4 * 1024;
//
////    public static synchronized ProxyServerStub getInstance() {
////        return REF;
////    }
////
////    public static synchronized ProxyServerStub newInstance(ExecutorService executor, String host, int port) {
////        if (REF == null)
////            REF = new ProxyServerStub(executor, host, port);
////        return REF;
////    }
//
////    private ProxyServerStub(ExecutorService executor, String host, int port) {
////
////        this.port = port;
////        this.executorService = executor;
////
////        jetty = HttpServer.getInstance(port, new FileHandlingServlet());
////
/////*
////        for (Command command : Command.values()) {
////			incomingQueue.put(command, new LinkedBlockingQueue<ResponseMessage>());
////		}
////*/
////
////    }
//
////    public long request(Command command) {
////        RequestMessage msg = new RequestMessage(random.nextLong(), command);
////        outgoingQueue.add(msg);
////        return msg.getId();
////
////    }
////
////    public long request(Command command, String filename) {
////        RequestMessage msg = new RequestMessage(random.nextLong(), command, filename);
////        outgoingQueue.add(msg);
////        return msg.getId();
////    }
//
//    public void start() {
//        executorService.submit(new Runnable() {
//            @Override
//            public void run() {
//                System.out.println("Starting ProxyServerStub on port " + port);
//                try {
//                    jetty.start();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    running = false;
//                }
//            }
//        });
//    }
//
//    public void shutdown() {
//        try {
//            System.out.println("Stopping ProxyServerStub ...");
//            jetty.stop();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            running = false;
//        }
//    }
//
////    public synchronized ResponseMessage getResponse(long messageId) throws InterruptedException {
////        System.out.println("Getting getResponse for message " + messageId);
////        incomingQueue.putIfAbsent(messageId, new LinkedBlockingQueue<ResponseMessage>());
////        return incomingQueue.get(messageId).take();
////    }
////
////    public void setResponse(ResponseMessage<? extends PluginOps> response) throws InterruptedException {
////        incomingQueue.get(response.getId()).put(response);
////    }
////
////    public List<RequestMessage> getCommands() {
////        System.out.println("Consumindo dados da fila de entrada...");
////        RequestMessage command = null;
////        List<RequestMessage> out = Lists.newArrayList();
////        while ((command = outgoingQueue.poll()) != null) {
////            out.add(command);
////        }
////        return out;
////    }
//
//    class FileHandlingServlet extends HttpServlet {
////
////		private static final long serialVersionUID = 8048142529312970675L;
////
////        private volatile long leaseTime = 0L;
////        public static final long INTERVAL_MS = 60 * 1000;
////
////        @Override
////        public void init() throws ServletException {
////            HealthChecks.register(new MyHealthCheck("latestConnectionTime"));
////            filePath = "/tmp";
////        }
////
////		public void doGet(HttpServletRequest request, HttpServletResponse getResponse)
////        throws ServletException, IOException {
////
////			getResponse.setContentType("application/json");
////			getResponse.setStatus(HttpServletResponse.SC_OK);
////
////            ObjectMapper mapper = new ObjectMapper();
////            getResponse.getWriter().write(mapper.writeValueAsString(out));
////			String result = request.getParameter("result");
////
////			if (result != null) {
////				System.out.println(result);
////			}
////
////            leaseTime = System.currentTimeMillis();
////		}
////
////		@Override
////		protected void doPost(HttpServletRequest req, HttpServletResponse resp)
////				throws ServletException, IOException {
////
////			String data = req.getParameter("data");
////			if (data != null) {
////				System.out.println(data);
////				StringTokenizer st = new StringTokenizer(data, "#");
////				String command = st.nextToken();
////				String json = st.nextToken();
////				try {
////					incomingQueue.get(command).put(json);
////				} catch (InterruptedException e) {
////					// TODO Auto-generated catch block
////					e.printStackTrace();
////				}
////			}
////
////			boolean isMultipart = ServletFileUpload.isMultipartContent(req);
////			java.io.PrintWriter out = resp.getWriter();
////			if (isMultipart)
////				try {
////					getFile(req);
////				} catch (Exception e) {
////					// TODO Auto-generated catch block
////					e.printStackTrace();
////				}
////
////            leaseTime = System.currentTimeMillis();
////		}
////
////		private void getFile(HttpServletRequest req) throws Exception {
////
////			DiskFileItemFactory factory = new DiskFileItemFactory();
////			// maximum size that will be stored in memory
////			factory.setSizeThreshold(maxMemSize);
////			// Location to save data that is larger than maxMemSize.
////			factory.setRepository(new File("/tmp"));
////
////			// Create a new file upload handler
////			ServletFileUpload upload = new ServletFileUpload(factory);
////			// maximum file size to be uploaded.
////			upload.setSizeMax(maxFileSize);
////
////			// Parse the request to get file items.
////			List<?> fileItems = upload.parseRequest(req);
////
////			// Process the uploaded file items
////			Iterator<?> i = fileItems.iterator();
////
////			while (i.hasNext()) {
////				FileItem fi = (FileItem) i.next();
////				if (!fi.isFormField()) {
////					// Get the uploaded file parameters
////					String fieldName = fi.getFieldName();
////					String fileName = fi.getName();
////					String contentType = fi.getContentType();
////					boolean isInMemory = fi.isInMemory();
////					long sizeInBytes = fi.getSize();
////
////					// Write the file
////					if (fileName.lastIndexOf("\\") >= 0) {
////						file = new File(
////								filePath
////										+ fileName.substring(fileName
////												.lastIndexOf("\\")));
////					} else {
////						file = new File(
////								filePath
////										+ fileName.substring(fileName
////												.lastIndexOf("\\") + 1));
////					}
////					fi.write(file);
////				}
////			}
////            leaseTime = System.currentTimeMillis();
////		}
////
////        class MyHealthCheck extends HealthCheck {
////
////            private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy H:mm:s");
////
////            protected MyHealthCheck(String name) {
////                super(name);
////            }
////
////            @Override
////            protected Result check() throws Exception {
////
////                if (INTERVAL_MS < 2 * (System.currentTimeMillis() - leaseTime)){
////
////                    String message = null;
////                    if (leaseTime == 0) {
////                        message = "ever detected.";
////                    }
////                    else {
////                        message = String.format("since %s (current time: %s)"
////                                ,sdf.format(new Date(leaseTime))
////                                ,sdf.format(new Date()));
////                    }
////                    return Result.unhealthy("No connection from stub " + message);
////                }
////                return Result.healthy();
////            }
////        }
//    }

}
