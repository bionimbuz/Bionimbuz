package br.unb.cic.bionimbus.storage;

import com.jcraft.jsch.*;

public class Put{
    public static void main(String args[]) {
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
            sftpChannel.put("/home/deric/nso23.pdf", "nso23.pdf");
            sftpChannel.exit();
            session.disconnect();
        } catch (JSchException e) {
            e.printStackTrace();  
        } catch (SftpException e) {
            e.printStackTrace();
        }
    }
}