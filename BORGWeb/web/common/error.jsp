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
<%@ page language="java" contentType="text/html;charset=UTF-8" isErrorPage="true"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<html>
  <head>
    <title>Error</title>
    <html:base/>
  </head>
  <body>

  <a href="../mainmenu.do"><img src="../images/logo.gif" border="0"></a>
<hr style="width: 100%; height: 1px;">
	
    <p style="vertical-align: center; text-align: center; font-size: 18pt;color: #FF0000;">
      An error has occurred:
    </p>
    <blockquote>
 <% if (exception != null) { %>
    <pre><% exception.printStackTrace(new java.io.PrintWriter(out)); %></pre>
 <% } else if ((Exception)request.getAttribute("javax.servlet.error.exception") != null) { %>
    <pre><% ((Exception)request.getAttribute("javax.servlet.error.exception"))
                           .printStackTrace(new java.io.PrintWriter(out)); %></pre>
 <% } %>
    </blockquote>
   
  </body>
</html>

<!-- Some browsers will not display this page unless the response status code is 200. -->
<% response.setStatus(200); %>
