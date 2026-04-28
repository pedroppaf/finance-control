package com.pedro.finance_control;

import com.pedro.finance_control.dto.TransactionRequest;
import com.pedro.finance_control.dto.TransactionResponse;
import com.pedro.finance_control.entity.Transaction;
import com.pedro.finance_control.entity.User;
import com.pedro.finance_control.enums.Category;
import com.pedro.finance_control.enums.TransactionType;
import com.pedro.finance_control.repository.TransactionRepository;
import com.pedro.finance_control.repository.UserRepository;
import com.pedro.finance_control.service.TransactionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreateTransaction() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken("test@email.com", null));
        SecurityContextHolder.setContext(context);

        TransactionRequest request = new TransactionRequest(
                "Salário",
                "Pagamento",
                new BigDecimal("5000.00"),
                TransactionType.RECEITA,
                Category.SALARIO,
                LocalDate.now()
        );

        User user = new User();
        user.setId(1L);
        user.setEmail("test@email.com");

        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse response = transactionService.create(request);

        assertNotNull(response);
        assertEquals("Salário", response.title());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void shouldListAllTransactionsForAuthenticatedUser() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken("test@email.com", null));
        SecurityContextHolder.setContext(context);

        User user = new User();
        user.setId(1L);
        user.setEmail("test@email.com");

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setTitle("Salário");
        transaction.setAmount(new BigDecimal("5000.00"));
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUser(user);

        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));
        when(transactionRepository.findByUser(user)).thenReturn(List.of(transaction));

        var responses = transactionService.findAll();

        assertEquals(1, responses.size());
        assertEquals("Salário", responses.getFirst().title());
    }

    @Test
    void shoulFindTransaction() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken("test@email.com", null));
        SecurityContextHolder.setContext(context);

        User user = new User();
        user.setId(1L);
        user.setEmail("test@email.com");

        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setTitle("Salário");
        transaction.setAmount(new BigDecimal("5000.00"));
        transaction.setUser(user);
        when(transactionRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(transaction));

        TransactionResponse response = transactionService.findById(1L);

        assertNotNull(response);
        assertEquals("Salário", response.title());
        verify(transactionRepository).findByIdAndUser(1L, user);
    }

    @Test
    void shouldThrowExceptionWhenTransactionNotFound() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken("test@email.com", null));
        SecurityContextHolder.setContext(context);

        User user = new User();
        user.setEmail("test@email.com");
        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));

        when(transactionRepository.findByIdAndUser(1L, user)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> transactionService.findById(1L));

        assertEquals("Transaction not found", exception.getMessage());
    }

    @Test
    void shouldReturnSummaryForAuthenticatedUser() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken("test@email.com", null));
        SecurityContextHolder.setContext(context);

        User user = new User();
        user.setEmail("test@email.com");

        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));
        when(transactionRepository.sumByType(user, TransactionType.RECEITA)).thenReturn(6500.0);
        when(transactionRepository.sumByType(user, TransactionType.DESPESA)).thenReturn(1200.0);

        var summary = transactionService.getSummary();

        assertEquals(6500.0, summary.receita());
        assertEquals(1200.0, summary.despesa());
        assertEquals(5300.0, summary.balance());
    }

    @Test
    void shouldUpdateTransaction() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken("test@email.com", null));
        SecurityContextHolder.setContext(context);

        User user = new User();
        user.setId(1L);
        user.setEmail("test@email.com");
        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setTitle("Salário");
        transaction.setAmount(new BigDecimal("5000.00"));
        transaction.setUser(user);
        when(transactionRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(transaction));

        TransactionRequest request = new TransactionRequest(
                "Salário Atualizado",
                "Novo pagamento",
                new BigDecimal("6000.00"),
                TransactionType.RECEITA,
                Category.SALARIO,
                LocalDate.now()
        );

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse response = transactionService.update(1L, request);

        assertNotNull(response);
        assertEquals("Salário Atualizado", response.title());
        assertEquals(new BigDecimal("6000.00"), response.amount());
        verify(transactionRepository).save(transaction);
    }

    @Test
    void shouldReturnFilteredTransactionsPage() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken("test@email.com", null));
        SecurityContextHolder.setContext(context);

        User user = new User();
        user.setEmail("test@email.com");

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setTitle("Janeiro");
        transaction.setAmount(new BigDecimal("5000.00"));
        transaction.setType(TransactionType.RECEITA);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUser(user);

        PageRequest pageable = PageRequest.of(0, 10);
        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));
        when(transactionRepository.findWithFilters(user, TransactionType.RECEITA, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), pageable))
                .thenReturn(new PageImpl<>(List.of(transaction), pageable, 1));

        Page<TransactionResponse> page = transactionService.findAll(
                TransactionType.RECEITA,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31),
                pageable
        );

        assertEquals(1, page.getTotalElements());
        assertEquals("Janeiro", page.getContent().getFirst().title());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingTransaction() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken("test@email.com", null));
        SecurityContextHolder.setContext(context);

        User user = new User();
        user.setEmail("test@email.com");
        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));

        TransactionRequest request = new TransactionRequest(
                "Teste",
                "Teste",
                new BigDecimal("100.00"),
                TransactionType.DESPESA,
                Category.ALIMENTACAO,
                LocalDate.now()
        );

        RuntimeException exception = assertThrows(RuntimeException.class, () -> transactionService.update(1L, request));

        assertEquals("Transaction not found", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAuthenticatedUserIsMissing() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken("missing@email.com", null));
        SecurityContextHolder.setContext(context);

        when(userRepository.findByEmail("missing@email.com")).thenReturn(Optional.empty());

        TransactionRequest request = new TransactionRequest(
                "Teste",
                "Teste",
                new BigDecimal("100.00"),
                TransactionType.DESPESA,
                Category.ALIMENTACAO,
                LocalDate.now()
        );

        RuntimeException exception = assertThrows(RuntimeException.class, () -> transactionService.create(request));

        assertEquals("Authenticated user not found", exception.getMessage());
    }

    @Test
    void shouldDeleteTranssaction() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken("test@email.com", null));
        SecurityContextHolder.setContext(context);

        User user = new User();
        user.setId(1L);
        user.setEmail("test@email.com");

        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setUser(user);

        when(transactionRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(transaction));

        transactionService.delete(1L);

        verify(transactionRepository).delete(transaction);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingTransaction() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken("test@email.com", null));
        SecurityContextHolder.setContext(context);

        User user = new User();
        user.setEmail("test@email.com");

        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));

        when(transactionRepository.findByIdAndUser(1L, user)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> transactionService.delete(1L));

        assertEquals("Transaction not found", exception.getMessage());

        verify(transactionRepository, never()).delete(any());
    }
}
