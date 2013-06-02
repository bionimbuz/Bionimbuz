package br.unb.cic.bionimbus.services.storage;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: edward
 * Date: 6/1/13
 * Time: 7:26 PM
 * To change this template use File | Settings | File Templates.
 */

public class HdfsClient {

    //  NameNode - http://localhost:50070/

    public static final int DEFAULT_PORT = 9000;

    private FileSystem dfs;

    public HdfsClient(String address, int port) throws IOException {
        Configuration config = new Configuration();
        config.set("fs.default.name","hdfs://" + address + ":" + port +"/");
        dfs = FileSystem.get(config);
    }

    public void putFile(File local) throws IOException {
        Path src = new Path(local.getAbsolutePath());
        Path dst = new Path(dfs.getWorkingDirectory()+"/" + local.getName());
        dfs.copyFromLocalFile(src, dst);
    }

    public void getFile(String dfsPath, String localPath) throws IOException {
         Path src = new Path(dfsPath);
         Path dst = new Path(localPath);
         dfs.copyToLocalFile(src, dst);
    }

    public void delete(String path) throws IOException {
        Path src = new Path(path);
        dfs.delete(src, true);
    }

    public static void main(String[] args) throws IOException {

        HdfsClient client = new HdfsClient("127.0.0.1", DEFAULT_PORT);
        client.getFile("/user/edward/saida-mp3.txt", "/home/edward/saida-mp3.txt");
    }
}
