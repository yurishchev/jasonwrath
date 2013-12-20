package controllers;

import com.google.gson.JsonObject;
import jasonwrath.exception.ApplicationException;
import jasonwrath.utils.AllowGuest;
import models.SocialAccount;
import models.User;
import notifiers.Notifier;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.cache.Cache;
import play.data.validation.*;
import play.i18n.Messages;
import play.libs.Codec;
import play.libs.Crypto;
import play.mvc.Before;
import play.mvc.Http;
import securesocial.provider.IdentityProvider;
import securesocial.provider.ProviderRegistry;
import securesocial.provider.ProviderType;
import securesocial.provider.SocialUser;

import java.util.Collection;

public class Auth extends BaseController {

    private final static String COOKIE_USER_ID = "userId";
    private final static String COOKIE_REMEMBERME = "rememberme";
    private static final String ERROR = "error";
    private static final String SECURESOCIAL_AUTH_ERROR = "securesocial.authError";
    private static final String ROOT = "/";  // seems to be a good default url

    public static final String ORIGINAL_URL = "originalUrl";
    public final static String COOKIE_AUTH_ACCESSTOKEN = "authAccessToken";
    public final static String COOKIE_AUTH_PROVIDER = "authProvider";
    public static final String COOKIE_AUTH_AVATARURL = "authAvatarUrl";
    public static final String COOKIE_AUTH_EMAIL = "email";
    public static final String COOKIE_AUTH_NAME = "name";

    @Before(unless = {"login", "authenticate", "authenticateJson", "authenticateSocial", "logout", "signup", "register",
            "forgotPassword", "restorePassword", "changeForgotPassword", "passwordReset", "changePassword"})
    static void checkAccess() throws Throwable {
        AllowGuest guest = getControllerInheritedAnnotation(AllowGuest.class);
        if (guest != null) {
            for (String action : guest.value()) {
                if (action.equalsIgnoreCase(request.actionMethod))
                    return;
            }
        }

        if (!session.contains(COOKIE_USER_ID)) {
            login();
        } else {
            checkIfLoggedInUserCanProceed();
        }
    }

    @Before
    static void keepUrl() {
        flash.keep(Auth.ORIGINAL_URL); // TODO need to avoid this hack...
    }

    static Long connectedId() {
        try {
            String userId = session.get(COOKIE_USER_ID);
            return StringUtils.isNotBlank(userId) ? Long.parseLong(userId) : null;
        } catch (NumberFormatException nfe) {
            Logger.error(nfe, "Parse exception for userId=" + session.get(COOKIE_USER_ID));
        }
        return null;
    }

    static boolean isConnected() {
        return connectedId() != null;
    }

    static User connectedUser() {
        redirectIfNotConnected();
        return User.findById(connectedId());
    }

    static void checkIfLoggedInUserCanProceed() {
        User user = connectedUser();
        if (user == null) {
            flash.put(ORIGINAL_URL, getCurrentURL());
            flash.error(Messages.get("login.error"));
            redirect(ROOT);
            return;
        }
        checkBlocked(user);
        if (user.getConfirmationUID() != null && !"/auth/sendConfirmationEmail".equals(request.url)) {  // TODO
            redirect("/profile");
        }
    }

    private static void redirectIfNotConnected() {
        if (!isConnected()) { // f.e. session timeout, etc.
            flash.put(ORIGINAL_URL, getCurrentURL());
            redirect(ROOT);
        }
    }

    private static void checkBlocked(User user) {
        if (user.isBlocked()) {
            flash.error(Messages.get("login.blocked_user"));
            redirect(ROOT); // TODO show popup dialog instead of balloon message
        }
    }

    private static void connect(User user) {
        checkBlocked(user);
        if (user.getForgotPasswordUID() != null) {
            user.setForgotPasswordUID(null);
        }
        session.put(COOKIE_USER_ID, user.getId());
    }

    private static String getCurrentURL() {
        return "GET".equals(request.method) ? request.url : ROOT;
    }

    private static void redirectToOriginalURL() {
        String url = flash.get(ORIGINAL_URL);
        if (StringUtils.isBlank(url) || url.startsWith("/auth")) {
            url = ROOT;
        }
        redirect(url);
    }

    // ~~~ Login

    public static void login(String... currentUrl) {
        if (!flash.contains(ORIGINAL_URL) && currentUrl != null && currentUrl.length == 1) {
            flash.put(ORIGINAL_URL, currentUrl[0]);
        }
        checkRememberMeAndIfPresentAuthenticate();
        final Collection providers = ProviderRegistry.all();
        renderTemplate("/auth/login.html", providers);
    }

