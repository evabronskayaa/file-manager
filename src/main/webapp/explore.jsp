<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <title>File Manager</title>
</head>
<body>
<form method="post">
    <input type="submit" name="exit" value="Exit"/>
</form>
<h3>${date}</h3>
<hr>
<h4>${currentPath}</h4>
<hr>

<a href="?path=${currentPath.substring(0, currentPath.lastIndexOf("\\") + (currentPath.lastIndexOf("\\") != currentPath.indexOf("\\") ? 0 : 1))}">Up</a><br>
<c:forEach var="directory" items="${directories}">
    <a href="?path=${directory.getAbsolutePath()}">${directory.getName()}/</a><br>
</c:forEach>
<c:forEach var="file" items="${files}">
    <a href="?path=${file.getAbsolutePath()}">${file.getName()}</a><br>
</c:forEach>
</body>
</html>