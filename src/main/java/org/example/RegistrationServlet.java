package org.example;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

public class RegistrationServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user;
        try {
            user = UserRepository.USER_REPOSITORY.getUserByCookies(req.getCookies());
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        if (user != null) {
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }

        RequestDispatcher requestDispatcher = req.getRequestDispatcher("registration.jsp");
        requestDispatcher.forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String login = req.getParameter("login");
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        try {
            if (login == null || UserRepository.USER_REPOSITORY.containsUserByLogin(login) || email == null || password == null) {
                return;
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        User user = new User(login, email, password);
        try {
            UserRepository.USER_REPOSITORY.addUser(user);
            UserRepository.USER_REPOSITORY.addUserBySession(CookieUtil.getValue(req.getCookies(), "JSESSIONID"), user);
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        resp.sendRedirect(req.getContextPath() + "/");
    }
}