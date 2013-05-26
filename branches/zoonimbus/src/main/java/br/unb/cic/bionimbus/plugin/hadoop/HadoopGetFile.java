package br.unb.cic.bionimbus.plugin.hadoop;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

import br.unb.cic.bionimbus.p2p.PeerNode;
import br.unb.cic.bionimbus.plugin.PluginFile;

public class HadoopGetFile implements Callable<HadoopGetFile> {

    private final PluginFile pluginFile;

    private final PeerNode receiver;

    private final String serverPath;

    private final String taskId;

    public HadoopGetFile(PluginFile pluginFile, String taskId, PeerNode receiver, String serverPath) {
        this.pluginFile = pluginFile;
        this.receiver = receiver;
        this.serverPath = serverPath;
        this.taskId = taskId;
    }

    public PluginFile getPluginFile() {
        return pluginFile;
    }

    public String getTaskId() {
        return taskId;
    }

    public PeerNode getReceiver() {
        return receiver;
    }

    @Override
    public HadoopGetFile call() throws Exception {
        File file = new File(pluginFile.getPath());
        Process p = null;

        try {
            p = Runtime.getRuntime().exec("hadoop fs -get " + pluginFile.getPath() + " " + serverPath + "/" + file.getName());
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
            // TODO TRATAR ERRO!
        }

        return this;
    }

}
