package id.ac.ui.cs.advprog.sistemticket.controller;

import id.ac.ui.cs.advprog.sistemticket.model.Ticket;
import id.ac.ui.cs.advprog.sistemticket.service.TicketService;
import id.ac.ui.cs.advprog.sistemticket.enums.TicketStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    
    @Autowired
    private TicketService ticketService;
    
    @GetMapping
    public ResponseEntity<List<Ticket>> getAllTickets() {
        List<Ticket> tickets = ticketService.findAll();
        return ResponseEntity.ok(tickets);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicketById(@PathVariable String id) {
        Ticket ticket = ticketService.findById(id);
        if (ticket == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ticket);
    }
    
    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<Ticket>> getTicketsByEventId(@PathVariable String eventId) {
        List<Ticket> tickets = ticketService.findAllByEventId(eventId);
        return ResponseEntity.ok(tickets);
    }
    
    @GetMapping("/available")
    public ResponseEntity<List<Ticket>> getAvailableTickets() {
        // Use current time to find available tickets
        Long currentTime = System.currentTimeMillis();
        List<Ticket> tickets = ticketService.findAllAvailable(currentTime);
        return ResponseEntity.ok(tickets);
    }
    
    @PostMapping
    public ResponseEntity<Ticket> createTicket(@RequestBody Ticket ticket) {
        Ticket createdTicket = ticketService.createTicket(ticket);
        if (createdTicket == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTicket);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Ticket> updateTicket(@PathVariable String id, @RequestBody Ticket ticket) {
        // Ensure the path variable ID matches the ticket ID
        ticket.setId(id);
        
        try {
            Ticket updatedTicket = ticketService.updateTicket(ticket);
            return ResponseEntity.ok(updatedTicket);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<Ticket> updateTicketStatus(@PathVariable String id, @RequestBody Map<String, String> statusUpdate) {
        String status = statusUpdate.get("status");
        
        try {
            Ticket updatedTicket = ticketService.updateStatus(id, status);
            return ResponseEntity.ok(updatedTicket);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/{id}/purchase")
    public ResponseEntity<Ticket> purchaseTicket(@PathVariable String id, @RequestBody Map<String, Integer> purchaseRequest) {
        Integer amount = purchaseRequest.get("amount");
        Long currentTime = System.currentTimeMillis();
        
        try {
            Ticket updatedTicket = ticketService.purchaseTicket(id, amount, currentTime);
            return ResponseEntity.ok(updatedTicket);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/{id}/validate")
    public ResponseEntity<Ticket> validateTicket(@PathVariable String id) {
        try {
            Ticket validatedTicket = ticketService.updateStatus(id, TicketStatus.USED.getValue());
            return ResponseEntity.ok(validatedTicket);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable String id) {
        try {
            ticketService.deleteTicket(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/batch")
    public CompletableFuture<ResponseEntity<List<Ticket>>> createTicketsBatch(@RequestBody List<Ticket> tickets) {
        return CompletableFuture.supplyAsync(() -> {
            List<Ticket> createdTickets = new ArrayList<>();
            for (Ticket ticket : tickets) {
                Ticket created = ticketService.createTicket(ticket);
                if (created != null) {
                    createdTickets.add(created);
                }
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTickets);
        });
    }
    
    @PostMapping("/{id}/expire-async")
    public ResponseEntity<String> processExpirationAsync(@PathVariable String id) {
        ticketService.processTicketExpiration(id);
        return ResponseEntity.accepted().body("Expiration process started for ticket: " + id);
    }
}
