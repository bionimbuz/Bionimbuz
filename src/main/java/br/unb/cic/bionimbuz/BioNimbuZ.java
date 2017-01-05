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

package br.unb.cic.bionimbuz;

import java.io.IOException;

import br.unb.cic.bionimbuz.config.BioNimbusConfig;
import br.unb.cic.bionimbuz.plugin.Plugin;
import br.unb.cic.bionimbuz.services.ServiceManager;
import br.unb.cic.bionimbuz.services.ServiceModule;

import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static br.unb.cic.bionimbuz.config.BioNimbusConfigLoader.*;
import br.unb.cic.bionimbuz.controller.ControllerManager;
import br.unb.cic.bionimbuz.controller.ControllerModule;
import br.unb.cic.bionimbuz.model.User;
import br.unb.cic.bionimbuz.persistence.dao.UserDao;
import static br.unb.cic.bionimbuz.plugin.PluginFactory.getPlugin;
import br.unb.cic.bionimbuz.plugin.PluginInfo;
import br.unb.cic.bionimbuz.plugin.linux.LinuxGetInfo;
import br.unb.cic.bionimbuz.plugin.linux.LinuxPlugin;
import br.unb.cic.bionimbuz.services.PricingGetterService;
import br.unb.cic.bionimbuz.services.tarification.Amazon.AmazonIndex;
import br.unb.cic.bionimbuz.services.tarification.Google.GoogleCloud;
import br.unb.cic.bionimbuz.toSort.Listeners;
import br.unb.cic.bionimbuz.utils.PBKDF2;
import com.amazonaws.util.json.JSONException;
//import br.unb.cic.bionimbus.utils.RuntimeUtil;

import static com.google.inject.Guice.createInjector;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

public class BioNimbuZ {

    private static final Logger LOGGER = LoggerFactory.getLogger(BioNimbuZ.class);

    public static final Injector serviceInjector = createInjector(new ServiceModule());
    public static final Injector controllerInjector = createInjector(new ControllerModule());

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                LOGGER.info("Shutdown hook");
            }

        });
    }

    /**
     * BioNimbuZ public constructor
     *
     * @param config
     * @throws IOException
     * @throws InterruptedException
     */
    public BioNimbuZ(BioNimbusConfig config) throws IOException, InterruptedException {
//        String ip = RuntimeUtil.runCommand("curl ifconfig.co");
//        config.setId(ip);
        config.setId(UUID.randomUUID().toString());
        List<Listeners> listeners = new CopyOnWriteArrayList<>();

        if (!config.isClient()) {
            LinuxGetInfo getinfo = new LinuxGetInfo();

            PluginInfo infopc = getinfo.call();
            infopc.setId(config.getId());
            infopc.setHost(config.getHost());
            infopc.setPrivateCloud(config.getPrivateCloud());

            final Plugin plugin = getPlugin(config.getInfra(), config);
            if (plugin instanceof LinuxPlugin) {
                ((LinuxPlugin) plugin).setMyInfo(infopc);
            }
            plugin.start();

        }

        // Intantiates ServiceManager and Controller
        ServiceManager serviceManager = serviceInjector.getInstance(ServiceManager.class);
        ControllerManager controllerManager = controllerInjector.getInstance(ControllerManager.class);

        // Starts all Services
        serviceManager.startAll(config, listeners);

        // Starts all Controllers
        controllerManager.startAll(config);

    }

    public static void main(String[] args) throws IOException, InterruptedException, NoSuchAlgorithmException, InvalidKeySpecException {

        final String configFile = System.getProperty("config.file", "conf/node.yaml");
        BioNimbusConfig config = loadHostConfig(configFile);

        // !!! MEDIDA PALEATIVA !!! Para nao ter que trocar o node.yaml toda vez

//        config.setZkConnString(InetAddress.getLocalHost().getHostAddress() + ":2181");

//        config.setAddress(InetAddress.getLocalHost().getHostAddress());

        // Adiciona usuário 'root' para teste 
        UserDao userDao = new UserDao();

        if (!userDao.exists("root")) {
            User u = new User();
            u.setLogin("root");
            u.setPassword(PBKDF2.generatePassword("root"));
            u.setCpf("01092010101");
            u.setCelphone("0");
            u.setEmail("@");
            u.setNome("nome");
            u.setStorageUsage(0l);

            new UserDao().persist(u);
        }
        //medida paleativa para criar o arquivo das instancias da google e da amazon precisa por no serviço
            AmazonIndex amazonIndex = new AmazonIndex();
            GoogleCloud googleCloud = new GoogleCloud();


        // !!! Fim MEDIDA PALEATIVA !!!
        LOGGER.debug("config = " + config);

        new BioNimbuZ(config);
//        new AmazonDataGet(config);
//        new GoogleDataGet(config);
//    
    }

}
