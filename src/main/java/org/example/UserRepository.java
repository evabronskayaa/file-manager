package org.example;

import javax.servlet.http.Cookie;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class UserRepository {
    public static final UserRepository USER_REPOSITORY = new UserRepository();

    public User getUserByCookies(Cookie[] cookies) throws SQLException, ClassNotFoundException {
        String session;
        User user;
        if ((session = CookieUtil.getValue(cookies, "JSESSIONID")) == null || (user = getUser("session", session)) == null) {
            return null;
        }

        return user;
    }

    public User getUser(String filter, String arg) throws SQLException, ClassNotFoundException {
        try (Connection connection = getConnection()) {
            PreparedStatement st = connection.prepareStatement("SELECT login, password, email FROM users WHERE " + filter + " = ?");
            st.setString(1, arg);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return new User(rs.getString("login"), rs.getString("password"), rs.getString("email"));
            } else {
                return null;
            }
        }
    }

    public void addUser(User user) throws SQLException, ClassNotFoundException {
        try (Connection connection = getConnection()) {
            PreparedStatement st = connection.prepareStatement("INSERT INTO users (login, password, email) VALUES (?, ?, ?)");
            st.setString(1, user.getLogin());
            st.setString(2, user.getPassword());
            st.setString(3, user.getEmail());
            st.executeUpdate();
        }
    }

    public void addUserBySession(String session, User user) throws SQLException, ClassNotFoundException {
        try (Connection connection = getConnection()) {
            PreparedStatement st = connection.prepareStatement("UPDATE users SET session = ? WHERE login = ?");
            st.setString(1, session);
            st.setString(2, user.getLogin());
            st.executeUpdate();
        }
    }

    public void removeUserBySession(String session) throws SQLException, ClassNotFoundException {
        try (Connection connection = getConnection()) {
            PreparedStatement st = connection.prepareStatement("UPDATE users SET session = ? WHERE session = ?");
            st.setString(1, null);
            st.setString(2, session);
            st.executeUpdate();
        }
    }

    public boolean containsUserByLogin(String login) throws SQLException, ClassNotFoundException {
        try (Connection connection = getConnection()) {
            PreparedStatement st = connection.prepareStatement("SELECT * FROM users WHERE login = ?");
            st.setString(1, login);
            ResultSet rs = st.executeQuery();
            return rs.next();
        }
    }

    private Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/database", "root", "root");
    }
}

