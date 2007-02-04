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
<title>BorgWeb Login Page</title>
<html:base/>
</head>
<%@ include file="/common/style.css" %>
<body>
<a href="../mainmenu.do"><img src="../images/logo.gif" border="0"></a>
<hr style="width: 100%; height: 1px;">

	<P>
    <form method="POST" action="j_security_check" >
  <table class="border" align="center">
  <tr>
	<th height="25" nowrap="nowrap">Please log in</th>
  </tr>
  <tr>
	<td><table width="100%">
		  <tr>
			<td colspan="2" align="center">&nbsp;</td>
		  </tr>
		  <tr>
			<td width="45%" align="right">Username:</td>
			<td>
			  <input type="text" name="j_username" size="15" maxlength="40" value="" />
			</td>
		  </tr>
		  <tr>
			<td align="right">Password:</td>
			<td>
			  <input type="password" name="j_password" size="15" maxlength="25" />
			</td>
		  </tr>
		  <tr align="center">
			<td colspan="2"><input type="submit" class="Button" value="Log in" /></td>
		  </tr>

		</table></td>
  </tr>
</table>

    </form>
    			
<hr style="width: 100%; height: 2px;">
        
</body>
</html>
