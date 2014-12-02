package br.unb.cic.bionimbus.services.files;

import br.unb.cic.bionimbus.services.storage.file.FileService;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: edward
 * To change this template use File | Settings | File Templates.
 */
public class FileServiceTest extends TestCase {

    @Test
    public void testSpaceLeft() throws IOException {
        long space = FileService.getFreeSpace("/dev/sda1");
        System.out.println(space);
        assertNotSame(space, 0L);
    }

    @Test
    public void testFileTransfer() throws IOException {
        FileService fs = new FileService();
        fs.copyFrom("localhost", new File("/home/edward/Anagrams.scala"));
    }
}
