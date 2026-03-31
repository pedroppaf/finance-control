package com.pedro.finance_control.controller;


import com.pedro.finance_control.dto.TransactionReponse;
import com.pedro.finance_control.dto.TransactionRequest;
import com.pedro.finance_control.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public List<TransactionReponse> findAll(){
        return transactionService.findAll();
    }

    @GetMapping("/{id}")
    public TransactionReponse findById(@PathVariable Long id){
        return transactionService.findById(id);
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
