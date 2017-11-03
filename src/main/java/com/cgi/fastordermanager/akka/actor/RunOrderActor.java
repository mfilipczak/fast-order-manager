package com.cgi.fastordermanager.akka.actor;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.statemachine.StateMachine;

import com.cgi.fastordermanager.ContextEntity;
import com.cgi.fastordermanager.DefaultStateMachineAdapter;
import com.cgi.fastordermanager.akka.ActorManager;
import com.cgi.fastordermanager.order.Order;
import com.cgi.fastordermanager.order.OrderEvent;
import com.cgi.fastordermanager.order.OrderRepository;
import com.cgi.fastordermanager.order.OrderState;

import akka.actor.AbstractActor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Access(AccessType.FIELD)
@Scope("prototype")
@AllArgsConstructor
@Configuration
public class RunOrderActor extends AbstractActor {

	
	@Autowired
	private final ActorManager actorManager;
	
	@Autowired
	private final OrderRepository orderRepository;
	
	@Autowired
	final DefaultStateMachineAdapter<OrderState, OrderEvent, ContextEntity<OrderState, OrderEvent, ? extends Serializable>> orderStateMachineAdapter;

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(Order.class, order -> {
			log.info("Received order to start: {}", order);
			StateMachine<OrderState, OrderEvent> machine  = orderStateMachineAdapter.create();
			machine.getExtendedState().getVariables().put("ID", order.getExternalId());
			machine.start();
			orderStateMachineAdapter.persist(machine, order);
			order.setCreateTime(new Date());
			orderRepository.saveAndFlush(order);
			actorManager.changeOrderStatus(order, OrderEvent.DECOMPOSE);
		}).matchAny(o -> log.info("received unknown message")).build();
	}
}
