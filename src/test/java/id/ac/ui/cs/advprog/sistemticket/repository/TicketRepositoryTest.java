package id.ac.ui.cs.advprog.sistemticket.repository;

import id.ac.ui.cs.advprog.sistemticket.enums.TicketStatus;
import id.ac.ui.cs.advprog.sistemticket.model.Ticket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        sampleTicket = new Ticket();
        sampleTicket.setType("VIP");
        sampleTicket.setPrice(750000.0);
        sampleTicket.setQuota(100);
        sampleTicket.setDescription("VIP access with exclusive merchandise");
        sampleTicket.setSalesStart(now.minusDays(1));
        sampleTicket.setSalesEnd(now.plusDays(30));
        sampleTicket.setEventId(eventId);
        sampleTicket.setStatus(TicketStatus.AVAILABLE);

        ticketRepository.save(sampleTicket);
    }

    @Test
    @DisplayName("Test Add Ticket")
    void testAddTicket() {
        Ticket newTicket = new Ticket();
        newTicket.setType("Regular");
        newTicket.setPrice(350000.0);
        newTicket.setQuota(500);
        newTicket.setDescription("Standard admission");
        newTicket.setSalesStart(now);
        newTicket.setSalesEnd(now.plusDays(14));
        newTicket.setEventId(eventId);

        Ticket savedTicket = ticketRepository.save(newTicket);
        assertThat(savedTicket.getId()).isNotNull();
    }

    @Test
    @DisplayName("Test List All Tickets")
    void testListTickets() {
        List<Ticket> tickets = ticketRepository.findAll();
        assertThat(tickets).isNotEmpty();
    }

    @Test
    @DisplayName("Test Get Ticket By ID")
    void testGetById() {
        Optional<Ticket> found = ticketRepository.findById(sampleTicket.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getType()).isEqualTo("VIP");
    }

    @Test
    @DisplayName("Test Find Tickets By Event ID")
    void testFindByEventId() {
        List<Ticket> tickets = ticketRepository.findByEventId(eventId);
        assertThat(tickets).isNotEmpty();
        assertThat(tickets.get(0).getEventId()).isEqualTo(eventId);
    }

    @Test
    @DisplayName("Test Update Ticket")
    void testUpdateTicket() {
        sampleTicket.setPrice(800000.0);
        Ticket updated = ticketRepository.save(sampleTicket);
        assertThat(updated.getPrice()).isEqualTo(800000.0);
    }

    @Test
    @DisplayName("Test Delete Ticket")
    void testDeleteTicket() {
        UUID id = sampleTicket.getId();
        ticketRepository.deleteById(id);
        Optional<Ticket> found = ticketRepository.findById(id);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Test Update Ticket Status")
    void testUpdateStatus() {
        sampleTicket.setStatus(TicketStatus.SOLD_OUT);
        Ticket updated = ticketRepository.save(sampleTicket);

        Optional<Ticket> result = ticketRepository.findById(sampleTicket.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(TicketStatus.SOLD_OUT);
    }

    @Test
    @DisplayName("Test Find Available Tickets")
    void testFindAvailableTickets() {
        List<Ticket> availableTickets = ticketRepository.findByStatus(TicketStatus.AVAILABLE);
        assertThat(availableTickets).isNotEmpty();
    }

    @Test
    @DisplayName("Test Find By Type")
    void testFindByType() {
        List<Ticket> vipTickets = ticketRepository.findByType("VIP");
        assertThat(vipTickets).isNotEmpty();
        assertThat(vipTickets.get(0).getType()).isEqualTo("VIP");
    }

    @Test
    @DisplayName("Test Find By Active Sales Period")
    void testFindByActiveSalesPeriod() {
        List<Ticket> activeTickets = ticketRepository.findBySalesStartBeforeAndSalesEndAfter(now, now);
        assertThat(activeTickets).isNotEmpty();
        assertThat(activeTickets.get(0).getId()).isEqualTo(sampleTicket.getId());
    }

    @Test
    @DisplayName("Test Find By Price Range")
    void testFindByPriceRange() {
        List<Ticket> expensiveTickets = ticketRepository.findByPriceBetween(500000.0, 1000000.0);
        assertThat(expensiveTickets).isNotEmpty();
        assertThat(expensiveTickets.get(0).getPrice()).isEqualTo(750000.0);
    }

    @Test
    @DisplayName("Test Find By Event ID And Status")
    void testFindByEventIdAndStatus() {
        List<Ticket> availableEventTickets = ticketRepository.findByEventIdAndStatus(eventId, TicketStatus.AVAILABLE);
        assertThat(availableEventTickets).isNotEmpty();
        assertThat(availableEventTickets.get(0).getEventId()).isEqualTo(eventId);
        assertThat(availableEventTickets.get(0).getStatus()).isEqualTo(TicketStatus.AVAILABLE);
    }
}