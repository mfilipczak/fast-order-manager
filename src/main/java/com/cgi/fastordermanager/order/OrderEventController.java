package com.cgi.fastordermanager.order;

import java.io.Serializable;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.statemachine.StateMachine;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.cgi.fastordermanager.ContextEntity;
import com.cgi.fastordermanager.DefaultStateMachineAdapter;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@RepositoryRestController
@RequiredArgsConstructor
@Slf4j
public class OrderEventController {

	@Autowired
	private final OrderRepository orderRepository;
	
	final DefaultStateMachineAdapter<OrderState, OrderEvent, ContextEntity<OrderState, OrderEvent, ? extends Serializable>> orderStateMachineAdapter;

	@RequestMapping(path = "/orders/{id}/receive/{event}", method = RequestMethod.POST)
	@SneakyThrows
	@Transactional
	public HttpEntity<Void> receiveEvent(@PathVariable("id") Order order, @PathVariable("event") OrderEvent event) {
		log.info("Otrzymalem event {} dla order {}", order.getStateMachineContext(), event);
		StateMachine<OrderState, OrderEvent> stateMachine = orderStateMachineAdapter.restore(order);
		log.info("Maszyna {}", stateMachine.getState());
		if (stateMachine.sendEvent(event)) {
			orderStateMachineAdapter.persist(stateMachine, order);
			orderRepository.saveAndFlush(order);
			return ResponseEntity.accepted().build();
		} else {
			return ResponseEntity.unprocessableEntity().build();
		}
	}

}
