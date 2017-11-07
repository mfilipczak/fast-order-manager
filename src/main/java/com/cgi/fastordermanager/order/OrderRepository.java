package com.cgi.fastordermanager.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByCurrentState(OrderState currentState, Pageable pageable);
    Order findByExternalId(@Param("externalId") String externalId);
    Order findByCfsRfsId(String rfsId);

}
