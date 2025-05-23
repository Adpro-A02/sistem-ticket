package id.ac.ui.cs.advprog.sistemticket.dto;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

public class StatusUpdateDto implements Serializable {
    @NotBlank(message = "Status is required")
    private String status;
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
