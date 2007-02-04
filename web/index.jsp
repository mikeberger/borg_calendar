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
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<html>
<head>
<title>BorgWeb Main Page</title>
<html:base/>
</head>
<%@ include file="common/include.jsp" %>
<body>
<a href="mainmenu.do"><img src="images/logo.gif" border="0"></a>
<hr style="width: 100%; height: 1px;">

<br>
<% if( request.isUserInRole("borg_user")) { %>
<table class="border" width="30%" cellpadding="4" cellspacing="1" border="0" align="center">
	<tr><th height="25" nowrap="nowrap">User Menu</th></tr>
	<tr><td align="center"><a href="appt/listAppt.do">Calendar</a></td></tr>
	<tr><td align="center"><a href="task/listTask.do?filter=open">Task Tracking</a></td></tr>
	<tr><td align="center"><a href="addr/listAddr.do">Address Book</a></td></tr>
	<tr><td align="center"><a href="user/chgPass.do">Change Password</a></td></tr>
</table>
<br>
<br>
<% } %>
<% if( request.isUserInRole("borg_admin")) { %>
<table class="border" width="30%" cellpadding="4" cellspacing="1" border="0" align="center">
	<tr><th height="25" nowrap="nowrap">Administration Menu</th></tr>
	<tr><td align="center"><a href="admin/userAdmin.do">User Administration</a></td></tr>
</table>
<br>
<% } %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<div style="text-align: left;">
<hr style="width: 100%; height: 1px;">
<table class="footer" cellpadding="2" cellspacing="0" border="0"
 style="text-align: left; width: 100%;">
  <tbody>
    <tr><td>
      <form method="get" action="mainmenu.do">
      <button type="submit" name="main" value="yes" class="Button">Main Menu</button>
      </form>
      </td>
      <td align="center">
      <c:choose>
	      <c:when test="${!empty request.remoteUser}">
				Not logged in
	      </c:when>
	      <c:otherwise>
			Logged in as: <%= request.getRemoteUser() %>
	      </c:otherwise>
      </c:choose>
	</td>
      <td align="right">
    <form method="get" action="common/logout.do">
	<button type="submit" name="logout" value="yes" class="Button">Logout</button>
    </form>
 
      </td>
    </tr>
  </tbody>
</table>
<br>
</div>

</body>
</html>
