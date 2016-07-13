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

package br.unb.cic.bionimbus;

import java.io.IOException;

import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.plugin.Plugin;
import br.unb.cic.bionimbus.services.ServiceManager;
import br.unb.cic.bionimbus.services.ServiceModule;

import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static br.unb.cic.bionimbus.config.BioNimbusConfigLoader.*;
import br.unb.cic.bionimbus.controller.ControllerManager;
import br.unb.cic.bionimbus.controller.ControllerModule;
import br.unb.cic.bionimbus.model.User;
import br.unb.cic.bionimbus.persistence.dao.UserDao;
import static br.unb.cic.bionimbus.plugin.PluginFactory.getPlugin;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.linux.LinuxGetInfo;
import br.unb.cic.bionimbus.plugin.linux.LinuxPlugin;
import br.unb.cic.bionimbus.toSort.Listeners;
import br.unb.cic.bionimbus.utils.PBKDF2;

import static com.google.inject.Guice.createInjector;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class BioNimbus {

    private static final Logger LOGGER = LoggerFactory.getLogger(BioNimbus.class);

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
    public BioNimbus(BioNimbusConfig config) throws IOException, InterruptedException {

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
//        config.setZkConnString(InetAddress.getLocalHost().getHostAddress() + ":2558");
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

        // !!! Fim MEDIDA PALEATIVA !!!
        LOGGER.debug("config = " + config);

        new BioNimbus(config);
    }

}
