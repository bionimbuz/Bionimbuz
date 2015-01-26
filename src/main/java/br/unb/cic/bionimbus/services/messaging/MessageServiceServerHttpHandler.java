package br.unb.cic.bionimbus.services.messaging;

import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

public class MessageServiceServerHttpHandler extends
        SimpleChannelUpstreamHandler {

//    private final MessageServiceServer server;

//    private boolean readingChunks = false;
//
//    private FileOutputStream fs;
//
//    private File file;

//    public MessageServiceServerHttpHandler(MessageServiceServer server) {
//        this.server = server;
//    }
//
//    @Override
//    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
//            throws Exception {
//        server.getChannelGroup().add(e.getChannel());
//    }
//
//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
//            throws Exception {
//        e.getCause().printStackTrace();
//        e.getChannel().close();
//    }

//    @Override
//    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
//            throws Exception {
//
//        if (!readingChunks) {
//            HttpRequest req = (HttpRequest) e.getMessage();
//            URI uri = new URI(req.getUri());
//
//            if (req.getMethod() == HttpMethod.GET) {
//                doGet(e, uri.getPath());
//                return;
//            }
//
////            file = new File(server.getPathDir() + uri.getPath());
//            fs = new FileOutputStream(file);
//
//            if (req.isChunked()) {
//                readingChunks = true;
//            } else {
//                ChannelBuffer content = req.getContent();
//                int length = content.readableBytes();
//                if (content.readable()) {
//                    content.readBytes(fs, length);
//                }
//                fs.close();
//                writeResponse(e);
//                Map<String, String> emptyMap = Collections.emptyMap();
////                server.getMessageService().recvFile(file, emptyMap);
//            }
//        } else {
//            HttpChunk chunk = (HttpChunk) e.getMessage();
//            if (chunk.isLast()) {
//                readingChunks = false;
//                fs.close();
//                writeResponse(e);
//                Map<String, String> emptyMap = Collections.emptyMap();
////                server.getMessageService().recvFile(file, emptyMap);
//            } else {
//                int length = chunk.getContent().readableBytes();
//                chunk.getContent().readBytes(fs, length);
//            }
//        }
//    }

//    private void doGet(MessageEvent e, String req) throws Exception {
////        File file = new File(server.getPathDir() + req);
//        if (file.isHidden() || !file.exists()) {
//            writeResponse(e, HttpResponseStatus.NOT_FOUND);
//            return;
//        }
//
//        if (!file.isFile()) {
//            writeResponse(e, HttpResponseStatus.FORBIDDEN);
//            return;
//        }
//
//        RandomAccessFile raf;
//        try {
//            raf = new RandomAccessFile(file, "r");
//        } catch (FileNotFoundException ex) {
//            writeResponse(e, HttpResponseStatus.NOT_FOUND);
//            return;
//        }
//        long length = raf.length();
//
//        HttpResponse resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
//        HttpHeaders.setContentLength(resp, length);
//
//        Channel ch = e.getChannel();
//        ch.write(resp);
//
//        ChannelFuture f = ch.write(new ChunkedFile(raf));
//        f.addListener(ChannelFutureListener.CLOSE);
//    }

//    private void writeResponse(MessageEvent e, HttpResponseStatus status) {
//        HttpResponse resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);
//        ChannelFuture f = e.getChannel().write(resp);
//        if (status != HttpResponseStatus.OK)
//            f.addListener(ChannelFutureListener.CLOSE);
//    }
//
//    private void writeResponse(MessageEvent e) {
//        writeResponse(e, HttpResponseStatus.CREATED);
//    }

}
