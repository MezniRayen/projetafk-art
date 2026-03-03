package tn.hounayda.utils;
import tn.hounayda.entities.UserRole;
import tn.hounayda.entities.Users;
import tn.hounayda.services.UserService;

import java.util.Date;

public class Session {
    private static Session instance;
    private Users currentUser;
    private Date loginTime;

    private Session() {}

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public void login(Users user) {
        this.currentUser = user;
        this.loginTime = new Date();
        // Update lastLogin in DB
        user.setLastLogin(new Date());
        UserService userService = new UserService();
        userService.updateUser(user);
    }

    public void logout() {
        this.currentUser = null;
        this.loginTime = null;
    }

    public Users getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return isLoggedIn() && currentUser.getRole() == UserRole.ADMIN;
    }

    public Date getLoginTime() {
        return loginTime;
    }
}