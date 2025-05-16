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
    List<Ticket> findBySalesStart(LocalDateTime salesStart);
    List<Ticket> findByType(String type);
    List<Ticket> findBySalesStartAfter(LocalDateTime now);
    List<Ticket> findByEventId(UUID eventId);
    List<Ticket> findByStatus(TicketStatus status);
    List<Ticket> findBySalesStartBeforeAndSalesEndAfter(LocalDateTime now, LocalDateTime sameNow);
    List<Ticket> findByPriceBetween(double minPrice, double maxPrice);
    List<Ticket> findByEventIdAndStatus(UUID eventId, TicketStatus status);
    List<Ticket> findByQuotaGreaterThan(int minimumQuota);
    List<Ticket> findByQuotaLessThan(int maximumQuota);
}