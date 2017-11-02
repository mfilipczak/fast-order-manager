package com.cgi.fastordermanager.order.plan.jms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.cgi.fastordermanager.order.plan.statemachine.StateMachineManager;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class Receiver {
	@Autowired
	private StateMachineManager stateMachineManager;

	
    @JmsListener(destination = "esb.rply")
    public void receiveMessage(com.cgi.fastordermanager.order.plan.jms.RfsMessage rfs) {
        System.out.println("Received rply <" + rfs + ">");
        stateMachineManager.sendEvent(rfs.orderId, rfs.rfsId.concat("_EVENT"));
    }

}