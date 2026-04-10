package com.pedro.finance_control;

import com.pedro.finance_control.dto.TransactionResponse;
import com.pedro.finance_control.dto.TransactionRequest;
import com.pedro.finance_control.entity.Transaction;
import com.pedro.finance_control.entity.User;
import com.pedro.finance_control.enums.Category;
import com.pedro.finance_control.enums.TransactionType;
import com.pedro.finance_control.repository.TransactionRepository;
import com.pedro.finance_control.repository.UserRepository;
import com.pedro.finance_control.security.JwtService;
import com.pedro.finance_control.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Test
    void shouldCreateTransaction(){

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken("test@email.com", null));
        SecurityContextHolder.setContext(context);

        TransactionRequest request = new TransactionRequest("Salário",
                "Pagamento",
                new BigDecimal(5000),
                TransactionType.RECEITA,
                Category.SALARIO,
                LocalDate.now());

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
    void shoulFindTransaction(){

        //mock usuário logado
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken("test@email.com", null));
        SecurityContextHolder.setContext(context);

        //usuário
        User user = new User();
        user.setId(1L);
        user.setEmail("test@email.com");

        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));

        //transação
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setTitle("Salário");
        transaction.setAmount(new BigDecimal("5000.00"));
        transaction.setUser(user);
        when(transactionRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(transaction));

        //execução
        TransactionResponse response = transactionService.findById(1L);

        //validações
        assertNotNull(response);
        assertEquals("Salário", response.title());

        verify(transactionRepository).findByIdAndUser(1L, user);
    }

    @Test
    void shouldThrowExceptionWhenTransactionNotFound(){

        //mock usuário logado
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken("test@email.com", null));
        SecurityContextHolder.setContext(context);

        User user = new User();
        user.setEmail("test@email.com");
        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));

        //não encontrou
        when(transactionRepository.findByIdAndUser(1L, user)).thenReturn(Optional.empty());

        //valida exceção
        RuntimeException exception = assertThrows(RuntimeException.class, () -> transactionService.findById(1L));

        assertEquals("Transaction not found", exception.getMessage());
    }

    @Test
    void shouldUpdateTransaction(){
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken("test@email.com", null));
        SecurityContextHolder.setContext(context);

        //usuário
        User user = new User();
        user.setId(1L);
        user.setEmail("test@email.com");
        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));

        //transação existente
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setTitle("Salário");
        transaction.setAmount(new BigDecimal("5000.00"));
        transaction.setUser(user);
        when(transactionRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(transaction));

        //request de atualização
        TransactionRequest request = new TransactionRequest(
                "Salário Atualizado",
                "Novo pagamento",
                new BigDecimal("6000.00"),
                TransactionType.RECEITA,
                Category.SALARIO,
                LocalDate.now()
        );

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //execução
        TransactionResponse response = transactionService.update(1L, request);

        //validações
        assertNotNull(response);
        assertEquals("Salário Atualizado", response.title());
        assertEquals(new BigDecimal("6000.00"), response.amount());

        //garante que salvou
        verify(transactionRepository).save(transaction);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingTransaction(){
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
    void shouldDeleteTranssaction(){

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
    void shouldThrowExceptionWhenDeletingNonExistingTransaction(){
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
