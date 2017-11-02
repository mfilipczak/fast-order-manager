package com.cgi.fastordermanager.order;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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
@Table(name = "rfs", indexes = {@Index(columnList = "CFS_ID")})
public class Rfs extends AbstractPersistable<Long> implements Serializable { // NOSONAR

    private static final long serialVersionUID = 8848887579564649636L;

 
    @Getter
    @Setter
    String currentState;
    
    @Getter
    @Setter
    String name;
    
    @Getter
    @Setter
    @NonNull
    String rfsId;

    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name="CFS_ID", nullable=false,insertable=false,updatable=false)
    Cfs cfs;
    
    @Getter
    @Setter
    private String blockedRfs;

    @JsonIgnore
    @Override
    public boolean isNew() {
        return super.isNew();
    }
}
