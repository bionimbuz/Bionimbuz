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
package br.unb.cic.bionimbuz.services.storage.file;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.*;
import java.net.*;

/**
 * Created with IntelliJ IDEA.
 * User: edward
 * Date: 5/15/13
 * Time: 11:03 AM
 * To change this template use File | Settings | File Templates.
 */
public class FileService {

    private ServerSocket serverSocket;
    public static final int DEFAULT_PORT = 2121;
    private volatile boolean running = false;
//    private ExecutorService executorService = Executors.newCachedThreadPool();


    /**
     * TODO: ALUNOS: OLHEM ESTAS OPÇÕES
     * long totalSpace = file.getTotalSpace(); //total disk space in bytes.
     * long usableSpace = file.getUsableSpace(); ///unallocated / free disk space in bytes.
     * long freeSpace = file.getFreeSpace(); //unallocated / free disk space in bytes.
     *
     * Investigar o uso do NIO do Java 7
     *
     */

    /**
     * Returns the total left space in bytes
     *
     * @param rootPath
     * @return
     */
    public static long getFreeSpace(String rootPath) throws IOException {
        File file = new File(rootPath);
        return file.getUsableSpace();
    }

    public static long getTotalSpace(String rootPath) throws IOException {
        File file = new File(rootPath);
        return file.getTotalSpace();
    }

    /**
     * TODO: ALUNOS: Utilizar a biblioteca http://www.jcraft.com/jsch (comando SCP)
     * Exemplos em http://www.jcraft.com/jsch/examples
     */

    public void copyFrom(String originHost, File file) throws IOException {
        Socket client = new Socket(originHost, DEFAULT_PORT);

        FileStat stat = new FileStat("tmp.txt");
        String json = new ObjectMapper().writeValueAsString(stat);
        send(json, client);

        client.close();
    }

    private void send(String message, Socket socket) throws IOException {
        DataOutputStream daos = new DataOutputStream(socket.getOutputStream());
        daos.writeInt(message.length());
        daos.writeUTF(message);
        daos.flush();
    }

    public void copyTo(String destinationHost) throws IOException {

    }

    public void startFileTransferService() throws IOException {
        serverSocket = new ServerSocket(DEFAULT_PORT);
        System.out.println("Iniciando File Transfer Service...");
        running = true;
        while (running) {
            try {
                Socket client = serverSocket.accept();
                System.out.println("client connected");

                DataInputStream dais = new DataInputStream(client.getInputStream());
                int length = dais.readInt();
                System.out.println(length);
                byte[] buf = new byte[length];
                String str = dais.readUTF();
                System.out.println(str);
            } catch (IOException ie) {
                if (running) {
                    running = false;
                    ie.printStackTrace();
                } else {
                    System.out.println("Closing server");
                }
            }
        }
    }

    public void stop() throws IOException {
        running = false;
        serverSocket.close();
//        Thread.currentThread().interrupt();
    }


    public static void main(String[] args) throws IOException, InterruptedException {

        final FileService fs = new FileService();
        new Thread() {
            public void run() {
                try {
                    fs.startFileTransferService();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        Thread.sleep(2000);

        fs.copyFrom("localhost", null);

        fs.stop();

    }


}
