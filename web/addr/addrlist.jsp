<!--
This file is part of BORG.
 
    BORG is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.
 
    BORG is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with BORG; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
Copyright 2004 by ==Quiet==
-->
<%@ include file="../common/include.jsp" %>

<html>
<head>
<title>Address List</title>
</head>
<body>
<%@ include file="../common/header.jsp" %>

<table cellpadding="4" cellspacing="1" border="0" class="border" align="center">
<tr>

	<th>First</th>
	<th>Last</th>
	<th>Email</th>
	<th>Screen Name</th>
	<th>Home Phone</th>
	<th>Work Phone</th>
	<th>Birthday</th>
	<th></th>
	<th></th>
</tr>

<c:forEach var="addr" items="${sessionScope.addresses}" varStatus="status">
	<jsp:useBean id="addr" type="net.sf.borg.model.Address" />
	<c:choose> 
  		<c:when test="${status.count % 2 == 0}" > 
     		<tr class="oddRow">
  		</c:when> 
  		<c:otherwise> 
    		<tr class="evenRow">
 	 	</c:otherwise> 
	</c:choose> 
	    <td><c:if test="${!empty addr.firstName}"><%= addr.getFirstName() %></c:if></td>
		<td><c:if test="${!empty addr.lastName}"><%= addr.getLastName() %></c:if></td>
		<td><c:if test="${!empty addr.email}"><%= addr.getEmail() %></c:if></td>
		<td><c:if test="${!empty addr.screenName}"><%= addr.getScreenName() %></c:if></td>		
		<td><c:if test="${!empty addr.homePhone}"><%= addr.getHomePhone() %></c:if></td>		               
		<td><c:if test="${!empty addr.workPhone}"><%= addr.getWorkPhone() %></c:if></td>                
 		<td><c:if test="${!empty addr.birthday}"><%= java.text.DateFormat.getDateInstance(java.text.DateFormat.SHORT).format(addr.getBirthday()) %></c:if></td> 
 		<td><a href="editAddr.do?key=<%= addr.getKey() %>">Edit</a></td> 
 		<td><a href="delAddr.do?key=<%= addr.getKey() %>">Delete</a></td>               
	</tr>
</c:forEach>

</table>
<br>
<br>

<table cellpadding="4" cellspacing="1" border="0" align="center">
<tr><td><a href="newAddr.do">Add New Record</a></td></tr>
</table>

<br>
<%@ include file="../common/footer.jsp" %>
</body>
</html>
