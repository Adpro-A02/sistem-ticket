package id.ac.ui.cs.advprog.sistemticket.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TicketModelTest {

    private TicketBuilder ticket;
    private final String type = "VIP";
    private final double price = 750000.0;
    private final int quota = 100;
    private final String description = "VIP access with exclusive merchandise";
    private final LocalDateTime salesStart = LocalDateTime.of(2025, 5, 1, 10, 0);
    private final LocalDateTime salesEnd = LocalDateTime.of(2025, 6, 1, 23, 59);
    private final UUID eventId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        ticket = new TicketBuilder();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(ticket, "Ticket should not be null");
        assertEquals(BuilderStatus.INITIALIZED, ticket.getBuilderStatus(), "Default status should be INITIALIZED");
    }

    @Test
    void testParameterizedConstructor() {
        TicketBuilder paramTicket = new TicketBuilder(type, price, quota, description, salesStart, salesEnd, eventId);

        assertEquals(type, paramTicket.getType(), "Type should match");
        assertEquals(price, paramTicket.getPrice(), "Price should match");
        assertEquals(quota, paramTicket.getQuota(), "Quota should match");
        assertEquals(description, paramTicket.getDescription(), "Description should match");
        assertEquals(salesStart, paramTicket.getSalesStart(), "Sales start should match");
        assertEquals(salesEnd, paramTicket.getSalesEnd(), "Sales end should match");
        assertEquals(eventId, paramTicket.getEventId(), "Event ID should match");
        assertEquals(BuilderStatus.READY, paramTicket.getBuilderStatus(), "Status should be READY");
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
        ticket.setType(type); // Set type first to follow builder state progression
        ticket.setPrice(price);
        assertEquals(price, ticket.getPrice(), "Price should match");
    }

    @Test
    void testSetAndGetQuota() {
        ticket.setType(type);
        ticket.setPrice(price);
        ticket.setQuota(quota);
        assertEquals(quota, ticket.getQuota(), "Quota should match");
    }

    @Test
    void testSetAndGetDescription() {
        ticket.setType(type);
        ticket.setPrice(price);
        ticket.setQuota(quota);
        ticket.setDescription(description);
        assertEquals(description, ticket.getDescription(), "Description should match");
    }

    @Test
    void testSetAndGetSalesPeriod() {
        ticket.setType(type);
        ticket.setPrice(price);
        ticket.setQuota(quota);
        ticket.setDescription(description);
        ticket.setSalesPeriod(salesStart, salesEnd);
        assertEquals(salesStart, ticket.getSalesStart(), "Sales start should match");
        assertEquals(salesEnd, ticket.getSalesEnd(), "Sales end should match");
    }

    @Test
    void testSetAndGetEventId() {
        ticket.setType(type);
        ticket.setPrice(price);
        ticket.setQuota(quota);
        ticket.setDescription(description);
        ticket.setSalesPeriod(salesStart, salesEnd);
        ticket.setEventId(eventId);
        assertEquals(eventId, ticket.getEventId(), "Event ID should match");
    }

    @Test
    void testBuilderStatusProgression() {
        ticket.setType(type);
        assertEquals(BuilderStatus.TYPE_SET, ticket.getBuilderStatus(), "Status should be TYPE_SET");

        ticket.setPrice(price);
        assertEquals(BuilderStatus.PRICE_SET, ticket.getBuilderStatus(), "Status should be PRICE_SET");

        ticket.setQuota(quota);
        assertEquals(BuilderStatus.QUOTA_SET, ticket.getBuilderStatus(), "Status should be QUOTA_SET");

        ticket.setDescription(description);
        assertEquals(BuilderStatus.DESCRIPTION_SET, ticket.getBuilderStatus(), "Status should be DESCRIPTION_SET");

        ticket.setSalesPeriod(salesStart, salesEnd);
        assertEquals(BuilderStatus.SALES_PERIOD_SET, ticket.getBuilderStatus(), "Status should be SALES_PERIOD_SET");

        ticket.setEventId(eventId);
        assertEquals(BuilderStatus.EVENT_ASSIGNED, ticket.getBuilderStatus(), "Status should be EVENT_ASSIGNED");
    }

    @Test
    void testBuildTicket() {
        ticket.setType(type)
                .setPrice(price)
                .setQuota(quota)
                .setDescription(description)
                .setSalesPeriod(salesStart, salesEnd)
                .setEventId(eventId);

        assertTrue(ticket.isReady(), "Builder should be ready");

        Ticket builtTicket = ticket.build();
        assertNotNull(builtTicket, "Built ticket should not be null");
        assertEquals(type, builtTicket.getType(), "Type should match");
        assertEquals(price, builtTicket.getPrice(), "Price should match");
        assertEquals(quota, builtTicket.getQuota(), "Quota should match");
    }

    @Test
    void testIncompleteBuild() {
        ticket.setType(type);
        ticket.setPrice(price);
        // Not setting all required properties

        assertFalse(ticket.isReady(), "Builder should not be ready");
        assertThrows(IllegalStateException.class, () -> ticket.build(),
                "Should throw exception when building with incomplete properties");
    }
}