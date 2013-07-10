/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.utils;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

/**
 * Metodo para a conexao entre o servidor e o cliente em casos de downloads
 * @author Deric
 */
public class Get {
    
    private JSch jsch = new JSch();
    private Session session = null;
    private String USER = "zoonimbus";
    private String PASSW = "Zoonimbus1";
    private int PORT = 22;
    private com.jcraft.jsch.Channel channel;
    
    public boolean startSession(String file, String host) throws JSchException, SftpException {
        String pathHome = System.getProperty("user.dir");
        String path =  (pathHome.substring(pathHome.length()).equals("/") ? pathHome+"data-folder/" : pathHome+"/data-folder/");
            try {
            session = jsch.getSession(USER, host, 22);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(PASSW);
            session.connect();
         
            com.jcraft.jsch.Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;
            System.out.println("\n\n Downloading file.....");
            sftpChannel.get(path+file,path);
            sftpChannel.exit();
            session.disconnect();
        } catch (JSchException e) {
            return false;  
        } catch (SftpException e) {
            return false;
        }
        return true;

    }
}
