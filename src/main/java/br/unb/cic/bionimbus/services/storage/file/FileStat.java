package br.unb.cic.bionimbus.services.storage.file;

/**
 * Created with IntelliJ IDEA.
 * User: edward
 * Date: 5/15/13
 * Time: 5:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileStat {

    private String filename;

    public FileStat() {
    }

    public FileStat(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public String toString() {
        return filename;
    }
}
