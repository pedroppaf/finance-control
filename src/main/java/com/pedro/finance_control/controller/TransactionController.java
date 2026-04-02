package com.pedro.finance_control.controller;


import com.pedro.finance_control.dto.TransactionReponse;
import com.pedro.finance_control.dto.TransactionRequest;
import com.pedro.finance_control.dto.transaction.SummaryResponse;
import com.pedro.finance_control.enums.TransactionType;
import com.pedro.finance_control.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public TransactionReponse create(@RequestBody @Valid TransactionRequest request){
        return  transactionService.create(request);
    }

    @GetMapping("/{id}")
    public TransactionReponse findById(@PathVariable Long id){
        return transactionService.findById(id);
    }

    @GetMapping("/summary")
    public SummaryResponse getSummary(){
        return transactionService.getSummary();
    }

    @GetMapping
    public Page<TransactionReponse> findAll(@RequestParam(required = false)TransactionType type,

                                            @RequestParam(required = false)
                                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                            LocalDate startDate,

                                            @RequestParam(required = false)
                                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                            LocalDate endDate,

                                            Pageable pageable
                                            ){
        return  transactionService.findAll(type, startDate, endDate, pageable);
    }

    @PutMapping("/{id}")
    public TransactionReponse update(@PathVariable Long id, @RequestBody @Valid TransactionRequest request){
        return transactionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        transactionService.delete(id);
    }
}
