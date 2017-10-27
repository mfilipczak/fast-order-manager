package com.cgi.fastordermanager;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.EntityLinks;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.StateMachinePersister;

import com.cgi.fastordermanager.order.OrderEvent;
import com.cgi.fastordermanager.order.OrderState;

import akka.actor.ActorSystem;
import static com.cgi.fastordermanager.akka.SpringExtension.SpringExtProvider;


@SpringBootApplication
@EntityScan
public class OrderApplication {

	private static final String FAST_ORDER_MANAGER = "fast-order-manager";

	@Autowired
	private ApplicationContext applicationContext;

	public static void main(String[] args) {
		SpringApplication.run(OrderApplication.class, args);
	}

	@Bean
	public ActorSystem actorSystem() {
		ActorSystem system = ActorSystem.create(FAST_ORDER_MANAGER);
		SpringExtProvider.get(system).initialize(applicationContext);
		return system;
	}

	@Bean
	public DefaultStateMachineAdapter<OrderState, OrderEvent, ContextEntity<OrderState, OrderEvent, Serializable>> orderStateMachineAdapter(
			StateMachineFactory<OrderState, OrderEvent> stateMachineFactory,
			StateMachinePersister<OrderState, OrderEvent, ContextEntity<OrderState, OrderEvent, Serializable>> persister) {
		return new DefaultStateMachineAdapter<>(stateMachineFactory, persister);
	}

	@Bean
	public ContextObjectResourceProcessor<OrderState, OrderEvent, ContextEntity<OrderState, OrderEvent, Serializable>> orderResourceProcessor(
			EntityLinks entityLinks,
			DefaultStateMachineAdapter<OrderState, OrderEvent, ContextEntity<OrderState, OrderEvent, ? extends Serializable>> orderStateMachineAdapter) {
		return new ContextObjectResourceProcessor<>(entityLinks, orderStateMachineAdapter);
	}

	@Bean
	public StateMachinePersister<OrderState, OrderEvent, ContextEntity<OrderState, OrderEvent, Serializable>> persister(
			StateMachinePersist<OrderState, OrderEvent, ContextEntity<OrderState, OrderEvent, Serializable>> persist) {
		return new DefaultStateMachinePersister<>(persist);
	}

	@Bean
	public StateMachinePersist<OrderState, OrderEvent, ContextEntity<OrderState, OrderEvent, Serializable>> persist() {
		return new StateMachinePersist<OrderState, OrderEvent, ContextEntity<OrderState, OrderEvent, Serializable>>() {

			@Override
			public StateMachineContext<OrderState, OrderEvent> read(
					ContextEntity<OrderState, OrderEvent, Serializable> order) throws Exception {
				return order.getStateMachineContext();
			}

			@Override
			public void write(StateMachineContext<OrderState, OrderEvent> context,
					ContextEntity<OrderState, OrderEvent, Serializable> order) throws Exception {
				order.setStateMachineContext(context);
			}
		};
	}
}
