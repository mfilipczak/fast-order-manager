package com.cgi.fastordermanager.order;

import java.util.EnumSet;

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

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@EnableStateMachineFactory(contextEvents=false)
public class OrderStateMachineConfiguration extends EnumStateMachineConfigurerAdapter<OrderState, OrderEvent> {

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
                .initial(OrderState.REGISTERED, context -> setUnpaid(context.getExtendedState()))
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


/**
+----------------------------------------------------------------------------------------------------------------------------+
|                                                     pre-payment flow                                                       |
+----------------------------------------------------------------------------------------------------------------------------+
|                                (1)                            (2) [if paid]                 (3)                            |
|     +------------------+ ReceivePayment  +-- ---------------+  Deliver +------------------+  Refund  +------------------+  |
| *-->|       Open       |---------------->| ReadyForDelivery |--------->|    Completed     |--------->|     Canceled     |  |
|     |                  |                 |                  |          |                  |          |                  |  |
|     |                  |                 |  ReceivePayment  |          |                  |          |  ReceivePayment  |  |
|     |                  |                 |  +------------+  |          |                  |          |  +------------+  |  |
|     |                  |                 |  |    (11)    |  |          |                  |          |  |    (12)    |  |  |
|     |                  |                 |  |            v  |          |                  |          |  |            v  |  |
|     +------------------+                 +------------------+          +------------------+          +------------------+  |
|        | ^                                             | |         [if paid] (4) Refund                ^   ^       | ^     |
|        | |                                             | +---------------------------------------------+   |       | |     |
|        | |                                             |                                                   |       | |     |
|        | |                                             |           [if !paid]  (8) Cancel                  |       | |     |
|        | |           (5) Reopen                        +---------------------------------------------------+       | |     |
|        | +---------------------------------------------------------------------------------------------------------+ |     |
|        |                                              (6) Cancel                                                     |     |
|        +-------------------------------------------------------------------------------------------------------------+     |
|                                                                                                                            |
+----------------------------------------------------------------------------------------------------------------------------+


+-------------------------------------------------------------------------------------------------------------------------------------------------------------+
|                                                                     post-payment flow                                                                       |
+-------------------------------------------------------------------------------------------------------------------------------------------------------------+
|                                (7)                            (9) [if !paid]                 (10)                            (3)                            |
|     +------------------+ UnlockDelivery  +-- ---------------+  Deliver +------------------+ ReceivePayment +---------------+  Refund  +------------------+  |
| *-->|       Open       |---------------->| ReadyForDelivery |--------->| AwaitingPayment  |--------------->|   Completed   |--------->|     Canceled     |  |
|     |                  |                 |                  |          |                  |                |               |          |                  |  |
|     |                  |                 |  ReceivePayment  |          |                  |                |               |          |  ReceivePayment  |  |
|     |                  |                 |  +------------+  |          |                  |                |               |          |  +------------+  |  |
|     |                  |                 |  |    (11)    |  |          |                  |                |               |          |  |    (12)    |  |  |
|     |                  |                 |  |            v  |          |                  |                |               |          |  |            v  |  |
|     +------------------+                 +------------------+          +------------------+                +---------------+          +------------------+  |
|        | ^                                             |  |          [if paid] (4) Refund                                               ^    ^      | ^     |
|        | |                                             |  +-----------------------------------------------------------------------------+    |      | |     |
|        | |                                             |                                                                                     |      | |     |
|        | |                                             |             [if !paid] (8) Cancel                                                   |      | |     |
|        | |           (5) Reopen                        +-------------------------------------------------------------------------------------+      | |     |
|        | +------------------------------------------------------------------------------------------------------------------------------------------+ |     |
|        |                                              (6) Cancel                                                                                      |     |
|        +----------------------------------------------------------------------------------------------------------------------------------------------+     |
|                                                                                                                                                             |
+-------------------------------------------------------------------------------------------------------------------------------------------------------------+
*/
    @Override
    public void configure(StateMachineTransitionConfigurer<OrderState, OrderEvent> transitions)
            throws Exception {
        transitions
            .withExternal()
            // (1)
                .source(OrderState.REGISTERED)
                .target(OrderState.DECOMPOSING)
                .event(OrderEvent.DECOMPOSE)
                .action(receivePayment())
            .and()
            // (2)
            .withExternal()
                .source(OrderState.DECOMPOSING)
                .target(OrderState.PROCESSING)
                .event(OrderEvent.PROCESS)
            .and()
            // (3)
            .withExternal()
                .source(OrderState.PROCESSING)
                .target(OrderState.COMPLETED)
                .event(OrderEvent.COMPLETE)
                .action(refundPayment())
        
            ;
    }

    public Action<OrderState, OrderEvent> receivePayment() {
        return context -> setPaid(context.getExtendedState());
    }

    public Action<OrderState, OrderEvent> refundPayment() {
        return context -> setUnpaid(context.getExtendedState());
    }

    private Guard<OrderState, OrderEvent> isPaid() {
        return context -> 
            (boolean) context.getExtendedState().get("paid", Boolean.class);
    }

    private Guard<OrderState, OrderEvent> not(Guard<OrderState, OrderEvent> guard) {
        return context -> !guard.evaluate(context);
    }

    void setUnpaid(ExtendedState extendedState) {
        log.info("Unsetting paid");
        extendedState.getVariables().put("paid", Boolean.FALSE);
    }

    void setPaid(ExtendedState extendedState) {
        log.info("Setting paid");
        extendedState.getVariables().put("paid", Boolean.TRUE);
    }

}
