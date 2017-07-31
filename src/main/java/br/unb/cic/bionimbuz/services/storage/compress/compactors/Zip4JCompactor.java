package br.unb.cic.bionimbuz.services.storage.compress.compactors;

import java.io.File;
import java.io.IOException;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import br.unb.cic.bionimbuz.services.storage.compress.Compactor;

public class Zip4JCompactor implements Compactor {

    @Override
    public String compact(String in, int compressionLevel) throws IOException {

        File out = new File(in + ".zip4j");
        try {

            ZipFile zipFile = new ZipFile(out.getAbsolutePath());
            ZipParameters parametes = new ZipParameters();
            parametes.setCompressionLevel(compressionLevel);
            zipFile.addFile(new File(in), parametes);

        } catch (ZipException e) {
            throw new IOException(e);
        }
        return out.getAbsolutePath();
    }

    @Override
    public String descompact(String in) throws IOException {

        String name = CompressionUtils.getName(in).replace(".zip4j", "");
        String folder = CompressionUtils.getParentFolder(in);

        try {

            ZipFile zipFile = new ZipFile(in);
            zipFile.extractFile(name, folder);

        } catch (ZipException e) {
            throw new IOException(e);
        }
        return folder + "/" + name;
    }

}
