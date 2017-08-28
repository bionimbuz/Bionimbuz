/*
 * BioNimbuZ is a federated cloud platform.
 * Copyright (C) 2012-2015 Laboratory of Bioinformatics and Data (LaBiD),
 * Department of Computer Science, University of Brasilia, Brazil
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package br.unb.cic.bionimbuz.config;

import java.io.IOException;

import org.codehaus.jackson.annotate.JsonIgnore;

import br.unb.cic.bionimbuz.constants.SystemConstants;
import br.unb.cic.bionimbuz.utils.YamlUtils;

public class DatabaseConfig {
    
    private static DatabaseConfig instance = null;
    static {
        try {
            instance = YamlUtils.mapToClass(SystemConstants.FILE_DATABASE, DatabaseConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private DatabaseConfig() {}
    
    public static DatabaseConfig get() {return instance;}

    @JsonIgnore
    private String databaseUrl;

    @JsonIgnore
    private String databaseUser;

    @JsonIgnore
    private String databasePass;

    public String getDatabaseUrl() {
        return databaseUrl;
    }
    public String getDatabaseUser() {
        return databaseUser;
    }
    public String getDatabasePass() {
        return databasePass;
    }
}
