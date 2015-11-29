package br.unb.cic.bionimbus.persistence.dao;

import br.unb.cic.bionimbus.persistence.EntityManagerProducer;
import javax.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseDao<T> implements GenericDao<T> {
    protected final Logger LOGGER = LoggerFactory.getLogger(BaseDao.class);
    
    protected EntityManager manager;

    public BaseDao() {
        manager = EntityManagerProducer.getEntityManager();
    }

}
