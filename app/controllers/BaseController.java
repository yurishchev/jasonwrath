package controllers;

import play.mvc.Before;
import play.mvc.Controller;

public abstract class BaseController extends Controller {
    @Before
    static void globals() {
        Auth.checkRememberMeAndIfPresentAuthenticate();
        if (Auth.isConnected()) {
            renderArgs.put("connected", Auth.connectedUser());
        }
    }

}
