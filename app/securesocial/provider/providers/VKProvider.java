/**
* Copyright 2011 Jorge Aliss (jaliss at gmail dot com) - twitter: @jaliss
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*/
package securesocial.provider.providers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import play.Logger;
import play.libs.WS;
import securesocial.provider.*;

import java.util.Map;

/**
 * A Twitter Provider
 */
public class VKProvider extends OAuth2Provider
{
    private static final String SELF_API = "https://api.vk.com/method/getProfiles?fields=uid,first_name,last_name,photo&access_token=%s";

    private static final String ID = "uid";
    private static final String FIRST_NAME = "first_name";
    private static final String LAST_NAME = "last_name";
    private static final String PHOTO = "photo";
    private static final String EMAIL = "email";

    private static final String RESPONSE = "response";
    private static final String ERROR = "error";
    private static final String ERROR_CODE = "error_code";
    private static final String ERROR_MESSAGE = "error_code";

    public VKProvider() {
        super(ProviderType.vk);
    }

    @Override
    protected void fillProfile(SocialUser user, Map<String, Object> authContext) {
        JsonObject me = WS.url(SELF_API, user.accessToken).get().getJson().getAsJsonObject();

        JsonArray response = me.getAsJsonArray(RESPONSE);
        if ( response == null ) {
            throw new AuthenticationException();
        }

        JsonObject userInfo = (JsonObject)response.get(0);
        if( userInfo == null ) {
            throw new AuthenticationException();
        }

        user.id.id = userInfo.get(ID).getAsString();
        user.displayName = userInfo.get(FIRST_NAME).getAsString();
        final JsonElement lastName = userInfo.get(LAST_NAME);
        if ( lastName != null ) {
            user.displayName = fullName(user.displayName, lastName.getAsString());
        }
        user.avatarUrl = userInfo.get(PHOTO).getAsString();
    }

    static String fullName(String first, String last) {
        return first + " " + last;
    }
}