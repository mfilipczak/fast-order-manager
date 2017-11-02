package com.cgi.fastordermanager.order.plan.statemachine;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachine;

import com.cgi.fastordermanager.order.Order;
import com.cgi.fastordermanager.order.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Configuration
@Slf4j
public class StateMachineManager {

	
	@Autowired
	private final OrderRepository orderRepository;
	
	private Map<Long, StateMachine<String, String>> stateMachines = new HashMap<>();

	public StateMachine<String, String> addStateMachine(Long orderId, StateMachine<String, String> st) {
		stateMachines.put(orderId, st);
		return st;
	}

	public void removeStateMachine(Long orderId) {
		stateMachines.remove(orderId);
	}

	public StateMachine<String, String> getStateMachine(Long orderId) {
		return stateMachines.get(orderId);
	}
	
	public void sendEvent(Long orderId, String event) {
		getStateMachine(orderId).sendEvent(event);
	}
}