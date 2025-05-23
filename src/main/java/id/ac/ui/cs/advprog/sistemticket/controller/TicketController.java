package id.ac.ui.cs.advprog.sistemticket.controller;

import id.ac.ui.cs.advprog.sistemticket.dto.UpdateTicketDTO;
import id.ac.ui.cs.advprog.sistemticket.model.Ticket;
import id.ac.ui.cs.advprog.sistemticket.model.TicketBuilder;
import id.ac.ui.cs.advprog.sistemticket.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.concurrent.CompletableFuture;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    @Autowired
    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public List<Ticket> getAllTickets() {
        return ticketService.getAllTickets();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicketById(@PathVariable UUID id) {
        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Ticket createTicket(@RequestBody TicketBuilder ticketBuilder) {
        return ticketService.createTicket(ticketBuilder);
    }

    @PutMapping("/{id}")
    public Ticket updateTicket(@PathVariable UUID id, @Valid @RequestBody UpdateTicketDTO dto) {
        return ticketService.updateTicket(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTicket(@PathVariable UUID id) {
        ticketService.deleteTicket(id);
    }

    @PostMapping("/{id}/purchase")
    public Ticket purchaseTicket(@PathVariable UUID id, @RequestParam int quantity) {
        return ticketService.purchaseTicket(id, quantity);
    }

    @GetMapping("/async/type/{type}")
    public CompletableFuture<List<Ticket>> findTicketsByTypeAsync(@PathVariable String type) {
        return ticketService.findTicketsByTypeAsync(type);
    }

    @GetMapping("/async/events/{eventId}/available")
    public CompletableFuture<List<Ticket>> getAvailableTicketsForEventAsync(@PathVariable UUID eventId) {
        return ticketService.getAvailableTicketsForEventAsync(eventId);
    }

    @GetMapping("/async/price")
    public CompletableFuture<List<Ticket>> searchTicketsByPriceRangeAsync(
            @RequestParam double min, @RequestParam double max) {
        return ticketService.searchTicketsByPriceRangeAsync(min, max);
    }

    @GetMapping("/stream/events/{eventId}/tickets")
    public SseEmitter streamTicketsForEvent(@PathVariable UUID eventId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        // Non-blocking background processing
        CompletableFuture.runAsync(() -> {
            try {
                List<Ticket> tickets = ticketService.getTicketsByEventId(eventId);
                for (Ticket ticket : tickets) {
                    // Simulate processing delay
                    Thread.sleep(200);
                    emitter.send(ticket);
                }
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}