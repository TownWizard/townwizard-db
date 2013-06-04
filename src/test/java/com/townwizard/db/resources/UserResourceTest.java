package com.townwizard.db.resources;

import java.util.Date;
import java.util.List;

import org.apache.http.StatusLine;
import org.hibernate.Query;
import org.hibernate.Session;
import org.junit.Assert;
import org.junit.Test;

import com.townwizard.db.model.Address;
import com.townwizard.db.model.User;

/**
 * Unit tests for user web services.
 */
public class UserResourceTest extends ResourceTest {
    
    @Test
    public void testPostEmptyJson() {
        StatusLine statusLine = executePostJsonRequest("/users", "{}");
        int status = statusLine.getStatusCode();
        Assert.assertEquals("HTTP status should be 400 when JSON is empty", 400, status);
    }
    
    @Test
    public void testPostJsonWithEmptyStrings() {
        StatusLine statusLine = executePostJsonRequest("/users", "{\"email\":\"\",\"password\":\"     \",\"username\":null}");
        int status = statusLine.getStatusCode();
        Assert.assertEquals("HTTP status should be 400 when JSON email or password is empty", 400, status);
    }    

    @Test
    public void testPostUserWithInvalidEmail() {
        String email = "invalid";
        try {
            deleteUserByEmail(email);
            StatusLine statusLine = executePostJsonRequest("/users", getMinimalUserJson(email));
            int status = statusLine.getStatusCode();
            Assert.assertEquals(
                    "HTTP status should be 400 when email is invalid", 400, status);
        } finally {
            deleteUserByEmail(email);
        }
    }
    
    @Test
    public void testPostUserWithEmptyEmailAndPassword() {
        try {
            deleteUserByLastName("UNIQUE_LAST_NAME");
            
            String json = "{\"firstName\":\"test\",\"lastName\":\"UNIQUE_LAST_NAME\",\"loginType\":\"FACEBOOK\"}";            
            StatusLine statusLine = executePostJsonRequest("/users", json);
            int status = statusLine.getStatusCode();
            Assert.assertEquals(
                    "HTTP status should be 400 when trying to create a facebook user with email and password null\n" + 
                    "For facebook users are create by using 'login' method not 'create' method", 400, status);
        } finally {
            deleteUserByLastName("UNIQUE_LAST_NAME");
        }
    }
    
    @Test
    public void testPostMinimalUserJson() {
        String email = "min_user_json@test.com";
        try {
            deleteUserByEmail(email);
            StatusLine statusLine = executePostJsonRequest("/users", getMinimalUserJson(email));
            int status = statusLine.getStatusCode();
            Assert.assertEquals(
                    "HTTP status should be 201 (created) for the minimal user JSON", 201, status);
            assertUserCreatedCorrectly(getMinimalUserJson(email), email);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            deleteUserByEmail(email);
        }
    }
    
    @Test
    public void testPostFullUserJson() {
        String email = "full_user_json@test.com";
        try {
            deleteUserByEmail(email);
            StatusLine statusLine = executePostJsonRequest("/users", getFullUserJson(email));
            int status = statusLine.getStatusCode();
            Assert.assertEquals(
                    "HTTP status should be 201 (created) for the minimal user JSON", 201, status);
            assertUserCreatedCorrectly(getFullUserJson(email), email);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            deleteUserByEmail(email);
        }
    }    
    
    @Test
    public void testDuplicateEmail() {
        String email = "dup_email_user@test.com";
        try {
            deleteUserByEmail(email);
            StatusLine statusLine = executePostJsonRequest("/users", getMinimalUserJson(email));
            int status = statusLine.getStatusCode();
            Assert.assertEquals(
                    "HTTP status should be 201 (created) for the minimal user JSON", 201, status);
            StatusLine statusLine2 = executePostJsonRequest("/users", getMinimalUserJson(email));
            int status2 = statusLine2.getStatusCode();
            Assert.assertEquals(
                    "HTTP status should be 409 (conflict) when email is a duplicate", 409, status2);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            deleteUserByEmail(email);
        }
    }
    
