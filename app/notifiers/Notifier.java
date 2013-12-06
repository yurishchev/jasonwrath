package notifiers;

import models.User;
import play.mvc.Mailer;

import javax.mail.internet.InternetAddress;

public class Notifier extends Mailer {

    public static boolean welcome(User user) {
        setFrom("Administrator");
        setSubject("Welcome %s", user.getName());
        addRecipient(user.getEmail());
        return sendAndWait(user);
    }

    public static boolean passwordRecovery(User user) {
        setFrom("Administrator");
        setSubject("Password recovery");
        addRecipient(user.getEmail());
        return sendAndWait(user);  // TODO not sure we should wait here..
    }
}