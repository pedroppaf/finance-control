package com.pedro.finance_control.repository;

import com.pedro.finance_control.entity.Transaction;
import com.pedro.finance_control.entity.User;
import com.pedro.finance_control.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository <Transaction, Long> {

    List<Transaction> findByUser (User user);

    Optional<Transaction> findByIdAndUser (Long id, User user);

    @Query("""
SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user = :user AND t.type = :type
""")
    Double sumByType(@Param("user") User user, @Param("type")TransactionType type);
}
