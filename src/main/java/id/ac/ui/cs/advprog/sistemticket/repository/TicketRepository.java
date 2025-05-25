package id.ac.ui.cs.advprog.sistemticket.repository;

import id.ac.ui.cs.advprog.sistemticket.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, String> {
    
    List<Ticket> findAllByEventId(String eventId);
    
    List<Ticket> findAllByType(String type);
    
    List<Ticket> findAllByStatus(String status);
    
    @Query("SELECT t FROM Ticket t WHERE t.status = 'AVAILABLE' AND t.remainingQuota > 0 AND :currentTime BETWEEN t.saleStart AND t.saleEnd")
    List<Ticket> findAllAvailable(@Param("currentTime") Long currentTime);
    
    @Query("SELECT t FROM Ticket t WHERE t.userId = :userId")
    List<Ticket> findAllByUserId(@Param("userId") String userId);
}
