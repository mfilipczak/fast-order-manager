package com.cgi.fastordermanager.order;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByCurrentState(OrderState currentState, Pageable pageable);
    
    Order findByExternalId(@Param("externalId") String externalId);
    
    List<Order> findByExternalIdIgnoreCaseContaining(@Param("externalId") String externalId);
    
    Order findByCfsRfsId(String rfsId);
    
    @Query("select new com.cgi.fastordermanager.order.OrderAnswerStatistics(o.currentState, count(o)) from Order o group by o.currentState")
    @RestResource(path="byList", rel="byList")
    List<OrderAnswerStatistics> findOrderStateCount();

}
