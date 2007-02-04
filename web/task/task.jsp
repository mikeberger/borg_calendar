
<%@ include file="../common/include.jsp" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<html>
<head>
<title>Task View</title>
</head>
<body>
<%@ include file="../common/header.jsp" %>
<html:form action="/task/saveTask.do">
<html:hidden property="key"/>
<table border="0" cellpadding="20" cellspacing="10" align="center">
<tr><td>
<table cellpadding="4" cellspacing="1" border="0" class="border" align="center">
<tr><th colspan="2">Task Details</th></tr>

<tr><td align="right">Task Number:</td><td><html:text property="taskNumber" readonly="true" size="5" /></td></tr>
<tr><td align="right">Type:</td><td><html:text property="type" readonly="true" size="15" /></td></tr>

<tr><td align="right">Status:</td>
	<td><html:select property="state">
	<c:forEach var="state" items="${sessionScope.task_states}">
		<jsp:useBean id="state" type="String" />
		<html:option value="<%= state %>"/>
	</c:forEach>
	</html:select></td>
</tr>

<tr><td align="right">Start Date:</td><td><html:text property="startDate" size="15" /></td></tr>
<tr><td align="right">Due Date:</td><td><html:text property="dueDate" size="15" /></td></tr>
<tr><td align="right">Priority:</td><td><html:text property="priority" size="15" /></td></tr>
<tr><td align="right">Person Assigned:</td><td><html:text property="personAssigned" size="15" /></td></tr>
<tr><td align="right">Category:</td><td><html:text property="category" size="15" /></td></tr>
</table>
</td>
<td>
<br>

<table cellpadding="4" cellspacing="1" border="0" class="border" align="center">
<tr><th>Subtasks</th></tr>
</table>
<br>
<table cellpadding="4" cellspacing="1" border="0" class="border" align="center">
<tr><th>User Tasks</th></tr>
</table>
</td>
<td>

<table cellpadding="4" cellspacing="1" border="0" class="border" align="center">
<tr><th>Description</th></tr>
<tr><td><html:textarea property="description" cols="40" rows="10"/></td></tr>
</table>
<br>

<table cellpadding="4" cellspacing="1" border="0" class="border" align="center">
<tr><th>Resolution</th></tr>
<tr><td><html:textarea property="resolution" cols="40" rows="10"/></td></tr>
</table>

</td>
</tr>
</table>
<br>
<br>
<table cellpadding="4" cellspacing="1" border="0" align="center">
<tr><td><button type="submit" name="save" value="yes" class="Button">Save</button></td>
	<td><button type="reset" class="Button">Reset</button></td>
</tr>
</table>
</html:form>
<br>
<html:errors/>
<br>
<%@ include file="../common/footer.jsp" %>
</body>
</html>
