package com.pedro.finance_control.service;

import com.pedro.finance_control.dto.TransactionResponse;
import com.pedro.finance_control.dto.TransactionRequest;
import com.pedro.finance_control.dto.transaction.SummaryResponse;
import com.pedro.finance_control.entity.Transaction;
import com.pedro.finance_control.entity.User;
import com.pedro.finance_control.enums.TransactionType;
import com.pedro.finance_control.exception.ResourceNotFoundException;
import com.pedro.finance_control.exception.UnauthorizedAccessException;
import com.pedro.finance_control.repository.TransactionRepository;
import com.pedro.finance_control.repository.UserRepository;
import com.pedro.finance_control.response.PageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionResponse create(TransactionRequest request) {
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

    public List<TransactionResponse> findAll() {
        User user = getAuthenticatedUser();
        return transactionRepository.findByUser(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public TransactionResponse findById(Long id){
        User user = getAuthenticatedUser();
        Transaction transaction = transactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        return toResponse(transaction);
    }

    public TransactionResponse update (Long id, TransactionRequest request){
        User user = getAuthenticatedUser();

        Transaction transaction = transactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

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
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        transactionRepository.delete(transaction);
    }

    private User getAuthenticatedUser(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedAccessException("Authenticated user not found"));
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(transaction.getId(),
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

    public PageDto<TransactionResponse> findAll(TransactionType type,
                                            LocalDate startDate,
                                            LocalDate endDate,
                                            Pageable pageable
    ){
        User user = getAuthenticatedUser();

        Page<Transaction> page = transactionRepository.findWithFilters(user, type, startDate, endDate, pageable);
        List<TransactionResponse> content = page.getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        return new PageDto<>(
            content,
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages()
        );
    }
}
