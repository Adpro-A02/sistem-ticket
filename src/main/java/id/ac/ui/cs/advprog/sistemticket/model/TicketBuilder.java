package id.ac.ui.cs.advprog.sistemticket.model;

import id.ac.ui.cs.advprog.sistemticket.enums.TicketStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class TicketBuilder {

    private UUID id;
    private String type;
    private double price;
    private int quota;
    private String description;
    private LocalDateTime salesStart;
    private LocalDateTime salesEnd;
    private UUID eventId;
    private TicketStatus builderStatus = TicketStatus.INITIALIZED;

    public TicketBuilder() {}

    public TicketBuilder(String type, double price, int quota, String description,
                         LocalDateTime salesStart, LocalDateTime salesEnd, UUID eventId) {
        this.type = type;
        this.price = price;
        this.quota = quota;
        this.description = description;
        this.salesStart = salesStart;
        this.salesEnd = salesEnd;
        this.eventId = eventId;
        this.builderStatus = TicketStatus.READY;
    }

    public UUID getId() {
        return id;
    }

    public TicketBuilder setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getType() {
        return type;
    }

    public TicketBuilder setType(String type) {
        this.type = type;
        if (builderStatus == TicketStatus.INITIALIZED) {
            builderStatus = TicketStatus.TYPE_SET;
        }
        return this;
    }

    public double getPrice() {
        return price;
    }

    public TicketBuilder setPrice(double price) {
        this.price = price;
        if (builderStatus == TicketStatus.TYPE_SET) {
            builderStatus = TicketStatus.PRICE_SET;
        }
        return this;
    }

    public int getQuota() {
        return quota;
    }

    public TicketBuilder setQuota(int quota) {
        this.quota = quota;
        if (builderStatus == TicketStatus.PRICE_SET) {
            builderStatus = TicketStatus.QUOTA_SET;
        }
        return this;
    }

    public String getDescription() {
        return description;
    }

    public TicketBuilder setDescription(String description) {
        this.description = description;
        if (builderStatus == TicketStatus.QUOTA_SET) {
            builderStatus = TicketStatus.DESCRIPTION_SET;
        }
        return this;
    }

    public LocalDateTime getSalesStart() {
        return salesStart;
    }

    public LocalDateTime getSalesEnd() {
        return salesEnd;
    }

    public TicketBuilder setSalesPeriod(LocalDateTime start, LocalDateTime end) {
        this.salesStart = start;
        this.salesEnd = end;
        if (builderStatus == TicketStatus.DESCRIPTION_SET) {
            builderStatus = TicketStatus.SALES_PERIOD_SET;
        }
        return this;
    }

    public UUID getEventId() {
        return eventId;
    }

    public TicketBuilder setEventId(UUID eventId) {
        this.eventId = eventId;
        if (builderStatus == TicketStatus.SALES_PERIOD_SET) {
            builderStatus = TicketStatus.EVENT_ASSIGNED;
        }
        return this;
    }

    public TicketStatus getBuilderStatus() {
        return builderStatus;
    }

    public void setBuilderStatus(TicketStatus builderStatus) {
        this.builderStatus = builderStatus;
    }

    public boolean isReady() {
        return builderStatus == TicketStatus.EVENT_ASSIGNED;
    }

    public Ticket build() {
        if (!isReady()) {
            throw new IllegalStateException("Cannot build ticket, not all required fields are set");
        }

        builderStatus = TicketStatus.READY;
        Ticket ticket = new Ticket(type, price, quota, description, salesStart, salesEnd, eventId);
        if (id != null) {
            ticket.setId(id);
        }

        return ticket;
    }
}