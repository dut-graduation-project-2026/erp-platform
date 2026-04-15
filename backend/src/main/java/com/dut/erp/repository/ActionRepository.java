package com.dut.erp.repository;

import com.dut.erp.entity.Action;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActionRepository extends JpaRepository<Action, UUID> {}
