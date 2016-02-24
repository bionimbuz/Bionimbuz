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
 * Classe com os metodos para a realização de um upload na federação
 *
 * @author Deric
 */
public class Put {

    private static final Logger LOGGER = LoggerFactory.getLogger(Put.class);

    private final JSch jsch = new JSch();
    private Session session = null;
    private final String address;
    private final String USER;
    private final String PASSW;
    private final int PORT;
    private Channel channel;
    private final String path;

    public Put(String address, String path) {
        this.address = address;
        this.path = path;
        
        SSHCredentials credentials = ConfigurationRepository.getSSHCredentials();
        
        USER = credentials.getUser();
        PASSW = credentials.getPassword();
        PORT = Integer.parseInt(credentials.getPort());
    }

    public Put() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Método que realiza a conexão entre o cliente e o servidor ou entre
     * servidores, para upar um arquivo em um peer.
     *
     * @return - true se o upload foi realizado com sucesso, false caso
     * contrário
     * @throws JSchException
     * @throws SftpException
     */
    public boolean startSession() throws JSchException, SftpException {
        String pathDest = ConfigurationRepository.getDataFolder();

        try {
            session = jsch.getSession(USER, address, PORT);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(PASSW);
            session.connect();
        } catch (JSchException e) {
            LOGGER.error("[JSchException] - " + e.getMessage());

            return false;
        }

        try {
            this.channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;
            /*
             * Sem setar nenhuma permissao o arquivo chega trancado no destino, sendo acessado apenas pelo root,
             * portanto preferi dar um 777 antes de enviar o arquivo para que chegue livre ao destino.
             * Por questões de segurança, talvez isso deva ser repensado futuramente.
             */
            //sftpChannel.chmod(777, path);

            LOGGER.info("Uploading file via SFTP");
            
            sftpChannel.put(path, pathDest);
            sftpChannel.exit();
            session.disconnect();

        } catch (JSchException a) {
            return false;
        }
        return true;

    }
}
