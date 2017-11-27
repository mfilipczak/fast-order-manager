package com.cgi.fastordermanager.order;

import java.io.Serializable;

import org.springframework.hateoas.core.Relation;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Component
@NoArgsConstructor
@AllArgsConstructor
@Data
@Relation(value = "order", collectionRelation = "orders")
public class OrderAnswerStatistics implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3624485076238260284L;
	private OrderState currentState;
	private Long cnt;

}