package id.ac.ui.cs.advprog.sistemticket.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.argThat;

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
import org.springframework.test.util.ReflectionTestUtils;

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
    
    @Mock
    private io.micrometer.core.instrument.Counter ticketCreatedCounter;
    
    @Mock
    private io.micrometer.core.instrument.Counter ticketPurchasedCounter;
    
    @Mock
    private io.micrometer.core.instrument.Timer ticketPurchaseTimer;
    
    @Mock
    private io.micrometer.core.instrument.Counter ticketStatusUpdateCounter;
    
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
        // Create a fresh ticket with known quota and ensure it's properly initialized
        Ticket ticket = new Ticket(
            eventId1,
            "REGULAR",
            150.0,
            5, // Small total quota
            "Regular ticket for concert",
            currentTime,
            currentTime + 86400000
        );
        // Manually set the remaining quota to ensure we know the exact state
        ticket.setRemainingQuota(3); // Only 3 tickets left
        
        int purchaseAmount = 5; // Trying to buy more than available
        
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        
        // The exception should be thrown due to insufficient quota
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            ticketService.purchaseTicket(ticket.getId(), purchaseAmount, currentTime + 1000));
        
        // Verify the exception message contains the expected text
        assertTrue(exception.getMessage().contains("Cannot purchase tickets:") || 
                  exception.getMessage().contains("Not enough tickets available"));
        verify(ticketRepository, never()).save(any(Ticket.class));
    }
    
    @Test
    void testPurchaseTicketOutsideSalePeriod() {
        Ticket ticket = new Ticket(
            eventId1,
            "REGULAR",
            150.0,
            100,
            "Regular ticket for concert",
            currentTime + 5000, // Sale starts in the future
            currentTime + 86400000 // Sale ends in the future
        );
        Long invalidTime = currentTime + 1000; // Before sale starts
        
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        
        // This should fail because the purchase time is before the sale start time
        assertThrows(IllegalArgumentException.class, () -> 
            ticketService.purchaseTicket(ticket.getId(), 1, invalidTime));
        verify(ticketRepository, never()).save(any(Ticket.class));
    }
    
    @Test
    void testPurchaseTicketNotAvailable() {
        // Create a ticket and explicitly set status to PURCHASED to make it unavailable
        Ticket ticket = new Ticket(
            eventId1,
            "REGULAR",
            150.0,
            100,
            "Regular ticket for concert",
            currentTime,
            currentTime + 86400000
        );
        // Set status to PURCHASED to make it unavailable for purchase
        ticket.setStatus(TicketStatus.PURCHASED.getValue());
        
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        
        // This should fail because ticket status is not AVAILABLE
        assertThrows(IllegalArgumentException.class, () -> 
            ticketService.purchaseTicket(ticket.getId(), 1, currentTime + 1000));
        verify(ticketRepository, never()).save(any(Ticket.class));
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
        // Setup - Create a fresh ticket with known state
        Ticket ticket = new Ticket(
            eventId1,
            "REGULAR",
            150.0,
            100,
            "Regular ticket for concert",
            currentTime - 1000, // Sale started 1 second ago
            currentTime + 86400000 // Sale ends much later
        );
        // Explicitly set all required fields to ensure the ticket is available
        ticket.setRemainingQuota(100); // Ensure enough quota
        ticket.setStatus(TicketStatus.AVAILABLE.getValue()); // Ensure it's available
        
        int purchaseAmount = 5;
        Long purchaseTime = currentTime + 500; // Purchase time within sale period
        
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Execute - Purchase within the sale period
        Ticket result = ticketService.purchaseTicket(ticket.getId(), purchaseAmount, purchaseTime);
        
        // Verify the purchase was successful
        assertNotNull(result);
        assertEquals(95, result.getRemainingQuota()); // 100 - 5 = 95
        
        // Use ArgumentCaptor to capture and verify the event
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        
        // Verify the captured event
        TicketPurchasedEvent capturedEvent = eventCaptor.getValue();
        assertNotNull(capturedEvent);
        assertEquals(ticket.getId(), capturedEvent.getTicket().getId());
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
    
    @Test
    void testCreateTicketWithNullCounter() {
        // Test the null check branch for ticketCreatedCounter
        Ticket ticket = tickets.get(0);
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.empty());
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        
        // Set counter to null to test the null check
        ReflectionTestUtils.setField(ticketService, "ticketCreatedCounter", null);
        
        Ticket result = ticketService.createTicket(ticket);
        
        verify(ticketRepository, times(1)).save(ticket);
        assertEquals(ticket.getId(), result.getId());
    }
    
    @Test
    void testCreateTicketWithCounter() {
        // Test the branch where counter is not null
        Ticket ticket = tickets.get(0);
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.empty());
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        
        // Ensure counter is set
        ReflectionTestUtils.setField(ticketService, "ticketCreatedCounter", ticketCreatedCounter);
        
        Ticket result = ticketService.createTicket(ticket);
        
        verify(ticketRepository, times(1)).save(ticket);
        verify(ticketCreatedCounter, times(1)).increment();
        assertEquals(ticket.getId(), result.getId());
    }
    
    @Test
    void testUpdateStatusWithNullCounter() {
        // Test the null check branch for ticketStatusUpdateCounter
        Ticket ticket = tickets.get(0);
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Set counter to null
        ReflectionTestUtils.setField(ticketService, "ticketStatusUpdateCounter", null);
        
        Ticket result = ticketService.updateStatus(ticket.getId(), TicketStatus.PURCHASED.getValue());
        
        assertEquals(TicketStatus.PURCHASED.getValue(), result.getStatus());
    }
    
    @Test
    void testUpdateStatusWithCounter() {
        // Test the branch where counter is not null
        Ticket ticket = tickets.get(0);
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Ensure counter is set
        ReflectionTestUtils.setField(ticketService, "ticketStatusUpdateCounter", ticketStatusUpdateCounter);
        
        Ticket result = ticketService.updateStatus(ticket.getId(), TicketStatus.PURCHASED.getValue());
        
        assertEquals(TicketStatus.PURCHASED.getValue(), result.getStatus());
        verify(ticketStatusUpdateCounter, times(1)).increment();
    }
    
    @Test
    void testPurchaseTicketWithNullTimer() {
        // Test the branch where timer is null
        Ticket ticket = new Ticket(
            eventId1,
            "REGULAR",
            150.0,
            100, // Use explicit quota
            "Regular ticket for concert",
            currentTime,
            currentTime + 86400000
        );
        ticket.setRemainingQuota(100); // Set explicit remaining quota
        
        int purchaseAmount = 5;
        
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Set timer to null
        ReflectionTestUtils.setField(ticketService, "ticketPurchaseTimer", null);
        ReflectionTestUtils.setField(ticketService, "ticketPurchasedCounter", null);
        
        Ticket result = ticketService.purchaseTicket(ticket.getId(), purchaseAmount, currentTime + 1000);
        
        // The remaining quota should be initial quota (100) minus purchased amount (5) = 95
        assertEquals(95, result.getRemainingQuota());
        verify(eventPublisher).publishEvent(any(TicketPurchasedEvent.class));
    }
    
    @Test
    void testPurchaseTicketWithTimerAndNullCounter() {
        // Test the branch where timer is not null but counter is null
        Ticket ticket = new Ticket(
            eventId1,
            "REGULAR",
            150.0,
            100, // Use explicit quota
            "Regular ticket for concert",
            currentTime,
            currentTime + 86400000
        );
        ticket.setRemainingQuota(100); // Set explicit remaining quota
        
        int purchaseAmount = 5;
        
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Mock the timer to execute the supplier
        when(ticketPurchaseTimer.record(any(java.util.function.Supplier.class))).thenAnswer(invocation -> {
            java.util.function.Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });
        
        // Set timer but null counter
        ReflectionTestUtils.setField(ticketService, "ticketPurchaseTimer", ticketPurchaseTimer);
        ReflectionTestUtils.setField(ticketService, "ticketPurchasedCounter", null);
        
        Ticket result = ticketService.purchaseTicket(ticket.getId(), purchaseAmount, currentTime + 1000);
        
        // The remaining quota should be initial quota (100) minus purchased amount (5) = 95
        assertEquals(95, result.getRemainingQuota());
        verify(ticketPurchaseTimer, times(1)).record(any(java.util.function.Supplier.class));
    }
    
    @Test
    void testPurchaseTicketWithTimerAndCounter() {
        // Test the branch where both timer and counter are not null
        Ticket ticket = new Ticket(
            eventId1,
            "REGULAR",
            150.0,
            100, // Use explicit quota
            "Regular ticket for concert",
            currentTime,
            currentTime + 86400000
        );
        ticket.setRemainingQuota(100); // Set explicit remaining quota
        
        int purchaseAmount = 5;
        
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Mock the timer to execute the supplier
        when(ticketPurchaseTimer.record(any(java.util.function.Supplier.class))).thenAnswer(invocation -> {
            java.util.function.Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });
        
        // Set both timer and counter
        ReflectionTestUtils.setField(ticketService, "ticketPurchaseTimer", ticketPurchaseTimer);
        ReflectionTestUtils.setField(ticketService, "ticketPurchasedCounter", ticketPurchasedCounter);
        
        Ticket result = ticketService.purchaseTicket(ticket.getId(), purchaseAmount, currentTime + 1000);
        
        // The remaining quota should be initial quota (100) minus purchased amount (5) = 95
        assertEquals(95, result.getRemainingQuota());
        verify(ticketPurchaseTimer, times(1)).record(any(java.util.function.Supplier.class));
        verify(ticketPurchasedCounter, times(1)).increment(purchaseAmount);
    }
    
    @Test
    void testPurchaseTicketNotFound() {
        // Test the case where ticket is not found
        String nonExistentId = "non-existent-id";
        when(ticketRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        
        assertThrows(NoSuchElementException.class, 
            () -> ticketService.purchaseTicket(nonExistentId, 1, currentTime + 1000));
        
        verify(ticketRepository, never()).save(any(Ticket.class));
    }
    
    @Test
    void testProcessTicketExpirationNotExpired() throws ExecutionException, InterruptedException {
        // Test the branch where ticket is not expired
        Ticket ticket = tickets.get(0);
        String ticketId = ticket.getId();
        
        // Set sale end time to future (not expired)
        ticket.setSaleEnd(System.currentTimeMillis() + 10000);
        
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        
        CompletableFuture<Void> future = ticketService.processTicketExpiration(ticketId);
        future.get();
        
        verify(ticketRepository).findById(ticketId);
        // Verify save is not called since ticket is not expired
        verify(ticketRepository, never()).save(any(Ticket.class));
    }
    
    @Test
    void testPurchaseTicketZeroAmount() {
        // Test edge case with zero amount - this should fail at the service level validation
        Ticket ticket = tickets.get(0);
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        
        // The service validates amount > 0 before checking ticket availability
        assertThrows(IllegalArgumentException.class, 
            () -> ticketService.purchaseTicket(ticket.getId(), 0, currentTime + 1000));
        
        verify(ticketRepository, never()).save(any(Ticket.class));
    }
    
    @Test
    void testPurchaseTicketNegativeAmount() {
        // Test edge case with negative amount - this should fail at the service level validation
        Ticket ticket = tickets.get(0);
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        
        // The service validates amount > 0 before checking ticket availability
        assertThrows(IllegalArgumentException.class, 
            () -> ticketService.purchaseTicket(ticket.getId(), -1, currentTime + 1000));
        
        verify(ticketRepository, never()).save(any(Ticket.class));
    }
    
    @Test
    void testPurchaseTicketWithExpiredStatus() {
        // Test purchasing expired ticket - create with explicit EXPIRED status
        Ticket ticket = new Ticket(
            eventId1,
            "REGULAR",
            150.0,
            100,
            "Regular ticket for concert",
            currentTime,
            currentTime + 86400000,
            TicketStatus.EXPIRED.getValue() // Create with EXPIRED status
        );
        
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        
        // This should fail because ticket status is EXPIRED
        assertThrows(IllegalArgumentException.class, () -> 
            ticketService.purchaseTicket(ticket.getId(), 1, currentTime + 1000));
        verify(ticketRepository, never()).save(any(Ticket.class));
    }
    
    @Test
    void testPurchaseTicketWithUsedStatus() {
        // Test purchasing used ticket - create with explicit USED status
        Ticket ticket = new Ticket(
            eventId1,
            "REGULAR",
            150.0,
            100,
            "Regular ticket for concert",
            currentTime,
            currentTime + 86400000,
            TicketStatus.USED.getValue() // Create with USED status
        );
        
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        
        // This should fail because ticket status is USED
        assertThrows(IllegalArgumentException.class, () -> 
            ticketService.purchaseTicket(ticket.getId(), 1, currentTime + 1000));
        verify(ticketRepository, never()).save(any(Ticket.class));
    }
    
    @Test
    void testProcessTicketExpirationWithExactTime() throws ExecutionException, InterruptedException {
        // Test the exact boundary condition for expiration
        Ticket ticket = tickets.get(0);
        String ticketId = ticket.getId();
        
        // Set sale end time to be in the past (ensuring expiration condition is met)
        ticket.setSaleEnd(System.currentTimeMillis() - 1000); // 1 second in the past
        
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        
        CompletableFuture<Void> future = ticketService.processTicketExpiration(ticketId);
        future.get();
        
        verify(ticketRepository).findById(ticketId);
        // Should save since sale end time is in the past
        verify(ticketRepository).save(argThat(savedTicket ->
            savedTicket.getStatus().equals(TicketStatus.EXPIRED.getValue())));
    }
    
    @Test
    void testUpdateStatusAllValidStatuses() {
        // Test updating to all valid status values to ensure branch coverage
        Ticket ticket = tickets.get(0);
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Test each valid status
        for (TicketStatus status : TicketStatus.values()) {
            ReflectionTestUtils.setField(ticketService, "ticketStatusUpdateCounter", ticketStatusUpdateCounter);
            
            Ticket result = ticketService.updateStatus(ticket.getId(), status.getValue());
            assertEquals(status.getValue(), result.getStatus());
        }
        
        // Verify counter was incremented for each status update
        verify(ticketStatusUpdateCounter, times(TicketStatus.values().length)).increment();
    }
    
    @Test
    void testFindAllEmptyList() {
        // Test finding all tickets when repository returns empty list
        when(ticketRepository.findAll()).thenReturn(new ArrayList<>());
        
        List<Ticket> result = ticketService.findAll();
        
        assertTrue(result.isEmpty());
        verify(ticketRepository, times(1)).findAll();
    }
    
    @Test
    void testFindAllByEventIdEmptyList() {
        // Test finding tickets by event ID when none exist
        when(ticketRepository.findAllByEventId("non-existent-event")).thenReturn(new ArrayList<>());
        
        List<Ticket> result = ticketService.findAllByEventId("non-existent-event");
        
        assertTrue(result.isEmpty());
        verify(ticketRepository, times(1)).findAllByEventId("non-existent-event");
    }
    
    @Test
    void testFindAllByTypeEmptyList() {
        // Test finding tickets by type when none exist
        when(ticketRepository.findAllByType("NON_EXISTENT_TYPE")).thenReturn(new ArrayList<>());
        
        List<Ticket> result = ticketService.findAllByType("NON_EXISTENT_TYPE");
        
        assertTrue(result.isEmpty());
        verify(ticketRepository, times(1)).findAllByType("NON_EXISTENT_TYPE");
    }
    
    @Test
    void testFindAllAvailableEmptyList() {
        // Test finding available tickets when none exist
        Long testTime = currentTime + 1000;
        when(ticketRepository.findAllAvailable(testTime)).thenReturn(new ArrayList<>());
        
        List<Ticket> result = ticketService.findAllAvailable(testTime);
        
        assertTrue(result.isEmpty());
        verify(ticketRepository, times(1)).findAllAvailable(testTime);
    }
}

