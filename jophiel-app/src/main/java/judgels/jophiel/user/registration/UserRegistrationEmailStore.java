package judgels.jophiel.user.registration;

import java.util.Optional;
import javax.inject.Inject;
import judgels.service.RandomCodeGenerator;

public class UserRegistrationEmailStore {
    private final UserRegistrationEmailDao userRegistrationEmailDao;

    @Inject
    public UserRegistrationEmailStore(UserRegistrationEmailDao userRegistrationEmailDao) {
        this.userRegistrationEmailDao = userRegistrationEmailDao;
    }

    public boolean isUserActivated(String userJid) {
        return !userRegistrationEmailDao.findByUserJid(userJid)
                .flatMap(model -> model.verified ? Optional.empty() : Optional.of(false))
                .isPresent();
    }

    public String generateEmailCode(String userJid) {
        UserRegistrationEmailModel model = new UserRegistrationEmailModel();
        model.userJid = userJid;
        model.emailCode = RandomCodeGenerator.newCode();
        userRegistrationEmailDao.insert(model);
        return model.emailCode;
    }

    public boolean verifyEmailCode(String emailCode) {
        return userRegistrationEmailDao.findByEmailCode(emailCode).flatMap(model -> {
            if (model.verified) {
                return Optional.empty();
            }
            model.verified = true;
            userRegistrationEmailDao.update(model);
            return Optional.of(true);
        }).isPresent();
    }
}
