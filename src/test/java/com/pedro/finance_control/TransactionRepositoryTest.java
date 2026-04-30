package com.pedro.finance_control;

import com.pedro.finance_control.entity.Transaction;
import com.pedro.finance_control.entity.User;
import com.pedro.finance_control.enums.Category;
import com.pedro.finance_control.enums.TransactionType;
import com.pedro.finance_control.repository.TransactionRepository;
import com.pedro.finance_control.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindTransactionsByUser() {
        User user = userRepository.save(new User(null, "Pedro", "pedro@email.com", "password", LocalDateTime.now()));
        User otherUser = userRepository.save(new User(null, "Maria", "maria@email.com", "password", LocalDateTime.now()));

        transactionRepository.save(Transaction.builder()
                .title("Salário")
                .description("Receita")
                .amount(new BigDecimal("5000.00"))
                .type(TransactionType.RECEITA)
                .category(Category.SALARIO)
                .date(LocalDate.of(2026, 1, 10))
                .createdAt(LocalDateTime.now())
                .user(user)
                .build());

        transactionRepository.save(Transaction.builder()
                .title("Outro")
                .description("Receita outro usuário")
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.RECEITA)
                .category(Category.OUTROS)
                .date(LocalDate.of(2026, 1, 11))
                .createdAt(LocalDateTime.now())
                .user(otherUser)
                .build());

        var transactions = transactionRepository.findByUser(user);

        assertEquals(1, transactions.size());
        assertEquals("Salário", transactions.get(0).getTitle());
    }

    @Test
    void shouldFindTransactionByIdAndUser() {
        User user = userRepository.save(new User(null, "Pedro", "pedro@email.com", "password", LocalDateTime.now()));

        Transaction transaction = transactionRepository.save(Transaction.builder()
                .title("Mercado")
                .description("Compras")
                .amount(new BigDecimal("200.00"))
                .type(TransactionType.DESPESA)
                .category(Category.ALIMENTACAO)
                .date(LocalDate.of(2026, 2, 1))
                .createdAt(LocalDateTime.now())
                .user(user)
                .build());

        assertTrue(transactionRepository.findByIdAndUser(transaction.getId(), user).isPresent());
        assertFalse(transactionRepository.findByIdAndUser(transaction.getId(), new User(999L, "Maria", "maria@email.com", "password", LocalDateTime.now())).isPresent());
    }

    @Test
    void shouldSumTransactionsByType() {
        User user = userRepository.save(new User(null, "Pedro", "pedro@email.com", "password", LocalDateTime.now()));

        transactionRepository.save(Transaction.builder()
                .title("Salário")
                .description("Receita")
                .amount(new BigDecimal("5000.00"))
                .type(TransactionType.RECEITA)
                .category(Category.SALARIO)
                .date(LocalDate.of(2026, 1, 10))
                .createdAt(LocalDateTime.now())
                .user(user)
                .build());

        transactionRepository.save(Transaction.builder()
                .title("Freela")
                .description("Receita extra")
                .amount(new BigDecimal("1500.00"))
                .type(TransactionType.RECEITA)
                .category(Category.OUTROS)
                .date(LocalDate.of(2026, 1, 15))
                .createdAt(LocalDateTime.now())
                .user(user)
                .build());

        transactionRepository.save(Transaction.builder()
                .title("Aluguel")
                .description("Despesa")
                .amount(new BigDecimal("1200.00"))
                .type(TransactionType.DESPESA)
                .category(Category.MORADIA)
                .date(LocalDate.of(2026, 1, 20))
                .createdAt(LocalDateTime.now())
                .user(user)
                .build());

        assertEquals(6500.0, transactionRepository.sumByType(user, TransactionType.RECEITA));
        assertEquals(1200.0, transactionRepository.sumByType(user, TransactionType.DESPESA));
    }

    @Test
    void shouldFindTransactionsWithFilters() {
        User user = userRepository.save(new User(null, "Pedro", "pedro@email.com", "password", LocalDateTime.now()));

        transactionRepository.save(Transaction.builder()
                .title("Janeiro")
                .description("Receita janeiro")
                .amount(new BigDecimal("5000.00"))
                .type(TransactionType.RECEITA)
                .category(Category.SALARIO)
                .date(LocalDate.of(2026, 1, 10))
                .createdAt(LocalDateTime.now())
                .user(user)
                .build());

        transactionRepository.save(Transaction.builder()
                .title("Fevereiro")
                .description("Receita fevereiro")
                .amount(new BigDecimal("5200.00"))
                .type(TransactionType.RECEITA)
                .category(Category.SALARIO)
                .date(LocalDate.of(2026, 2, 10))
                .createdAt(LocalDateTime.now())
                .user(user)
                .build());

        Page<Transaction> page = transactionRepository.findWithFilters(
                user,
                TransactionType.RECEITA,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31),
                PageRequest.of(0, 10)
        );

        assertEquals(1, page.getTotalElements());
        assertEquals("Janeiro", page.getContent().get(0).getTitle());
    }
}


