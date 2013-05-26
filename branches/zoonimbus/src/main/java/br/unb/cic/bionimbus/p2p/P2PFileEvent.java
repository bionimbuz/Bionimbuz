package br.unb.cic.bionimbus.p2p;

import java.io.File;
import java.util.Map;

public class P2PFileEvent implements P2PEvent {

    private File file;

    private Map<String, String> parms;

    public P2PFileEvent(File file, Map<String, String> parms) {
        this.file = file;
        this.parms = parms;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Map<String, String> getParms() {
        return parms;
    }

    public void setParms(Map<String, String> parms) {
        this.parms = parms;
    }

    @Override
    public P2PEventType getType() {
        return P2PEventType.FILE;
    }

}
