package br.unb.cic.bionimbus.rest.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base resource for other REST resources
 * @author Vinicius
 */
public abstract class BaseResource {
    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseResource.class);
    
    /*
    public boolean isLogged(String login) {
        return LoggedUsers.isLogged(login);
    }
    */
}
