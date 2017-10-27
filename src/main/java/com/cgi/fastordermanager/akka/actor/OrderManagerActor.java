package com.cgi.fastordermanager.akka.actor;

import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.cgi.fastordermanager.ContextEntity;
import com.cgi.fastordermanager.DefaultStateMachineAdapter;
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
public class OrderManagerActor extends AbstractActor {

	@Autowired
	final DefaultStateMachineAdapter<OrderState, OrderEvent, ContextEntity<OrderState, OrderEvent, ? extends Serializable>> orderStateMachineAdapter;

	@AllArgsConstructor
	public static class OrderStateChanged {
		private Order order;
		private OrderEvent event;
	}
	
	@Override
	public Receive createReceive() {
		return receiveBuilder().match(OrderStateChanged.class, order -> {
			log.info("Received order to start: {}", order);
			RestTemplate restTemplate = new RestTemplate();
			HttpEntity<Void> request = new HttpEntity<>(null);
			String fooResourceUrl  = String.format("http://localhost:8080/orders/%s/receive/%s", order.order.getId(), order.event.name());
			restTemplate.postForEntity(fooResourceUrl, request, HttpEntity.class);
		}).matchAny(o -> log.info("received unknown message")).build();
	}
}
