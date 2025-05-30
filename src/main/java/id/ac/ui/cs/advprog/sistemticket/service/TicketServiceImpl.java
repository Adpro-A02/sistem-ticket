package id.ac.ui.cs.advprog.sistemticket.service;

import id.ac.ui.cs.advprog.sistemticket.enums.TicketStatus;
import id.ac.ui.cs.advprog.sistemticket.event.TicketPurchasedEvent;
import id.ac.ui.cs.advprog.sistemticket.model.Ticket;
import id.ac.ui.cs.advprog.sistemticket.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class TicketServiceImpl implements TicketService {
    
    @Autowired
    private TicketRepository ticketRepository;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @Override
    public Ticket createTicket(Ticket ticket) {
        if (ticketRepository.findById(ticket.getId()).isPresent()) {
            return null;
        }
        return ticketRepository.save(ticket);
    }
    
    @Override
    public Ticket findById(String id) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(id);
        return ticketOpt.orElse(null);
    }
    
    @Override
    public List<Ticket> findAll() {
        return ticketRepository.findAll();
    }
    
    @Override
    public List<Ticket> findAllByEventId(String eventId) {
        return ticketRepository.findAllByEventId(eventId);
    }
    
    @Override
    public List<Ticket> findAllByType(String type) {
        return ticketRepository.findAllByType(type);
    }
    
    @Override
    public List<Ticket> findAllAvailable(Long currentTime) {
        return ticketRepository.findAllAvailable(currentTime);
    }
    
    @Override
    public Ticket updateTicket(Ticket ticket) {
        Optional<Ticket> existingTicket = ticketRepository.findById(ticket.getId());
        if (existingTicket.isEmpty()) {
            throw new NoSuchElementException("Ticket with ID " + ticket.getId() + " not found");
        }
        
        // Update the ticket
        return ticketRepository.save(ticket);
    }
    
    @Override
    public Ticket updateStatus(String id, String status) {
        Optional<Ticket> optionalTicket = ticketRepository.findById(id);
        if (optionalTicket.isEmpty()) {
            throw new NoSuchElementException("Ticket with ID " + id + " not found");
        }
        
        Ticket ticket = optionalTicket.get();
        
        // Validate the status
        if (!TicketStatus.contains(status)) {
            throw new IllegalArgumentException("Invalid ticket status: " + status);
        }
        
        ticket.setStatus(status);
        return ticketRepository.save(ticket);
    }
    
    @Override
    public Ticket purchaseTicket(String id, int amount, Long currentTime) {
        Optional<Ticket> optionalTicket = ticketRepository.findById(id);
        if (optionalTicket.isEmpty()) {
            throw new NoSuchElementException("Ticket with ID " + id + " not found");
        }
        
        Ticket ticket = optionalTicket.get();
        
        // Check if ticket is available for purchase
        if (!ticket.isAvailableForPurchase(currentTime)) {
            throw new IllegalArgumentException("Ticket is not available for purchase at this time");
        }
        
        // Try to decrease the quota
        try {
            ticket.decreaseRemainingQuota(amount);
        } catch (IllegalArgumentException e) {
            // Rethrow with additional context if needed
            throw new IllegalArgumentException("Cannot purchase tickets: " + e.getMessage());
        }
        
        // Save and return the updated ticket
        Ticket updatedTicket = ticketRepository.save(ticket);
        
        // Publish event for asynchronous processing
        eventPublisher.publishEvent(new TicketPurchasedEvent(updatedTicket, amount));
        
        return updatedTicket;
    }
    
    @Override
    public void deleteTicket(String id) {
        Optional<Ticket> optionalTicket = ticketRepository.findById(id);
        if (optionalTicket.isEmpty()) {
            throw new NoSuchElementException("Ticket with ID " + id + " not found");
        }
        
        ticketRepository.deleteById(id);
    }
    
    @Async("taskExecutor")
    public CompletableFuture<Void> processTicketExpiration(String ticketId) {
        return CompletableFuture.runAsync(() -> {
            Optional<Ticket> optionalTicket = ticketRepository.findById(ticketId);
            if (optionalTicket.isPresent() && optionalTicket.get().getSaleEnd() < System.currentTimeMillis()) {
                Ticket ticket = optionalTicket.get();
                ticket.setStatus(TicketStatus.EXPIRED.getValue());
                ticketRepository.save(ticket);
            }
        });
    }
}
