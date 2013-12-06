package models;


import jasonwrath.exception.ApplicationException;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.*;

@Entity
@Table(name = "friend_group")
public class FriendGroup extends Model {

    @Required
    private String name;

    @Required
    @ManyToOne
    private User user;

    @ManyToMany
    private Set<User> members = new HashSet<User>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<User> getAllMembers() {
        return Collections.unmodifiableList(new ArrayList<User>(members));
    }

    public User getMember(Long id) {
        for (User member : members) {
            if (member.getId().equals(id)) {
                return member;
            }
        }
        return null;
    }

    public boolean hasMember(Long id) {
        return getMember(id) != null;
    }

    public FriendGroup addMember(Long id) throws ApplicationException {
        if (hasMember(id) || this.user.getId().equals(id)) {
            throw new ApplicationException("Member with id='" + id + "' already exists!");
        }
        User member = User.findById(id);
        members.add(member);
        return this;
    }

    public FriendGroup removeMember(Long id) throws ApplicationException {
        if (!hasMember(id)) {
            throw new ApplicationException("Member with id='" + id + "' doesn't exist!");
        }
        User member = User.findById(id);
        members.remove(member);
        return this;
    }

}
