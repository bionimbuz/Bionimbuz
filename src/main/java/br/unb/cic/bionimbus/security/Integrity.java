/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.security;

import br.unb.cic.bionimbus.client.shell.SimpleShell;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 *
 * @author rafaelsardenberg
 */
public class Integrity {

    private List<br.unb.cic.bionimbus.avro.gen.PluginFile> pluginList;

    public Boolean verifyFile(String filePeerHash, String fileUploadedHash) throws NoSuchAlgorithmException, IOException {
         //TO-DO: Printar no lugar correto.

        //Compara os hashes
        if (filePeerHash == null ? fileUploadedHash == null : filePeerHash.equals(fileUploadedHash)) {
            System.out.println("Integridade do arquivo  verificada com sucesso! Arquivo transferido corretamente.");
            return true;
        } else {
            System.out.println("Erro na transferência do arquivo!");
            return false;
        }
    }

    public Boolean verifyAllFiles() throws IOException {
        SimpleShell shell = new SimpleShell();
        pluginList = shell.getRpcClient().getProxy().listFiles();

        //TO-DO: Retornar dados referentes aos arquivos do zookeeper
        return true;
    }
}
