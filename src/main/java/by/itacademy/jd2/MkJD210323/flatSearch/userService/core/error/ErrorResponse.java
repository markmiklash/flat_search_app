package by.itacademy.jd2.MkJD210323.flatSearch.userService.core.error;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String logRef;
    private String message;
}
