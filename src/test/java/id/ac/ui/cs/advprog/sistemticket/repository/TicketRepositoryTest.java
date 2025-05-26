package id.ac.ui.cs.advprog.sistemticket.repository;

import id.ac.ui.cs.advprog.sistemticket.model.Ticket;
import id.ac.ui.cs.advprog.sistemticket.enums.TicketStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class TicketRepositoryTest {
    
    @Autowired
    private TicketRepository ticketRepository;
    
    private Ticket ticket1;
    private Ticket ticket2;
    private Ticket ticket3;
    private String eventId1;
    private String eventId2;
    private Long currentTime;
    
    @BeforeEach
    void setUp() {
        // Clear previous test data
        ticketRepository.deleteAll();
        
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
        
        // Save test data
        ticketRepository.save(ticket1);
        ticketRepository.save(ticket2);
        ticketRepository.save(ticket3);
    }
    
    @Test
    void testSaveCreate() {
        // Create a new ticket
        Ticket newTicket = new Ticket(
            eventId1,
            "PREMIUM",
            500.0,
            20,
            "Premium ticket for concert",
            currentTime,
            currentTime + 86400000 // 1 day later
        );
        
        // Save the ticket
        Ticket savedTicket = ticketRepository.save(newTicket);
        
        // Retrieve the ticket
        Optional<Ticket> findResultOptional = ticketRepository.findById(savedTicket.getId());
        
        // Assertions
        assertTrue(findResultOptional.isPresent());
        Ticket findResult = findResultOptional.get();
        assertEquals(savedTicket.getId(), findResult.getId());
        assertEquals(savedTicket.getEventId(), findResult.getEventId());
        assertEquals(savedTicket.getType(), findResult.getType());
        assertEquals(savedTicket.getPrice(), findResult.getPrice());
        assertEquals(savedTicket.getQuota(), findResult.getQuota());
        assertEquals(savedTicket.getStatus(), findResult.getStatus());
    }
    
    @Test
    void testSaveUpdate() {
        // Retrieve existing ticket
        Optional<Ticket> optTicket = ticketRepository.findById(ticket1.getId());
        assertTrue(optTicket.isPresent());
        Ticket ticket = optTicket.get();
        
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
        Optional<Ticket> findResultOptional = ticketRepository.findById(ticket1.getId());
        
        assertTrue(findResultOptional.isPresent());
        Ticket findResult = findResultOptional.get();
        
        assertEquals(ticket1.getId(), findResult.getId());
        assertEquals(ticket1.getEventId(), findResult.getEventId());
        assertEquals(ticket1.getType(), findResult.getType());
    }
    
    @Test
    void testFindByIdIfIdNotFound() {
        Optional<Ticket> findResultOptional = ticketRepository.findById("non-existent-id");
        assertFalse(findResultOptional.isPresent());
    }
    
    @Test
    void testFindAllByEventId() {
        List<Ticket> eventTickets = ticketRepository.findAllByEventId(eventId1);
        assertEquals(2, eventTickets.size());
        
        // Verify the tickets are for the correct event
        for (Ticket ticket : eventTickets) {
            assertEquals(eventId1, ticket.getEventId());
        }
    }
    
    @Test
    void testFindAllByType() {
        List<Ticket> regularTickets = ticketRepository.findAllByType("REGULAR");
        assertEquals(2, regularTickets.size());
        
        // Verify the tickets are of the correct type
        for (Ticket ticket : regularTickets) {
            assertEquals("REGULAR", ticket.getType());
        }
    }
    
    @Test
    void testFindAllByStatus() {
        // All tickets should have AVAILABLE status initially
        List<Ticket> availableTickets = ticketRepository.findAllByStatus(TicketStatus.AVAILABLE.getValue());
        assertEquals(3, availableTickets.size());
        
        // Change status of one ticket to PURCHASED
        Optional<Ticket> optTicket = ticketRepository.findById(ticket1.getId());
        assertTrue(optTicket.isPresent());
        Ticket ticketToUpdate = optTicket.get();
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
        // Set one ticket to have zero remaining quota
        Optional<Ticket> optTicket1 = ticketRepository.findById(ticket1.getId());
        assertTrue(optTicket1.isPresent());
        Ticket ticket = optTicket1.get();
        ticket.setRemainingQuota(0);
        ticketRepository.save(ticket);
        
        // Set another ticket to PURCHASED status
        Optional<Ticket> optTicket2 = ticketRepository.findById(ticket2.getId());
        assertTrue(optTicket2.isPresent());
        Ticket ticket2Clone = optTicket2.get();
        ticket2Clone.setStatus(TicketStatus.PURCHASED.getValue());
        ticketRepository.save(ticket2Clone);
        
        // Find available tickets (AVAILABLE status and remaining quota > 0)
        List<Ticket> availableTickets = ticketRepository.findAllAvailable(currentTime + 1000);
        assertEquals(1, availableTickets.size());
        assertEquals(ticket3.getId(), availableTickets.get(0).getId());
    }
    
    @Test
    void testFindAll() {
        List<Ticket> allTickets = ticketRepository.findAll();
        assertEquals(3, allTickets.size());
    }
    
    @Test
    void testDeleteById() {
        // Verify ticket exists
        assertTrue(ticketRepository.findById(ticket1.getId()).isPresent());
        
        // Delete the ticket
        ticketRepository.deleteById(ticket1.getId());
        
        // Verify ticket no longer exists
        assertFalse(ticketRepository.findById(ticket1.getId()).isPresent());
        
        // Verify other tickets still exist
        List<Ticket> allTickets = ticketRepository.findAll();
        assertEquals(2, allTickets.size());
    }
}
