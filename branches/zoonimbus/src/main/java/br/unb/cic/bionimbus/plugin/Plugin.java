package br.unb.cic.bionimbus.plugin;

import br.unb.cic.bionimbus.p2p.P2PService;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include= JsonTypeInfo.As.PROPERTY, property="@class")
public interface Plugin {

    public void start();

    public void shutdown();

    public void setP2P(P2PService p2p);
}
