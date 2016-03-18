/*
    BioNimbuZ is a federated cloud platform.
    Copyright (C) 2012-2015 Laboratory of Bioinformatics and Data (LaBiD), 
    Department of Computer Science, University of Brasilia, Brazil

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package br.unb.cic.bionimbus.utils;

import br.unb.cic.bionimbus.config.ConfigurationRepository;
import java.io.File;
import java.io.IOException;

import br.unb.cic.bionimbus.services.storage.bandwidth.BandwidthCalculator;
import br.unb.cic.bionimbus.services.storage.compress.CompressPolicy;
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
    private static final Long MIN_SIZE_FOR_COMPRESSION = 10 * 1024 * 1024L;

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
        throw new UnsupportedOperationException("Not supported yet."); // To
        // Templates.
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

        long inicio = 0, fim = 0;
        String toBeSent = path;
        try {
            this.channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;

            inicio = System.currentTimeMillis();
            if (new File(toBeSent).getTotalSpace() >= MIN_SIZE_FOR_COMPRESSION) {
                try {

                    System.out.println("\n Compressing file.....\n\n\n");
                    toBeSent = CompressPolicy.verifyAndCompress(path,
                            BandwidthCalculator.linkSpeed(address));
                } catch (IOException e) {
                    toBeSent = path;
                }
            }
            /*
             * Sem setar nenhuma permissao o arquivo chega trancado no destino,
             * sendo acessado apenas pelo root, portanto preferi dar um 777
             * antes de enviar o arquivo para que chegue livre ao destino. Por
             * questões de segurança, talvez isso deva ser repensado
             * futuramente.
             */
            //sftpChannel.chmod(777, path);
            System.out.println("\n Uploading file.....\n\n\n");
            sftpChannel.put(toBeSent, pathDest);
            sftpChannel.exit();
            session.disconnect();
            fim = System.currentTimeMillis();

            CompressPolicy.deleteIfCompressed(toBeSent);

        } catch (JSchException a) {
            return false;
        }

        LOGGER.info("Upload total time: " + (fim - inicio));
        LOGGER.info("Sent file: " + toBeSent);

        return true;

    }

}
