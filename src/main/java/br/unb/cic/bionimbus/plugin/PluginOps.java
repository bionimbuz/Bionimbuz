package br.unb.cic.bionimbus.plugin;

import org.codehaus.jackson.annotate.JsonSubTypes;

/**
 * Created by IntelliJ IDEA.
 * User: edward
 * Date: 5/24/12
 * Time: 4:26 PM
 * To change this template use File | Settings | File Templates.
 */
@JsonSubTypes({
        @JsonSubTypes.Type(value = PluginInfo.class, name = "info"),
        @JsonSubTypes.Type(value = PluginFile.class, name = "file")
})
public interface PluginOps {
}
