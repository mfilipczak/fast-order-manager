package com.cgi.fastordermanager.order;

import java.util.EnumSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import com.cgi.fastordermanager.akka.ActorManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@EnableStateMachineFactory
@RequiredArgsConstructor
public class OrderStateMachineConfiguration extends EnumStateMachineConfigurerAdapter<OrderState, OrderEvent> {

	
	@Autowired
	private final ActorManager actorManager;
	
    @Override
    public void configure(StateMachineConfigurationConfigurer<OrderState, OrderEvent> config) throws Exception {

        config
        .withConfiguration()
        .listener(loggingListener());
    }	

    
    @Override
    public void configure(StateMachineStateConfigurer<OrderState, OrderEvent> states)
            throws Exception {
        states
            .withStates()
                .initial(OrderState.REGISTERED)
                .end(OrderState.COMPLETED)
                .states(EnumSet.allOf(OrderState.class));
    }

    public StateMachineListener<OrderState, OrderEvent> loggingListener() {
        return new StateMachineListenerAdapter<OrderState, OrderEvent>() {
            @Override
            public void stateChanged(State<OrderState, OrderEvent> from, State<OrderState, OrderEvent> to) {
                log.info("State changed to {}", to.getId());
            }
            @Override
            public void eventNotAccepted(Message<OrderEvent> event) {
                log.error("Event not accepted: {}", event.getPayload());
            }
        };
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<OrderState, OrderEvent> transitions)
            throws Exception {
        transitions
            .withExternal()
            // (1)
                .source(OrderState.REGISTERED)
                .target(OrderState.DECOMPOSING)
                .event(OrderEvent.DECOMPOSE)
                .action(decompose())
            .and()
            // (2)
            .withExternal()
                .source(OrderState.DECOMPOSING)
                .target(OrderState.PROCESSING)
                .event(OrderEvent.PROCESS)
                .action(process())
            .and()
            // (3)
            .withExternal()
                .source(OrderState.PROCESSING)
                .target(OrderState.COMPLETED)
                .event(OrderEvent.COMPLETE)
            ;
    }

    
    public Action<OrderState, OrderEvent> decompose() {
        return context -> actorManager.decompose((String)context.getExtendedState().getVariables().get("ID"));
    }
    
    public Action<OrderState, OrderEvent> process() {
        return context -> actorManager.process((String)context.getExtendedState().getVariables().get("ID"));
    }
}
