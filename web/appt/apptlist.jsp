<%@ include file="../common/include.jsp" %>
<%@ page import="net.sf.borgweb.form.MonthDTO,net.sf.borgweb.form.AppointmentForm,java.util.*" %>
<html>
<head>
<title>Appointment Calendar</title>
</head>
<body>
<%@ include file="../common/header.jsp" %>
<% MonthDTO md = (MonthDTO) session.getAttribute("monthDTO"); %>
<p class="title"><%= md.getTitle() %></p>
<table cellpadding="4" width="90%" cellspacing="1" border="0" class="month" align="center">
<COLGROUP span="7" width="14%">
   </COLGROUP>
<tr>

	<th>Sunday</th>
	<th>Monday</th>
	<th>Tuesday</th>
	<th>Wednesday</th>
	<th>Thursday</th>
	<th>Friday</th>
	<th>Saturday</th>
</tr>
<%  Collection[] days = md.getDays();
	for( int i = 0; i < 6; i++ ){ %>
   	<tr>
<%		for( int j = 0; j < 7; j++ ){ 
			int d = i*7+j;
			int daynumber = d - md.getFirstDay() + 1;
	     
			if( daynumber <= 0 || daynumber > md.getLastDay() ) 
			{ %>
			<td></td>
<%			}
			else
			{ %>
				<td>
				<P class="date"><%= daynumber %></P>
<%				Collection appts = days[daynumber-1];
				if( appts != null )
				{
					Iterator it = appts.iterator();
					while( it.hasNext() )
					{
						AppointmentForm af = (AppointmentForm) it.next(); 
						String text = af.getText();
						if( af.getText().length() > 50 )
						{
							text = af.getText().substring(0,50);
						} %>
						
						<a class="appt" href="editAppt.do?key=<%= af.getKey() %>"><%= text %></a><br>
<%					} 
				} %>		
				</td>
				
<%			}
				
		} 

%>
	</tr>	
<% } %>
<!--
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
-->
</table>
<br>
<br>


<br>
<%@ include file="../common/footer.jsp" %>
</body>
</html>
