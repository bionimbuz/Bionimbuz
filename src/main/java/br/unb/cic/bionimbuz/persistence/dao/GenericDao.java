package br.unb.cic.bionimbuz.persistence.dao;

import java.util.List;

public interface GenericDao<T> {

    public void persist(T entity);

    public List<T> list();

    public void update(T entity);

    public void delete(T entity);

    public T findById(Long id);
}
