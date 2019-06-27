<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Neighborhood</title>
</head>
<body>

	<form action="addtofavorites">
		<c:forEach items="${list}" var="l">
			<h2>Neighborhood: ${l.key }</h2>
			<c:forEach items="${l.value }" var="m">
				<img src="${m.imgloc }" style='height: 400px'>
				<br>
				<a href="https://www.google.com/maps/dir//${m.address }"
					target="_blank">${m.address }</a>
				<br>
	${m.artistname }
	<br>
	Add to favorites? <input type="checkbox" class="favorites"
					name="favorites[]" value="${m.muralid}">
				<br>
			</c:forEach>
		</c:forEach>
		<br> <input type="hidden" name="favoritez" value="${userid}">
		<input type="submit" value="submitfavorites">
	</form>
	<br>

	<a href="userpage"> Return to user page </a>

</body>
</html>