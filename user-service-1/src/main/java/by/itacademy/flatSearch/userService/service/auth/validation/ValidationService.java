package by.itacademy.flatSearch.userService.service.auth.validation;

import by.itacademy.flatSearch.userService.core.dto.UserLoginDTO;
import by.itacademy.flatSearch.userService.core.dto.UserRegistrationDTO;
import by.itacademy.flatSearch.userService.core.enums.ErrorFieldNames;
import by.itacademy.flatSearch.userService.core.enums.ValidationPattern;
import by.itacademy.flatSearch.userService.core.error.ErrorDetail;
import by.itacademy.flatSearch.userService.core.enums.messages.ErrorMessages;
import by.itacademy.flatSearch.userService.core.error.StructuredErrorResponse;
import by.itacademy.flatSearch.userService.core.exception.InternalServerException;
import by.itacademy.flatSearch.userService.core.exception.ValidationException;
import by.itacademy.flatSearch.userService.dao.api.ICRUDUserDao;
import by.itacademy.flatSearch.userService.dao.entity.User;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ValidationService implements IValidationService {
    private ICRUDUserDao crudUserDao;
    private StructuredErrorResponse errorsResponse;
    private PasswordEncoder passwordEncoder;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int REQUIRED_WORDS_IN_FIO = 3;

    public ValidationService(ICRUDUserDao crudUserDao, StructuredErrorResponse errorsResponse, PasswordEncoder passwordEncoder) {
        this.crudUserDao = crudUserDao;
        this.errorsResponse = errorsResponse;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void validateRegistration(UserRegistrationDTO registrationDTO) {
        errorsResponse = new StructuredErrorResponse();

        validateMail(registrationDTO.getMail());
        validateFio(registrationDTO.getFio());
        validatePassword(registrationDTO.getPassword());
        checkIfUserExists(registrationDTO);

        if (!errorsResponse.getErrors().isEmpty()) {
            throw new ValidationException(errorsResponse);
        }
    }

    @Override
    public void validateLogin(UserLoginDTO loginDTO) {
        errorsResponse = new StructuredErrorResponse();

        validateMail(loginDTO.getMail());
        validatePassword(loginDTO.getPassword());

        String correctPassword = getCorrectPassword(loginDTO.getMail());

        if (!passwordEncoder.matches(loginDTO.getPassword(), correctPassword)) {
            throw new ValidationException(ErrorMessages.INCORRECT_MAIL_OR_PASSWORD.getMessage());
        }

        if (!errorsResponse.getErrors().isEmpty()) {
            throw new ValidationException(errorsResponse);
        }
    }

    private void validateMail(String mail) {
        String regexPattern = ValidationPattern.EMAIL.getPattern();
        if (mail.isBlank()) {
            addError(ErrorFieldNames.MAIL.getField(), ErrorMessages.MAIL_IS_EMPTY.getMessage());
        }
        if (!mail.matches(regexPattern)) {
            addError(ErrorFieldNames.MAIL.getField(), ErrorMessages.INCORRECT_MAIL_FORMAT.getMessage());
        }
    }

    private void validatePassword(String password) {
        String regex = ValidationPattern.PASSWORD.getPattern();
        if (password.isBlank()) {
            addError(ErrorFieldNames.PASSWORD.getField(), ErrorMessages.PASSWORD_IS_EMPTY.getMessage());
        }
        if (password.length() < MIN_PASSWORD_LENGTH && !password.matches(regex)) {
            addError(ErrorFieldNames.PASSWORD.getField(), ErrorMessages.PASSWORD_LENGTH_REQUIREMENT.getMessage());
        }
    }

    private void validateFio(String fio) {
        String regex = ValidationPattern.FIO.getPattern();
        String[] words = fio.split("[ -]");
        if (fio.isBlank()) {
            addError(ErrorFieldNames.FIO.getField(), ErrorMessages.FIO_IS_EMPTY.getMessage());
        }

        if (!fio.matches(regex) && words.length != REQUIRED_WORDS_IN_FIO) {
            addError(ErrorFieldNames.FIO.getField(), ErrorMessages.INVALID_FIO.getMessage());
        }
    }

    private void checkIfUserExists(UserRegistrationDTO user) {
        if (crudUserDao.existsByMail(user.getMail())) {
            addError(ErrorFieldNames.MAIL.getField(), ErrorMessages.EMAIL_ALREADY_REGISTERED.getMessage());
        }
    }

    private void addError(String field, String message) {
        ErrorDetail error = new ErrorDetail(field, message);
        errorsResponse.addError(error);
    }
    private String getCorrectPassword(String mail) {
        try {
            Optional<User> user = crudUserDao.findByMail(mail);
            if (user.isEmpty()) {
                throw new ValidationException(ErrorMessages.USER_NOT_FOUND.getMessage());
            }
            return user.get().getPassword();
        } catch (DataAccessException e) {
            throw new InternalServerException(ErrorMessages.SERVER_ERROR.getMessage(), e);
        }
    }
    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }
}
