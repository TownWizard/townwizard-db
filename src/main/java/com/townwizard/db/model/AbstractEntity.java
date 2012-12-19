package com.townwizard.db.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * A common super class for all entity classes in the application.
 * 
 * From this class all entities in the system will inherit:
 * 
 * - serializability
 * - exclusion of null fields during json rendering
 * - ability to be used as hash keys (the class provides generic equals() and hashCode() for all subclasses)
 * - ability to be sorted (the class implements Comparable for all subclasses)
 * - a database generated ID (primary key)
 * - a database active flag.  This flag should be used for soft deletes.  No objects should be
 *   retrieved from the db with the active flag set to false 
 */
@MappedSuperclass
@JsonSerialize (include = JsonSerialize.Inclusion.NON_EMPTY)
public abstract class AbstractEntity implements Serializable, Comparable<AbstractEntity> {
    
    private static final long serialVersionUID = 1L;
    
    @Id @GeneratedValue @Column(nullable = false, updatable = false)
    private Long id;
    @JsonIgnore
    private Boolean active;
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
        
    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     * Database ID based implementation of hashCode()
     */
    @Override
    public int hashCode() {
        if(id == null) {
            return super.hashCode();
        }
        return id.hashCode();
    }
    
    /**
     * Database ID based implementation of equals()
     */
    @Override
    public boolean equals(Object o) {
        if(o == null) return false;
        if(this == o) return true;
        if(!(o instanceof AbstractEntity)) return false;
        AbstractEntity e = (AbstractEntity)o;
        if(id == null || e.id == null) {
            return false;
        }
        return id.equals(e.id);
    }
    
    /**
     * Database ID based implementation of compareTo()
     */
    @Override
    public int compareTo(AbstractEntity e) {
        if(id == null) {
            return e.id == null ? 0 : 1;
        }
        return e.id == null ? -1 : id.compareTo(e.id);
    }
}