package id.ac.ui.cs.advprog.sistemticket.dto;

import jakarta.validation.constraints.Min;
import java.io.Serializable;

public class TicketUpdateDto implements Serializable {
    private String type;
    
    @Min(value = 0, message = "Price must be greater than or equal to 0")
    private Double price;
    
    @Min(value = 1, message = "Quota must be at least 1")
    private Integer quota;
    
    private String description;
    private Long saleStart;
    private Long saleEnd;
    
    // Getters and setters
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
}
