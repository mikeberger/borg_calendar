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
<title>Task List</title>
</head>
<body>
<%@ include file="../common/header.jsp" %>


<c:if test="${param['filter'] != \"all\"}">
<a href="listTask.do?filter=all">Show All Tasks</a>
</c:if>
<c:if test="${param['filter'] == \"all\"}">
<a href="listTask.do?filter=open">Show Only Open Tasks</a>
</c:if>
<br>
<br>
<br>

<table cellpadding="4" cellspacing="1" border="0" class="border" align="center">
<tr>

	<th>Task #</th>
	<th>Status</th>
	<th>Type</th>
	<th>Category</th>
	<th>Start Date</th>
	<th>Due Date</th>
	<th>Description</th>
	<th></th>
	<th></th>
</tr>

<c:forEach var="task" items="${sessionScope.tasks}" varStatus="status">
	<jsp:useBean id="task" type="net.sf.borg.model.Task" />
	<c:choose> 
  		<c:when test="${status.count % 2 == 0}" > 
     		<tr class="oddRow">
  		</c:when> 
  		<c:otherwise> 
    		<tr class="evenRow">
 	 	</c:otherwise> 
	</c:choose> 
	    <td><c:if test="${!empty task.taskNumber}"><%= task.getTaskNumber() %></c:if></td>
	    <td><c:if test="${!empty task.state}"><%= task.getState() %></c:if></td>
	    <td><c:if test="${!empty task.type}"><%= task.getType() %></c:if></td>
	    <td><c:if test="${!empty task.category}"><%= task.getCategory() %></c:if></td>
 		<td><c:if test="${!empty task.startDate}"><%= java.text.DateFormat.getDateInstance(java.text.DateFormat.SHORT).format(task.getStartDate()) %></c:if></td>
 		<td><c:if test="${!empty task.dueDate}"><%= java.text.DateFormat.getDateInstance(java.text.DateFormat.SHORT).format(task.getDueDate()) %></c:if></td> 
 		<td><c:if test="${!empty task.description}"><%= task.getDescription() %></c:if></td>
 		<td><a href="editTask.do?key=<%= task.getKey() %>">Edit</a></td> 
 		<td><a href="delTask.do?key=<%= task.getKey() %>">Delete</a></td>               
	</tr>
</c:forEach>

</table>
<br>
<br>

<table cellpadding="4" cellspacing="1" border="0" align="center">
<tr><td><a href="newTask.do">Add New Task</a></td></tr>
</table>

<br>

</body>
<%@ include file="../common/footer.jsp" %>
</html>
