package models;

import jasonwrath.exception.ApplicationException;
import play.data.validation.Email;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.libs.Codec;
import securesocial.provider.ProviderType;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "user",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"email"})})
public class User extends Model {
    @Required
    @Email
    @Column(unique = true)
    private String email;

    @Required
    private String password;

    @Required
    @MaxSize(100)
    private String name;

    @Lob
    private String description;

    private String confirmationUID;

    private String forgotPasswordUID;

    private boolean blocked;

    @Column(unique = true)
    private String token;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<SocialAccount> socialAccounts = new HashSet<SocialAccount>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<FriendGroup> friendGroups = new HashSet<FriendGroup>();

    @ManyToMany(mappedBy = "members")
    private Set<FriendGroup> membershipGroups = new HashSet<FriendGroup>();


    public static User create(String email, String password, String name) throws ApplicationException {
        if (findByEmail(email) != null) {
            throw new ApplicationException("There's already a user with such email in database!");
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setName(name);
        user.setConfirmationUID(Codec.UUID());
        user.create();
        return user;
    }

    public void setPassword(String password) {
        this.password = encryptPassword(password);
        this.token = Codec.hexMD5(this.email + this.password);
    }

    public boolean checkPassword(String password) {
        return this.password.equals(encryptPassword(password));
    }

    public static User connect(String email, String password) {
        return find("email = ? and password = ?", email, encryptPassword(password)).first();
    }

    private static String encryptPassword(String password) {
        return Codec.hexMD5(password);
    }

    public static User findByEmail(String email) {
        return find("email", email).first();
    }

    public static User findByToken(String token) {
        return find("token", token).first();
    }

    public static User findByRegistrationUUID(String uuid) {
        return find("confirmationUID", uuid).first();
    }

    public static User findByForgotPasswordUUID(String uuid) {
        return find("forgotPasswordUID", uuid).first();
    }

    public String getFullName() {
        return (name != null) ? name : email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getConfirmationUID() {
        return confirmationUID;
    }

    public void setConfirmationUID(String confirmationUID) {
        this.confirmationUID = confirmationUID;
    }

    public String getForgotPasswordUID() {
        return forgotPasswordUID;
    }

    public void setForgotPasswordUID(String forgotPasswordUID) {
        this.forgotPasswordUID = forgotPasswordUID;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    @Override
    public String toString() {
        return email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Set<SocialAccount> getSocialAccounts() {
        return Collections.unmodifiableSet(socialAccounts);
    }

    public SocialAccount addSocialAccount(ProviderType provider, String token) {
        SocialAccount account = new SocialAccount();
        account.setProvider(provider);
        account.setAccessToken(token);
        account.setUser(this);
        socialAccounts.add(account);
        return account;
    }

    public List<FriendGroup> getFriendGroups() {
        return Collections.unmodifiableList(new ArrayList<FriendGroup>(friendGroups));
    }

    public FriendGroup getFriendGroup(Long id) {
        for (FriendGroup group : friendGroups) {
            if (group.getId().equals(id)) {
                return group;
            }
        }
        return null;
    }

    public FriendGroup createFriendGroup(String name) throws ApplicationException {
        for (FriendGroup group : friendGroups) {
            if (group.getName().equalsIgnoreCase(name)) {
                throw new ApplicationException("Group with name '" + name + "' already exists!");
            }
        }
        FriendGroup group = new FriendGroup();
        group.setName(name);
        group.setUser(this);
        friendGroups.add(group);
        return group;
    }
}
