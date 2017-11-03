package com.cgi.fastordermanager.order;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.statemachine.StateMachineContext;

import com.cgi.fastordermanager.ContextEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Entity
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Access(AccessType.FIELD)
@Table(name = "orders", indexes = { @Index(columnList = "currentState"), @Index(columnList= "externalId", unique = true) })
@Slf4j
public class Order extends AbstractPersistable<Long> implements ContextEntity<OrderState, OrderEvent, Long> , Serializable{ // NOSONAR

    private static final long serialVersionUID = 8848887579564649636L;

    @Getter
    @JsonIgnore
    private StateMachineContext<OrderState, OrderEvent> stateMachineContext; // NOSONAR

    @Getter
    @Enumerated(EnumType.STRING)
    private OrderState currentState;
    
    @Getter
    @Setter
    @NonNull
    private String externalId;

    @Getter
    @Setter
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;
    
    @Getter
    @Setter
    @Temporal(TemporalType.TIMESTAMP)
    private Date completeTime;
    
    @Getter
    @Setter
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="order_id",nullable=false)
    List<Cfs> cfs;
    
    @Override
    public void setStateMachineContext(@NonNull StateMachineContext<OrderState, OrderEvent> stateMachineContext) {
    	this.currentState = stateMachineContext.getState();
        this.stateMachineContext = stateMachineContext;
        log.info("Stan: {} , Kontext: {}", currentState, stateMachineContext);
    }

    @JsonIgnore
    @Override
    public boolean isNew() {
        return super.isNew();
    }
}
