package id.ac.ui.cs.advprog.sistemticket.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import id.ac.ui.cs.advprog.sistemticket.enums.TicketStatus;
import id.ac.ui.cs.advprog.sistemticket.event.TicketPurchasedEvent;
import id.ac.ui.cs.advprog.sistemticket.model.Ticket;
import id.ac.ui.cs.advprog.sistemticket.repository.TicketRepository;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.ArgumentMatchers.argThat;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {
    
    @InjectMocks
    TicketServiceImpl ticketService;
    
    @Mock
    TicketRepository ticketRepository;
    
    @Mock
    private ApplicationEventPublisher eventPublisher;
    
    @Captor
    private ArgumentCaptor<TicketPurchasedEvent> eventCaptor;
    
    private List<Ticket> tickets;
    private String eventId1;
    private String eventId2;
    private Long currentTime;
    
    @BeforeEach
    void setUp() {
        currentTime = System.currentTimeMillis();
        
        // Set up test data
        eventId1 = "eb558e9f-1c39-460e-8860-71af6af63bd6";
        eventId2 = "7f9e15bb-4b15-42f4-aebc-c3af385fb078";
        
        tickets = new ArrayList<>();
        
        // Create regular ticket for event 1
        Ticket ticket1 = new Ticket(
            eventId1,
            "REGULAR",
            150.0,
            100,
            "Regular ticket for concert",
            currentTime,
            currentTime + 86400000 // 1 day later
        );
        tickets.add(ticket1);
        
        // Create VIP ticket for event 1
        Ticket ticket2 = new Ticket(
            eventId1,
            "VIP",
            300.0,
            50,
            "VIP ticket for concert",
            currentTime,
            currentTime + 86400000 // 1 day later
        );
        tickets.add(ticket2);
        
        // Create regular ticket for event 2
        Ticket ticket3 = new Ticket(
            eventId2,
            "REGULAR",
            200.0,
            200,
            "Regular ticket for exhibition",
            currentTime,
            currentTime + 86400000 // 1 day later
        );
        tickets.add(ticket3);
    }
    
    // Core CRUD operations
    @Test
    void testCreateTicket() {
        Ticket ticket = tickets.get(0);
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.empty());
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        
        Ticket result = ticketService.createTicket(ticket);
        
        verify(ticketRepository, times(1)).save(ticket);
        assertEquals(ticket.getId(), result.getId());
    }
    
    @Test
    void testCreateTicketIfAlreadyExists() {
        Ticket ticket = tickets.get(0);
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        
        assertNull(ticketService.createTicket(ticket));
        verify(ticketRepository, times(0)).save(ticket);
    }
    
    @Test
    void testFindTicketById() {
        Ticket ticket = tickets.get(0);
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        
        Ticket result = ticketService.findById(ticket.getId());
        
        assertEquals(ticket.getId(), result.getId());
        assertEquals(ticket.getEventId(), result.getEventId());
        assertEquals(ticket.getType(), result.getType());
    }
    
    @Test
    void testFindTicketByIdNotFound() {
        when(ticketRepository.findById("non-existent-id")).thenReturn(Optional.empty());
        
        assertNull(ticketService.findById("non-existent-id"));
    }
    
    @Test
    void testFindAllTickets() {
        doReturn(tickets).when(ticketRepository).findAll();
        
        List<Ticket> result = ticketService.findAll();
        
        assertEquals(tickets.size(), result.size());
        assertEquals(tickets.get(0).getId(), result.get(0).getId());
    }
    
    @Test
    void testFindAllTicketsByEventId() {
        List<Ticket> eventTickets = new ArrayList<>();
        eventTickets.add(tickets.get(0));
        eventTickets.add(tickets.get(1));
        
        doReturn(eventTickets).when(ticketRepository).findAllByEventId(eventId1);
        
        List<Ticket> result = ticketService.findAllByEventId(eventId1);
        
        assertEquals(2, result.size());
        for (Ticket ticket : result) {
            assertEquals(eventId1, ticket.getEventId());
        }
    }
    
    @Test
    void testFindAllTicketsByType() {
        List<Ticket> regularTickets = new ArrayList<>();
        regularTickets.add(tickets.get(0));
        regularTickets.add(tickets.get(2));
        
        doReturn(regularTickets).when(ticketRepository).findAllByType("REGULAR");
        
        List<Ticket> result = ticketService.findAllByType("REGULAR");
        
        assertEquals(2, result.size());
        for (Ticket ticket : result) {
            assertEquals("REGULAR", ticket.getType());
        }
    }
    
    @Test
    void testFindAllAvailableTickets() {
        List<Ticket> availableTickets = new ArrayList<>();
        availableTickets.add(tickets.get(2));
        
        Long testTime = currentTime + 1000;
        doReturn(availableTickets).when(ticketRepository).findAllAvailable(testTime);
        
        List<Ticket> result = ticketService.findAllAvailable(testTime);
        
        assertEquals(1, result.size());
        assertEquals(tickets.get(2).getId(), result.get(0).getId());
    }
    
    @Test
    void testUpdateTicket() {
        Ticket originalTicket = tickets.get(0);
        Ticket updatedTicket = new Ticket(
            originalTicket.getEventId(),
            originalTicket.getType(),
            200.0, // Updated price
            originalTicket.getQuota(),
            "Updated description",
            originalTicket.getSaleStart(),
            originalTicket.getSaleEnd()
        );
        updatedTicket.setId(originalTicket.getId()); // Ensure same ID
        
        when(ticketRepository.findById(originalTicket.getId())).thenReturn(Optional.of(originalTicket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(updatedTicket);
        
        Ticket result = ticketService.updateTicket(updatedTicket);
        
        assertEquals(updatedTicket.getId(), result.getId());
        assertEquals(200.0, result.getPrice());
        assertEquals("Updated description", result.getDescription());
    }
    
    @Test
    void testUpdateTicketNotFound() {
        Ticket ticket = tickets.get(0);
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.empty());
        
        assertThrows(NoSuchElementException.class, () -> ticketService.updateTicket(ticket));
        verify(ticketRepository, times(0)).save(any(Ticket.class));
    }
    
    @Test
    void testUpdateTicketStatus() {
        Ticket ticket = tickets.get(0);
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket savedTicket = invocation.getArgument(0);
            return savedTicket;
        });
        
        Ticket result = ticketService.updateStatus(ticket.getId(), TicketStatus.PURCHASED.getValue());
        
        assertEquals(TicketStatus.PURCHASED.getValue(), result.getStatus());
    }
    
    @Test
    void testUpdateTicketStatusInvalidStatus() {
        Ticket ticket = tickets.get(0);
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        
        assertThrows(IllegalArgumentException.class, () -> ticketService.updateStatus(ticket.getId(), "INVALID_STATUS"));
        verify(ticketRepository, times(0)).save(any(Ticket.class));
    }
    
    @Test
    void testUpdateTicketStatusNotFound() {
        when(ticketRepository.findById("non-existent-id")).thenReturn(Optional.empty());
        
        assertThrows(NoSuchElementException.class, () -> ticketService.updateStatus("non-existent-id", TicketStatus.PURCHASED.getValue()));
        verify(ticketRepository, times(0)).save(any(Ticket.class));
    }
    
    @Test
    void testPurchaseTicket() {
        Ticket ticket = tickets.get(0);
        int initialQuota = ticket.getRemainingQuota();
        int purchaseAmount = 5;
        
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        Ticket result = ticketService.purchaseTicket(ticket.getId(), purchaseAmount, currentTime + 1000);
        
        assertEquals(initialQuota - purchaseAmount, result.getRemainingQuota());
        verify(eventPublisher).publishEvent(any(TicketPurchasedEvent.class));
    }
    
    @Test
    void testPurchaseTicketTooMany() {
        Ticket ticket = tickets.get(0);
        int purchaseAmount = ticket.getRemainingQuota() + 1; // One more than available
        
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        
        assertThrows(IllegalArgumentException.class, () -> ticketService.purchaseTicket(ticket.getId(), purchaseAmount, currentTime + 1000));
        verify(ticketRepository, times(0)).save(any(Ticket.class));
    }
    
    @Test
    void testPurchaseTicketOutsideSalePeriod() {
        Ticket ticket = tickets.get(0);
        Long invalidTime = ticket.getSaleEnd() + 1000; // After sale period
        
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        
        assertThrows(IllegalArgumentException.class, () -> ticketService.purchaseTicket(ticket.getId(), 1, invalidTime));
        verify(ticketRepository, times(0)).save(any(Ticket.class));
    }
    
    @Test
    void testPurchaseTicketNotAvailable() {
        Ticket ticket = tickets.get(0);
        ticket.setStatus(TicketStatus.PURCHASED.getValue()); // Set to non-available status
        
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        
        assertThrows(IllegalArgumentException.class, () -> ticketService.purchaseTicket(ticket.getId(), 1, currentTime + 1000));
        verify(ticketRepository, times(0)).save(any(Ticket.class));
    }
    
    @Test
    void testDeleteTicket() {
        String ticketId = tickets.get(0).getId();
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(tickets.get(0)));
        
        ticketService.deleteTicket(ticketId);
        
        verify(ticketRepository, times(1)).deleteById(ticketId);
    }
    
    @Test
    void testDeleteTicketNotFound() {
        String ticketId = "non-existent-id";
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());
        
        assertThrows(NoSuchElementException.class, () -> ticketService.deleteTicket(ticketId));
        verify(ticketRepository, times(0)).deleteById(ticketId);
    }
    
    @Test
    void testPurchaseTicketPublishesEvent() {
        // Setup
        Ticket ticket = tickets.get(0);
        int purchaseAmount = 5;
        
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        
        // Execute
        ticketService.purchaseTicket(ticket.getId(), purchaseAmount, currentTime + 1000);
        
        // Use ArgumentCaptor to capture and verify the event
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        
        // Verify the captured event
        TicketPurchasedEvent capturedEvent = eventCaptor.getValue();
        assertNotNull(capturedEvent);
        assertEquals(ticket, capturedEvent.getTicket());
        assertEquals(purchaseAmount, capturedEvent.getAmount());
    }
    
    @Test
    void testProcessTicketExpiration() throws ExecutionException, InterruptedException {
        // Setup
        Ticket ticket = tickets.get(0);
        String ticketId = ticket.getId();

        // Set the sale end time to be in the past
        ticket.setSaleEnd(System.currentTimeMillis() - 10000); // 10 seconds in the past

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        // Execute
        CompletableFuture<Void> future = ticketService.processTicketExpiration(ticketId);

        // Wait for completion and verify
        future.get(); // This will wait for the async operation to complete

        // Verify the ticket status was updated to EXPIRED
        verify(ticketRepository).save(argThat(savedTicket ->
            savedTicket.getStatus().equals(TicketStatus.EXPIRED.getValue())));
    }

    @Test
    void testProcessTicketExpirationNoTicket() throws ExecutionException, InterruptedException {
        // Setup
        String nonExistentId = "non-existent";
        when(ticketRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Execute
        CompletableFuture<Void> future = ticketService.processTicketExpiration(nonExistentId);

        // Wait for completion and verify
        future.get(); // This will wait for the async operation to complete

        verify(ticketRepository).findById(nonExistentId);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }
}

