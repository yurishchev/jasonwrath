package models;

import play.data.validation.Required;
import play.db.jpa.Model;
import securesocial.provider.ProviderType;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "social_account")
public class SocialAccount extends Model {
    @Required
    @ManyToOne
    private User user;

    @Required
    @Enumerated(EnumType.STRING)
    private ProviderType provider;

    @Required
    private String accessToken;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ProviderType getProvider() {
        return provider;
    }

    public void setProvider(ProviderType provider) {
        this.provider = provider;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public static User findUser(ProviderType provider, String token) {
        List<SocialAccount> accounts = SocialAccount.find("provider = ? and accessToken = ?", provider, token).fetch();
        if (accounts.size() == 1) {
            return accounts.get(0).getUser();
        }
        return null;
    }

}
