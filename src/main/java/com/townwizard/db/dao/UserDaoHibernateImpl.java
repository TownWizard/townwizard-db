package com.townwizard.db.dao;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import com.townwizard.db.model.LoginRequest;
import com.townwizard.db.model.User;
import com.townwizard.db.model.User.LoginType;

/**
 * Hibernate implementation of UserDao interface
 */
@Component("userDao")
public class UserDaoHibernateImpl extends AbstractDaoHibernateImpl implements UserDao {

    @Override
    public User getByEmailAndLoginType(String email, LoginType loginType) {
        Query q = getSession().createQuery(
                "from User where email = :email and loginType = :login_type and active = true")
                .setString("email", email)
                .setInteger("login_type", loginType.getId());
        return (User)q.uniqueResult();
    }
    
    @Override
    public User getByExternalIdAndLoginType(Long externalId, LoginType loginType) {
        Query q = getSession().createQuery(
                "from User where externalId = :external_id and loginType = :login_type and active = true")
                .setLong("external_id", externalId)
                .setInteger("login_type", loginType.getId());
        User u = (User)q.uniqueResult();
        return u;
    }
    
    @Override
    public void createLoginRequest(LoginRequest loginRequest) {
        getSession().save(loginRequest);
    }
    
    @Override
    public LoginRequest getLoginRequest(String uuid) {
        Session session = getSession();
        LoginRequest loginRequest = (LoginRequest)session.load(LoginRequest.class, uuid);
        session.delete(loginRequest);
        return loginRequest;
    }
}