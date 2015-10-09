/*
    BioNimbuZ is a federated cloud platform.
    Copyright (C) 2012-2015 Laboratory of Bioinformatics and Data (LaBiD), 
    Department of Computer Science, University of Brasilia, Brazil

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
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
