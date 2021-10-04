<%@ page import="com.percussion.error.PSExceptionUtils" %>
<%@page contentType="text/html; charset=utf-8" isErrorPage="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%--
  ~     Percussion CMS
  ~     Copyright (C) 1999-2020 Percussion Software, Inc.
  ~
  ~     This program is free software: you can redistribute it and/or modify
  ~     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  ~
  ~     This program is distributed in the hope that it will be useful,
  ~     but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~     GNU Affero General Public License for more details.
  ~
  ~     Mailing Address:
  ~
  ~      Percussion Software, Inc.
  ~      PO Box 767
  ~      Burlington, MA 01803, USA
  ~      +01-781-438-9900
  ~      support@percussion.com
  ~      https://www.percussion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  --%>

<%
   String[] errorMessages = (String[])session.getAttribute("errorMessages");
   if (errorMessages == null)
      errorMessages = new String[0];
   Integer errorCode = (Integer)session.getAttribute("errorCode");
   if (errorCode == null)
      errorCode = 500;
   response.setStatus(errorCode);
   
   if (exception != null)
   {
	   pageContext.setAttribute("message", PSExceptionUtils.getMessageForLog((Exception)exception));
       //stack trace is no longer sent
	   pageContext.setAttribute("stacktrace", "");
   }
   else
   {
	   pageContext.setAttribute("message","");
   }
%>
<!DOCTYPE html>
<html>
   <head>
      <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
   
      <title>Rhythmyx - Error</title>
  	<%@include file="header.jsp"%>
   </head>
   <body class="backgroundcolor" topmargin="5" leftmargin="5">
      <table width="100%" height="125" cellpadding="0" cellspacing="0" border="0">
         <tr>
            <td height="75">
               <table width="100%" height="75" cellpadding="0" cellspacing="0" border="0">
                  <tr class="bannerbackground">
                     <td width="315" valign="top" align="left"><img src="/rx_resources/images/en-us/banner_longlogo.jpg" width="640" height="75" border="0" alt="Rhythmyx Content Manager" title="Rhythmyx Content Manager"></td>
                     <td height="75" align="left" class="tabs" width="100%">
                        <table width="100%" border="0" cellspacing="0" cellpadding="0" height="75" background="/rx_resources/images/en-us/banner_bg_noline.gif">
                           <tr>
                              <td align="left" valign="bottom"><img src="/sys_resources/images/spacer.gif"></td>
                           </tr>
                        </table>
                     </td>
                  </tr>
               </table>
            </td>
         </tr>
         <tr>
            <td height="1" class="backgroundcolor"><img src="/sys_resources/images/spacer.gif" width="1" height="1" border="0" alt=""></td>
         </tr>
         <tr class="outerboxcell">
            <td>
			 <c:choose>
				<c:when test="${empty message}">
               <table width="100%" cellpadding="0" cellspacing="1" border="0" class="backgroundcolor">
                  <tr class="headercell">
                     <td class="headercell2font">Processing Error: <%= session.getAttribute("errorType") %></td>
                  </tr>
                  <tr>
                     <td>
                        <table border="0" cellpadding="0" cellspacing="1" class="backgroundcolor" width="100%">
                           <tr class="headercell">
                              <td class="headercell2font">ID</td>
                              <td class="headercell2font">Message</td>
                           </tr>
<% 
   for (int i = 0; i < errorMessages.length; i++) 
   {
%>
                           
                           <tr class="headercell">
                              <td width="10%" class="datacell1font">0</td>
                              <td class="headererrorcell"><%= errorMessages[i] %></td>
                           </tr>
<%
   }
%>
                        </table>
                     </td>
                  </tr>
               </table>
            </td>
			</c:when>
			<c:otherwise>
				<p style="border: 1px solid black; padding: 1em; margin: 1em">${message}</p>
				<p style="font-family: courier monospaced">${stacktrace}</p>
			</c:otherwise>			
		 </c:choose>
		 </tr>
      </table>
   </body>
</html>
