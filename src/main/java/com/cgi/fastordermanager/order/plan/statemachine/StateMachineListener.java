package com.cgi.fastordermanager.order.plan.statemachine;

import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@AllArgsConstructor
@Slf4j
public class StateMachineListener extends StateMachineListenerAdapter<String,String> {


    @Override
    public void stateChanged(State<String,String> from, State<String,String> to) {
        log.debug(String.format("Transitioned from %s to %s", from == null ? "none" : from.getId(), to.getId()));
    }
    
    @Override
    public void stateEntered(State<String, String> state) {
        log.debug(String.format("Entered to %s", state.getId()));
        
    }
    
    @Override
    public void stateExited(State<String, String> state) {
        log.debug(String.format("Exited from %s", state.getId()));
    }
    
	@Override
	public void stateMachineStarted(StateMachine<String, String> stateMachine) {
		 log.debug(String.format("Started " + stateMachine.getId()));
	}

	@Override
	public void stateMachineStopped(StateMachine<String, String> stateMachine) {
		log.debug(String.format("Stopped " + stateMachine.getId()));
	}
	
}