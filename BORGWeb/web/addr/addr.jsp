<%@ include file="../common/include.jsp" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<html>
<head>
<title>Address View</title>
</head>
<body>
<%@ include file="../common/header.jsp" %>
<br>
<html:errors/>
<br>
<html:form action="/addr/saveAddr.do">
<html:hidden property="key"/> 
<table border="0" cellpadding="20" cellspacing="10" align="center">
<tr><td>
<table cellpadding="4" cellspacing="1" border="0" class="border" align="center">
<tr><th colspan="2">Contact Details</th></tr>
<tr><td align="right">First Name:</td><td><html:text property="firstName" size="25" /></td></tr>
<tr><td align="right">Last Name:</td><td><html:text property="lastName" size="25" /></td></tr>
<tr><td align="right">Email:</td><td><html:text property="email" size="25" /></td></tr>
<tr><td align="right">Nick Name:</td><td><html:text property="nickname" size="25" /></td></tr>
<tr><td align="right">Screen Name:</td><td><html:text property="screenName" size="25" /></td></tr>
<tr><td align="right">Work Phone:</td><td><html:text property="workPhone" size="25" /></td></tr>
<tr><td align="right">Home Phone:</td><td><html:text property="homePhone" size="25" /></td></tr>
<tr><td align="right">Pager:</td><td><html:text property="pager" size="25" /></td></tr>
<tr><td align="right">Fax:</td><td><html:text property="fax" size="25" /></td></tr>
<tr><td align="right">Web Page:</td><td><html:text property="webPage" size="25" /></td></tr>
<tr><td align="right">Company:</td><td><html:text property="company" size="25" /></td></tr>
<tr><td align="right">Birthday:</td><td><html:text property="birthday" size="25" /></td></tr>
</table>
</td>
<td>
<br>
<table cellpadding="4" cellspacing="1" border="0" class="border" align="center">
<tr><th colspan="2">Home Address</th></tr>
<tr><td align="right">Street Address:</td><td><html:text property="streetAddress" size="25" /></td></tr>
<tr><td align="right">City:</td><td><html:text property="city" size="25" /></td></tr>
<tr><td align="right">State:</td><td><html:text property="state" size="25" /></td></tr>
<tr><td align="right">Country:</td><td><html:text property="country" size="25" /></td></tr>
<tr><td align="right">Zip Code:</td><td><html:text property="zip" size="25" /></td></tr>
</table>
<br>
<table cellpadding="4" cellspacing="1" border="0" class="border" align="center">
<tr><th colspan="2">Work Address</th></tr>
<tr><td align="right">Street Address:</td><td><html:text property="workStreetAddress" size="25" /></td></tr>
<tr><td align="right">City:</td><td><html:text property="workCity" size="25" /></td></tr>
<tr><td align="right">State:</td><td><html:text property="workState" size="25" /></td></tr>
<tr><td align="right">Country:</td><td><html:text property="workCountry" size="25" /></td></tr>
<tr><td align="right">Zip Code:</td><td><html:text property="workZip" size="25" /></td></tr>
</table>
</td>
<td>
<table cellpadding="4" cellspacing="1" border="0" class="border" align="center">
<tr><th>Notes</th></tr>
<tr><td><html:textarea property="notes" cols="40" rows="10"/></td></tr>
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
<%@ include file="../common/footer.jsp" %>
</body>
</html>
