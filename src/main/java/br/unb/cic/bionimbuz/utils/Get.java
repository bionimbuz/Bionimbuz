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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbuz.utils;

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
        String path = BioNimbusConfig.get().getDataFolder();
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
