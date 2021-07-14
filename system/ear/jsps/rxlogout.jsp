<%@ page 
   import="com.percussion.i18n.PSI18nUtils" 
   import="java.net.URLEncoder"
   import="java.text.MessageFormat"
   import="java.util.*"
   %>
<%@ taglib uri="http://rhythmyx.percussion.com/components"
    prefix="rxcomp"%>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>

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
    String locale = PSI18nUtils.getSystemLanguage();
    pageContext.setAttribute("locale",locale);
    String redirect = "../cm";
    String pattern = PSI18nUtils.getString("jsp_logout@click here", locale);
    String[] args = {redirect};
    String msg = MessageFormat.format(pattern, args);
%>  
<!DOCTYPE html>
<html>
    <head>
        <title>Logout Page</title> 
        <style type="text/css">
        body {background-color: #6C717C; font-family: Verdana; margin: 0; padding: 0; }
        .perc-login-logo {color: #121212; margin-top: 160px; margin-bottom: 100px;}
        #loginform .perc-form    { }
        #perc-forgot {color: #fff;}
        #perc-forgot:hover   {cursor:pointer;}
        input { padding: 0; }
        table { border: 0; border-spacing: 0; }
        td { border: 0; margin: 0; padding: 0 4px 0 4px ;}
        img:hover    {cursor: pointer;}
        .error { font-weight:bold; margin-top:10px; }
        .perc-form .windowName a, .perc-form .windowName a:visited { color: #444444; text-decoration: none; font-weight: bold; }
        </style>
		<link rel="stylesheet" type="text/css" href="/cm/cui/components/twitter-bootstrap-3.0.0/dist/css/bootstrap.min.css"/>
    <script
            src="/Rhythmyx/tmx/tmx.jsp?mode=js&amp;prefix=perc.ui.&amp;sys_lang=en-us"></script>
        <script src="/JavaScriptServlet"></script>
        <script src="/cm/cui/components/jquery/jquery.min.js"></script>
    <script src="/cm/cui/components/jquery-migrate/jquery-migrate.min.js"></script>
    <script src="/cm/cui/components/twitter-bootstrap-3.0.0/dist/js/bootstrap.min.js"></script>
    </head>
    <body>
        <table align="center">
            <tr>
                <td align="center">
                    <div class='perc-login'>
                        <csrf:form id="loginform" name="loginform" method="post" enctype="multipart/form-data">
                            <div class='perc-login-logo'><img src="/sys_resources/images/percussion-logo.png" alt="${rxcomp:i18ntext('general@Percussion Logo Alt',locale)}" title="${rxcomp:i18ntext('general@Percussion Logo Title',locale)}"/></div>
                            <table class='perc-form'> 
                                <tr>
                                    <td> 
                                        <p class="windowName" align=center>${rxcomp:i18ntext('jsp_logout@logged out',locale)}</p>
                                        <p class="windowName" align=center><%= msg %></p>
                                    </td>
                                </tr>   
                            </table>
                        </csrf:form>
                    </div>
                </td>
            </tr>
        </table>
    </body>
</html>
