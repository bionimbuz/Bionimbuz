package br.unb.cic.bionimbuz.utils;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class YamlUtils {
    public static <T> T mapToClass(final String filename, Class<T> cfgClass) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        T config = (T) mapper.readValue(new File(filename), cfgClass);
        return config;
    }    
}
