package com.cgi.fastordermanager.order;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RfsRepository extends JpaRepository<Rfs, Long> {

    Rfs findByCfs(String rfsId, Order order); 

}
