package com.cgi.fastordermanager.order;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface CfsRepository extends JpaRepository<Cfs, Long> {

    Page<Cfs> findByOrder(Order order, Pageable pageable);
    Page<Cfs> findByOrderId(@Param("orderId") Long orderId, Pageable pageable);
    Cfs findByRfsCurrentState(Long cfsId, ProductState state); 

}
