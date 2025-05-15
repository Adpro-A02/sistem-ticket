package id.ac.ui.cs.advprog.sistemticket.repository;

import id.ac.ui.cs.advprog.sistemticket.enums.TicketStatus;
import id.ac.ui.cs.advprog.sistemticket.model.Ticket;
import id.ac.ui.cs.advprog.sistemticket.model.TicketBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class TicketRepositoryTest {

    @Autowired
    private TicketRepository ticketRepository;

    private Ticket sampleTicket;
    private final UUID eventId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        sampleTicket = new Ticket();
        sampleTicket.setType("VIP");
        sampleTicket.setPrice(750000.0);
        sampleTicket.setQuota(100);
        sampleTicket.setDescription("VIP access with exclusive merchandise");
        sampleTicket.setSalesStart(LocalDateTime.now().minusDays(1));
        sampleTicket.setSalesEnd(LocalDateTime.now().plusDays(30));
        sampleTicket.setEventId(eventId);
        sampleTicket.setStatus(TicketStatus.AVAILABLE);

        ticketRepository.save(sampleTicket);
    }

    @Test
    void testAddTicket() {
        Ticket newTicket = new Ticket();
        newTicket.setType("Regular");
        newTicket.setPrice(350000.0);
        newTicket.setQuota(500);
        newTicket.setDescription("Standard admission");
        newTicket.setSalesStart(LocalDateTime.now());
        newTicket.setSalesEnd(LocalDateTime.now().plusDays(14));
        newTicket.setEventId(eventId);

        Ticket savedTicket = ticketRepository.save(newTicket);
        assertThat(savedTicket.getId()).isNotNull();
    }

    @Test
    void testListTickets() {
        List<Ticket> tickets = ticketRepository.findAll();
        assertThat(tickets).isNotEmpty();
    }

    @Test
    void testGetById() {
        Optional<Ticket> found = ticketRepository.findById(sampleTicket.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getType()).isEqualTo("VIP");
    }

    @Test
    void testFindByEventId() {
        List<Ticket> tickets = ticketRepository.findByEventId(eventId);
        assertThat(tickets).isNotEmpty();
        assertThat(tickets.get(0).getEventId()).isEqualTo(eventId);
    }

    @Test
    void testUpdateTicket() {
        sampleTicket.setPrice(800000.0);
        Ticket updated = ticketRepository.save(sampleTicket);
        assertThat(updated.getPrice()).isEqualTo(800000.0);
    }

    @Test
    void testDeleteTicket() {
        UUID id = sampleTicket.getId();
        ticketRepository.deleteById(id);
        Optional<Ticket> found = ticketRepository.findById(id);
        assertThat(found).isEmpty();
    }

    @Test
    void testUpdateStatus() {
        sampleTicket.setStatus(TicketStatus.SOLD_OUT);
        Ticket updated = ticketRepository.save(sampleTicket);

        Optional<Ticket> result = ticketRepository.findById(sampleTicket.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(TicketStatus.SOLD_OUT);
    }

    @Test
    void testFindAvailableTickets() {
        List<Ticket> availableTickets = ticketRepository.findByStatus(TicketStatus.AVAILABLE);
        assertThat(availableTickets).isNotEmpty();
    }
}