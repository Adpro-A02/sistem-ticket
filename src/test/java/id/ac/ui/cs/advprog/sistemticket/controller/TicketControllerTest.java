package id.ac.ui.cs.advprog.sistemticket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.sistemticket.dto.UpdateTicketDTO;
import id.ac.ui.cs.advprog.sistemticket.enums.TicketStatus;
import id.ac.ui.cs.advprog.sistemticket.model.Ticket;
import id.ac.ui.cs.advprog.sistemticket.model.TicketBuilder;
import id.ac.ui.cs.advprog.sistemticket.service.TicketService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketController.class)
public class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketService ticketService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID ticketId;
    private Ticket ticket;
    private List<Ticket> ticketList;
    private TicketBuilder ticketBuilder;
    private UpdateTicketDTO updateTicketDTO;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        ticketId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        now = LocalDateTime.now();

        // Create a sample ticket
        ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setType("VIP");
        ticket.setPrice(750000.0);
        ticket.setQuota(100);
        ticket.setDescription("VIP access with exclusive merchandise");
        ticket.setSalesStart(now.minusDays(1));
        ticket.setSalesEnd(now.plusDays(30));
        ticket.setEventId(eventId);
        ticket.setStatus(TicketStatus.AVAILABLE);

        // Create a list of tickets
        ticketList = Arrays.asList(ticket);

        // Create a sample ticket builder
        ticketBuilder = mock(TicketBuilder.class);
        when(ticketBuilder.build()).thenReturn(ticket);

        // Create a sample update DTO
        updateTicketDTO = new UpdateTicketDTO();
        updateTicketDTO.setType("Regular");
        updateTicketDTO.setPrice(500000.0);
        updateTicketDTO.setQuota(200);
        updateTicketDTO.setDescription("Regular access");
        updateTicketDTO.setSalesStart(now);
        updateTicketDTO.setSalesEnd(now.plusDays(15));
        updateTicketDTO.setEventId(eventId);
    }

    @Test
    void testGetAllTickets_ReturnsListOfTickets() throws Exception {
        // Arrange
        when(ticketService.getAllTickets()).thenReturn(ticketList);

        // Act & Assert
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(ticketId.toString()))
                .andExpect(jsonPath("$[0].type").value("VIP"));
    }

    @Test
    void testGetTicketById_ExistingTicket_ReturnsTicket() throws Exception {
        // Arrange
        when(ticketService.getTicketById(ticketId)).thenReturn(ticket);

        // Act & Assert
        mockMvc.perform(get("/api/tickets/{id}", ticketId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(ticketId.toString()))
                .andExpect(jsonPath("$.type").value("VIP"))
                .andExpect(jsonPath("$.price").value(750000.0))
                .andExpect(jsonPath("$.quota").value(100));
    }

    @Test
    void testGetTicketById_NonExistingTicket_Returns404() throws Exception {
        // Arrange
        when(ticketService.getTicketById(any(UUID.class)))
                .thenThrow(new EntityNotFoundException("Ticket not found"));

        // Act & Assert
        mockMvc.perform(get("/api/tickets/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateTicket_ValidTicketBuilder_ReturnsCreatedTicket() throws Exception {
        // Arrange
        when(ticketService.createTicket(any(TicketBuilder.class))).thenReturn(ticket);

        // Act & Assert
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ticketBuilder)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ticketId.toString()))
                .andExpect(jsonPath("$.type").value("VIP"));

        verify(ticketService).createTicket(any(TicketBuilder.class));
    }

    @Test
    void testUpdateTicket_ValidDTO_ReturnsUpdatedTicket() throws Exception {
        // Arrange
        Ticket updatedTicket = new Ticket();
        updatedTicket.setId(ticketId);
        updatedTicket.setType("Regular");
        updatedTicket.setPrice(500000.0);

        when(ticketService.updateTicket(eq(ticketId), any(UpdateTicketDTO.class))).thenReturn(updatedTicket);

        // Act & Assert
        mockMvc.perform(put("/api/tickets/{id}", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateTicketDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ticketId.toString()))
                .andExpect(jsonPath("$.type").value("Regular"))
                .andExpect(jsonPath("$.price").value(500000.0));

        verify(ticketService).updateTicket(eq(ticketId), any(UpdateTicketDTO.class));
    }

    @Test
    void testUpdateTicket_InvalidDTO_ReturnsBadRequest() throws Exception {
        // Arrange
        UpdateTicketDTO invalidDTO = new UpdateTicketDTO();
        // Missing required fields like type, price, etc.

        // Act & Assert
        mockMvc.perform(put("/api/tickets/{id}", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteTicket_ExistingTicket_ReturnsNoContent() throws Exception {
        // Arrange
        doNothing().when(ticketService).deleteTicket(ticketId);

        // Act & Assert
        mockMvc.perform(delete("/api/tickets/{id}", ticketId))
                .andExpect(status().isNoContent());

        verify(ticketService).deleteTicket(ticketId);
    }

    @Test
    void testDeleteTicket_NonExistingTicket_Returns404() throws Exception {
        // Arrange
        UUID nonExistingId = UUID.randomUUID();
        doThrow(new EntityNotFoundException("Ticket not found"))
                .when(ticketService).deleteTicket(nonExistingId);

        // Act & Assert
        mockMvc.perform(delete("/api/tickets/{id}", nonExistingId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testPurchaseTicket_ValidRequest_ReturnsUpdatedTicket() throws Exception {
        // Arrange
        Ticket purchasedTicket = new Ticket();
        purchasedTicket.setId(ticketId);
        purchasedTicket.setType("VIP");
        purchasedTicket.setPrice(750000.0);
        purchasedTicket.setQuota(50); // Reduced quota after purchase

        when(ticketService.purchaseTicket(ticketId, 50)).thenReturn(purchasedTicket);

        // Act & Assert
        mockMvc.perform(post("/api/tickets/{id}/purchase", ticketId)
                        .param("quantity", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ticketId.toString()))
                .andExpect(jsonPath("$.quota").value(50));

        verify(ticketService).purchaseTicket(ticketId, 50);
    }

    @Test
    void testPurchaseTicket_InvalidQuantity_ReturnsBadRequest() throws Exception {
        // Arrange
        doThrow(new IllegalStateException("Not enough tickets available"))
                .when(ticketService).purchaseTicket(eq(ticketId), eq(200));

        // Act & Assert
        mockMvc.perform(post("/api/tickets/{id}/purchase", ticketId)
                        .param("quantity", "200"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testPurchaseTicket_TicketNotAvailable_ReturnsBadRequest() throws Exception {
        // Arrange
        doThrow(new IllegalStateException("Ticket is not available for purchase"))
                .when(ticketService).purchaseTicket(eq(ticketId), anyInt());

        // Act & Assert
        mockMvc.perform(post("/api/tickets/{id}/purchase", ticketId)
                        .param("quantity", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAllTickets_EmptyList_ReturnsEmptyArray() throws Exception {
        // Arrange
        when(ticketService.getAllTickets()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testCreateTicket_BuilderNotReady_ReturnsBadRequest() throws Exception {
        // Arrange
        doThrow(new IllegalStateException("Ticket builder is not ready for building"))
                .when(ticketService).createTicket(any(TicketBuilder.class));

        // Act & Assert
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ticketBuilder)))
                .andExpect(status().isBadRequest());
    }
}