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
package br.unb.cic.bionimbus.services.files;

import br.unb.cic.bionimbuz.services.storage.file.FileService;
import junit.framework.TestCase;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA. User: edward To change this template use File |
 * Settings | File Templates.
 */
@Ignore //These tests cannot run in the Continuous Integration Enviroment
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
