
<%@ page import="com.percussion.services.utils.jspel.PSRoleUtilities" %>
<%@ taglib uri="/WEB-INF/tmxtags.tld" prefix="i18n" %>
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
    boolean isDebug = "true".equals(debug);
    String debugQueryString = isDebug ? "?debug=true" : "";
    String site = request.getParameter("site");
    if (debug == null)
        debug = "false";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
 <head>
    <title>Process Monitor</title>
    <link rel="stylesheet" type="text/css" href="/cm/cssMin/perc_dashboard.packed.css"/>
    <script src="/Rhythmyx/tmx/tmx.jsp?mode=js&amp;prefix=perc.ui.&amp;"></script>
     <script src="/JavaScriptServlet"></script>
    <script src="/cm/jslibMin/perc_dashboard.packed.js"></script>
    <script src="/cm/gadgets/repository/PercProcessorMonitorGadget/PercProcessorMonitorGadget.js"  ></script>
    <script>
        jQuery(function(){
            jQuery.renderProcessMonitor(jQuery,false);
        });
    </script>
    <style>
        body{
            margin:15px;
        }
        div#perc-process-monitor-actions
        {
            width:66px;
        }
    </style>
 </head>

 <body>

 </body>
</html>
