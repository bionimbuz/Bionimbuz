package br.unb.cic.bionimbus.p2p.udp;


public final class WireMessage {

    private int id;

    private String data;

    public WireMessage() {
    }

    public WireMessage(int id, String data) {
        this.id = id;
        this.data = data;
    }

    public int getId() {

        return id;
    }

    public void setId(int id) {

        this.id = id;
    }

    public String getData() {

        return data;
    }

    public void setData(String data) {

        this.data = data;
    }

    public String toString() {
        return id + ":" + data;
    }
}
