package id.ac.ui.cs.advprog.sistemticket.service;

import id.ac.ui.cs.advprog.sistemticket.dto.UpdateTicketDTO;
import id.ac.ui.cs.advprog.sistemticket.model.Ticket;
import id.ac.ui.cs.advprog.sistemticket.model.TicketBuilder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketService {
    Ticket createTicket(TicketBuilder ticketBuilder);
    List<Ticket> getAllTickets();
    Ticket getTicketById(UUID id);
    Optional<Ticket> getTicketByIdOptional(UUID id);
    List<Ticket> getTicketsByEventId(UUID eventId);
    List<Ticket> getAvailableTicketsForEvent(UUID eventId);
    void deleteTicket(UUID id);
    Ticket purchaseTicket(UUID ticketId, int quantity);
    Ticket validateTicket(UUID ticketId);
    List<Ticket> findTicketsByType(String type);
    List<Ticket> findActiveTickets();
    List<Ticket> findTicketsByPriceRange(double minPrice, double maxPrice);
    List<Ticket> findTicketsWithAvailableQuota(int minimumQuota);
    Ticket updateTicket(UUID id, TicketBuilder updatedTicketBuilder);
    Ticket updateTicket(UUID id, UpdateTicketDTO dto);
    boolean areTicketsAvailableForEvent(UUID eventId);
    Ticket markTicketAsSoldOut(UUID id);
    Ticket markTicketAsAvailable(UUID id);
    Ticket markTicketAsPurchased(UUID id);
}