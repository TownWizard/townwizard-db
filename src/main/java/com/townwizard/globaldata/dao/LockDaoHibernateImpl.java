package com.townwizard.globaldata.dao;

import org.springframework.stereotype.Component;

import com.townwizard.db.dao.AbstractDaoHibernateImpl;

@Component("lockDao")
public class LockDaoHibernateImpl extends AbstractDaoHibernateImpl implements LockDao {

    @Override
    public void lock() {
        getSession().createSQLQuery("SELECT * FROM `Lock` FOR UPDATE").list();   
    }


}
