package org.example;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileManagerServlet extends HttpServlet {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private static final boolean WIN = System.getProperty("os.name").toLowerCase().startsWith("win");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        User user = UserRepository.USER_REPOSITORY.getUserByCookies(req.getCookies());
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String path = req.getParameter("path");
        String startPath = WIN ? "C:" + File.separator + user.getLogin() + File.separator : "/home/" + user.getLogin() + "/";
        if (path == null || !path.startsWith(startPath)) {
            path = startPath;
        }

        path = path.replaceAll("%20", " ");

        File currentPath = new File(path);
        if (!currentPath.exists()) {
            currentPath.mkdirs();
        }

        if (currentPath.isDirectory()) {
            showFiles(req, currentPath);

            req.setAttribute("date", DATE_FORMAT.format(new Date()));
            req.setAttribute("currentPath", path);
            req.setAttribute("upPath", path.substring(0, path.lastIndexOf(File.separator) + (path.lastIndexOf(File.separator) != path.indexOf(File.separator) ? 0 : 1)));

            RequestDispatcher requestDispatcher = req.getRequestDispatcher("explore.jsp");
            requestDispatcher.forward(req, resp);
        } else {
            downloadFile(resp, currentPath);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (req.getParameter("exit") != null) {
            CookieUtil.addCookie(resp, "login", null);
            CookieUtil.addCookie(resp, "password", null);
            resp.sendRedirect(req.getContextPath() + "/");
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
