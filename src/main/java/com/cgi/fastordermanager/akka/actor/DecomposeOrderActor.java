package com.cgi.fastordermanager.akka.actor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Access;
import javax.persistence.AccessType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.statemachine.StateMachine;
import org.springframework.web.client.RestTemplate;

import com.cgi.fastordermanager.ContextEntity;
import com.cgi.fastordermanager.DefaultStateMachineAdapter;
import com.cgi.fastordermanager.akka.ActorManager;
import com.cgi.fastordermanager.order.Order;
import com.cgi.fastordermanager.order.OrderEvent;
import com.cgi.fastordermanager.order.OrderRepository;
import com.cgi.fastordermanager.order.OrderState;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.cgi.tpc.model.*;

@Slf4j
@Access(AccessType.FIELD)
@Scope("prototype")
@AllArgsConstructor
@Configuration
public class DecomposeOrderActor extends AbstractActor {

	
	@Autowired
	private final ActorManager actorManager;
	
	@Autowired
	private final OrderRepository orderRepository;
	
	@Autowired
	final DefaultStateMachineAdapter<OrderState, OrderEvent, ContextEntity<OrderState, OrderEvent, ? extends Serializable>> orderStateMachineAdapter;

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(String.class, orderId -> {
			Order order = orderRepository.findByExternalId(orderId);
			log.info("Received order to start: {}", order);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			RestTemplate restTemplate = new RestTemplate();
			List<CfsDef> cfsList = new ArrayList<>();
			order.getCfs().stream().forEach(cfs -> {CfsDef d = new CfsDef(); d.setCfsId(cfs.getCfsId()); cfsList.add(d);});
			//httpEnitity       
			HttpEntity<Object> requestEntity = new HttpEntity<Object>(cfsList,headers);

			String url  = "http://localhost:8181/rfs";
			List<RfsDef> rfs = Arrays.asList(restTemplate.postForObject(url, requestEntity, RfsDef[].class));
		//	ResponseEntity<List<RfsDef>> rateResponse = restTemplate.exchange(url, HttpMethod.POST, requestEntity,new ParameterizedTypeReference<List<CfsDef>>() {});

			actorManager.changeOrderStatus(order, OrderEvent.PROCESS);
		}).matchAny(o -> log.info("received unknown message")).build();
	}
}
