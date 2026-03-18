package com.pedro.finance_control.repository;

import com.pedro.finance_control.entity.Transaction;
import com.pedro.finance_control.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository <Transaction, Long> {

    List<Transaction> findByUser (User user);

    Optional<Transaction> findByIdAndUser (Long id, User user);
}
