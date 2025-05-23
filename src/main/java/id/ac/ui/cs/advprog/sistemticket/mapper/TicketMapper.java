package id.ac.ui.cs.advprog.sistemticket.mapper;

import id.ac.ui.cs.advprog.sistemticket.dto.TicketCreationDto;
import id.ac.ui.cs.advprog.sistemticket.dto.TicketDto;
import id.ac.ui.cs.advprog.sistemticket.dto.TicketUpdateDto;
import id.ac.ui.cs.advprog.sistemticket.model.Ticket;
import org.springframework.stereotype.Component;

@Component
public class TicketMapper {

    public TicketDto toDto(Ticket ticket) {
        if (ticket == null) {
            return null;
        }
        
        TicketDto dto = new TicketDto();
        dto.setId(ticket.getId());
        dto.setEventId(ticket.getEventId());
        dto.setType(ticket.getType());
        dto.setPrice(ticket.getPrice());
        dto.setQuota(ticket.getQuota());
        dto.setRemainingQuota(ticket.getRemainingQuota());
        dto.setDescription(ticket.getDescription());
        dto.setSaleStart(ticket.getSaleStart());
        dto.setSaleEnd(ticket.getSaleEnd());
        dto.setStatus(ticket.getStatus());
        
        return dto;
    }
    
    public Ticket toEntity(TicketCreationDto dto) {
        if (dto == null) {
            return null;
        }
        
        return new Ticket(
            dto.getEventId(),
            dto.getType(),
            dto.getPrice(),
            dto.getQuota(),
            dto.getDescription(),
            dto.getSaleStart(),
            dto.getSaleEnd()
        );
    }
    
    public void updateEntityFromDto(TicketUpdateDto dto, Ticket ticket) {
        if (dto == null || ticket == null) {
            return;
        }
        
        if (dto.getType() != null) {
            ticket.setType(dto.getType());
        }
        
        if (dto.getPrice() != null) {
            ticket.setPrice(dto.getPrice());
        }
        
        if (dto.getQuota() != null) {
            ticket.setQuota(dto.getQuota());
        }
        
        if (dto.getDescription() != null) {
            ticket.setDescription(dto.getDescription());
        }
        
        if (dto.getSaleStart() != null) {
            ticket.setSaleStart(dto.getSaleStart());
        }
        
        if (dto.getSaleEnd() != null) {
            ticket.setSaleEnd(dto.getSaleEnd());
        }
    }
}
