package id.ac.ui.cs.advprog.sistemticket.service;

import id.ac.ui.cs.advprog.sistemticket.enums.TicketStatus;
import id.ac.ui.cs.advprog.sistemticket.model.Ticket;
import id.ac.ui.cs.advprog.sistemticket.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TicketServiceImpl implements TicketService {
    
    @Autowired
    private TicketRepository ticketRepository;
    
    @Override
    public Ticket createTicket(Ticket ticket) {
        if (ticketRepository.findById(ticket.getId()) == null) {
            ticketRepository.save(ticket);
            return ticket;
        }
        return null;
    }
    
    @Override
    public Ticket findById(String id) {
        return ticketRepository.findById(id);
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
        Ticket existingTicket = ticketRepository.findById(ticket.getId());
        if (existingTicket == null) {
            throw new NoSuchElementException("Ticket with ID " + ticket.getId() + " not found");
        }
        
        // Update the ticket
        return ticketRepository.save(ticket);
    }
    
    @Override
    public Ticket updateStatus(String id, String status) {
        Ticket ticket = ticketRepository.findById(id);
        if (ticket == null) {
            throw new NoSuchElementException("Ticket with ID " + id + " not found");
        }
        
        // Validate the status
        if (!TicketStatus.contains(status)) {
            throw new IllegalArgumentException("Invalid ticket status: " + status);
        }
        
        ticket.setStatus(status);
        return ticketRepository.save(ticket);
    }
    
    @Override
    public Ticket purchaseTicket(String id, int amount, Long currentTime) {
        Ticket ticket = ticketRepository.findById(id);
        if (ticket == null) {
            throw new NoSuchElementException("Ticket with ID " + id + " not found");
        }
        
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
        return ticketRepository.save(ticket);
    }
    
    @Override
    public void deleteTicket(String id) {
        Ticket ticket = ticketRepository.findById(id);
        if (ticket == null) {
            throw new NoSuchElementException("Ticket with ID " + id + " not found");
        }
        
        ticketRepository.deleteById(id);
    }
}
