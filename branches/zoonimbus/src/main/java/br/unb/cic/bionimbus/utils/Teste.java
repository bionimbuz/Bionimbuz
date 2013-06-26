/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.utils;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author biocloud2
 */
public class Teste {
    public static void main(String[] args) throws IOException {
        String path ="/a/b/cde.pdf";
        File file =new File("/home/biocloud2/abc.pdf");
        System.out.println("1"+file.getPath()+" 2"+file.getAbsolutePath()+"3"+file.getCanonicalPath()+"4"+file.getName());
    }
}
