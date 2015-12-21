package br.unb.cic.bionimbus.plugin;

import br.unb.cic.bionimbus.toSort.Listeners;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public interface Plugin extends Listeners {

    public void start();

    public void shutdown();

}
