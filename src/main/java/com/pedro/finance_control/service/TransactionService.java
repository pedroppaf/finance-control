package com.pedro.finance_control.service;

import com.pedro.finance_control.dto.TransactionReponse;
import com.pedro.finance_control.dto.TransactionRequest;
import com.pedro.finance_control.dto.transaction.SummaryResponse;
import com.pedro.finance_control.entity.Transaction;
import com.pedro.finance_control.entity.User;
import com.pedro.finance_control.enums.TransactionType;
import com.pedro.finance_control.repository.TransactionRepository;
import com.pedro.finance_control.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionReponse create(TransactionRequest request) {
        User user = getAuthenticatedUser();

        Transaction transaction = Transaction.builder()
                .title(request.title())
                .description(request.description())
                .amount(request.amount())
                .type(request.type())
                .category(request.category())
                .date(request.date())
                .createdAt(LocalDateTime.now())
                .updatedAt(null)
                .user(user)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        return toResponse(savedTransaction);
    }

    public List<TransactionReponse> findAll() {
        User user = getAuthenticatedUser();
        return transactionRepository.findByUser(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public TransactionReponse findById(Long id){
        User user = getAuthenticatedUser();
        Transaction transaction = transactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        return toResponse(transaction);
    }

    public TransactionReponse update (Long id, TransactionRequest request){
        User user = getAuthenticatedUser();

        Transaction transaction = transactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        transaction.setTitle(request.title());
        transaction.setDescription(request.description());
        transaction.setAmount(request.amount());
        transaction.setType(request.type());
        transaction.setCategory(request.category());
        transaction.setDate(request.date());
        transaction.setUpdatedAt(LocalDateTime.now());

        Transaction updatedTransaction = transactionRepository.save(transaction);

        return toResponse(updatedTransaction);
    }

    public void delete(Long id){
        User user = getAuthenticatedUser();

        Transaction transaction = transactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        transactionRepository.delete(transaction);
    }

    private User getAuthenticatedUser(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }

    private TransactionReponse toResponse(Transaction transaction) {
        return new TransactionReponse(transaction.getId(),
                transaction.getTitle(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getCategory(),
                transaction.getDate(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }

    public SummaryResponse getSummary(){
        User user = getAuthenticatedUser();

        Double receita = transactionRepository.sumByType(user, TransactionType.RECEITA);

        Double despesa = transactionRepository.sumByType(user, TransactionType.DESPESA);

        Double balance = receita - despesa;

        return new SummaryResponse(receita, despesa, balance);
    }
}
