/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.utils;

import br.unb.cic.bionimbus.config.ConfigurationRepository;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Metodo para a conexao entre o servidor e o cliente em casos de downloads
 *
 * @author Deric
 */
public class Get {
    private static final Logger LOGGER = LoggerFactory.getLogger(Get.class);
    
    private final JSch jsch = new JSch();
    private Session session = null;
    private final String USER;
    private final String PASSW;
    private final int PORT;
    private Channel channel;

    public Get() {
        SSHCredentials credentials = ConfigurationRepository.getSSHCredentials();
        USER = credentials.getUser();
        PASSW = credentials.getPassword();
        PORT = Integer.parseInt(credentials.getPort());
    }

    public boolean startSession(String file, String host) throws JSchException, SftpException {        
        String path = ConfigurationRepository.getDataFolder();
        try {
            session = jsch.getSession(USER, host, PORT);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(PASSW);
            session.connect();

            channel = session.openChannel("sftp");
            channel.connect();
            
            ChannelSftp sftpChannel = (ChannelSftp) channel;
            
            LOGGER.info("Downloading file");
            sftpChannel.get(path + file, path);
            sftpChannel.exit();
            session.disconnect();
            
        } catch (JSchException | SftpException e) {
            return false;
        }
        return true;

    }
}
