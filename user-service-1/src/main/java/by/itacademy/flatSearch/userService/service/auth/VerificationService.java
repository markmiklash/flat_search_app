package by.itacademy.flatSearch.userService.service.auth;

import by.itacademy.flatSearch.userService.core.enums.messages.ErrorMessages;
import by.itacademy.flatSearch.userService.core.enums.UserStatus;
import by.itacademy.flatSearch.userService.core.exception.custom_exceptions.ValidationException;
import by.itacademy.flatSearch.userService.dao.api.IMailQueueDao;
import by.itacademy.flatSearch.userService.dao.entity.User;
import by.itacademy.flatSearch.userService.dao.entity.VerificationMailEntity;
import by.itacademy.flatSearch.userService.service.auth.api.IVerificationService;
import by.itacademy.flatSearch.userService.service.user.api.IUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class VerificationService implements IVerificationService {
    private final IMailQueueDao verificationDao;
    private final IUserService userService;


    public VerificationService(IMailQueueDao verificationDao, IUserService userService) {
        this.verificationDao = verificationDao;
        this.userService = userService;
    }

    @Override
    @Transactional
    public void verify(VerificationMailEntity verificationMailEntity) {
        VerificationMailEntity storedEntity = verificationDao.findByMailAndCode(verificationMailEntity.getMail(),
                verificationMailEntity.getCode()).orElseThrow(() ->
                new ValidationException(ErrorMessages.INCORRECT_VERIFICATION_CODE.getMessage()));

        User user = userService.get(storedEntity.getMail());

        user.setStatus(UserStatus.ACTIVATED);
        userService.save(user);
        verificationDao.delete(storedEntity);     // Delete user im mail Queue table
    }
}
