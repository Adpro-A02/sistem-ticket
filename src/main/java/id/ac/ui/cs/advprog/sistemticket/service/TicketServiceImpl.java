package id.ac.ui.cs.advprog.sistemticket.service;

import id.ac.ui.cs.advprog.sistemticket.dto.UpdateTicketDTO;
import id.ac.ui.cs.advprog.sistemticket.enums.TicketStatus;
import id.ac.ui.cs.advprog.sistemticket.model.Ticket;
import id.ac.ui.cs.advprog.sistemticket.model.TicketBuilder;
import id.ac.ui.cs.advprog.sistemticket.repository.TicketRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;

    @Autowired
    public TicketServiceImpl(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    @Transactional
    public Ticket createTicket(TicketBuilder ticketBuilder) {
        if (!ticketBuilder.isReady()) {
            throw new IllegalStateException("Ticket builder is not ready for building");
        }

        Ticket ticket = ticketBuilder.build();
        ticket.setStatus(TicketStatus.AVAILABLE);
        return ticketRepository.save(ticket);
    }

    @Override
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    @Override
    public Ticket getTicketById(UUID id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with id: " + id));
    }

    @Override
    public Optional<Ticket> getTicketByIdOptional(UUID id) {
        return ticketRepository.findById(id);
    }

    @Override
    public List<Ticket> getTicketsByEventId(UUID eventId) {
        return ticketRepository.findByEventId(eventId);
    }

    @Override
    public List<Ticket> getAvailableTicketsForEvent(UUID eventId) {
        return ticketRepository.findByEventIdAndStatus(eventId, TicketStatus.AVAILABLE);
    }

    @Override
    @Transactional
    public void deleteTicket(UUID id) {
        Ticket ticket = getTicketById(id);

        if (isTicketPurchasedOrUsed(ticket)) {
            throw new IllegalStateException("Cannot delete ticket that has been purchased");
        }

        ticketRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Ticket purchaseTicket(UUID ticketId, int quantity) {
        Ticket ticket = getTicketById(ticketId);

        validateTicketForPurchase(ticket, quantity);

        // Use the ticket's business logic
        ticket.reduceQuota(quantity);

        // If sold out, update status
        if (ticket.getQuota() == 0) {
            ticket.setStatus(TicketStatus.SOLD_OUT);
        }

        return ticketRepository.save(ticket);
    }

    @Override
    @Transactional
    public Ticket validateTicket(UUID ticketId) {
        Ticket ticket = getTicketById(ticketId);

        if (ticket.getStatus() != TicketStatus.PURCHASED) {
            throw new IllegalStateException("Ticket cannot be validated");
        }

        return changeStatus(ticket, TicketStatus.USED);
    }

    @Override
    public List<Ticket> findTicketsByType(String type) {
        return ticketRepository.findByType(type);
    }

    @Override
    public List<Ticket> findActiveTickets() {
        LocalDateTime now = LocalDateTime.now();
        return ticketRepository.findBySalesStartBeforeAndSalesEndAfter(now, now);
    }

    @Override
    public List<Ticket> findTicketsByPriceRange(double minPrice, double maxPrice) {
        return ticketRepository.findByPriceBetween(minPrice, maxPrice);
    }

    @Override
    public List<Ticket> findTicketsWithAvailableQuota(int minimumQuota) {
        return ticketRepository.findByQuotaGreaterThan(minimumQuota - 1);
    }

    @Override
    @Transactional
    public Ticket updateTicket(UUID id, TicketBuilder updatedTicketBuilder) {
        return updateTicket(id, ticketBuilderToDTO(updatedTicketBuilder));
    }

    @Override
    @Transactional
    public Ticket updateTicket(UUID id, UpdateTicketDTO dto) {
        Ticket existingTicket = getTicketById(id);

        if (isTicketPurchasedOrUsed(existingTicket)) {
            throw new IllegalStateException("Cannot update ticket that has been purchased or used");
        }

        // Update fields from DTO
        existingTicket.setType(dto.getType());
        existingTicket.setPrice(dto.getPrice());
        existingTicket.setQuota(dto.getQuota());
        existingTicket.setDescription(dto.getDescription());
        existingTicket.setSalesStart(dto.getSalesStart());
        existingTicket.setSalesEnd(dto.getSalesEnd());
        existingTicket.setEventId(dto.getEventId());

        return ticketRepository.save(existingTicket);
    }

    @Override
    public boolean areTicketsAvailableForEvent(UUID eventId) {
        List<Ticket> availableTickets = getAvailableTicketsForEvent(eventId);
        return !availableTickets.isEmpty();
    }

    @Override
    @Transactional
    public Ticket markTicketAsSoldOut(UUID id) {
        Ticket ticket = getTicketById(id);
        return changeStatus(ticket, TicketStatus.SOLD_OUT);
    }

    @Override
    @Transactional
    public Ticket markTicketAsAvailable(UUID id) {
        Ticket ticket = getTicketById(id);

        if (ticket.getQuota() <= 0) {
            throw new IllegalStateException("Cannot mark ticket as available with zero quota");
        }

        return changeStatus(ticket, TicketStatus.AVAILABLE);
    }

    @Override
    @Transactional
    public Ticket markTicketAsPurchased(UUID id) {
        Ticket ticket = getTicketById(id);
        return changeStatus(ticket, TicketStatus.PURCHASED);
    }

    // Private helper methods

    private boolean isTicketPurchasedOrUsed(Ticket ticket) {
        return ticket.getStatus() == TicketStatus.PURCHASED ||
                ticket.getStatus() == TicketStatus.USED;
    }

    private void validateTicketForPurchase(Ticket ticket, int quantity) {
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
    }

    private Ticket changeStatus(Ticket ticket, TicketStatus status) {
        ticket.setStatus(status);
        return ticketRepository.save(ticket);
    }

    private UpdateTicketDTO ticketBuilderToDTO(TicketBuilder builder) {
        Ticket ticket = builder.build();

        UpdateTicketDTO dto = new UpdateTicketDTO();
        dto.setType(ticket.getType());
        dto.setPrice(ticket.getPrice());
        dto.setQuota(ticket.getQuota());
        dto.setDescription(ticket.getDescription());
        dto.setSalesStart(ticket.getSalesStart());
        dto.setSalesEnd(ticket.getSalesEnd());
        dto.setEventId(ticket.getEventId());
        dto.setStatus(ticket.getStatus());

        return dto;
        }

    @Override
    @Async("taskExecutor")
    public CompletableFuture<List<Ticket>> findTicketsByTypeAsync(String type) {
        return CompletableFuture.completedFuture(ticketRepository.findByType(type));
    }

    @Override
    @Async("taskExecutor")
    public CompletableFuture<List<Ticket>> getAvailableTicketsForEventAsync(UUID eventId) {
        return CompletableFuture.completedFuture(
                ticketRepository.findByEventIdAndStatus(eventId, TicketStatus.AVAILABLE)
        );
    }

    @Override
    @Async("taskExecutor")
    public CompletableFuture<List<Ticket>> searchTicketsByPriceRangeAsync(double minPrice, double maxPrice) {
        return CompletableFuture.completedFuture(
                ticketRepository.findByPriceBetween(minPrice, maxPrice)
        );
    }

    @Override
    @Async("taskExecutor")
    public void checkAndUpdateExpiredTickets() {
        LocalDateTime now = LocalDateTime.now();
        List<Ticket> expiredTickets = ticketRepository.findAll().stream()
                .filter(ticket -> ticket.getStatus() == TicketStatus.AVAILABLE)
                .filter(ticket -> ticket.getSalesEnd().isBefore(now))
                .collect(Collectors.toList());

        for (Ticket ticket : expiredTickets) {
            ticket.setStatus(TicketStatus.EXPIRED);
            ticketRepository.save(ticket);
        }
    }
}