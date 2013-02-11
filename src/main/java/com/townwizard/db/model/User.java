package com.townwizard.db.model;

import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.townwizard.db.util.EmailValidator;

/**
 * A model class representing users.
 * 
 * A user object has among other attributes an email and a login type, which together comprise
 * a unique user identifier
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="users")
public class User extends AuditableEntity {
    
    /**
     * Enumeration representing login types.  This is mapped to the 'login_type_id' column in the
     * 'User' table
     */
    public static enum LoginType {
        ZERO(0, "Zero"), //to make sure Java Enum ordinals will start with 1 for hibernate mapping
        TOWNWIZARD(1, "Townwizard"),
        FACEBOOK(2, "Facebook"),
        TWITTER(3, "Twitter");
        
        private final int id;
        private final String name;
        private LoginType(int id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public int getId() {return id;}
        public String getName() {return name;}
        
        public static LoginType byId(int id) {
            switch(id) {
            case 1: return TOWNWIZARD;
            case 2: return FACEBOOK;
            case 3: return TWITTER;
            default: return ZERO;
            }            
        }
    }     

    private static final long serialVersionUID = -6562731576094594464L;
    
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String name;
    private Integer year;
    private Character gender;
    private String mobilePhone;
    private String registrationIp;
    @Column(name="login_type_id")
    @Enumerated(EnumType.ORDINAL)
    private LoginType loginType;
    private Long externalId;
    private Integer siteId;
    
    @OneToOne(mappedBy = "user", cascade = {CascadeType.ALL})
    private Address address;
    
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Address getAddress() {
        return address;
    }
    public void setAddress(Address address) {
        this.address = address;
        if(address != null) {
            address.setUser(this);
        }
    }
    public Integer getYear() {
        return year;
    }
    public void setYear(Integer year) {
        this.year = year;
    }
    public Character getGender() {
        return gender;
    }
    public void setGender(Character gender) {
        this.gender = gender;
    }
    public String getMobilePhone() {
        return mobilePhone;
    }
    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }
    public String getRegistrationIp() {
        return registrationIp;
    }
    public void setRegistrationIp(String registrationIp) {
        this.registrationIp = registrationIp;
    }    
    public LoginType getLoginType() {
        return loginType;
    }
    public void setLoginType(LoginType loginType) {
        this.loginType = loginType;
    }
    public Long getExternalId() {
        return externalId;
    }
    public void setExternalId(Long externalId) {
        this.externalId = externalId;
    }
    public Integer getSiteId() {
        return siteId;
    }
    public void setSiteId(Integer siteId) {
        this.siteId = siteId;
    }
    @JsonIgnore
    public boolean isValid() {
        if(getLoginType() == null) {
            return false;
        }        
        if(getLoginType().equals(LoginType.TOWNWIZARD)) {
            return isEmailValid() && isPasswordValid();
        }
        return true;
    }
    
    public User asJsonView() {
        setPassword(null);
        return this;
    }
    
    public static User fromExternalUser(Map<String, Object> userData, LoginType loginType) {
        if(loginType == LoginType.FACEBOOK) {
            return fromFbUser(userData);
        } else if(loginType == LoginType.TWITTER) {
            return fromTwitterUser(userData);
        }
        return null;
    }    

    @Override
    public String toString() {
        return "User [username=" + username + ", email=" + email
                + ", firstName=" + firstName
                + ", lastName=" + lastName + ", year=" + year + ", gender="
                + gender + ", mobilePhone=" + mobilePhone + ", registrationIp="
                + registrationIp + ", address=" + address + "]";
    }
    
    private static User fromFbUser(Map<String, Object> fbUser) throws NumberFormatException {
        //fbUser represents the following JSON
        
        //{"id":"1205619426","name":"Vladimir Mazheru","first_name":"Vladimir","last_name":"Mazheru",
        //"link":"http:\/\/www.facebook.com\/vmazheru","username":"vmazheru","gender":"male",
        //"email":"v_mazheru\u0040yahoo.com","timezone":-5,"locale":"en_US","verified":true,
        //"updated_time":"2012-11-27T20:05:07+0000"}
        
        User u = new User();
        u.setExternalId(new Long((String)fbUser.get("id")));
        u.setEmail((String)fbUser.get("email"));
        u.setFirstName((String)fbUser.get("first_name"));
        u.setLastName((String)fbUser.get("last_name"));
        u.setName((String)fbUser.get("name"));
        u.setUsername((String)fbUser.get("username"));
        String gender = (String)fbUser.get("gender");
        if(gender != null && !gender.isEmpty()) {
            switch(gender.charAt(0)) {
            case 'm': u.setGender('M'); break;
            case 'f': u.setGender('F'); break;
            }
        }
        u.setLoginType(LoginType.FACEBOOK);
        
        return u;
    }
    
    private static User fromTwitterUser(Map<String, Object> twitterUser) {
        //twitterUser represents JSON like
        
        //{"id":28483095,"id_str":"28483095","name":"Vladimir Mazheru","screen_name":"j2vm","location":"New York",
        //"url":null,"description":"","protected":false,"followers_count":15,"friends_count":8,"listed_count":0,
        //"created_at":"Fri Apr 03 02:38:35 +0000 2009","favourites_count":0,"utc_offset":-18000,
        //"time_zone":"Quito","geo_enabled":true,"verified":false,"statuses_count":19,"lang":"en",
        //"status":{"created_at":"Tue Nov 30 01:21:46 +0000 2010","id":9416763842232320,"id_str":"9416763842232320",
        //"text":"@mnarrell and you are now officially an iPad architect",
        //"source":"\u003ca href=\"http:\/\/twitter.com\/#!\/download\/ipad\" rel=\"nofollow\"\u003eTwitter for iPad\u003c\/a\u003e",
        //"truncated":false,"in_reply_to_status_id":9407649854529536,"in_reply_to_status_id_str":"9407649854529536",
        //"in_reply_to_user_id":45436181,"in_reply_to_user_id_str":"45436181","in_reply_to_screen_name":"mnarrell",
        //"geo":null,"coordinates":null,"place":null,"contributors":null,"retweet_count":0,"favorited":false,
        //"retweeted":false},"contributors_enabled":false,"is_translator":false,"profile_background_color":"C0DEED",
        //"profile_background_image_url":"http:\/\/a0.twimg.com\/images\/themes\/theme1\/bg.png",
        //"profile_background_image_url_https":"https:\/\/si0.twimg.com\/images\/themes\/theme1\/bg.png",
        //"profile_background_tile":false,"profile_image_url":"http:\/\/a0.twimg.com\/sticky\/default_profile_images\/default_profile_4_normal.png",
        //"profile_image_url_https":"https:\/\/si0.twimg.com\/sticky\/default_profile_images\/default_profile_4_normal.png",
        //"profile_link_color":"0084B4","profile_sidebar_border_color":"C0DEED","profile_sidebar_fill_color":"DDEEF6",
        //"profile_text_color":"333333","profile_use_background_image":true,"default_profile":true,
        //"default_profile_image":true,"following":null,"follow_request_sent":null,"notifications":null}
        
        User u = new User();
        u.setExternalId(new Long((Integer)twitterUser.get("id")));
        u.setName((String)twitterUser.get("name"));
        u.setUsername((String)twitterUser.get("screen_name"));
        u.setLoginType(LoginType.TWITTER);
        
        return u;
    }
      
    private boolean isEmailValid() {        
        return EmailValidator.isValidEmailAddress(getEmail());
    }
    
    private boolean isPasswordValid() {
        return (password != null);
    }
}