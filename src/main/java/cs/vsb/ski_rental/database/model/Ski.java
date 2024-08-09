package cs.vsb.ski_rental.database.model;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
@Getter
@Setter
@Builder
public class Ski {
    private int skiId;
    private String brand;
    private String model;
    private int length;
    private BigDecimal price;
    private String description;
    private String skierExperience;
    private int categoryId;
    private LocalDate deletedAt;
    private int countOfAllRentals;
    private int countOfActiveRentals;
}
