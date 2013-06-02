package br.unb.cic.bionimbus.p2p.plugin.proxy;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import br.unb.cic.bionimbus.plugin.*;

import br.unb.cic.bionimbus.p2p.Host;
import br.unb.cic.bionimbus.p2p.P2PService;

//import static br.unb.cic.bionimbus.p2p.plugin.proxy.Command.GET_INFO;

public class RemotePlugin extends AbstractPlugin {

//    private final ProxyServerStub server;
    private final Host host;
    private final String id;
    private final ExecutorService executor;
    private final String proxyHost;
    private final int proxyPort;

    public RemotePlugin(final P2PService p2p, ExecutorService executor) {

        super(p2p);

        host = p2p.getPeerNode().getHost();
        id = p2p.getPeerNode().getId().toString();

        proxyHost = p2p.getConfig().getProxyHost();
        proxyPort = p2p.getConfig().getProxyPort();

        System.out.println(String.format("Iniciando remote proxy on %s:%s", proxyHost, proxyPort));

        this.executor = executor;
//        server = ProxyServerStub.newInstance(executor, proxyHost, proxyPort);
//        server.start();
    }

    @Override
    protected Future<PluginInfo> startGetInfo() {

        FutureTask<PluginInfo> future = new FutureTask<PluginInfo>(
                new Callable<PluginInfo>() {

                    public PluginInfo call() throws Exception {

//						long messageId = server.request(GET_INFO);
//						ResponseMessage<PluginInfo> response = server.getResponse(messageId);
//						PluginInfo info = response.getResponse();
//						info.setId(id);
//						info.setHost(host);
//
//						return info;

                        return null;
                    }
                });
        executor.execute(future);
        return future;
    }

    @Override
    protected Future<PluginFile> saveFile(final String filePath) {
        FutureTask<PluginFile> future = new FutureTask<PluginFile>(
                new Callable<PluginFile>() {

                    public PluginFile call() throws Exception {
//
////						Hashifier.hashContent(new File(filePath))
//
//						System.out.println("Transfering file ...");
//
//                        final File file = new File(filePath);
//                        server.request("SAVE-FILE" + "#" + filePath + "#", file);
//                        System.out.println("Finished file " + filePath);
//						String getResponse = server.getResponse("SAVE-FILE");
//						ObjectMapper mapper = new ObjectMapper();
//						PluginFile info = mapper.readValue(getResponse, PluginFile.class);
//
//						return info;
                        return null;
                    }
                });
        executor.execute(future);
        return future;
    }

    @Override
    protected Future<PluginGetFile> getFile(final Host origin,
                                            final PluginFile pluginFile, final String taskId,
                                            final String filename) {

        FutureTask<PluginGetFile> future = new FutureTask<PluginGetFile>(
                new Callable<PluginGetFile>() {

                    public PluginGetFile call() throws Exception {
//
//						PluginGetFile getFile = new PluginGetFile();
//						getFile.setPeer(origin);
//						getFile.setPluginFile(pluginFile);
//						getFile.setTaskId(taskId);
//
//						server.request("GET-FILE" + "#" + filename, new File(filename));
//
//						String getResponse = server.getResponse("GET-FILE");
//
//						ObjectMapper mapper = new ObjectMapper();
//						PluginGetFile info = mapper.readValue(getResponse, PluginGetFile.class);
//
//						return info;
                        return null;
                    }
                });
        executor.execute(future);
        return future;
    }


    @Override
    protected Future<PluginTask> startTask(final PluginTask task) {

        final PluginService service = getMyInfo().getService(task.getJobInfo().getServiceId());
        if (service == null) return null;

        FutureTask<PluginTask> future = new FutureTask<PluginTask>(
                new Callable<PluginTask>() {

                    @Override
                    public PluginTask call() throws Exception {

//                         server.request("RUN-TASK" + "#" + task + "#" + service + "#" + getP2P().getConfig().getServerPath());

                        return null;
                    }
                });

        return future;
    }
}
