package org.example;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@WebServlet(urlPatterns = "/")
public class FileManagerServlet extends HttpServlet {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        User user = UserRepository.USER_REPOSITORY.getUserByCookies(req.getCookies());
        if (user == null) {
            resp.sendRedirect("/login");
            return;
        }

        String path = req.getParameter("path");
        if (path == null || !path.startsWith("/home/" + user.getLogin() + "/")) {
            path = "/home/" + user.getLogin() + "/";
        }

        path = path.replaceAll("%20", " ");

        File currentPath = new File(path);
        if (!currentPath.exists()) {
            currentPath.mkdir();
        }

        if (currentPath.isDirectory()) {
            showFiles(req, currentPath);

            req.setAttribute("date", DATE_FORMAT.format(new Date()));
            req.setAttribute("currentPath", path);

            RequestDispatcher requestDispatcher = req.getRequestDispatcher("explore.jsp");
            requestDispatcher.forward(req, resp);
        } else {
            downloadFile(resp, currentPath);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (req.getParameter("exit") != null) {
            UserRepository.USER_REPOSITORY.removeUserBySession(CookieUtil.getValue(req.getCookies(), "JSESSIONID"));
            CookieUtil.addCookie(resp, "JSESSIONID", null);
            resp.sendRedirect("/");
        }
    }

    private void downloadFile(HttpServletResponse resp, File file) throws IOException {
        resp.setContentType("text/plain");
        resp.setHeader("Content-disposition", "attachment; filename=" + file.getName());

        try (InputStream in = Files.newInputStream(file.toPath()); OutputStream out = resp.getOutputStream()) {
            byte[] buffer = new byte[1048];

            int numBytesRead;
            while ((numBytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, numBytesRead);
            }
        }
    }

    private void showFiles(HttpServletRequest req, File currentPath) {
        File[] allFiles = currentPath.listFiles();
        if (allFiles == null) {
            return;
        }
        List<File> directories = new ArrayList<>();
        List<File> files = new ArrayList<>();
        for (File file : allFiles) {
            (file.isDirectory() ? directories : files).add(file);
        }
        req.setAttribute("files", files);
        req.setAttribute("directories", directories);
    }
}