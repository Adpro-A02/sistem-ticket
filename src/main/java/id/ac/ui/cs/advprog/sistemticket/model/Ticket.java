package id.ac.ui.cs.advprog.sistemticket.model;

import id.ac.ui.cs.advprog.sistemticket.enums.TicketStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.PrePersist;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "tickets")
public class Ticket {
    // Valid ticket types
    private static final Set<String> VALID_TYPES = new HashSet<>(
        Arrays.asList("REGULAR", "VIP")
    );
    
    @Id
    private String id;
    
    @Column(nullable = false)
    private String eventId;
    
    @Column(nullable = false)
    private String type;
    
    @Column(nullable = false)
    private Double price;
    
    @Column(nullable = false)
    private Integer quota;
    
    @Column(nullable = false)
    private Integer remainingQuota;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private Long saleStart;
    
    @Column(nullable = false)
    private Long saleEnd;
    
    @Column(nullable = false)
    private String status;
    
    @Column
    private String userId;
    
    // Default constructor required by JPA
    public Ticket() {
        this.id = UUID.randomUUID().toString();
        this.status = TicketStatus.AVAILABLE.getValue();
    }
    
    // Private constructor used by the Builder
    private Ticket(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID().toString();
        this.eventId = builder.eventId;
        this.type = builder.type;
        this.price = builder.price;
        this.quota = builder.quota;
        this.remainingQuota = builder.remainingQuota != null ? builder.remainingQuota : builder.quota;
        this.description = builder.description;
        this.saleStart = builder.saleStart;
        this.saleEnd = builder.saleEnd;
        this.status = builder.status != null ? builder.status : TicketStatus.AVAILABLE.getValue();
        this.userId = builder.userId;
    }
    
    // Maintain backward compatibility with existing constructors
    public Ticket(String eventId, String type, Double price, 
                 Integer quota, String description, 
                 Long saleStart, Long saleEnd) {
        this(new Builder()
                .eventId(eventId)
                .type(type)
                .price(price)
                .quota(quota)
                .description(description)
                .saleStart(saleStart)
                .saleEnd(saleEnd));
    }
    
    public Ticket(String eventId, String type, Double price, 
                 Integer quota, String description, 
                 Long saleStart, Long saleEnd, String status) {
        this(new Builder()
                .eventId(eventId)
                .type(type)
                .price(price)
                .quota(quota)
                .description(description)
                .saleStart(saleStart)
                .saleEnd(saleEnd)
                .status(status));
    }
    
    // Add validation methods to the main class
    private void validateTicketType(String type) {
        if (type == null || !VALID_TYPES.contains(type)) {
            throw new IllegalArgumentException("Invalid ticket type: " + type);
        }
    }
    
    private void validateStatus(String status) {
        if (status == null || !TicketStatus.contains(status)) {
            throw new IllegalArgumentException("Invalid ticket status: " + status);
        }
    }
    
