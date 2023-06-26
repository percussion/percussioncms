<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
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
        <title>Import Template</title>
        <link rel="stylesheet" type="text/css" href="/cm/jslib/profiles/3x/libraries/fontawesome/css/all.css"/>
            <link rel="stylesheet" type="text/css" href="../cssMin/perc_admin.packed.min.css" />
            <link rel="stylesheet" type="text/css" href="../css/layout.css" />
            <link rel="stylesheet" type="text/css" href="../css/styles.css" />
        <script src="/Rhythmyx/tmx/tmx.jsp?mode=js&amp;prefix=perc.ui.&amp;sys_lang=<%=locale%>"></script>
        <script src="/JavaScriptServlet"></script>
            <%@include file="includes/common_js.jsp" %>

              <script>
              $(function() {
                      $(".perc-template-import-field").on("change", function(){
                            $(".perc-import-error").hide();
                            $("#per-import-message").hide();
                            $("#perc-import-template-frame").css('height', '41px'); 
                        });
                });        
              </script>
    </head>
    <body style = "margin:0; background-color:#E6E6E9">
        <csrf:form id="perc-import-template-form" encType="multipart/form-data"  method="post" name="perc-import-template-form" style = "margin-bottom:0px" action="importTemplate.jsp">
            <div id = "perc-import-label" style = "margin-top:-4px">
                <label style = "font-size:11px; font-family:Verdana,serif; font-weight:normal"><i18n:message key = "perc.ui.import.template@File Name"/></label>
            </div>
            <div id = "per-import-input-field">
                <input role="button" tabindex="0" type = "file" title='<i18n:message key = "perc.ui.import.template@File Name"/>' size = "53" class = "perc-template-import-field" style = "margin:5px 0px 0px 0px" name = "import-template" />
            </div>
            <div class = "perc-import-error">
                <i18n:message key = "perc.ui.import.template@Select Template File"/>
            </div>
            <%if(status != null){%>
                <div id="per-import-message" class="perc-import-upload-error"><%=message%></div>
            <%}%>
        </csrf:form>
    </body>
</html>
