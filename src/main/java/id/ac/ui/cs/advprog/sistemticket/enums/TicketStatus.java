package id.ac.ui.cs.advprog.sistemticket.enums;

import lombok.Getter;   

@Getter
public enum TicketStatus {
    AVAILABLE("AVAILABLE"),
    PURCHASED("PURCHASED"),
    EXPIRED("EXPIRED"),
    USED("USED");

    private final String value;

    private TicketStatus(String value) {
        this.value = value;
    }

    public static boolean contains(String params) {
        for (TicketStatus ticketStatus : TicketStatus.values()) {
            if (ticketStatus.value.equals(params)) {
                return true;
            }
        }
        return false;
    }
}
