package by.itacademy.flatSearch.userService.core.exceptions.exceptions;

import by.itacademy.flatSearch.userService.core.enums.ErrorsTypes;
import by.itacademy.flatSearch.userService.core.error.ErrorResponse;
import by.itacademy.flatSearch.userService.core.error.StructuredErrorResponse;
import lombok.Getter;

@Getter
public class AccountActivationException extends RuntimeException {
    private StructuredErrorResponse structuredErrorResponse;
    private ErrorResponse errorResponse;
    private boolean isStructuredError;

    public AccountActivationException(StructuredErrorResponse errorResponse) {
        isStructuredError = true;
        this.structuredErrorResponse = errorResponse;
        errorResponse.setLogRef(ErrorsTypes.STRUCTURED_ERROR.getMessage());
    }

    public AccountActivationException(String message) {
        super(message);
        errorResponse = new ErrorResponse();
        errorResponse.setMessage(message);
        errorResponse.setLogRef(ErrorsTypes.ERROR.getMessage());
    }
}
