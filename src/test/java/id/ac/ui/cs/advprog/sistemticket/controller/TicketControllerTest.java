package id.ac.ui.cs.advprog.sistemticket.controller;

import id.ac.ui.cs.advprog.sistemticket.model.Ticket;
import id.ac.ui.cs.advprog.sistemticket.service.TicketService;
import id.ac.ui.cs.advprog.sistemticket.enums.TicketStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(TicketController.class)
public class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketService ticketService;

    @Autowired
    private ObjectMapper objectMapper;

    private List<Ticket> tickets;
    private String ticketId;
    private String eventId;
    private Long currentTime;

    @BeforeEach
    void setUp() {
        currentTime = System.currentTimeMillis();
        ticketId = "13652556-012a-4c07-b546-54eb1396d79b";
        eventId = "eb558e9f-1c39-460e-8860-71af6af63bd6";

        tickets = new ArrayList<>();
        
        // Regular ticket
        Ticket ticket1 = new Ticket(
            eventId,
            "REGULAR",
            150.0,
            100,
            "Regular concert ticket",
            currentTime,
            currentTime + 86400000 // 1 day later
        );
        ticket1.setId(ticketId);
        tickets.add(ticket1);
        
        // VIP ticket
        Ticket ticket2 = new Ticket(
            eventId,
            "VIP",
            300.0,
            50,
            "VIP concert ticket",
            currentTime,
            currentTime + 86400000 // 1 day later
        );
        ticket2.setId("7f9e15bb-4b15-42f4-aebc-c3af385fb078");
        tickets.add(ticket2);
    }

    // Core CRUD operations
    @Test
    void testGetAllTickets() throws Exception {
        when(ticketService.findAll()).thenReturn(tickets);

        mockMvc.perform(get("/api/tickets"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(2)))
               .andExpect(jsonPath("$[0].id", is(ticketId)))
               .andExpect(jsonPath("$[0].type", is("REGULAR")));
               
        verify(ticketService, times(1)).findAll();
    }

    @Test
    void testGetTicketById() throws Exception {
        Ticket ticket = tickets.get(0);
        when(ticketService.findById(ticketId)).thenReturn(ticket);

        mockMvc.perform(get("/api/tickets/{id}", ticketId))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id", is(ticketId)))
               .andExpect(jsonPath("$.type", is("REGULAR")));
               
        verify(ticketService, times(1)).findById(ticketId);
    }

    @Test
    void testCreateTicket() throws Exception {
        Ticket ticket = tickets.get(0);
        when(ticketService.createTicket(any(Ticket.class))).thenReturn(ticket);

        mockMvc.perform(post("/api/tickets")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(ticket)))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.id", is(ticketId)));
               
        verify(ticketService, times(1)).createTicket(any(Ticket.class));
    }

    @Test
    void testUpdateTicket() throws Exception {
        Ticket ticket = tickets.get(0);
        when(ticketService.updateTicket(any(Ticket.class))).thenReturn(ticket);

        mockMvc.perform(put("/api/tickets/{id}", ticketId)
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(ticket)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id", is(ticketId)));
               
        verify(ticketService, times(1)).updateTicket(any(Ticket.class));
    }

    @Test
    void testDeleteTicket() throws Exception {
        when(ticketService.findById(ticketId)).thenReturn(tickets.get(0));
        doNothing().when(ticketService).deleteTicket(ticketId);

        mockMvc.perform(delete("/api/tickets/{id}", ticketId))
               .andExpect(status().isNoContent());
               
        verify(ticketService, times(1)).deleteTicket(ticketId);
    }

    // Core business operations
    @Test
    void testUpdateTicketStatus() throws Exception {
        Ticket ticket = tickets.get(0);
        ticket.setStatus(TicketStatus.PURCHASED.getValue());
        
        when(ticketService.updateStatus(ticketId, TicketStatus.PURCHASED.getValue())).thenReturn(ticket);

        mockMvc.perform(patch("/api/tickets/{id}/status", ticketId)
               .contentType(MediaType.APPLICATION_JSON)
               .content("{\"status\":\"" + TicketStatus.PURCHASED.getValue() + "\"}"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status", is(TicketStatus.PURCHASED.getValue())));
               
        verify(ticketService, times(1)).updateStatus(eq(ticketId), eq(TicketStatus.PURCHASED.getValue()));
    }

    @Test
    void testPurchaseTicket() throws Exception {
        Ticket ticket = tickets.get(0);
        ticket.setRemainingQuota(95);
        
        when(ticketService.purchaseTicket(eq(ticketId), eq(5), anyLong())).thenReturn(ticket);

        mockMvc.perform(post("/api/tickets/{id}/purchase", ticketId)
               .contentType(MediaType.APPLICATION_JSON)
               .content("{\"amount\": 5}"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.remainingQuota", is(95)));
               
        verify(ticketService, times(1)).purchaseTicket(eq(ticketId), eq(5), anyLong());
    }
}
