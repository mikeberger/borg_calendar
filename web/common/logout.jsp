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
<%@ include file="include.jsp" %>
<html>
  <head>
    <title>Logout</title>
  </head>
  <body>
<%@ include file="header.jsp" %>
    <p style="vertical-align: center; text-align: center; font-size: 18pt;color: #FF0000;">
      You have been logged out successfully
    </p>
    <center>
	<a href="../mainmenu.do">Log In</a>
	</center>
  </body>
  <% response.setStatus(200); %>
</html>
