package br.unb.cic.bionimbus.services.messaging;

public interface MessageListener {

    void onEvent(Message message);

}