    @Test
    public void testLogin() {
        String email = "login_test_user@test.com";
        try {
            deleteUserByEmail(email);
            StatusLine statusLine = executePostJsonRequest("/users", getMinimalUserJson(email));
            int status = statusLine.getStatusCode();
            Assert.assertEquals("Can't create user for login test.  Expected 201 http status", 201, status);
                        
            StatusLine statusLine2 = executePostJsonRequest("/users/login", getMinimalUserJson(email));
            Assert.assertEquals("HTTP status should be 200 when login in existing user", 
                    200, statusLine2.getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            deleteUserByEmail(email);
        }
    }
    
    @Test
    public void testRegistrationIpUpdate() {
        String email = "reg_ip_test_user@test.com";
        String testIp = "216.215.88.11";
        try {
            deleteUserByEmail(email);
            StatusLine statusLine = executePostJsonRequest("/users", getMinimalUserJson(email));
            Assert.assertEquals(
                    "Can't create user for update registration ip test.  Expected 201 http status", 
                    201, statusLine.getStatusCode());
            
            User u = getUserByEmailFromTheDb(email);
            Assert.assertNull(u.getRegistrationIp());
            Date updatedOnCreate = u.getUpdated();
            Assert.assertNotNull("User updated time should be set when created", updatedOnCreate);
            
            try {Thread.sleep(1000);} catch(Exception e) {}
            String response = executeGetRequest("/users/" + u.getId() + "?ip=" + testIp);
            User updatedUser = userFromJson(response);
            Assert.assertEquals("Test IP must be set", testIp, updatedUser.getRegistrationIp());

            User userFromDb = getUserByEmailFromTheDb(email);
            Date updated = userFromDb.getUpdated();            
            Assert.assertTrue("The updated time should change on IP update", updated.after(updatedOnCreate));
            
            String differentIp = "127.0.0.1";
            response = executeGetRequest("/users/" + u.getId() + "?ip=" + differentIp);
            User notUpdatedUser = userFromJson(response);
            Assert.assertEquals("Test IP must not change if present", testIp, notUpdatedUser.getRegistrationIp());            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            deleteUserByEmail(email);
        }
    }
    
    @Test
    public void testSiteIdUpdate() {
        String email = "reg_site_id_test_user@test.com";
        Integer testSiteId = 2;
        try {
            deleteUserByEmail(email);
            StatusLine statusLine = executePostJsonRequest("/users", getMinimalUserJson(email));
            Assert.assertEquals(
                    "Can't create user for update registration ip test.  Expected 201 http status", 
                    201, statusLine.getStatusCode());
            
            User u = getUserByEmailFromTheDb(email);
            Assert.assertNull(u.getSiteId());

            String response = executeGetRequest("/users/" + u.getId() + "?siteid=" + testSiteId);
            User updatedUser = userFromJson(response);
            Assert.assertEquals("Test site ID must be set", testSiteId, updatedUser.getSiteId());
            
            Integer differentSiteId = 5;
            response = executeGetRequest("/users/" + u.getId() + "?siteid=" + differentSiteId);
            User notUpdatedUser = userFromJson(response);
            Assert.assertEquals("Test site ID must not change if present", testSiteId, notUpdatedUser.getSiteId());            
            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            deleteUserByEmail(email);
        }
    }
    
    private String getMinimalUserJson(String email) {
        return "{\"email\":\"" + email + "\",\"password\":\"secret\"}";
    }
    
    private String getFullUserJson(String email) {
        return "{\"username\":\"j2vm\",\"email\":\"" + email + 
                "\",\"password\":\"secret\",\"firstName\":\"Vlad\",\"lastName\":\"Mazheru\",\"year\":1968,\"gender\":\"M\",\"mobilePhone\":\"917-439-7193\",\"registrationIp\":\"127.0.0.1\"," +
                "\"loginType\":\"TOWNWIZARD\"," + "\"externalId\":\"123456\"," +
                "\"address\":{\"address1\":\"324 Nelson Ave\",\"address2\":\"Frnt\",\"city\":\"Staten Island\",\"state\":\"NY\",\"postalCode\":\"10308\",\"country\":\"USA\"}}";
    }
    
    private void assertUserCreatedCorrectly(String userJson, String email) throws Exception {
        User createdUser = getUserByEmailFromTheService(email);
        User userFromJson = userFromJson(userJson);
        Assert.assertTrue("User created should have the same properties as user submitted", 
                usersEqual(createdUser, userFromJson));
    }
    
    private void deleteUserByLastName(String lastName) {
        Session session = null;
        try {
            session = getMasterSessionFactory().openSession();
            session.beginTransaction();
            Query q = session.createQuery("from User where lastName = :last_name").setString("last_name", lastName);
            @SuppressWarnings("unchecked")
            List<User> users = q.list();
            for(User u : users) {
              session.delete(u);
            }
            session.getTransaction().commit();
        } finally {
            if(session != null) {
                session.close();
            }
        }
    }
    
    private boolean usersEqual(User fromDb, User original) {
        boolean result = true;
        result &= fromDb.getEmail().equals(original.getEmail());
        result &= compareWithNulls(fromDb.getUsername(), original.getUsername());
        result &= compareWithNulls(fromDb.getFirstName(), original.getFirstName());
        result &= compareWithNulls(fromDb.getLastName(), original.getLastName());
        result &= compareWithNulls(fromDb.getYear(), original.getYear());
        result &= compareWithNulls(fromDb.getGender(), original.getGender());
        result &= compareWithNulls(fromDb.getMobilePhone(), original.getMobilePhone());
        result &= addressesEqual(fromDb.getAddress(), original.getAddress());
        return result;
    }
    
    @SuppressWarnings("null")
    private boolean addressesEqual(Address a1, Address a2) {
        if(a1 == null && a2 == null) return true;
        if(a1 == null && a2 != null) return false;
        if(a1 != null && a2 == null) return false;
        
        boolean result = true;
        result &= compareWithNulls(a1.getAddress1(), a2.getAddress1());
        result &= compareWithNulls(a1.getAddress2(), a2.getAddress2());
        result &= compareWithNulls(a1.getCity(), a2.getCity());
        result &= compareWithNulls(a1.getState(), a2.getState());
        result &= compareWithNulls(a1.getPostalCode(), a2.getPostalCode());
        result &= compareWithNulls(a1.getCountry(), a2.getCountry());
        return result;
    }
    
    @SuppressWarnings("null")
    private boolean compareWithNulls(Object o1, Object o2) {
        if(o1 == null && o2 == null) return true;
        if(o1 != null && o2 == null) return false;
        if(o1 == null && o2 != null) return false;
        return o1.equals(o2);
    }
}