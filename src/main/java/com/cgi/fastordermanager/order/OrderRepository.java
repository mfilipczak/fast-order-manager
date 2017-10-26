package com.cgi.fastordermanager.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByCurrentState(OrderState currentState, Pageable pageable);

}
