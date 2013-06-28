package br.unb.cic.bionimbus.utils;

import br.unb.cic.bionimbus.p2p.Host;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

/**
 * Classe com os metodos para a realização de um upload na federação
 * @author Deric
 */
public class Put {

    private JSch jsch = new JSch();
    private Session session = null;
    private Host host;
    private String address;
    private String USER = "zoonimbus";
    private String PASSW = "zoonimbus";
    private int PORT = 22;
    private Channel channel;
    private String path;
    private String dest = "/home/zoonimbus/NetBeansProjects/zoonimbus/data-folder/";
    /*
     * Flag usada para separar o processo de upload do processo de replicação
     * flag 0 = upload
     * flag 1 = replicação
     */
    private int flag;

    public Put(String address, String path, int flag) {
        this.address = address;
        this.path = path;
        this.flag = flag;
    }

    public Put() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Método que realiza a conexão entre o cliente e o servidor ou entre servidores,
     * para upar um arquivo em um peer.
     * @return - true se o upload foi realizado com sucesso, false caso contrário
     * @throws JSchException
     * @throws SftpException
     */
    public boolean startSession() throws JSchException, SftpException {
        try {

            session = jsch.getSession(USER, address, PORT);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(PASSW);
            session.connect();
        } catch (JSchException e) {
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
            System.out.println("\n Uploading file.....\n\n\n");
            if(flag == 0){
                sftpChannel.put(path, dest);
                sftpChannel.exit();
                session.disconnect();
            }
            else{
                sftpChannel.put(dest+path, dest);
                sftpChannel.exit();
                session.disconnect();
            }
        } catch (JSchException a) {
            return false;
        }
        return true;

    }
}