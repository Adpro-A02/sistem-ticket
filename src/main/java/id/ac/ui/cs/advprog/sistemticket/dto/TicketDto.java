package id.ac.ui.cs.advprog.sistemticket.dto;

import java.io.Serializable;

public class TicketDto implements Serializable {
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
        this.type = type;
    }
    
    public Double getPrice() {
        return price;
    }
    
    public void setPrice(Double price) {
        this.price = price;
    }
    
    public Integer getQuota() {
        return quota;
    }
    
    public void setQuota(Integer quota) {
        this.quota = quota;
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
        this.status = status;
    }
}
