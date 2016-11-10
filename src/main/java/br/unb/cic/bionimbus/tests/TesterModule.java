package br.unb.cic.bionimbus.tests;

import br.unb.cic.bionimbus.services.RepositoryService;
import br.unb.cic.bionimbus.services.Service;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
import br.unb.cic.bionimbus.services.messaging.CuratorMessageService;
import com.google.inject.AbstractModule;

/**
 *
 * @author zoonimbus
 */
public class TesterModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Service.class).to(RepositoryService.class);
        
        // If someone changes CloudMessageService implementation, need to change to() method
        bind(CloudMessageService.class).to(CuratorMessageService.class);
    }

}
