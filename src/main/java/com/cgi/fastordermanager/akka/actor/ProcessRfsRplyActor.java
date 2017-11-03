package com.cgi.fastordermanager.akka.actor;

import java.util.Date;

import javax.jms.Queue;
import javax.persistence.Access;
import javax.persistence.AccessType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.cgi.fastordermanager.akka.ActorManager;
import com.cgi.fastordermanager.order.CfsRepository;
import com.cgi.fastordermanager.order.Order;
import com.cgi.fastordermanager.order.OrderEvent;
import com.cgi.fastordermanager.order.OrderRepository;
import com.cgi.fastordermanager.order.ProductState;
import com.cgi.fastordermanager.order.Rfs;
import com.cgi.fastordermanager.order.RfsRepository;
import com.cgi.fastordermanager.order.plan.statemachine.StateMachineManager;

import akka.actor.AbstractActor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Access(AccessType.FIELD)
@Scope("prototype")
@AllArgsConstructor
@Configuration
public class ProcessRfsRplyActor extends AbstractActor {

	@Autowired
	private final OrderRepository orderRepository;

	@Autowired
	private final RfsRepository rfsRepository;
	
	@Autowired
	private final CfsRepository cfsRepository;
	
	@Autowired
	private StateMachineManager stateMachineManager;

	@Autowired
	private final ActorManager actorManager;
	
	
	@AllArgsConstructor
	public static class ProcessRfsRplyMessage {
		private Long orderId;
		private Long rfsId;
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(ProcessRfsRplyMessage.class, rfsMessage -> {
			Order order = orderRepository.findOne(rfsMessage.orderId);
			Rfs rfs = rfsRepository.findOne(rfsMessage.rfsId);
			rfs.setCurrentState(ProductState.INSTALLED);
			rfsRepository.save(rfs);
			rfs.getCfs().getRfs().stream().anyMatch( r -> r.getCurrentState().equals(ProductState.PENDING));
			stateMachineManager.sendEvent(rfsMessage.orderId, rfs.getRfsId().concat("_EVENT"));
			if(checkPending(rfs, order)) {
				actorManager.changeOrderStatus(order, OrderEvent.COMPLETE);
			};
		}).matchAny(o -> log.info("received unknown message")).build();
	}
	
	public synchronized boolean checkPending(Rfs rfs, Order order) {
		boolean completed = false;
		boolean cfsCompleted = !rfs.getCfs().getRfs().stream().anyMatch( r -> r.getCurrentState().equals(ProductState.PENDING)); 
		if(cfsCompleted) {
			rfs.getCfs().setCurrentState(ProductState.INSTALLED);
			cfsRepository.save(rfs.getCfs());
		}
		
		if(cfsCompleted && !rfs.getCfs().getOrder().getCfs().stream().anyMatch(c -> c.getCurrentState().equals(ProductState.PENDING))) {
			rfs.getCfs().getOrder().setCompleteTime(new Date());
			orderRepository.save(rfs.getCfs().getOrder());
			completed = true;
		}
		return completed;
	}

}
