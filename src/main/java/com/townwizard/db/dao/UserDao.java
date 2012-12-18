package com.townwizard.db.dao;

import com.townwizard.db.model.LoginRequest;
import com.townwizard.db.model.User;
import com.townwizard.db.model.User.LoginType;

public interface UserDao extends AbstractDao {
    
    User getByEmailAndLoginType(String email, LoginType loginType);
    void createLoginRequest(LoginRequest loginRequest);
    LoginRequest getLoginRequest(String uuid);
    
}