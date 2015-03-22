<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>CS5300 Session State Manager</title>
</head>
<body>
<h1>${state}</h1>
<h1>timeout will occur at system clock time: ${timeout}</h1>
<form name="replaceform" method="post" action="SessionServlet">
	<input type="text" name="replacetext" />
	<input type="submit" name="replace" value="replace" />
</form>
<form name="sessionform" method="get" action="SessionServlet">
	<input type="submit" name="logout" value="logout" />
	<input type="submit" name="refresh" value="refresh" />
</form>
</body>
</html>