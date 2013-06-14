package br.unb.cic.bionimbus.utils;

import br.unb.cic.bionimbus.p2p.Host;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class Put {

        private JSch jsch = new JSch();
        private Session session = null;
        private Host host;
        private String address;
        private String USER="zoonimbus";
        private String PASSW="zoonimbus";
        private int PORT=22;
        private  Channel channel;
        private String path;
        
        public  Put(String address, String path)
        {
            this.address=address;
            this.path=path;
        }

    public Put() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
        
        public boolean startSession() throws JSchException, SftpException{
            System.out.println("\n Uploading file.....");
            session = jsch.getSession(USER, address, PORT); 
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(PASSW);
            session.connect();
            
            this.channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel; 
            sftpChannel.put(path, path.substring(1+path.lastIndexOf("/")).trim());
            
            sftpChannel.exit();
            session.disconnect();
            System.out.println(" Uploaded Complete !!");
            return true;
        }
}