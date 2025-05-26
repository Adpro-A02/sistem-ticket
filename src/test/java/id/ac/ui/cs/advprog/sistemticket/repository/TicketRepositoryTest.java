package id.ac.ui.cs.advprog.sistemticket.repository;

import id.ac.ui.cs.advprog.sistemticket.model.Ticket;
import id.ac.ui.cs.advprog.sistemticket.enums.TicketStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TicketRepositoryTest {
    @Autowired
    private TicketRepository ticketRepository;
    
    @Autowired
    private TestEntityManager entityManager;
    
    private Ticket ticket1;
    private Ticket ticket2;
    private Ticket ticket3;
    private String eventId1;
    private String eventId2;
    private Long currentTime;
    
    @BeforeEach
    void setUp() {
        currentTime = System.currentTimeMillis();
        
        // Set up test data
        eventId1 = "eb558e9f-1c39-460e-8860-71af6af63bd6";
        eventId2 = "7f9e15bb-4b15-42f4-aebc-c3af385fb078";
        
        // Create regular ticket for event 1
        ticket1 = new Ticket(
            eventId1,
            "REGULAR",
            150.0,
            100,
            "Regular ticket for concert",
            currentTime,
            currentTime + 86400000 // 1 day later
        );
        
        // Create VIP ticket for event 1
        ticket2 = new Ticket(
            eventId1,
            "VIP",
            300.0,
            50,
            "VIP ticket for concert",
            currentTime,
            currentTime + 86400000 // 1 day later
        );
        
        // Create regular ticket for event 2
        ticket3 = new Ticket(
            eventId2,
            "REGULAR",
            200.0,
            200,
            "Regular ticket for exhibition",
            currentTime,
            currentTime + 86400000 // 1 day later
        );
    }
    
    @Test
    void testSaveCreate() {
        // Save the ticket
        Ticket savedTicket = ticketRepository.save(ticket1);
        
        // Retrieve the ticket
        Optional<Ticket> foundTicket = ticketRepository.findById(savedTicket.getId());
        
        // Assertions
        assertTrue(foundTicket.isPresent());
        assertEquals(savedTicket.getId(), foundTicket.get().getId());
        assertEquals(savedTicket.getEventId(), foundTicket.get().getEventId());
        assertEquals(savedTicket.getType(), foundTicket.get().getType());
        assertEquals(savedTicket.getPrice(), foundTicket.get().getPrice());
        assertEquals(savedTicket.getQuota(), foundTicket.get().getQuota());
        assertEquals(savedTicket.getStatus(), foundTicket.get().getStatus());
    }
    
    @Test
    void testSaveUpdate() {
        Ticket ticket = ticket1;
        ticketRepository.save(ticket);
        
        // Update the ticket
        ticket.setStatus(TicketStatus.PURCHASED.getValue());
        ticket.setRemainingQuota(ticket.getRemainingQuota() - 10);
        
        Ticket result = ticketRepository.save(ticket);
        Optional<Ticket> findResultOptional = ticketRepository.findById(ticket.getId());
        
        assertTrue(findResultOptional.isPresent());
        Ticket findResult = findResultOptional.get();
        
        assertEquals(ticket.getId(), result.getId());
        assertEquals(ticket.getId(), findResult.getId());
        assertEquals(TicketStatus.PURCHASED.getValue(), findResult.getStatus());
        assertEquals(ticket.getRemainingQuota(), findResult.getRemainingQuota());
    }
    
    @Test
    void testFindByIdIfIdFound() {
        Ticket saved = ticketRepository.save(ticket1);
        ticketRepository.save(ticket2);
        
        Optional<Ticket> findResultOptional = ticketRepository.findById(saved.getId());
        
        assertTrue(findResultOptional.isPresent());
        Ticket findResult = findResultOptional.get();
        
        assertEquals(saved.getId(), findResult.getId());
        assertEquals(saved.getEventId(), findResult.getEventId());
        assertEquals(saved.getType(), findResult.getType());
    }
    
    @Test
    void testFindByIdIfIdNotFound() {
        ticketRepository.save(ticket1);
        
        Optional<Ticket> findResultOptional = ticketRepository.findById("non-existent-id");
        assertFalse(findResultOptional.isPresent());
    }
    
    @Test
    void testFindAllByEventId() {
        ticketRepository.save(ticket1);
        ticketRepository.save(ticket2);
        ticketRepository.save(ticket3);
        
        List<Ticket> eventTickets = ticketRepository.findAllByEventId(eventId1);
        assertEquals(2, eventTickets.size());
        
        // Verify the tickets are for the correct event
        for (Ticket ticket : eventTickets) {
            assertEquals(eventId1, ticket.getEventId());
        }
    }
    
    @Test
    void testFindAllByType() {
        ticketRepository.save(ticket1);
        ticketRepository.save(ticket2);
        ticketRepository.save(ticket3);
        
        List<Ticket> regularTickets = ticketRepository.findAllByType("REGULAR");
        assertEquals(2, regularTickets.size());
        
        // Verify the tickets are of the correct type
        for (Ticket ticket : regularTickets) {
            assertEquals("REGULAR", ticket.getType());
        }
    }
    
    @Test
    void testFindAllByStatus() {
        ticketRepository.save(ticket1);
        ticketRepository.save(ticket2);
        ticketRepository.save(ticket3);
        
        // All tickets should have AVAILABLE status initially
        List<Ticket> availableTickets = ticketRepository.findAllByStatus(TicketStatus.AVAILABLE.getValue());
        assertEquals(3, availableTickets.size());
        
        // Change status of one ticket to PURCHASED
        Ticket ticketToUpdate = ticket1;
        ticketToUpdate.setStatus(TicketStatus.PURCHASED.getValue());
        ticketRepository.save(ticketToUpdate);
        
        // Now we should have 2 AVAILABLE tickets and 1 PURCHASED
        availableTickets = ticketRepository.findAllByStatus(TicketStatus.AVAILABLE.getValue());
        assertEquals(2, availableTickets.size());
        
        List<Ticket> purchasedTickets = ticketRepository.findAllByStatus(TicketStatus.PURCHASED.getValue());
        assertEquals(1, purchasedTickets.size());
        assertEquals(ticketToUpdate.getId(), purchasedTickets.get(0).getId());
    }
    
    @Test
    void testFindAllAvailableTickets() {
        ticketRepository.save(ticket1);
        ticketRepository.save(ticket2);
        ticketRepository.save(ticket3);
        
        // Set one ticket to have zero remaining quota
        Ticket ticket = ticket1;
        ticket.setRemainingQuota(0);
        ticketRepository.save(ticket);
        
        // Set another ticket to PURCHASED status
        Ticket ticket2Clone = ticket2;
        ticket2Clone.setStatus(TicketStatus.PURCHASED.getValue());
        ticketRepository.save(ticket2Clone);
        
        // Find available tickets (AVAILABLE status and remaining quota > 0)
        List<Ticket> availableTickets = ticketRepository.findAllAvailable(currentTime + 1000);
        assertEquals(1, availableTickets.size());
        assertEquals(ticket3.getId(), availableTickets.get(0).getId());
    }
    
    @Test
    void testFindAll() {
        ticketRepository.save(ticket1);
        ticketRepository.save(ticket2);
        ticketRepository.save(ticket3);
        
        List<Ticket> allTickets = ticketRepository.findAll();
        assertEquals(3, allTickets.size());
    }
    
    @Test
    void testDeleteById() {
        ticketRepository.save(ticket1);
        ticketRepository.save(ticket2);
        ticketRepository.save(ticket3);
        
        String idToDelete = ticket1.getId();
        
        // Verify ticket exists
        assertTrue(ticketRepository.findById(idToDelete).isPresent());
        
        // Delete the ticket
        ticketRepository.deleteById(idToDelete);
        
        // Verify ticket no longer exists
        assertFalse(ticketRepository.findById(idToDelete).isPresent());
        
        // Verify other tickets still exist
        List<Ticket> allTickets = ticketRepository.findAll();
        assertEquals(2, allTickets.size());
    }
}
