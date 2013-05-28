package br.unb.cic.bionimbus.config;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;


public final class BioNimbusConfigLoader {

    private BioNimbusConfigLoader() {
    }

    public static BioNimbusConfig loadHostConfig(final String filename) throws IOException {

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        BioNimbusConfig config = mapper.readValue(new File(filename), BioNimbusConfig.class);

        if (config.getInfra() == null) {
            config.setInfra("linux");
        }

        config.setInfra(config.getInfra());
        return config;
    }

}
