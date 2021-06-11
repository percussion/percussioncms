<%@ page import="com.percussion.services.utils.jspel.PSRoleUtilities" %>
<%@ taglib uri="/WEB-INF/tmxtags.tld" prefix="i18n"%>

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
  ~      https://www.percusssion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  --%>

<%
	String locale= PSRoleUtilities.getUserCurrentLocale();
	String lang="en";
	if(locale==null){
		locale="en-us";
	}else{
		if(locale.contains("-"))
			lang=locale.split("-")[0];
		else
			lang=locale;
	}
    String debug = request.getParameter("debug");
    String status = request.getParameter("status");
    String msgClass = null;
    if(status != null && status.equals("PERC_SUCCESS"))
        msgClass = "perc-success";
    else if(status != null && status.equals("PERC_ERROR"))
        msgClass = "perc-error";
    String message = request.getParameter("message");
%>
<i18n:settings lang="<%=locale %>" prefixes="perc.ui." debug="<%= debug %>"/>
<!DOCTYPE html>
<html lang="<%= lang %>">
    <head>
        <title><i18n:message key = "perc.ui.page.not.found@Page Not Found Error"/></title>
        <link rel="stylesheet" type="text/css" href="/cm/jslib/profiles/3x/libraries/fontawesome/css/all.css"/>
        <style>
        body {background-color: #6C717C; font-family: Verdana; margin: 0; padding: 0; }
        .perc-login-logo { font-size: 48px; color: #121212; margin-top: 160px; margin-bottom: 100px;}
        #loginform .perc-form    {font-size: 14px; }
        #perc-forgot {color: #fff;}
        #perc-forgot:hover   {cursor:pointer;}
        a img{border-style: none;}
        input { padding: 0; }
        table { border: 0; border-spacing: 0; }
        td { border: 0; margin: 0; padding: 0 4px 0 4px ;}        
        .error { font-size: 12px; font-weight:bold; margin-top:10px; color: #CCCCCC}
        .perc-form .windowName a, .perc-form .windowName a:visited { color: #CCCCCC; text-decoration: none; font-weight: bold; }
        </style>
    </head>
    <body>
        <table align="center">

            <tr>
                <td align="center">
                    <div class='perc-login'>                        
                            <div class='perc-login-logo'><a href="/cm"><img src="/Rhythmyx/sys_resources/images/loginLogo.jpg"/></a></div>   
                            <table class='perc-form'> 
                                <tr>
                                    <td> 
                                        <div class="error" id="perc-pagenotfound-msg" align="center">
                                          <i18n:message key = "perc.ui.page.not.found@Page Not Found Sorry"/><br/>
                                          <i18n:message key = "perc.ui.page.not.found@Check URL Validity"/>
                                        </div> 
                                    </td>
                                </tr>   
                            </table>                        
                    </div>
                </td>
            </tr>
        </table>
    </body>

</html>
