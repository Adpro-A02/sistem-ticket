package id.ac.ui.cs.advprog.sistemticket.config;

import id.ac.ui.cs.advprog.sistemticket.dto.TicketCreationDto;
import id.ac.ui.cs.advprog.sistemticket.dto.TicketDto;
import id.ac.ui.cs.advprog.sistemticket.dto.TicketUpdateDto;
import id.ac.ui.cs.advprog.sistemticket.mapper.TicketMapper;
import id.ac.ui.cs.advprog.sistemticket.model.Ticket;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfig {

    @Bean
    public TicketMapper ticketMapper() {
        return new TicketMapper() {
            @Override
            public TicketDto toDto(Ticket ticket) {
                if (ticket == null) return null;
                
                // Create a simple DTO with the essential properties
                TicketDto dto = new TicketDto();
                dto.setId(ticket.getId());
                dto.setEventId(ticket.getEventId());
                dto.setType(ticket.getType());
                dto.setPrice(ticket.getPrice());
                dto.setQuota(ticket.getQuota());
                dto.setRemainingQuota(ticket.getRemainingQuota());
                dto.setDescription(ticket.getDescription());
                dto.setStatus(ticket.getStatus());
                dto.setSaleStart(ticket.getSaleStart());
                dto.setSaleEnd(ticket.getSaleEnd());
                
                return dto;
            }

            @Override
            public Ticket toEntity(TicketCreationDto dto) {
                if (dto == null) return null;
                
                Ticket ticket = new Ticket(
                    dto.getEventId(),
                    dto.getType(),
                    dto.getPrice(),
                    dto.getQuota(),
                    dto.getDescription(),
                    dto.getSaleStart(),
                    dto.getSaleEnd()
                );
                
                return ticket;
            }

            @Override
            public void updateEntityFromDto(TicketUpdateDto dto, Ticket ticket) {
                if (dto == null || ticket == null) return;
                
                // Update the ticket with values from the DTO
                if (dto.getPrice() != null) ticket.setPrice(dto.getPrice());
                if (dto.getQuota() != null) ticket.setQuota(dto.getQuota());
                if (dto.getDescription() != null) ticket.setDescription(dto.getDescription());
                if (dto.getSaleStart() != null) ticket.setSaleStart(dto.getSaleStart());
                if (dto.getSaleEnd() != null) ticket.setSaleEnd(dto.getSaleEnd());
                if (dto.getType() != null) ticket.setType(dto.getType());
            }
        };
    }
}
