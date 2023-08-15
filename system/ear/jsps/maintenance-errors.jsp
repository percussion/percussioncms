<%@ page import="java.util.*" %>
<%@ page import="com.percussion.i18n.PSI18nUtils" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>
<%@ taglib uri="http://rhythmyx.percussion.com/components"
    prefix="rxcomp"%>


<%
    String locale = PSI18nUtils.getSystemLanguage();
    pageContext.setAttribute("locale",locale); 
    response.setStatus(503);
%>
<!DOCTYPE html>
<html>
    <head>
        <title>${rxcomp:i18ntext('jsp_maintenance@Maintenance',locale)}</title> 
        <style type="text/css">
         body { font-family: Verdana; margin: 0; padding: 0; }
        .perc-login-logo { font-size: 48px; color: #121212; margin-top: 160px; margin-bottom: 100px;}
        .perc-warning-message { width:500px; text-align:center;}
        .perc-warning-message a { color: #CCCCCC; text-decoration: none; font-weight: bold; }
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
                    <div class='perc-warning-wrapper'>
                        <div class='perc-login-logo'><img src="/sys_resources/images/percussion-logo.png" alt="${rxcomp:i18ntext('general@Percussion Logo Alt',locale)}" title="${rxcomp:i18ntext('general@Percussion Logo Title',locale)}"/></div>
                  		<p class = "perc-warning-message">${rxcomp:i18ntext('jsp_maintenance@Maintenance Error Part One',locale)} <br/>${rxcomp:i18ntext('jsp_maintenance@Maintenance Error Part Two',locale)}</p>
                    </div>
                </td>
            </tr>
        </table>
    </body>
</html>
