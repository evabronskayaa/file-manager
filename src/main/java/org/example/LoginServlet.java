package org.example;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

public class LoginServlet extends HttpServlet {
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

        RequestDispatcher requestDispatcher = req.getRequestDispatcher("login.jsp");

        req.setAttribute("contextPath", req.getContextPath());

        requestDispatcher.forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String login = req.getParameter("login");
        String password = req.getParameter("password");

        if (login == null || password == null) {
            return;
        }

        try {
            User user = UserRepository.USER_REPOSITORY.getUser("login", login);
            if (user == null || !user.getPassword().equals(password)) {
                resp.sendRedirect(req.getContextPath() + "/login");
                return;
            }

            UserRepository.USER_REPOSITORY.addUserBySession(CookieUtil.getValue(req.getCookies(), "JSESSIONID"), user);
            resp.sendRedirect(req.getContextPath() + "/");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}