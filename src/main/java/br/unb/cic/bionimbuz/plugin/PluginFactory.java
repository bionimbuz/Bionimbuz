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
package br.unb.cic.bionimbuz.plugin;

import java.io.IOException;

import br.unb.cic.bionimbuz.plugin.linux.LinuxPlugin;

public class PluginFactory {

    private static Plugin REF;

    private PluginFactory() {
    }

    public static synchronized Plugin getPlugin(final String pluginType) throws IOException {
        if (REF == null) {
//            if (pluginType.equals("hadoop"))
//                REF = new HadoopPlugin();
//            else 
            if (pluginType.equals("linux")) {
                REF = new LinuxPlugin();
            }
//            else if (pluginType.equals("sge")) {
//                REF = new SGEPlugin(config);
//            }
        }
        return REF;
    }

}
