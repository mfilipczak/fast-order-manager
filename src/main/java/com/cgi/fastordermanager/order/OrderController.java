package com.cgi.fastordermanager.order;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cgi.fastordermanager.ContextEntity;
import com.cgi.fastordermanager.DefaultStateMachineAdapter;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OrderController {

	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	final DefaultStateMachineAdapter<OrderState, OrderEvent, ContextEntity<OrderState, OrderEvent, ? extends Serializable>> orderStateMachineAdapter;

	
	@RequestMapping(value = "/createOrder", 
			method = RequestMethod.POST, 
			produces = MediaType.APPLICATION_JSON_VALUE, 
			headers = "Accept=application/json,application/xml")
	public @ResponseBody HttpEntity<Void> setCurrentDataList(@RequestBody Order order) {
		orderStateMachineAdapter.persist(orderStateMachineAdapter.create(), order);
		orderRepository.saveAndFlush(order);
		return ResponseEntity.accepted().build();
	}
}
