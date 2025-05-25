package id.ac.ui.cs.advprog.sistemticket.controller;

import id.ac.ui.cs.advprog.sistemticket.dto.*;
import id.ac.ui.cs.advprog.sistemticket.enums.TicketStatus;
import id.ac.ui.cs.advprog.sistemticket.mapper.TicketMapper;
import id.ac.ui.cs.advprog.sistemticket.model.Ticket;
import id.ac.ui.cs.advprog.sistemticket.service.TicketService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tickets")
@Validated
public class TicketController {
    
    @Autowired
    private TicketService ticketService;
    
    @Autowired
    private TicketMapper ticketMapper;
    
    // Read operations - accessible to all
    @GetMapping
    public ResponseEntity<List<TicketDto>> getAllTickets() {
        List<Ticket> tickets = ticketService.findAll();
        List<TicketDto> ticketDtos = tickets.stream()
                .map(ticketMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ticketDtos);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TicketDto> getTicketById(@PathVariable String id) {
        Ticket ticket = ticketService.findById(id);
        if (ticket == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ticketMapper.toDto(ticket));
    }
    
    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<TicketDto>> getTicketsByEventId(@PathVariable String eventId) {
        List<Ticket> tickets = ticketService.findAllByEventId(eventId);
        List<TicketDto> ticketDtos = tickets.stream()
                .map(ticketMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ticketDtos);
    }
    
    @GetMapping("/available")
    public ResponseEntity<List<TicketDto>> getAvailableTickets() {
        Long currentTime = System.currentTimeMillis();
        List<Ticket> tickets = ticketService.findAllAvailable(currentTime);
        List<TicketDto> ticketDtos = tickets.stream()
                .map(ticketMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ticketDtos);
    }
    
    // Create operations - only for Organizer
    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<TicketDto> createTicket(@Valid @RequestBody TicketCreationDto ticketDto) {
        Ticket ticket = ticketMapper.toEntity(ticketDto);
        Ticket createdTicket = ticketService.createTicket(ticket);
        if (createdTicket == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketMapper.toDto(createdTicket));
    }
    
    // Update operations
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<TicketDto> updateTicket(@PathVariable String id, 
                                               @Valid @RequestBody TicketUpdateDto ticketDto) {
        try {
            Ticket existingTicket = ticketService.findById(id);
            if (existingTicket == null) {
                return ResponseEntity.notFound().build();
            }
            
            ticketMapper.updateEntityFromDto(ticketDto, existingTicket);
            Ticket updatedTicket = ticketService.updateTicket(existingTicket);
            
            return ResponseEntity.ok(ticketMapper.toDto(updatedTicket));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Status update - only for Admin and Organizer
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<TicketDto> updateTicketStatus(@PathVariable String id, 
                                                     @Valid @RequestBody StatusUpdateDto statusDto) {
        try {
            Ticket updatedTicket = ticketService.updateStatus(id, statusDto.getStatus());
            return ResponseEntity.ok(ticketMapper.toDto(updatedTicket));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Purchase operations - only for Attendee
    @PostMapping("/{id}/purchase")
    @PreAuthorize("hasAnyRole('ATTENDEE', 'ADMIN')")
    public ResponseEntity<TicketDto> purchaseTicket(@PathVariable String id, 
                                                 @Valid @RequestBody TicketPurchaseDto purchaseDto) {
        try {
            Long timestamp = purchaseDto.getTimestamp() != null ? 
                    purchaseDto.getTimestamp() : System.currentTimeMillis();
                    
            Ticket updatedTicket = ticketService.purchaseTicket(id, purchaseDto.getAmount(), timestamp);
            return ResponseEntity.ok(ticketMapper.toDto(updatedTicket));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Validate operations - only for Admin and Organizer
    @PostMapping("/{id}/validate")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<TicketDto> validateTicket(@PathVariable String id) {
        try {
            Ticket validatedTicket = ticketService.updateStatus(id, TicketStatus.USED.getValue());
            return ResponseEntity.ok(ticketMapper.toDto(validatedTicket));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Delete operations - only for Admin and Organizer
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<Void> deleteTicket(@PathVariable String id) {
        try {
            // Get the ticket first to check if it can be deleted
            Ticket ticket = ticketService.findById(id);
            if (ticket == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Check if any tickets have been purchased
            if (ticket.getRemainingQuota() < ticket.getQuota()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .header("X-Error-Message", "Cannot delete tickets that have been purchased")
                        .build();
            }
            
            ticketService.deleteTicket(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Batch operations - only for Organizer
    @PostMapping("/batch")
    @PreAuthorize("hasRole('ORGANIZER')")
    public CompletableFuture<ResponseEntity<List<TicketDto>>> createTicketsBatch(
            @Valid @RequestBody List<TicketCreationDto> ticketDtos) {
        return CompletableFuture.supplyAsync(() -> {
            List<TicketDto> createdTicketDtos = new ArrayList<>();
            
            for (TicketCreationDto dto : ticketDtos) {
                Ticket ticket = ticketMapper.toEntity(dto);
                Ticket created = ticketService.createTicket(ticket);
                if (created != null) {
                    createdTicketDtos.add(ticketMapper.toDto(created));
                }
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTicketDtos);
        });
    }
    
    @PostMapping("/{id}/expire-async")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> processExpirationAsync(@PathVariable String id) {
        ticketService.processTicketExpiration(id);
        return ResponseEntity.accepted().body("Expiration process started for ticket: " + id);
    }
}
