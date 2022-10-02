package org.example;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Main extends HttpServlet {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String path = req.getParameter("path");
        if (path == null) {
            path = "/";
        }

        path = path.replaceAll("%20", " ");

        File currentPath = new File(path);
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

    private void downloadFile(HttpServletResponse resp, File file) throws IOException {
        resp.setContentType("text/plain");
        resp.setHeader("Content-disposition", "attachment; filename=" + file.getName());

        try (InputStream in = new FileInputStream(file); OutputStream out = resp.getOutputStream()) {
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