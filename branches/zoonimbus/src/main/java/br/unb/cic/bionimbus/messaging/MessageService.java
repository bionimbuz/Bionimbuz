package br.unb.cic.bionimbus.messaging;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import br.unb.cic.bionimbus.config.BioNimbusConfig;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Singleton;

@Singleton
public class MessageService {

    private final MessageServiceServer server = new MessageServiceServer();

    private final MessageServiceClient client = new MessageServiceClient();

    private final Multimap<Integer, MessageListener> listenersMap = HashMultimap.create();

    private final List<FileListener> fileListenersList = new ArrayList<FileListener>();

    private BioNimbusConfig config;

    private InetSocketAddress bindSocket;

    public BioNimbusConfig getConfig() {
        return config;
    }

    public void bind(InetSocketAddress bindSocket) {
        this.bindSocket = bindSocket;
    }

    public InetSocketAddress getSocket() {
        return bindSocket;
    }

    public void start(MessageFactory messageFactory, BioNimbusConfig config) {
        this.config = config;
        server.start(this, messageFactory);
        client.setService(this);
    }

    public void shutdown() {
        server.shutdown();
        client.shutdown();
    }

    public void addListener(MessageListener listener, List<Integer> types) {
        for (Integer type : types)
            listenersMap.put(type, listener);
    }

    public void recvMessage(Message message) {
        for (MessageListener listener : listenersMap.get(message.getType()))
            listener.onEvent(message);
    }

    public void sendMessage(InetSocketAddress addr, Message message) {
        client.sendMessage(addr, message);
    }

    public void addFileListener(FileListener listener) {
        fileListenersList.remove(listener);
        fileListenersList.add(listener);
    }

    public void sendFile(InetSocketAddress addr, String fileName) {
        client.sendFile(addr, fileName);
    }

    public void getFile(InetSocketAddress addr, String fileName, Map<String, String> parms) {
        client.getFile(addr, fileName, parms);
    }

    public void recvFile(File file, Map<String, String> parms) {
        for (FileListener listener : fileListenersList)
            listener.onFileReceived(file, parms);
    }
}
