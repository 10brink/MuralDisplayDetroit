<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page import="co.grandcircus.BaddamBoseTenbrick.MuralDisplayDetroit.entity.User" %>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>All Art</title>
<link rel="stylesheet" type="text/css" href="design2.css" />
</head>

<body>

<h2>Gallery Top Favorites</h2>


<div class="dropdown">
  <button class="dropbtn"> Sort By</button>
  <div class="dropdown-content">
  <a href="/style">Style</a>
  <a href="#">Neighborhood</a>
  <a href="#">Year</a>
  <a href="#">Artist</a>
  </div>
</div>
<form action="addtofavorites">
<c:forEach items="${list}" var="l">
<<<<<<< HEAD
	<div>
	<img src="${l.imgloc}" height="500px"> 
	<br />
	Add to favorites? <input type="checkbox" class="favorites" name="favorites[]" value="${l.muralid}">
	${l.muralid}
	</div>
=======
	<img src="${l.imgloc}" style ='height:400px'> <br>
	Add to favorites? <input type="checkbox" class="favorites" name="favorites[]" value="${l.muralid}">
	${l.muralid}<br>
>>>>>>> a62585f00fd1464369addb7a3b76e13830f1f182
</c:forEach>
	<input type="hidden" name="favoritez" value="${userid}" >
	<input type="submit" value="submitfavorites">
 </form>
 



</body>
</html>