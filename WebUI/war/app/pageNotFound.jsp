<%@ page import="com.percussion.services.utils.jspel.PSRoleUtilities" %>
<%@ taglib uri="/WEB-INF/tmxtags.tld" prefix="i18n"%>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>




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
        <script src="/Rhythmyx/tmx/tmx.jsp?mode=js&amp;prefix=perc.ui.&amp;sys_lang=<%=locale%>"></script>
        <script src="/JavaScriptServlet"></script>
        <style>
        body { font-family: Verdana; margin: 0; padding: 0; }
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