    static boolean checkRememberMeAndIfPresentAuthenticate() {
        Http.Cookie remember = request.cookies.get(COOKIE_REMEMBERME);
        if (remember != null && remember.value.indexOf("-") > 0) {
            String sign = remember.value.substring(0, remember.value.indexOf("-"));
            String userId = remember.value.substring(remember.value.indexOf("-") + 1);
            if (Crypto.sign(userId).equals(sign)) {
                session.put(COOKIE_USER_ID, userId);
                return true;
            }
        }
        return false;
    }

    public static void logout() {
        response.removeCookie(COOKIE_REMEMBERME);
        session.clear();
        redirect(ROOT);
    }

    /**
     * This is the entry point for all social authentication requests from the login page.
     * The type is used to invoke the right provider.
     *
     * @param type The provider type as selected by the user in the login page
     * @see ProviderType
     * @see securesocial.provider.IdentityProvider
     */
    public static void authenticateSocial(ProviderType type, String currentUrl) {
        if (!flash.contains(ORIGINAL_URL) && StringUtils.isNotEmpty(currentUrl)) {
            flash.put(ORIGINAL_URL, currentUrl);
        }
        doAuthenticate(type);
    }

    private static void doAuthenticate(ProviderType type) {
        if (type == null) {
            Logger.error("Provider type was missing in request");
            flash.error(Messages.get("login.error"));
            login();
        }

        IdentityProvider provider = ProviderRegistry.get(type);
        try {
            SocialUser socialUser = provider.authenticate();
            User user = SocialAccount.findUser(socialUser.id.provider, socialUser.id.id);
            if (user == null) {
                Logger.debug("Couldn't find existing user for accountToken=%s, provider=%s",
                        socialUser.id.provider, socialUser.id.id);
                setSocialUserInfo(socialUser);
                login();
            } else {
                connect(user);
                clearSocialUserInfo();
                redirectToOriginalURL();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error(e, "Error authenticating user");
            if (flash.get(ERROR) == null) {
                flash.error(Messages.get(SECURESOCIAL_AUTH_ERROR));
            }
            login();
        }
    }

    public static void authenticate(@Required String username, @Required String password, boolean remember, String currentUrl)
            throws Throwable {
        if (!flash.contains(ORIGINAL_URL) && StringUtils.isNotEmpty(currentUrl)) {
            flash.put(ORIGINAL_URL, currentUrl);
        }
        User user = User.connect(username, password);
        if (user == null || !user.checkPassword(password)) {
            flash.error(Messages.get("login.error"));
            Validation.keep();
            params.flash();
            login();
            return;
        }
        connect(user);
        if (session.get(COOKIE_AUTH_ACCESSTOKEN) != null) {
            SocialAccount account = user.addSocialAccount(ProviderType.valueOf(session.get(COOKIE_AUTH_PROVIDER)),
                    session.get(COOKIE_AUTH_ACCESSTOKEN));
            account.save();
        }
        if (remember) {
            response.setCookie(COOKIE_REMEMBERME, Crypto.sign(user.getId().toString()) + "-" + user.getId(), "30d");
        }
        clearSocialUserInfo();
        redirectToOriginalURL();
    }

    // Authentication via modal dialog and ajax call
    public static void authenticateJson(@Required String username, @Required String password, boolean remember)
            throws Throwable {
        JsonObject obj = new JsonObject();
        if (Validation.hasErrors()) {
            obj.addProperty("errors", Messages.get("validation.invalid.parameters"));
            renderJSON(obj);
            return;
        }
        User user = User.connect(username, password);
        if (user == null || !user.checkPassword(password)) {
            obj.addProperty("errors", Messages.get("login.error"));
            renderJSON(obj);
            return;
        }
        renderJSON(obj);
    }

    private static void setSocialUserInfo(SocialUser user) {
        session.put(COOKIE_AUTH_ACCESSTOKEN, user.id.id);
        session.put(COOKIE_AUTH_PROVIDER, user.id.provider);
        session.put(COOKIE_AUTH_EMAIL, user.email);
        session.put(COOKIE_AUTH_NAME, user.displayName);
        session.put(COOKIE_AUTH_AVATARURL, user.avatarUrl);
    }

    private static void clearSocialUserInfo() {
        session.remove(COOKIE_AUTH_ACCESSTOKEN);
        session.remove(COOKIE_AUTH_PROVIDER);
        session.remove(COOKIE_AUTH_EMAIL);
        session.remove(COOKIE_AUTH_NAME);
        session.remove(COOKIE_AUTH_AVATARURL);
    }

    public static void signup() {
        flash.put(COOKIE_AUTH_EMAIL, session.get(COOKIE_AUTH_EMAIL));
        flash.put(COOKIE_AUTH_NAME, session.get(COOKIE_AUTH_NAME));
        flash.put(COOKIE_AUTH_AVATARURL, session.get(COOKIE_AUTH_AVATARURL));
        String randomID = Codec.UUID();
        renderTemplate("/auth/signup.html", randomID);
    }

    public static void register(@Required @Email String username,
                                @Required String password,
                                @Equals("password") String repeatPassword,
                                @Required(message = "validation.required.captcha") String code,
                                String randomID,
                                @Required String name) throws Exception {
        validation.equals(code, Cache.get(randomID)).message("validation.invalid.captcha");
        if (Validation.hasErrors()) {
            Validation.keep();
            params.flash();
            flash.error(Messages.get("validation.invalid.parameters"));
            signup();
            return;
        }
        try {
            User user = User.create(username, password, name);
            if (session.get(COOKIE_AUTH_ACCESSTOKEN) != null) {
                user.addSocialAccount(ProviderType.valueOf(session.get(COOKIE_AUTH_PROVIDER)), session.get(COOKIE_AUTH_ACCESSTOKEN));
            }
            Cache.delete(randomID);
            if (Notifier.welcome(user)) {
                connect(user);
            } else {
                flash.error(Messages.get("mail.error"));
            }
            UserController.profile();
        } catch (ApplicationException ste) {
            Validation.keep();
            params.flash();
            flash.error(Messages.get("unique.user.error"));
            signup();
        }
    }

    public static void confirmRegistration(String uuid) {
        User user = User.findByRegistrationUUID(uuid);
        if (user == null) {
            flash.error(Messages.get("confirmation.error"));
        } else {
            user.setConfirmationUID(null);
            user.save();
            connect(user);
            flash.success(Messages.get("welcome.user", user.getName()));
        }
        redirect(ROOT);
    }

    public static void sendConfirmationEmail() throws Exception {
        JsonObject obj = new JsonObject();
        User user = connectedUser();
        if (user == null) {
            obj.addProperty("errors", Messages.get("not.logged_in.user"));
        } else {
            Notifier.welcome(user);
        }
        renderJSON(obj);
    }

    public static void forgotPassword() {
        String randomID = Codec.UUID();
        renderTemplate("/auth/forgotPassword.html", randomID);
    }

    public static void restorePassword(@Required @Email String email,
                                       @Required(message = "validation.required.captcha") String code,
                                       String randomID) throws Exception {
        validation.equals(code, Cache.get(randomID)).message("validation.invalid.captcha");
        if (Validation.hasErrors()) {
            Validation.keep();
            params.flash();
            forgotPassword();
        }
        Cache.delete(randomID);
        User user = User.findByEmail(email);
        if (user != null) {
            user.setForgotPasswordUID(Codec.UUID());
            user.save();
            Notifier.passwordRecovery(user);
            flash.success(Messages.get("forgotPassword.mail.success"));
            redirect(ROOT);
        } else {
            flash.error(Messages.get("no.user.found", email));
            forgotPassword();
        }
    }

    public static void changeForgotPassword(String uuid) {
        User user = User.findByForgotPasswordUUID(uuid);
        if (user == null) {
            flash.error(Messages.get("forgotPassword.error"));
            Application.index();
        } else {
            passwordReset(uuid);
        }
    }

    public static void passwordReset(String uuid) {
        renderTemplate("/auth/passwordReset.html", uuid);
        render(uuid);
    }

    public static void changePassword(@Required String uuid,
                                      @Required @MinSize(6) String password,
                                      @Equals("password") String password2) throws Exception {
        if (Validation.hasErrors()) {
            Validation.keep();
            params.flash();
            passwordReset(uuid);
        }
        User user = User.findByForgotPasswordUUID(uuid);
        if (user != null) {
            connect(user);
            user.setForgotPasswordUID(null);
            user.setPassword(password);
            user.save();
            flash.success(Messages.get("changePassword.success"));
            UserController.profile();
        } else {
            flash.error(Messages.get("no.user.found"));
            passwordReset(uuid);
        }
    }
}
