/*
 * BioNimbuZ is a federated cloud platform.
 * Copyright (C) 2012-2015 Laboratory of Bioinformatics and Data (LaBiD),
 * Department of Computer Science, University of Brasilia, Brazil
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package br.unb.cic.bionimbuz.utils;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import br.unb.cic.bionimbuz.config.BioNimbusConfig;
import br.unb.cic.bionimbuz.config.ConfigurationRepository;
import br.unb.cic.bionimbuz.services.storage.bandwidth.BandwidthCalculator;
import br.unb.cic.bionimbuz.services.storage.compress.CompressPolicy;

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

        final SSHCredentials credentials = ConfigurationRepository.getSSHCredentials();

        this.USER = credentials.getUser();
        this.PASSW = credentials.getPassword();
        this.PORT = Integer.parseInt(credentials.getPort());
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
     *         contrário
     * @throws JSchException
     * @throws SftpException
     */
    public boolean startSession() throws JSchException, SftpException {

        if (NetworkUtil.isLocalhost(this.address)) {
            LOGGER.info("\n\n It is not needed to use the SFTP channel to transfer the file because the system is using a localhost configuration.\n\n\n");
            return true;
        }

        final String pathDest = BioNimbusConfig.get().getDataFolder();
        try {
            this.session = this.jsch.getSession(this.USER, this.address, this.PORT);
            this.session.setConfig("StrictHostKeyChecking", "no");
            this.session.setPassword(this.PASSW);
            this.session.connect();
        } catch (final JSchException e) {
            LOGGER.error("[JSchException] - " + e.getMessage());
            return false;
        }

        long inicio = 0, fim = 0;
        String toBeSent = this.path;
        try {
            this.channel = this.session.openChannel("sftp");
            this.channel.connect();
            final ChannelSftp sftpChannel = (ChannelSftp) this.channel;

            inicio = System.currentTimeMillis();
            if (new File(toBeSent).length() >= MIN_SIZE_FOR_COMPRESSION) {
                try {
                    System.out.println("\n Compressing file.....\n\n\n");
                    toBeSent = CompressPolicy.verifyAndCompress(this.path, BandwidthCalculator.linkSpeed(this.address));
                } catch (final IOException e) {
                    toBeSent = this.path;
                }
            }
            /*
             * Sem setar nenhuma permissao o arquivo chega trancado no destino,
             * sendo acessado apenas pelo root, portanto preferi dar um 777
             * antes de enviar o arquivo para que chegue livre ao destino. Por
             * questões de segurança, talvez isso deva ser repensado
             * futuramente.
             */
            // sftpChannel.chmod(777, path);
            System.out.println("\n Uploading file.....\n\n\n");
            sftpChannel.put(toBeSent, pathDest);
            sftpChannel.exit();
            this.session.disconnect();
            fim = System.currentTimeMillis();

            CompressPolicy.deleteIfCompressed(toBeSent);

        } catch (final JSchException a) {
            return false;
        }

        LOGGER.info("Upload total time: " + (fim - inicio));
        LOGGER.info("Sent file: " + toBeSent);

        return true;
    }
}
