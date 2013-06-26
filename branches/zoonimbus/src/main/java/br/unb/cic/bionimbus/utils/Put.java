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
    private String USER = "zoonimbus";
    private String PASSW = "zoonimbus";
    private int PORT = 22;
    private Channel channel;
    private String path;
    private String dest = "/home/zoonimbus/NetBeansProjects/zoonimbus/data-folder";

    public Put(String address, String path) {
        this.address = address;
        this.path = path;
    }

    public Put() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

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
            System.out.println("\n Uploading file.....\n\n\n");
            //sftpChannel.put(path, path.substring(1+path.lastIndexOf("/")).trim());
            sftpChannel.chmod(777, path);
            sftpChannel.put(path, dest);
            sftpChannel.exit();
            session.disconnect();
        } catch (JSchException a) {
            return false;
        }
        return true;

    }

    public static void main(String[] args) throws JSchException, SftpException {

        String path = "/home/biocloud2/excel11.pdf";

        Put conexao = new Put("192.168.1.102", path);
        if (!conexao.startSession()) {
            System.out.println("Erro no upload");
        }
    }
}