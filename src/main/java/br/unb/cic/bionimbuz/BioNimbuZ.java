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

package br.unb.cic.bionimbuz;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.inject.Guice;
import com.google.inject.Injector;

import br.unb.cic.bionimbuz.config.BioNimbusConfig;
import br.unb.cic.bionimbuz.controller.ControllerManager;
import br.unb.cic.bionimbuz.controller.ControllerModule;
import br.unb.cic.bionimbuz.model.User;
import br.unb.cic.bionimbuz.persistence.dao.UserDao;
import br.unb.cic.bionimbuz.plugin.Plugin;
import br.unb.cic.bionimbuz.plugin.PluginFactory;
import br.unb.cic.bionimbuz.plugin.PluginInfo;
import br.unb.cic.bionimbuz.plugin.linux.LinuxGetInfo;
import br.unb.cic.bionimbuz.plugin.linux.LinuxPlugin;
import br.unb.cic.bionimbuz.services.ServiceManager;
import br.unb.cic.bionimbuz.services.ServiceModule;
import br.unb.cic.bionimbuz.services.tarification.Amazon.AmazonIndex;
import br.unb.cic.bionimbuz.services.tarification.Google.GoogleCloud;
import br.unb.cic.bionimbuz.toSort.Listeners;
import br.unb.cic.bionimbuz.utils.NetworkUtil;
import br.unb.cic.bionimbuz.utils.PBKDF2;
import br.unb.cic.bionimbuz.utils.ZookeeperUtil;

public final class BioNimbuZ {

    private static Injector serviceInjector;
    private static Injector controllerInjector;

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Constructors.
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    private BioNimbuZ() {
        super();
        serviceInjector = Guice.createInjector(new ServiceModule());
        controllerInjector = Guice.createInjector(new ControllerModule());
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Statics methods.
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        
        if (NetworkUtil.isLocalhost(BioNimbusConfig.get().getZkHosts())) {
            ZookeeperUtil.startZookeeper();
        }

        // medida paleativa para criar o arquivo das instancias da google e da amazon precisa por no serviço
        new AmazonIndex();
        new GoogleCloud();

        final BioNimbuZ system = new BioNimbuZ();
        system.saveRootUser();
        system.start();
    }

    public static Injector getControllerInjector() {
        return controllerInjector;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Instances methods.
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    private void start() throws IOException {

        BioNimbusConfig.get().setId(UUID.randomUUID().toString());
        if (!BioNimbusConfig.get().isClient()) {

            final LinuxGetInfo linuxGetInfo = new LinuxGetInfo();

            final PluginInfo infopc = linuxGetInfo.call();
            infopc.setId(BioNimbusConfig.get().getId());
            infopc.setHost(BioNimbusConfig.get().getHost());
            infopc.setPrivateCloud(BioNimbusConfig.get().getPrivateCloud());

            final Plugin plugin = PluginFactory.getPlugin(BioNimbusConfig.get().getInfra());
            if (plugin instanceof LinuxPlugin) {
                ((LinuxPlugin) plugin).setMyInfo(infopc);
            }
            plugin.start();
        }

        // Instantiates ServiceManager and Controller
        final ServiceManager serviceManager = serviceInjector.getInstance(ServiceManager.class);
        final ControllerManager controllerManager = controllerInjector.getInstance(ControllerManager.class);

        // Starts all Services
        final List<Listeners> listeners = new CopyOnWriteArrayList<>();
        serviceManager.startAll(listeners);

        // Starts all Controllers
        controllerManager.startAll();
    }

    private void saveRootUser() throws NoSuchAlgorithmException, InvalidKeySpecException {
        final UserDao dao = new UserDao();
        final String rootLogin = "root";
        if (!dao.exists(rootLogin)) {
            final User object = this.bindRootUser();
            dao.persist(object);
        }
    }

    private User bindRootUser() throws NoSuchAlgorithmException, InvalidKeySpecException {
        final User object = new User();
        object.setLogin("root");
        object.setPassword(PBKDF2.generatePassword("root"));
        object.setCpf("00000000000");
        object.setCelphone("(61) 9 0000-0000");
        object.setEmail("root@bionimbuz.com");
        object.setNome("Usuário Administrador");
        object.setStorageUsage(0l);
        return object;
    }
}
