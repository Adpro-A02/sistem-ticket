package id.ac.ui.cs.advprog.sistemticket.service;

import id.ac.ui.cs.advprog.sistemticket.enums.TicketStatus;
import id.ac.ui.cs.advprog.sistemticket.model.Ticket;
import id.ac.ui.cs.advprog.sistemticket.model.TicketBuilder;
import id.ac.ui.cs.advprog.sistemticket.repository.TicketRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;

    @Autowired
    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public Ticket createTicket(TicketBuilder ticketBuilder) {
        if (!ticketBuilder.isReady()) {
            throw new IllegalStateException("Ticket builder is not ready for building");
        }
        
        Ticket ticket = ticketBuilder.build();
        ticket.setStatus(TicketStatus.AVAILABLE);
        return ticketRepository.save(ticket);
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public Ticket getTicketById(UUID id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with id: " + id));
    }

    public List<Ticket> getTicketsByEventId(UUID eventId) {
        return ticketRepository.findByEventId(eventId);
    }

    public List<Ticket> getAvailableTicketsForEvent(UUID eventId) {
        return ticketRepository.findByEventIdAndStatus(eventId, TicketStatus.AVAILABLE);
    }

    @Transactional
    public void deleteTicket(UUID id) {
        Ticket ticket = getTicketById(id);

        if (ticket.getStatus() == TicketStatus.PURCHASED || 
            ticket.getStatus() == TicketStatus.USED) {
            throw new IllegalStateException("Cannot delete ticket that has been purchased");
        }
        
        ticketRepository.deleteById(id);
    }

    @Transactional
    public Ticket purchaseTicket(UUID ticketId, int quantity) {
        Ticket ticket = getTicketById(ticketId);
        
        if (ticket.getStatus() != TicketStatus.AVAILABLE) {
            throw new IllegalStateException("Ticket is not available for purchase");
        }
        
        if (ticket.getQuota() < quantity) {
            throw new IllegalStateException("Not enough tickets available");
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(ticket.getSalesStart()) || now.isAfter(ticket.getSalesEnd())) {
            throw new IllegalStateException("Ticket sales period is not active");
        }
        
        int newQuota = ticket.getQuota() - quantity;
        ticket.setQuota(newQuota);
        
        if (newQuota == 0) {
            ticket.setStatus(TicketStatus.SOLD_OUT);
        }
        
        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket validateTicket(UUID ticketId) {
        Ticket ticket = getTicketById(ticketId);
        
        if (ticket.getStatus() != TicketStatus.PURCHASED) {
            throw new IllegalStateException("Ticket cannot be validated");
        }
        
        ticket.setStatus(TicketStatus.USED);
        return ticketRepository.save(ticket);
    }
    
    public List<Ticket> findTicketsByType(String type) {
        return ticketRepository.findByType(type);
    }
    
    public List<Ticket> findActiveTickets() {
        LocalDateTime now = LocalDateTime.now();
        return ticketRepository.findBySalesStartBeforeAndSalesEndAfter(now, now);
    }
    
    public List<Ticket> findTicketsByPriceRange(double minPrice, double maxPrice) {
        return ticketRepository.findByPriceBetween(minPrice, maxPrice);
    }
    
    public List<Ticket> findTicketsWithAvailableQuota(int minimumQuota) {
        return ticketRepository.findByQuotaGreaterThan(minimumQuota - 1);
    }
    
    @Transactional
    public Ticket updateTicket(UUID id, TicketBuilder updatedTicketBuilder) {
        Ticket existingTicket = getTicketById(id);

        if (existingTicket.getStatus() == TicketStatus.PURCHASED || 
            existingTicket.getStatus() == TicketStatus.USED) {
            throw new IllegalStateException("Cannot update ticket that has been purchased or used");
        }
        
        Ticket updatedTicket = updatedTicketBuilder.build();
        updatedTicket.setId(id); // Ensure ID remains the same
        updatedTicket.setStatus(existingTicket.getStatus()); // Preserve status
        
        return ticketRepository.save(updatedTicket);
    }
    
    public boolean areTicketsAvailableForEvent(UUID eventId) {
        List<Ticket> availableTickets = getAvailableTicketsForEvent(eventId);
        return !availableTickets.isEmpty();
    }
}