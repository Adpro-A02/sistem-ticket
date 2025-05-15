package id.ac.ui.cs.advprog.sistemticket.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TicketModelTest {

    private Ticket ticket;
    private final String type = "VIP";
    private final double price = 750000.0;
    private final int quota = 100;
    private final String description = "VIP access with exclusive merchandise";
    private final LocalDateTime salesStart = LocalDateTime.of(2025, 5, 1, 10, 0);
    private final LocalDateTime salesEnd = LocalDateTime.of(2025, 6, 1, 23, 59);
    private final UUID eventId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        ticket = new Ticket();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(ticket, "Ticket should not be null");
        assertEquals(TicketStatus.AVAILABLE, ticket.getStatus(), "Default status should be AVAILABLE");
    }

    @Test
    void testParameterizedConstructor() {
        Ticket paramTicket = new Ticket(type, price, quota, description, salesStart, salesEnd, eventId);

        assertEquals(type, paramTicket.getType(), "Type should match");
        assertEquals(price, paramTicket.getPrice(), "Price should match");
        assertEquals(quota, paramTicket.getQuota(), "Quota should match");
        assertEquals(description, paramTicket.getDescription(), "Description should match");
        assertEquals(salesStart, paramTicket.getSalesStart(), "Sales start should match");
        assertEquals(salesEnd, paramTicket.getSalesEnd(), "Sales end should match");
        assertEquals(eventId, paramTicket.getEventId(), "Event ID should match");
        assertEquals(TicketStatus.AVAILABLE, paramTicket.getStatus(), "Status should be AVAILABLE");
    }

    @Test
    void testSetAndGetId() {
        UUID id = UUID.randomUUID();
        ticket.setId(id);
        assertEquals(id, ticket.getId(), "ID should match");
    }

    @Test
    void testSetAndGetType() {
        ticket.setType(type);
        assertEquals(type, ticket.getType(), "Type should match");
    }

    @Test
    void testSetAndGetPrice() {
        ticket.setPrice(price);
        assertEquals(price, ticket.getPrice(), "Price should match");
    }

    @Test
    void testSetAndGetQuota() {
        ticket.setQuota(quota);
        assertEquals(quota, ticket.getQuota(), "Quota should match");
    }

    @Test
    void testSetAndGetDescription() {
        ticket.setDescription(description);
        assertEquals(description, ticket.getDescription(), "Description should match");
    }

    @Test
    void testSetAndGetSalesStart() {
        ticket.setSalesStart(salesStart);
        assertEquals(salesStart, ticket.getSalesStart(), "Sales start should match");
    }

    @Test
    void testSetAndGetSalesEnd() {
        ticket.setSalesEnd(salesEnd);
        assertEquals(salesEnd, ticket.getSalesEnd(), "Sales end should match");
    }

    @Test
    void testSetAndGetEventId() {
        ticket.setEventId(eventId);
        assertEquals(eventId, ticket.getEventId(), "Event ID should match");
    }

    @Test
    void testSetAndGetStatus() {
        ticket.setStatus(TicketStatus.PURCHASED);
        assertEquals(TicketStatus.PURCHASED, ticket.getStatus(), "Status should match");

        ticket.setStatus(TicketStatus.USED);
        assertEquals(TicketStatus.USED, ticket.getStatus(), "Status should match after update");

        ticket.setStatus(TicketStatus.EXPIRED);
        assertEquals(TicketStatus.EXPIRED, ticket.getStatus(), "Status should match after update");
    }

    @Test
    void testReduceQuota() {
        ticket.setQuota(10);
        ticket.reduceQuota(3);
        assertEquals(7, ticket.getQuota(), "Quota should be reduced by 3");
    }

    @Test
    void testReduceQuotaBelowZero() {
        ticket.setQuota(5);
        assertThrows(IllegalArgumentException.class, () -> ticket.reduceQuota(6),
                "Should throw exception when reducing quota below zero");
    }

    @Test
    void testIsAvailableForPurchase() {
        ticket.setQuota(10);
        ticket.setStatus(TicketStatus.AVAILABLE);
        ticket.setSalesStart(LocalDateTime.now().minusDays(1));
        ticket.setSalesEnd(LocalDateTime.now().plusDays(1));

        assertTrue(ticket.isAvailableForPurchase(), "Ticket should be available for purchase");

        // Test with zero quota
        ticket.setQuota(0);
        assertFalse(ticket.isAvailableForPurchase(), "Ticket with zero quota should not be available");

        // Test with non-AVAILABLE status
        ticket.setQuota(10);
        ticket.setStatus(TicketStatus.EXPIRED);
        assertFalse(ticket.isAvailableForPurchase(), "Expired ticket should not be available");

        // Test with sales period ended
        ticket.setStatus(TicketStatus.AVAILABLE);
        ticket.setSalesEnd(LocalDateTime.now().minusDays(1));
        assertFalse(ticket.isAvailableForPurchase(), "Ticket with past sales end date should not be available");

        // Test with sales period not started yet
        ticket.setSalesStart(LocalDateTime.now().plusDays(1));
        ticket.setSalesEnd(LocalDateTime.now().plusDays(2));
        assertFalse(ticket.isAvailableForPurchase(), "Ticket with future sales start date should not be available");
    }
}