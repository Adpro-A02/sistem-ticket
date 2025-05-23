package id.ac.ui.cs.advprog.sistemticket.repository;

import id.ac.ui.cs.advprog.sistemticket.model.Ticket;
import id.ac.ui.cs.advprog.sistemticket.enums.TicketStatus;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class TicketRepository {
    private List<Ticket> ticketData = new ArrayList<>();

    public Ticket save(Ticket ticket) {
        int i = 0;
        for (Ticket savedTicket : ticketData) {
            if (savedTicket.getId().equals(ticket.getId())) {
                ticketData.remove(i);
                ticketData.add(i, ticket);
                return ticket;
            }
            i += 1;
        }
        ticketData.add(ticket);
        return ticket;
    }

    public Ticket findById(String id) {
        for (Ticket savedTicket : ticketData) {
            if (savedTicket.getId().equals(id)) {
                return savedTicket;
            }
        }
        return null;
    }

    public List<Ticket> findAllByEventId(String eventId) {
        List<Ticket> result = new ArrayList<>();
        for (Ticket savedTicket : ticketData) {
            if (savedTicket.getEventId().equals(eventId)) {
                result.add(savedTicket);
            }
        }
        return result;
    }

    public List<Ticket> findAllByType(String type) {
        List<Ticket> result = new ArrayList<>();
        for (Ticket savedTicket : ticketData) {
            if (savedTicket.getType().equals(type)) {
                result.add(savedTicket);
            }
        }
        return result;
    }

    public List<Ticket> findAllByStatus(String status) {
        List<Ticket> result = new ArrayList<>();
        for (Ticket savedTicket : ticketData) {
            if (savedTicket.getStatus().equals(status)) {
                result.add(savedTicket);
            }
        }
        return result;
    }

    public List<Ticket> findAllAvailable(Long currentTime) {
        List<Ticket> result = new ArrayList<>();
        for (Ticket savedTicket : ticketData) {
            if (savedTicket.isAvailableForPurchase(currentTime)) {
                result.add(savedTicket);
            }
        }
        return result;
    }

    public List<Ticket> findAll() {
        return new ArrayList<>(ticketData);
    }

    public void deleteById(String id) {
        ticketData.removeIf(ticket -> ticket.getId().equals(id));
    }
}
