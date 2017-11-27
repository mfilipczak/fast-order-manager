package com.cgi.fastordermanager.akka.actor;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Access;
import javax.persistence.AccessType;

import org.jgrapht.graph.SimpleDirectedGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.cgi.fastordermanager.ContextEntity;
import com.cgi.fastordermanager.DefaultStateMachineAdapter;
import com.cgi.fastordermanager.graph.RfsGraphEdge;
import com.cgi.fastordermanager.order.Order;
import com.cgi.fastordermanager.order.OrderEvent;
import com.cgi.fastordermanager.order.OrderRepository;
import com.cgi.fastordermanager.order.OrderState;
import com.cgi.fastordermanager.order.Rfs;
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
public class ProcessOrderActor extends AbstractActor {

	@Autowired
	private final OrderRepository orderRepository;

	@Autowired
	private OrderPlanStateMachineBilder orderPlanstateMachineBuilder;

	@Autowired
	private StateMachineManager stateMachineManager;

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(String.class, orderId -> {
			Order order = orderRepository.findByExternalId(orderId);
			log.info("Received order to process: {}", order);

			List<Rfs> rfsList = order.getCfs().stream().flatMap(cfs -> cfs.getRfs().stream())
					.collect(Collectors.toList());
			SimpleDirectedGraph<String, RfsGraphEdge> directedGraph = new SimpleDirectedGraph<>(RfsGraphEdge.class);
			directedGraph.addVertex("0");
			directedGraph.addVertex("-1");
			rfsList.stream().forEach(r -> {
				directedGraph.addVertex(r.getRfsId());
				directedGraph.addEdge("0", r.getRfsId());
				directedGraph.addEdge(r.getRfsId(), "-1");
			});
			rfsList.stream().filter(r -> !r.getBlockedRfs().isEmpty()).forEach(r -> {
				Arrays.asList(r.getBlockedRfs().split("#")).stream()
						.filter(blocked -> directedGraph.containsVertex(blocked)).forEach(blocked -> {
							directedGraph.removeEdge("0", blocked);
							directedGraph.removeEdge(r.getRfsId(), "-1");
							directedGraph.addEdge(r.getRfsId(), blocked);

						});
			});
			stateMachineManager
					.addStateMachine(order.getId(), orderPlanstateMachineBuilder.buildMachine(order, directedGraph))
					.start();
		}).matchAny(o -> log.info("received unknown message")).build();
	}

}
