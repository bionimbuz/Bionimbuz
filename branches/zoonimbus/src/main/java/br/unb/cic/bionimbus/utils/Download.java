package br.unb.cic.bionimbus.utils;

import com.jcraft.jsch.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Download {
    @SuppressWarnings("empty-statement")
    public static void main(String args[]) throws FileNotFoundException, IOException {
        JSch jsch = new JSch();
        Session session = null;
        try {
            session = jsch.getSession("biocloud1", "192.168.1.111", 22);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword("cloudbio");
            session.connect();
         
            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel; 
            sftpChannel.get("/home/biocloud1/nso.pdf","/tmp" );
            sftpChannel.exit();
            session.disconnect();
        } catch (JSchException e) {
            e.printStackTrace();  
        } catch (SftpException e) {
            e.printStackTrace();
        }
    }
}