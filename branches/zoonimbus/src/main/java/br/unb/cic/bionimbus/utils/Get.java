/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.utils;

import br.unb.cic.bionimbus.p2p.Host;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import org.jboss.netty.channel.Channel;

/**
 *
 * @author biocloud2
 */
public class Get {
    
    private JSch jsch = new JSch();
    private Session session = null;
    private String HOST = "192.168.1.111";
    private String USER = "biocloud1";
    private String PASSW = "cloudbio";
    private int PORT = 22;
    private com.jcraft.jsch.Channel channel;
    private String path = "/home/zoonimbus/NetBeansProjects/zoonimbus/data-folder/";
    
    public boolean startSession(String file) throws JSchException, SftpException {
            
            try {
            session = jsch.getSession(USER, HOST, 22);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(PASSW);
            session.connect();
         
            com.jcraft.jsch.Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;
            //sftpChannel.cd(path);
            sftpChannel.get("/home/biocloud1/NetBeansProjects/zoonimbus/data-folder/"+file,"/tmp" );
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