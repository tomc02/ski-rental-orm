package cs.vsb.ski_rental.database.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
public class Rental {
    private int rentalId;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;
    private String rentalStatus;
    private LocalDateTime dateReturned;
    private int customerId;
    private int employeeId;
    private List<Ski> rentedSkis;
}