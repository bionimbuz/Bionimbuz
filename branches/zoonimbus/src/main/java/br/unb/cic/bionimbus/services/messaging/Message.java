package br.unb.cic.bionimbus.services.messaging;

public interface Message {

    byte[] serialize() throws Exception;

    void deserialize(byte[] buffer) throws Exception;

    int getType();

}
