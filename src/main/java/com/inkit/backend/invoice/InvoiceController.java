package com.inkit.backend.invoice;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inkit.backend.invoice.dto.InvoiceFilterRequest;
import com.inkit.backend.invoice.dto.InvoiceResponse;
import com.inkit.backend.invoice.dto.InvoiceUpsertRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    public List<InvoiceResponse> listInvoices(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false, name = "case_id") String caseId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "-created_date") String sortBy,
            @RequestParam(defaultValue = "50") Integer limit) {
        return invoiceService.listInvoices(userDetails.getUsername(), caseId, status, sortBy, limit);
    }

    @PostMapping("/filter")
    public List<InvoiceResponse> filterInvoices(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody InvoiceFilterRequest request) {
        return invoiceService.filterInvoices(userDetails.getUsername(), request);
    }

    @GetMapping("/{id}")
    public InvoiceResponse getInvoice(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        return invoiceService.getById(userDetails.getUsername(), id);
    }

    @PostMapping
    public InvoiceResponse createInvoice(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody InvoiceUpsertRequest request) {
        return invoiceService.create(userDetails.getUsername(), request);
    }

    @PutMapping("/{id}")
    public InvoiceResponse updateInvoice(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @RequestBody InvoiceUpsertRequest request) {
        return invoiceService.update(userDetails.getUsername(), id, request);
    }

    @DeleteMapping("/{id}")
    public String deleteInvoice(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        invoiceService.delete(userDetails.getUsername(), id);
        return "Invoice deleted successfully";
    }
}

