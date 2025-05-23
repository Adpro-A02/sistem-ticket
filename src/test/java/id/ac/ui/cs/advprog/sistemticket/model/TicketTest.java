package id.ac.ui.cs.advprog.sistemticket.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TicketTest {
    private String validEventId;
    private String validDescription;
    private Double validPrice;
    private Integer validQuota;
    private Long validSaleStart;
    private Long validSaleEnd;
    
    @BeforeEach
    void setUp() {
        validEventId = "eb558e9f-1c39-460e-8860-71af6af63bd6";
        validDescription = "Regular concert ticket";
        validPrice = 150.0;
        validQuota = 100;
        validSaleStart = System.currentTimeMillis();
        validSaleEnd = validSaleStart + 86400000; // One day later
    }
    
    @Test
    void testCreateTicketWithDefaultConstructor() {
        Ticket ticket = new Ticket();
        assertNotNull(ticket.getId(), "Ticket ID should be automatically generated");
        assertEquals("AVAILABLE", ticket.getStatus(), "Default status should be AVAILABLE");
    }
    
    @Test
    void testCreateCompleteTicket() {
        Ticket ticket = new Ticket(validEventId, "REGULAR", validPrice, 
                                   validQuota, validDescription, 
                                   validSaleStart, validSaleEnd);
        
        assertEquals(validEventId, ticket.getEventId());
        assertEquals("REGULAR", ticket.getType());
        assertEquals(validPrice, ticket.getPrice());
        assertEquals(validQuota, ticket.getQuota());
        assertEquals(validQuota, ticket.getRemainingQuota(), "Remaining quota should equal total quota on creation");
        assertEquals(validDescription, ticket.getDescription());
        assertEquals(validSaleStart, ticket.getSaleStart());
        assertEquals(validSaleEnd, ticket.getSaleEnd());
        assertEquals("AVAILABLE", ticket.getStatus());
    }
    
    @Test
    void testCreateTicketWithCustomStatus() {
        Ticket ticket = new Ticket(validEventId, "VIP", validPrice, 
                                   validQuota, validDescription, 
                                   validSaleStart, validSaleEnd, "PURCHASED");
        assertEquals("PURCHASED", ticket.getStatus());
    }
    
    @Test
    void testCreateTicketWithInvalidStatus() {
        assertThrows(IllegalArgumentException.class, () -> {
            Ticket ticket = new Ticket(validEventId, "REGULAR", validPrice, 
                                      validQuota, validDescription, 
                                      validSaleStart, validSaleEnd, "INVALID_STATUS");
        });
    }
    
    @Test
    void testCreateTicketWithInvalidType() {
        assertThrows(IllegalArgumentException.class, () -> {
            Ticket ticket = new Ticket(validEventId, "INVALID_TYPE", validPrice, 
                                      validQuota, validDescription, 
                                      validSaleStart, validSaleEnd);
        });
    }
    
    @Test
    void testCreateTicketWithNegativePrice() {
        assertThrows(IllegalArgumentException.class, () -> {
            Ticket ticket = new Ticket(validEventId, "REGULAR", -10.0, 
                                      validQuota, validDescription, 
                                      validSaleStart, validSaleEnd);
        });
    }
    
    @Test
    void testCreateTicketWithZeroPrice() {
        Ticket ticket = new Ticket(validEventId, "REGULAR", 0.0, 
                                  validQuota, validDescription, 
                                  validSaleStart, validSaleEnd);
        assertEquals(0.0, ticket.getPrice());
    }
    
    @Test
    void testCreateTicketWithNegativeQuota() {
        assertThrows(IllegalArgumentException.class, () -> {
            Ticket ticket = new Ticket(validEventId, "REGULAR", validPrice, 
                                      -5, validDescription, 
                                      validSaleStart, validSaleEnd);
        });
    }
    
    @Test
    void testCreateTicketWithZeroQuota() {
        assertThrows(IllegalArgumentException.class, () -> {
            Ticket ticket = new Ticket(validEventId, "REGULAR", validPrice, 
                                      0, validDescription, 
                                      validSaleStart, validSaleEnd);
        });
    }
    
    @Test
    void testCreateTicketWithInvalidSaleDates() {
        Long invalidSaleEnd = validSaleStart - 86400000;
        
        assertThrows(IllegalArgumentException.class, () -> {
            Ticket ticket = new Ticket(validEventId, "REGULAR", validPrice, 
                                      validQuota, validDescription, 
                                      validSaleStart, invalidSaleEnd);
        });
    }
    
    @Test
    void testSetStatus() {
        Ticket ticket = new Ticket();
        
        ticket.setStatus("PURCHASED");
        assertEquals("PURCHASED", ticket.getStatus());
        
        ticket.setStatus("EXPIRED");
        assertEquals("EXPIRED", ticket.getStatus());
        
        ticket.setStatus("USED");
        assertEquals("USED", ticket.getStatus());
        
        assertThrows(IllegalArgumentException.class, () -> 
            ticket.setStatus("INVALID_STATUS"));
    }
    
    @Test
    void testDecreaseRemainingQuota() {
        Ticket ticket = new Ticket(validEventId, "REGULAR", validPrice, 
                                   100, validDescription, 
                                   validSaleStart, validSaleEnd);
        
        ticket.decreaseRemainingQuota(5);
        assertEquals(95, ticket.getRemainingQuota());

        assertThrows(IllegalArgumentException.class, () -> 
            ticket.decreaseRemainingQuota(96));

        assertThrows(IllegalArgumentException.class, () -> 
            ticket.decreaseRemainingQuota(-1));
    }
    
    @Test
    void testIsAvailableForPurchase() {
        Ticket ticket = new Ticket(validEventId, "REGULAR", validPrice, 
                                   validQuota, validDescription, 
                                   validSaleStart, validSaleEnd);

        Long currentTime = validSaleStart + 1000;
        assertTrue(ticket.isAvailableForPurchase(currentTime));

        currentTime = validSaleStart - 1000;
        assertFalse(ticket.isAvailableForPurchase(currentTime));

        currentTime = validSaleEnd + 1000;
        assertFalse(ticket.isAvailableForPurchase(currentTime));

        ticket.setStatus("PURCHASED");
        currentTime = validSaleStart + 1000;
        assertFalse(ticket.isAvailableForPurchase(currentTime));

        ticket.setStatus("AVAILABLE");
        ticket.setRemainingQuota(0);
        assertFalse(ticket.isAvailableForPurchase(currentTime));
    }
}
