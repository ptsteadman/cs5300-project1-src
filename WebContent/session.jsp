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
<h1>local server id: ${local}</h1>
<h1>found at: ${found}</h1>
<h1>primary ip: ${primary}</h1>
<h1>primary timeout: ${timeout1}</h1>
<h1>backup ip: ${backup}</h1>
<h1>backup timeout: ${timeout2}</h1>
<h1>view: ${view}</h1>

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