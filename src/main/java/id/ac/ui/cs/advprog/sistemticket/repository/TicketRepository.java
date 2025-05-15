package id.ac.ui.cs.advprog.sistemticket.repository;

import id.ac.ui.cs.advprog.sistemticket.enums.TicketStatus;
import id.ac.ui.cs.advprog.sistemticket.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    // Find tickets by event ID
    List<Ticket> findByEventId(UUID eventId);

    // Find tickets by status
    List<Ticket> findByStatus(TicketStatus status);

    // Find tickets by type
    List<Ticket> findByType(String type);

    // Find tickets with sales period active
    List<Ticket> findBySalesStartBeforeAndSalesEndAfter(LocalDateTime now, LocalDateTime sameNow);

    // Find tickets by price range
    List<Ticket> findByPriceBetween(double minPrice, double maxPrice);

    // Custom methods based on your requirements
    List<Ticket> findByEventIdAndStatus(UUID eventId, TicketStatus status);
}