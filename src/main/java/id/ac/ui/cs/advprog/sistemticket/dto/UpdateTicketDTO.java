package id.ac.ui.cs.advprog.sistemticket.dto;

import id.ac.ui.cs.advprog.sistemticket.enums.TicketStatus;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.UUID;

public class UpdateTicketDTO {

    @NotBlank(message = "Ticket type tidak boleh kosong")
    private String type;

    @NotNull(message = "Price tidak boleh kosong")
    @Positive(message = "Price harus lebih dari 0")
    private Double price;

    @NotNull(message = "Quota tidak boleh kosong")
    @Min(value = 1, message = "Quota harus minimal 1")
    private Integer quota;

    private String description;

    @NotNull(message = "Sales start time tidak boleh kosong")
    @FutureOrPresent(message = "Sales start time harus saat ini atau di masa depan")
    private LocalDateTime salesStart;

    @NotNull(message = "Sales end time tidak boleh kosong")
    @Future(message = "Sales end time harus di masa depan")
    private LocalDateTime salesEnd;

    @NotNull(message = "Event ID tidak boleh kosong")
    private UUID eventId;

    private TicketStatus status;

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
}