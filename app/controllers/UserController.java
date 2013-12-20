package controllers;

import com.google.gson.JsonObject;
import models.User;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.i18n.Messages;

public class UserController extends BaseController {

    public static void profile() {
        User user = Auth.connectedUser();
        render(user);
    }

    public static void saveProfile(@Required String name) {
        User user = Auth.connectedUser();
        JsonObject obj = new JsonObject();
        if (Validation.hasErrors()) {
            Validation.keep();
            params.flash();
            obj.addProperty("errors", Messages.get("login.error"));
        } else {
            user.setName(name);
            user.save();
        }
        renderJSON(obj);
    }

}
