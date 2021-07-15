<%@ page import="com.percussion.i18n.PSI18nUtils" %>
<%@ page import="java.util.*" %>
<%@ taglib uri="http://rhythmyx.percussion.com/components" prefix="rxcomp"%>
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
%>
<!DOCTYPE html>
<html>
    <head>
        <title>${rxcomp:i18ntext('jsp_unsupported@browser title',locale)}</title> 
        <style type="text/css">
         body {background-color: #6C717C; font-family: Verdana; margin: 0; padding: 0; }
        .perc-login-logo { font-size: 48px; color: #121212; margin-top: 160px; margin-bottom: 100px;}
        .perc-warning-message { width:400px; text-align:left;}
        .perc-warning-message a { color: #CCCCCC; text-decoration: none; font-weight: bold; }
        </style>
        <script>
            function createCookie(name,value,days) {
                if (days) {
                    var date = new Date();
                    date.setTime(date.getTime()+(days*24*60*60*1000));
                    var expires = "; expires="+date.toGMTString();
                }
                else var expires = "";
            document.cookie = name+"="+value+expires+"; path=/";
            }
        </script>
			<link rel="stylesheet" type="text/css" href="/cm/cui/components/twitter-bootstrap-3.0.0/dist/css/bootstrap.min.css"/>
    <script
            src="/Rhythmyx/tmx/tmx.jsp?mode=js&amp;prefix=perc.ui.&amp;sys_lang=en-us"></script>
        <script src="/JavaScriptServlet"></script>
        <script src="/cm/cui/components/jquery/dist/jquery.min.js"></script>
    <script src="/cm/cui/components/twitter-bootstrap-3.0.0/dist/js/bootstrap.min.js"></script>
    </head>
    <body>
        <table align="center">
            <tr>
                <td align="center">
                    <div class='perc-warning-wrapper'>
                        <div class='perc-login-logo'><img src="sys_resources/images/percussion-logo.png" alt="${rxcomp:i18ntext('general@Percussion Logo Alt',locale)}" title="${rxcomp:i18ntext('general@Percussion Logo Title',locale)}"/></div>   
                  		<p class = "perc-warning-message">${rxcomp:i18ntext('jsp_unsupported@warning message part1',locale)}</p><p class = "perc-warning-message">${rxcomp:i18ntext('jsp_unsupported@warning message part2',locale)}<p>
						<p class="perc-warning-message">
						${rxcomp:i18ntext('jsp_unsupported@warning message part3',locale)} <a href="mailto:support@percussion.com">${rxcomp:i18ntext('jsp_unsupported@the technical support team',locale)}.</a></p>
						<p class = "perc-warning-message">${rxcomp:i18ntext('jsp_unsupported@warning message part4',locale)} <a href="${rxcomp:i18ntext('jsp_unsupported@supported browser link',locale)}" target="_blank" title="${rxcomp:i18ntext('jsp_unsupported@supported browser link title',locale)}">${rxcomp:i18ntext('jsp_unsupported@supported browser link text',locale)}</a>.</p>	
                    </div>
                </td>
            </tr>
            <tr>
                <td align="center">
					<button class="btn btn-primary btn-default" type = "button" onclick = "createCookie('unsupportedBrowserWarningSeen','true', 730);window.location='/';">${rxcomp:i18ntext('jsp_unsupported@i agree button',locale)}</button>
                </td>
            </tr>            
        </table>
    </body>
</html>
