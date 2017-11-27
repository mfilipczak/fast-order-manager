package com.cgi.fastordermanager.order;

import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Access(AccessType.FIELD)
@Table(name = "attribute")
public class Attribute extends AbstractPersistable<Long> implements Serializable { // NOSONAR

    private static final long serialVersionUID = 8848887579564649636L;

 
    @Getter
    @Setter
    @NonNull
    String name;
    
    @Getter
    @Setter
    @NonNull
    String value;
    
    @Getter
    @Setter
    String oldValue;
    

    @JsonIgnore
    @Override
    public boolean isNew() {
        return super.isNew();
    }
    
}
