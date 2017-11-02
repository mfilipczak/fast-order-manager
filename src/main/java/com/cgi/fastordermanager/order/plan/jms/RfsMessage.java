package com.cgi.fastordermanager.order.plan.jms;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class RfsMessage implements Serializable { // NOSONAR

    private static final long serialVersionUID = 8848887579564649636L;

 
    @Getter
    @Setter
    Long id;
    
    @Getter
    @Setter
    Long orderId;
    
    @Getter
    @Setter
    String name;
    
    @Getter
    @Setter
    String rfsId;

}
