package controllers;

import play.cache.Cache;
import play.libs.Images;
import play.mvc.Controller;

public class Application extends Controller {

    public static void index() {
        countDown();
    }

    public static void angular() {
        render();
    }

    public static void countDown() {
        render();
    }

    public static void about() {
        render();
    }

    public static void captcha(String id) {
        Images.Captcha captcha = Images.captcha();
        captcha.addNoise();
        String code = captcha.getText("#555555", 5, "abcdefghijkmnopqrstuvwxyz23456789");
        Cache.set(id, code);
        renderBinary(captcha);
    }


}