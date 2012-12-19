package com.townwizard.db.resources;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.townwizard.db.model.User;
import com.townwizard.db.model.User.LoginType;
import com.townwizard.db.services.UserService;

/**
 * Contains user (that is user registration, login, etc) related services.
 */
@Component
@Path("/users")
public class UserResource extends ResourceSupport {
    
    @Autowired
    private UserService userService;
    
    /**
     * Given a GET request with user id as a path parameter, return JSON user representation.
     */
    @GET
    @Path("/{userid}")
    @Produces(MediaType.APPLICATION_JSON)
    public User getUserById(@PathParam("userid") Long userId) {
        User u = null;
        try {
            u = userService.getById(userId);            
        } catch (Exception e) {
            handleGenericException(e);
        }
        
        if (u == null) {
            throw new WebApplicationException(Response
                    .status(Status.NOT_FOUND)
                    .entity(EMPTY_JSON)
                    .type(MediaType.APPLICATION_JSON).build());
        }
        return u.asJsonView();
    }

    /**
     * Given a GET request with user email and user login type, return JSON user representation.
     */
    @GET
    @Path("/{logintype}/{email}")
    @Produces(MediaType.APPLICATION_JSON)
    public User getUserByEmail(
            @PathParam("logintype") int loginTypeId,
            @PathParam("email") String email) {
        User u = null;
        try {
            u = userService.getByEmailAndLoginType(email, LoginType.byId(loginTypeId));
        } catch (Exception e) {
            handleGenericException(e);
        }
        
        if (u == null) {
            throw new WebApplicationException(Response
                    .status(Status.NOT_FOUND)
                    .entity(EMPTY_JSON)
                    .type(MediaType.APPLICATION_JSON).build());
        }
        return u.asJsonView();
    }    
    
    /**
     * Given POST json body, parse it into a user object, and login the user by verifying the user's
     * email and password.
     * 
     * This service is used to login Townwizard's own users (that is with login type TOWNWIZARD).
     */
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public User login(InputStream is) {
        User u = null;
        try {
            User fromJson = parseJson(User.class, is);
            u = userService.login(fromJson.getEmail(), fromJson.getPassword());
        } catch (Exception e) {
            handleGenericException(e);
        }
        
        if (u == null) {
            throw new WebApplicationException(Response
                    .status(Status.NOT_FOUND)
                    .entity(EMPTY_JSON)
                    .type(MediaType.APPLICATION_JSON).build());
        }
        return u.asJsonView();
    }
    
    /**
     * Given POST json body, parse it into a user object and create or update the user in the DB
     * with the data from the request.
     * 
     * This is actually a service to update external users data.  Such users have NULL password.
     * 
     * This method is generic and not used for Facebook login.
     */
    @POST
    @Path("/loginwith")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public User loginWith(InputStream is) {
        User u = null;        
        try {
            u = parseJson(User.class, is);
            createOrUpdateExternalUser(u);
        } catch (Exception e) {
            handleGenericException(e);
        }
        
        if (u == null || u.getId() == null) {
            throw new WebApplicationException(Response
                    .status(Status.NOT_FOUND)
                    .entity(EMPTY_JSON)
                    .type(MediaType.APPLICATION_JSON).build());
        }
        
        return u.asJsonView();
    }
    
    /**
     * Given POST json body, parse it into a user object and save the user in the DB.
     * 
     * This is a "user registration" service.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createTownwizardUser(InputStream is) {
        User user = null;
        try {
            user = parseJson(User.class, is);
            user.setLoginType(LoginType.TOWNWIZARD);
        } catch(Exception e) {
            handleGenericException(e);
        }
              
        if(user == null || !user.isValid()) {
            throw new WebApplicationException(Response
                    .status(Status.BAD_REQUEST)
                    .entity("Cannot create user: missing or invalid user data")
                    .type(MediaType.TEXT_PLAIN).build());
        }
        if(userService.getByEmailAndLoginType(user.getEmail(), LoginType.TOWNWIZARD) != null) {
            throw new WebApplicationException(Response
                    .status(Status.CONFLICT)
                    .entity(String.format("User with email %s already exists", user.getEmail()))
                    .type(MediaType.TEXT_PLAIN).build());            
        }
        
        if(user.getAddress() != null && !user.getAddress().isValid()) {
            user.setAddress(null);
        }
        
        try {
            Long id = userService.create(user);
            if(id == null) {
                sendServerError(new Exception("Problem creating user: user id is null"));
            }
        } catch(Exception e) {
            handleGenericException(e);
        }
        
        return Response.status(Status.CREATED).entity(user).build();
    }
    
    
    protected void createOrUpdateExternalUser(User u) {
        User fromDb = userService.getByEmailAndLoginType(u.getEmail(), u.getLoginType()); 
        if(fromDb != null) {
            u.setId(fromDb.getId());
            u.setCreated(fromDb.getCreated());
            u.setActive(fromDb.getActive());
            userService.update(u);
        } else {
            userService.create(u);
        }
    }
}