package be.uantwerpen.fti.se.imagineframe_backend.model.dto;

import be.uantwerpen.fti.se.imagineframe_backend.label.EventLabel;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class EventEditDto {
    @NotBlank(message = "name is required")
    private String name;
    @NotBlank(message = "description is required")
    private String description;
    @NotBlank(message = "location is required")
    private String location;
    @NotNull
    private LocalDateTime startdate;
    @NotNull
    private LocalDateTime enddate;
    private Long id;
    private EventLabel label = EventLabel.OTHER;
    private String imageUrl;
    private boolean isPublic = true;

    /**
     * validates with @AssertTrue that the end date is later in time than the start date
     *
     * @return true if end date is later than start date or if one of the two dates is equal to null (null validation checked with @NotNull)
     */
    @AssertTrue(message = "End date must be later than the startdate")
    private boolean isEndDateValid() {
        if (startdate == null || enddate == null) {
            return true; // return true because @NotNull will fail
        }
        return enddate.isAfter(startdate);
    }

    @AssertTrue(message = "Start date cannot be in the past")
    private boolean isStartDateValid() {
        if (startdate == null) {
            return true; // return true here to let @NotNull handle null validation separately
                        // if we returned false we wouldn't then know if validation failed due to null or because of the past date
        }
        return !startdate.isBefore(LocalDateTime.now());
    }
}
