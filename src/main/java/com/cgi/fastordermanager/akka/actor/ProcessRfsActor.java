package com.cgi.fastordermanager.akka.actor;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.jms.Queue;
import javax.persistence.Access;
import javax.persistence.AccessType;

import org.jgrapht.graph.SimpleDirectedGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.core.JmsTemplate;

import com.cgi.fastordermanager.ContextEntity;
import com.cgi.fastordermanager.DefaultStateMachineAdapter;
import com.cgi.fastordermanager.graph.RfsGraphEdge;
import com.cgi.fastordermanager.order.Order;
import com.cgi.fastordermanager.order.OrderEvent;
import com.cgi.fastordermanager.order.OrderRepository;
import com.cgi.fastordermanager.order.OrderState;
import com.cgi.fastordermanager.order.Rfs;
import com.cgi.fastordermanager.order.RfsRepository;
import com.cgi.fastordermanager.order.plan.jms.RfsMessage;
import com.cgi.fastordermanager.order.plan.statemachine.OrderPlanStateMachineBilder;
import com.cgi.fastordermanager.order.plan.statemachine.StateMachineManager;

import akka.actor.AbstractActor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Access(AccessType.FIELD)
@Scope("prototype")
@AllArgsConstructor
@Configuration
public class ProcessRfsActor extends AbstractActor {

	@Autowired
	private final OrderRepository orderRepository;

	@Autowired
	private StateMachineManager stateMachineManager;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private JmsMessagingTemplate jmsMessagingTemplate;

	@Autowired
	private Queue queue;

	@AllArgsConstructor
	public static class ProcessRfsMessage {
		private Long orderId;
		private String rfsId;
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(ProcessRfsMessage.class, rfsMessage -> {
			try {
				log.info("Received rfs to process: orderid= {}; rfsId={}", rfsMessage.orderId, rfsMessage.rfsId);
				Order order = orderRepository.findOne(rfsMessage.orderId);
				Optional<Rfs> rfs = order.getCfs().stream().flatMap(cfs -> cfs.getRfs().stream())
						.filter(r -> r.getRfsId().equals(rfsMessage.rfsId)).findFirst();
				if (rfs.isPresent()) {
					jmsMessagingTemplate.convertAndSend(queue, new RfsMessage(rfs.get().getId(), order.getId(),
							rfs.get().getName(), rfs.get().getRfsId()));
				}
			} catch (Exception e) {
				log.info("Rfs Process exception: {}",e);
			}

		}).matchAny(o -> log.info("received unknown message")).build();
	}

}
