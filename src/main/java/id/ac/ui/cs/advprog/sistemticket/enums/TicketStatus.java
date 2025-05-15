package id.ac.ui.cs.advprog.sistemticket.enums;

public enum TicketStatus {
    INITIALIZED("INITIALIZED"),
    TYPE_SET("TYPE_SET"),
    PRICE_SET("PRICE_SET"),
    QUOTA_SET("QUOTA_SET"),
    DESCRIPTION_SET("DESCRIPTION_SET"),
    SALES_PERIOD_SET("SALES_PERIOD_SET"),
    EVENT_ASSIGNED("EVENT_ASSIGNED"),
    READY("READY");

    private final String value;

    TicketStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}