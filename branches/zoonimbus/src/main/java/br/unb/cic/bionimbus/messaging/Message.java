package br.unb.cic.bionimbus.messaging;

public interface Message {

    byte[] serialize() throws Exception;

    void deserialize(byte[] buffer) throws Exception;

    int getType();

}
