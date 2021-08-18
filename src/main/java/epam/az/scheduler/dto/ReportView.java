package epam.az.scheduler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportView {

    private Long workerId;
    private LocalDate date;
    private String workDescription;
    private Integer hours;
    private String createdAt;
}
