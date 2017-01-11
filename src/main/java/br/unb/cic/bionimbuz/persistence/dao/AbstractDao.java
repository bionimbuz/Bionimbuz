package br.unb.cic.bionimbuz.persistence.dao;

import javax.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDao<T> implements GenericDao<T> {

    protected final Logger LOGGER = LoggerFactory.getLogger(AbstractDao.class);

    protected EntityManager manager;

}
