package id.ac.ui.cs.advprog.sistemticket.model;

import id.ac.ui.cs.advprog.sistemticket.enums.TicketStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false)
    private int quota;

    @Column(length = 1000)
    private String description;

    @Column(name = "sales_start", nullable = false)
    private LocalDateTime salesStart;

    @Column(name = "sales_end", nullable = false)
    private LocalDateTime salesEnd;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    // Default constructor needed by JPA
    public Ticket() {}

    // Constructor used by the builder
    public Ticket(String type, double price, int quota, String description,
                  LocalDateTime salesStart, LocalDateTime salesEnd, UUID eventId) {
        this.type = type;
        this.price = price;
        this.quota = quota;
        this.description = description;
        this.salesStart = salesStart;
        this.salesEnd = salesEnd;
        this.eventId = eventId;
        this.status = TicketStatus.AVAILABLE;
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuota() {
        return quota;
    }

    public void setQuota(int quota) {
        this.quota = quota;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getSalesStart() {
        return salesStart;
    }

    public void setSalesStart(LocalDateTime salesStart) {
        this.salesStart = salesStart;
    }

    public LocalDateTime getSalesEnd() {
        return salesEnd;
    }

    public void setSalesEnd(LocalDateTime salesEnd) {
        this.salesEnd = salesEnd;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    /**
     * Reduces the ticket quota by the specified quantity.
     * Throws an exception if the reduction would result in a negative quota.
     */
    public void reduceQuota(int quantity) {
        if (quota < quantity) {
            throw new IllegalArgumentException("Not enough tickets available");
        }
        quota -= quantity;
    }

    /**
     * Checks if this ticket is available for purchase based on quota, status, and sales period.
     */
    public boolean isAvailableForPurchase() {
        LocalDateTime now = LocalDateTime.now();
        return quota > 0 &&
                status == TicketStatus.AVAILABLE &&
                now.isAfter(salesStart) &&
                now.isBefore(salesEnd);
    }
}