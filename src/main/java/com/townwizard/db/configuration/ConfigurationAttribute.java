package com.townwizard.db.configuration;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="Configuration")
public class ConfigurationAttribute {
    
    @Id @GeneratedValue @Column(nullable = false, updatable = false)
    private Integer id;
    @Column(name="`key`")
    private String key;
    @Column(name="`value`")
    private String value;
    private String description;
    
    public ConfigurationAttribute(){}
    
    public ConfigurationAttribute(String key, String value, String description) {
        this.key = key;
        this.value = value;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

}
