package id.ac.ui.cs.advprog.sistemticket.service;

import id.ac.ui.cs.advprog.sistemticket.dto.UpdateTicketDTO;
import id.ac.ui.cs.advprog.sistemticket.enums.TicketStatus;
import id.ac.ui.cs.advprog.sistemticket.model.Ticket;
import id.ac.ui.cs.advprog.sistemticket.model.TicketBuilder;
import id.ac.ui.cs.advprog.sistemticket.repository.TicketRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private UUID ticketId;
    private UUID eventId;
    private Ticket ticket;
    private TicketBuilder ticketBuilder;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        ticketId = UUID.randomUUID();
        eventId = UUID.randomUUID();
        now = LocalDateTime.now();

        // Set up a sample ticket
        ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setType("VIP");
        ticket.setPrice(750000.0);
        ticket.setQuota(100);
        ticket.setDescription("VIP access with exclusive merchandise");
        ticket.setSalesStart(now.minusDays(1));
        ticket.setSalesEnd(now.plusDays(30));
        ticket.setEventId(eventId);
        ticket.setStatus(TicketStatus.AVAILABLE);

        // Set up a ticket builder
        ticketBuilder = mock(TicketBuilder.class);
        when(ticketBuilder.isReady()).thenReturn(true);
        when(ticketBuilder.build()).thenReturn(ticket);
    }

    @Test
    void testCreateTicket_Success() {
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        Ticket result = ticketService.createTicket(ticketBuilder);

        assertNotNull(result);
        assertEquals(ticketId, result.getId());
        assertEquals(TicketStatus.AVAILABLE, result.getStatus());
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void testCreateTicket_BuilderNotReady() {
        when(ticketBuilder.isReady()).thenReturn(false);

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            ticketService.createTicket(ticketBuilder);
        });

        assertEquals("Ticket builder is not ready for building", exception.getMessage());
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void testGetAllTickets() {
        List<Ticket> tickets = Arrays.asList(ticket);
        when(ticketRepository.findAll()).thenReturn(tickets);

        List<Ticket> result = ticketService.getAllTickets();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(ticketRepository).findAll();
    }

    @Test
    void testGetTicketById_Success() {
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        Ticket result = ticketService.getTicketById(ticketId);

        assertNotNull(result);
        assertEquals(ticketId, result.getId());
        verify(ticketRepository).findById(ticketId);
    }

    @Test
    void testGetTicketById_NotFound() {
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class, () -> {
            ticketService.getTicketById(ticketId);
        });

        assertTrue(exception.getMessage().contains("Ticket not found"));
        verify(ticketRepository).findById(ticketId);
    }

    @Test
    void testGetTicketsByEventId() {
        List<Ticket> tickets = Arrays.asList(ticket);
        when(ticketRepository.findByEventId(eventId)).thenReturn(tickets);

        List<Ticket> result = ticketService.getTicketsByEventId(eventId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(ticketRepository).findByEventId(eventId);
    }

    @Test
    void testGetAvailableTicketsForEvent() {
        List<Ticket> tickets = Arrays.asList(ticket);
        when(ticketRepository.findByEventIdAndStatus(eventId, TicketStatus.AVAILABLE)).thenReturn(tickets);

        List<Ticket> result = ticketService.getAvailableTicketsForEvent(eventId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(ticketRepository).findByEventIdAndStatus(eventId, TicketStatus.AVAILABLE);
    }

    @Test
    void testDeleteTicket_Success() {
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        ticketService.deleteTicket(ticketId);

        verify(ticketRepository).deleteById(ticketId);
    }

    @Test
    void testDeleteTicket_CannotDelete() {
        ticket.setStatus(TicketStatus.PURCHASED);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            ticketService.deleteTicket(ticketId);
        });

        assertEquals("Cannot delete ticket that has been purchased", exception.getMessage());
        verify(ticketRepository, never()).deleteById(any());
    }

    @Test
    void testPurchaseTicket_Success() {
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        Ticket result = ticketService.purchaseTicket(ticketId, 50);

        assertEquals(50, result.getQuota());
        assertEquals(TicketStatus.AVAILABLE, result.getStatus());
        verify(ticketRepository).save(ticket);
    }

    @Test
    void testPurchaseTicket_AllTickets() {
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        Ticket result = ticketService.purchaseTicket(ticketId, 100);

        assertEquals(0, result.getQuota());
        assertEquals(TicketStatus.SOLD_OUT, result.getStatus());
        verify(ticketRepository).save(ticket);
    }

    @Test
    void testPurchaseTicket_NotAvailable() {
        ticket.setStatus(TicketStatus.SOLD_OUT);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            ticketService.purchaseTicket(ticketId, 10);
        });

        assertEquals("Ticket is not available for purchase", exception.getMessage());
    }

    @Test
    void testPurchaseTicket_NotEnoughQuota() {
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            ticketService.purchaseTicket(ticketId, 200);
        });

        assertEquals("Not enough tickets available", exception.getMessage());
    }

    @Test
    void testPurchaseTicket_SalesPeriodNotActive() {
        ticket.setSalesStart(now.plusDays(1));
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            ticketService.purchaseTicket(ticketId, 10);
        });

        assertEquals("Ticket sales period is not active", exception.getMessage());
    }

    @Test
    void testValidateTicket_Success() {
        ticket.setStatus(TicketStatus.PURCHASED);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        Ticket result = ticketService.validateTicket(ticketId);

        assertEquals(TicketStatus.USED, result.getStatus());
        verify(ticketRepository).save(ticket);
    }

    @Test
    void testValidateTicket_CannotValidate() {
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            ticketService.validateTicket(ticketId);
        });

        assertEquals("Ticket cannot be validated", exception.getMessage());
    }

    @Test
    void testFindTicketsByType() {
        List<Ticket> tickets = Arrays.asList(ticket);
        when(ticketRepository.findByType("VIP")).thenReturn(tickets);

        List<Ticket> result = ticketService.findTicketsByType("VIP");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(ticketRepository).findByType("VIP");
    }

    @Test
    void testFindActiveTickets() {
        List<Ticket> tickets = Arrays.asList(ticket);
        when(ticketRepository.findBySalesStartBeforeAndSalesEndAfter(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(tickets);

        List<Ticket> result = ticketService.findActiveTickets();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(ticketRepository).findBySalesStartBeforeAndSalesEndAfter(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testFindTicketsByPriceRange() {
        List<Ticket> tickets = Arrays.asList(ticket);
        when(ticketRepository.findByPriceBetween(500000.0, 800000.0)).thenReturn(tickets);

        List<Ticket> result = ticketService.findTicketsByPriceRange(500000.0, 800000.0);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(ticketRepository).findByPriceBetween(500000.0, 800000.0);
    }

    @Test
    void testFindTicketsWithAvailableQuota() {
        List<Ticket> tickets = Arrays.asList(ticket);
        when(ticketRepository.findByQuotaGreaterThan(9)).thenReturn(tickets);

        List<Ticket> result = ticketService.findTicketsWithAvailableQuota(10);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(ticketRepository).findByQuotaGreaterThan(9);
    }

    @Test
    void testUpdateTicket_Success() {
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        Ticket result = ticketService.updateTicket(ticketId, ticketBuilder);

        assertNotNull(result);
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void testUpdateTicket_CannotUpdate() {
        ticket.setStatus(TicketStatus.PURCHASED);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            ticketService.updateTicket(ticketId, ticketBuilder);
        });

        assertEquals("Cannot update ticket that has been purchased or used", exception.getMessage());
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void testAreTicketsAvailableForEvent_Available() {
        List<Ticket> tickets = Arrays.asList(ticket);
        when(ticketRepository.findByEventIdAndStatus(eventId, TicketStatus.AVAILABLE)).thenReturn(tickets);

        boolean result = ticketService.areTicketsAvailableForEvent(eventId);

        assertTrue(result);
    }

    @Test
    void testAreTicketsAvailableForEvent_NotAvailable() {
        when(ticketRepository.findByEventIdAndStatus(eventId, TicketStatus.AVAILABLE)).thenReturn(List.of());

        boolean result = ticketService.areTicketsAvailableForEvent(eventId);

        assertFalse(result);
    }

    // Add these tests to your TicketServiceTest.java file

    @Test
    void testMarkTicketAsSoldOut() {
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        Ticket result = ticketService.markTicketAsSoldOut(ticketId);

        assertEquals(TicketStatus.SOLD_OUT, result.getStatus());
        verify(ticketRepository).save(ticket);
    }

    @Test
    void testMarkTicketAsAvailable() {
        ticket.setStatus(TicketStatus.SOLD_OUT);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        Ticket result = ticketService.markTicketAsAvailable(ticketId);

        assertEquals(TicketStatus.AVAILABLE, result.getStatus());
        verify(ticketRepository).save(ticket);
    }

    @Test
    void testMarkTicketAsAvailable_ZeroQuota() {
        ticket.setStatus(TicketStatus.SOLD_OUT);
        ticket.setQuota(0);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            ticketService.markTicketAsAvailable(ticketId);
        });

        assertEquals("Cannot mark ticket as available with zero quota", exception.getMessage());
    }

    @Test
    void testMarkTicketAsPurchased() {
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        Ticket result = ticketService.markTicketAsPurchased(ticketId);

        assertEquals(TicketStatus.PURCHASED, result.getStatus());
        verify(ticketRepository).save(ticket);
    }

    @Test
    void testUpdateTicketWithDTO() {
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        UpdateTicketDTO dto = new UpdateTicketDTO();
        dto.setType("Regular");
        dto.setPrice(500000.0);
        dto.setQuota(200);
        dto.setDescription("Regular access");
        dto.setSalesStart(now);
        dto.setSalesEnd(now.plusDays(15));
        dto.setEventId(eventId);

        Ticket result = ticketService.updateTicket(ticketId, dto);

        assertEquals("Regular", result.getType());
        assertEquals(500000.0, result.getPrice());
        assertEquals(200, result.getQuota());
        verify(ticketRepository).save(ticket);
    }

    @Test
    void testGetTicketByIdOptional() {
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        Optional<Ticket> result = ticketService.getTicketByIdOptional(ticketId);

        assertTrue(result.isPresent());
        assertEquals(ticketId, result.get().getId());
    }
}