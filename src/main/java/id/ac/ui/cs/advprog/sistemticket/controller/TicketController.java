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
        // Use current time to find available tickets
        Long currentTime = System.currentTimeMillis();
        List<Ticket> tickets = ticketService.findAllAvailable(currentTime);
        List<TicketDto> ticketDtos = tickets.stream()
                .map(ticketMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ticketDtos);
    }
    
    @PostMapping
    public ResponseEntity<TicketDto> createTicket(@Valid @RequestBody TicketCreationDto ticketDto) {
        Ticket ticket = ticketMapper.toEntity(ticketDto);
        Ticket createdTicket = ticketService.createTicket(ticket);
        if (createdTicket == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketMapper.toDto(createdTicket));
    }
    
    @PutMapping("/{id}")
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
    
    @PatchMapping("/{id}/status")
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
    
    @PostMapping("/{id}/purchase")
    public ResponseEntity<TicketDto> purchaseTicket(@PathVariable String id, 
                                                 @Valid @RequestBody TicketPurchaseDto purchaseDto) {
        try {
            // Use provided timestamp or current time
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
    
    @PostMapping("/{id}/validate")
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
    public ResponseEntity<String> processExpirationAsync(@PathVariable String id) {
        ticketService.processTicketExpiration(id);
        return ResponseEntity.accepted().body("Expiration process started for ticket: " + id);
    }
}