    private void validatePrice(Double price) {
        if (price == null || price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
    }
    
    private void validateQuota(Integer quota) {
        if (quota == null || quota <= 0) {
            throw new IllegalArgumentException("Quota must be positive");
        }
    }
    
    private void validateSaleDates(Long saleStart, Long saleEnd) {
        if (saleStart == null || saleEnd == null || saleEnd <= saleStart) {
            throw new IllegalArgumentException("Sale end must be after sale start");
        }
    }
    
    // Builder class
    public static class Builder {
        private String id;
        private String eventId;
        private String type;
        private Double price;
        private Integer quota;
        private Integer remainingQuota;
        private String description;
        private Long saleStart;
        private Long saleEnd;
        private String status;
        private String userId;
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder eventId(String eventId) {
            this.eventId = eventId;
            return this;
        }
        
        public Builder type(String type) {
            validateTicketType(type);
            this.type = type;
            return this;
        }
        
        public Builder price(Double price) {
            validatePrice(price);
            this.price = price;
            return this;
        }
        
        public Builder quota(Integer quota) {
            validateQuota(quota);
            this.quota = quota;
            return this;
        }
        
        public Builder remainingQuota(Integer remainingQuota) {
            this.remainingQuota = remainingQuota;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder saleStart(Long saleStart) {
            this.saleStart = saleStart;
            return this;
        }
        
        public Builder saleEnd(Long saleEnd) {
            this.saleEnd = saleEnd;
            if (this.saleStart != null && saleEnd != null) {
                validateSaleDates(this.saleStart, saleEnd);
            }
            return this;
        }
        
        public Builder status(String status) {
            validateStatus(status);
            this.status = status;
            return this;
        }
        
        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }
        
        public Ticket build() {
            validateRequiredFields();
            return new Ticket(this);
        }
        
        private void validateRequiredFields() {
            if (eventId == null) throw new IllegalArgumentException("Event ID is required");
            if (type == null) throw new IllegalArgumentException("Ticket type is required");
            if (price == null) throw new IllegalArgumentException("Price is required");
            if (quota == null) throw new IllegalArgumentException("Quota is required");
            if (saleStart == null) throw new IllegalArgumentException("Sale start is required");
            if (saleEnd == null) throw new IllegalArgumentException("Sale end is required");
            
            // Final validation of date range
            validateSaleDates(saleStart, saleEnd);
        }
        
        private void validateTicketType(String type) {
            if (type == null || !VALID_TYPES.contains(type)) {
                throw new IllegalArgumentException("Invalid ticket type: " + type);
            }
        }
        
        private void validateStatus(String status) {
            if (status == null || !TicketStatus.contains(status)) {
                throw new IllegalArgumentException("Invalid ticket status: " + status);
            }
        }
        
        private void validatePrice(Double price) {
            if (price == null || price < 0) {
                throw new IllegalArgumentException("Price cannot be negative");
            }
        }
        
        private void validateQuota(Integer quota) {
            if (quota == null || quota <= 0) {
                throw new IllegalArgumentException("Quota must be positive");
            }
        }
        
        private void validateSaleDates(Long saleStart, Long saleEnd) {
            if (saleStart == null || saleEnd == null || saleEnd <= saleStart) {
                throw new IllegalArgumentException("Sale end must be after sale start");
            }
        }
    }
    
    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.status == null) {
            this.status = TicketStatus.AVAILABLE.getValue();
        }
        if (this.remainingQuota == null) {
            this.remainingQuota = this.quota;
        }
    }
    
    // Business logic methods
    public void decreaseRemainingQuota(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        
        if (amount > remainingQuota) {
            throw new IllegalArgumentException("Not enough tickets available");
        }
        
        remainingQuota -= amount;
    }
    
    public boolean isAvailableForPurchase(Long currentTime) {
        if (currentTime == null) {
            return false;
        }
        
        return TicketStatus.AVAILABLE.getValue().equals(status) && 
               remainingQuota > 0 && 
               currentTime >= saleStart && 
               currentTime <= saleEnd;
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getEventId() {
        return eventId;
    }
    
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        validateTicketType(type);
        this.type = type;
    }
    
    public Double getPrice() {
        return price;
    }
    
    public void setPrice(Double price) {
        validatePrice(price);
        this.price = price;
    }
    
    public Integer getQuota() {
        return quota;
    }
    
    public void setQuota(Integer quota) {
        validateQuota(quota);
        this.quota = quota;
        if (this.remainingQuota == null) {
            this.remainingQuota = quota;
        }
    }
    
    public Integer getRemainingQuota() {
        return remainingQuota;
    }
    
    public void setRemainingQuota(Integer remainingQuota) {
        this.remainingQuota = remainingQuota;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Long getSaleStart() {
        return saleStart;
    }
    
    public void setSaleStart(Long saleStart) {
        this.saleStart = saleStart;
    }
    
    public Long getSaleEnd() {
        return saleEnd;
    }
    
    public void setSaleEnd(Long saleEnd) {
        this.saleEnd = saleEnd;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        validateStatus(status);
        this.status = status;
    }
    
    // Getters and setters for userId
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
}
