package id.ac.ui.cs.advprog.sistemticket.enums;

public enum TicketStatus {
    INITIALIZED("INITIALIZED"),
    TYPE_SET("TYPE_SET"),
    PRICE_SET("PRICE_SET"),
    QUOTA_SET("QUOTA_SET"),
    DESCRIPTION_SET("DESCRIPTION_SET"),
    SALES_PERIOD_SET("SALES_PERIOD_SET"),
    EVENT_ASSIGNED("EVENT_ASSIGNED"),
    READY("READY"),

    AVAILABLE("AVAILABLE"),
    PURCHASED("PURCHASED"),
    USED("USED"),
    SOLD_OUT("SOLD_OUT"),
    EXPIRED("EXPIRED");

    private final String value;

    TicketStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}