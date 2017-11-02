package com.cgi.fastordermanager.order.plan.jms.mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class ReceiverMock {
	@Autowired
	private JmsMessagingTemplate jmsMessagingTemplate;
	
	
    @JmsListener(destination = "esb")
    public void receiveMessage(com.cgi.fastordermanager.order.plan.jms.RfsMessage rfs) {
        System.out.println("Received <" + rfs + ">");
        jmsMessagingTemplate.convertAndSend("esb.rply", rfs);
    }

}