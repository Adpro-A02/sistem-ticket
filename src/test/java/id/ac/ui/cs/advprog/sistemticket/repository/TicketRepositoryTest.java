package id.ac.ui.cs.advprog.sistemticket.repository;

import id.ac.ui.cs.advprog.sistemticket.model.Ticket;
import id.ac.ui.cs.advprog.sistemticket.enums.TicketStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketRepositoryTest {
    
    @Mock
    private TicketRepository ticketRepository;
    
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
        // Create a new ticket
        Ticket newTicket = new Ticket(
            eventId1,
            "VIP",
            500.0,
            20,
            "Premium ticket for concert",
            currentTime,
            currentTime + 86400000
        );
        
        // Mock save behavior
        when(ticketRepository.save(newTicket)).thenReturn(newTicket);
        when(ticketRepository.findById(newTicket.getId())).thenReturn(Optional.of(newTicket));
        
        // Save the ticket
        Ticket savedTicket = ticketRepository.save(newTicket);
        
        // Retrieve the ticket
        Optional<Ticket> findResultOptional = ticketRepository.findById(savedTicket.getId());
        
        assertTrue(findResultOptional.isPresent());
        assertEquals(savedTicket.getId(), findResultOptional.get().getId());
    }
    
    @Test
    void testSaveUpdate() {
        // Mock finding existing ticket
        when(ticketRepository.findById(ticket1.getId())).thenReturn(Optional.of(ticket1));
        
        // Update the ticket
        ticket1.setStatus(TicketStatus.PURCHASED.getValue());
        ticket1.setRemainingQuota(ticket1.getRemainingQuota() - 10);
        
        // Mock save behavior
        when(ticketRepository.save(ticket1)).thenReturn(ticket1);
        when(ticketRepository.findById(ticket1.getId())).thenReturn(Optional.of(ticket1));
        
        Ticket result = ticketRepository.save(ticket1);
        Optional<Ticket> findResultOptional = ticketRepository.findById(ticket1.getId());
        
        assertTrue(findResultOptional.isPresent());
        Ticket findResult = findResultOptional.get();
        
        assertEquals(ticket1.getId(), result.getId());
        assertEquals(ticket1.getId(), findResult.getId());
        assertEquals(TicketStatus.PURCHASED.getValue(), findResult.getStatus());
    }
    
    @Test
    void testFindByIdIfIdFound() {
        when(ticketRepository.findById(ticket1.getId())).thenReturn(Optional.of(ticket1));
        
        Optional<Ticket> findResultOptional = ticketRepository.findById(ticket1.getId());
        
        assertTrue(findResultOptional.isPresent());
        assertEquals(ticket1.getId(), findResultOptional.get().getId());
    }
    
    @Test
    void testFindByIdIfIdNotFound() {
        when(ticketRepository.findById("non-existent-id")).thenReturn(Optional.empty());
        
        Optional<Ticket> findResultOptional = ticketRepository.findById("non-existent-id");
        assertFalse(findResultOptional.isPresent());
    }
    
    @Test
    void testFindAllByEventId() {
        List<Ticket> eventTickets = List.of(ticket1, ticket2);
        when(ticketRepository.findAllByEventId(eventId1)).thenReturn(eventTickets);
        
        List<Ticket> result = ticketRepository.findAllByEventId(eventId1);
        assertEquals(2, result.size());
        
        for (Ticket ticket : result) {
            assertEquals(eventId1, ticket.getEventId());
        }
    }
    
    @Test
    void testFindAllByType() {
        List<Ticket> regularTickets = List.of(ticket1, ticket3);
        when(ticketRepository.findAllByType("REGULAR")).thenReturn(regularTickets);
        
        List<Ticket> result = ticketRepository.findAllByType("REGULAR");
        assertEquals(2, result.size());
        
        // Verify the tickets are of the correct type
        for (Ticket ticket : result) {
            assertEquals("REGULAR", ticket.getType());
        }
    }
    
    @Test
    void testFindAllByStatus() {
        // All tickets should have AVAILABLE status initially
        List<Ticket> availableTickets = List.of(ticket1, ticket2, ticket3);
        when(ticketRepository.findAllByStatus(TicketStatus.AVAILABLE.getValue())).thenReturn(availableTickets);
        
        List<Ticket> result = ticketRepository.findAllByStatus(TicketStatus.AVAILABLE.getValue());
        assertEquals(3, result.size());
        
        // Test with purchased status
        ticket1.setStatus(TicketStatus.PURCHASED.getValue());
        List<Ticket> purchasedTickets = List.of(ticket1);
        when(ticketRepository.findAllByStatus(TicketStatus.PURCHASED.getValue())).thenReturn(purchasedTickets);
        
        List<Ticket> purchasedResult = ticketRepository.findAllByStatus(TicketStatus.PURCHASED.getValue());
        assertEquals(1, purchasedResult.size());
        assertEquals(ticket1.getId(), purchasedResult.get(0).getId());
    }
    
    @Test
    void testFindAllAvailableTickets() {
        // Mock scenario where only ticket3 is available
        List<Ticket> availableTickets = List.of(ticket3);
        when(ticketRepository.findAllAvailable(currentTime + 1000)).thenReturn(availableTickets);
        
        List<Ticket> result = ticketRepository.findAllAvailable(currentTime + 1000);
        assertEquals(1, result.size());
        assertEquals(ticket3.getId(), result.get(0).getId());
    }
    
    @Test
    void testFindAll() {
        List<Ticket> allTickets = List.of(ticket1, ticket2, ticket3);
        when(ticketRepository.findAll()).thenReturn(allTickets);
        
        List<Ticket> result = ticketRepository.findAll();
        assertEquals(3, result.size());
    }
    
    @Test
    void testDeleteById() {
        doNothing().when(ticketRepository).deleteById(ticket1.getId());
        when(ticketRepository.findById(ticket1.getId())).thenReturn(Optional.empty());
        
        ticketRepository.deleteById(ticket1.getId());
        
        verify(ticketRepository).deleteById(ticket1.getId());
        assertFalse(ticketRepository.findById(ticket1.getId()).isPresent());
    }
    
    @Test
    void testFindAllByUserId() {
        String userId = "user-123";
        ticket1.setUserId(userId);
        List<Ticket> userTickets = List.of(ticket1);
        when(ticketRepository.findAllByUserId(userId)).thenReturn(userTickets);
        
        List<Ticket> result = ticketRepository.findAllByUserId(userId);
        assertEquals(1, result.size());
        assertEquals(userId, result.get(0).getUserId());
    }
}
