package com.pedro.finance_control.controller;

import com.pedro.finance_control.dto.TransactionResponse;
import com.pedro.finance_control.dto.TransactionRequest;
import com.pedro.finance_control.dto.transaction.SummaryResponse;
import com.pedro.finance_control.enums.TransactionType;
import com.pedro.finance_control.response.AppApiResponse;
import com.pedro.finance_control.response.PageDto;
import com.pedro.finance_control.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(summary = "Create a new transaction", description = "Creates a transaction for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transaction created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")})
    @PostMapping
    public ResponseEntity<AppApiResponse<TransactionResponse>> create(@RequestBody @Valid TransactionRequest request){
        return  ResponseEntity.status(HttpStatus.CREATED)
                .body(AppApiResponse.success(transactionService.create(request), "Transaction created successfully"));
    }

    @Operation(summary = "Get transaction by ID", description = "Retrieve a specific transaction by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Transaction not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")})
    @GetMapping("/{id}")
    public AppApiResponse<TransactionResponse> findById(@PathVariable Long id){
        return AppApiResponse.success(transactionService.findById(id));
    }

    @Operation(summary = "Get financial summary", description = "Get the financial summary (income, expenses, and balance) for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Summary retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")})
    @GetMapping("/summary")
    public SummaryResponse getSummary(){
        return transactionService.getSummary();
    }

    @Operation(summary = "List transactions", description = "List all transactions with optional filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")})
    @GetMapping
    public AppApiResponse<PageDto<TransactionResponse>> findAll(@RequestParam(required = false) TransactionType type,

                                                        @RequestParam(required = false)
                                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                        LocalDate startDate,

                                                        @RequestParam(required = false)
                                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                        LocalDate endDate,

                                                        Pageable pageable
                                    ){
        return AppApiResponse.success(transactionService.findAll(type, startDate, endDate, pageable));
    }

    @Operation(summary = "Update a transaction", description = "Update an existing transaction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "404", description = "Transaction not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")})
    @PutMapping("/{id}")
    public AppApiResponse<TransactionResponse> update(@PathVariable Long id, @RequestBody @Valid TransactionRequest request){
        return AppApiResponse.success(transactionService.update(id, request), "Transaction updated successfully");
    }

    @Operation(summary = "Delete a transaction", description = "Delete an existing transaction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Transaction deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Transaction not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        transactionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
