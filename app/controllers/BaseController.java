package controllers;

import play.mvc.Before;
import play.mvc.Controller;
import securesocial.provider.ProviderRegistry;

import java.util.Collection;

public abstract class BaseController extends Controller {
    @Before
    static void globals() {
        Auth.checkRememberMeAndIfPresentAuthenticate();
        final Collection providers = ProviderRegistry.all();
        renderArgs.put("providers", providers);
        if (Auth.isConnected()) {
            renderArgs.put("connected", Auth.connectedUser());
        }
    }

}
