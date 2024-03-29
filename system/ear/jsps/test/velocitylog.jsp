<%@page import="com.percussion.server.PSServer,java.io.*,java.text.ParseException,java.text.SimpleDateFormat" pageEncoding="UTF-8"
        contentType="text/html; charset=UTF-8"
        import="java.util.Date"
        import="com.percussion.services.utils.jspel.*"
        import="com.percussion.i18n.*"
        import="java.nio.charset.StandardCharsets"
%>
<%@ page import="com.percussion.security.SecureStringUtils" %>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>
<%@ taglib uri="/WEB-INF/tmxtags.tld" prefix="i18n" %>

<%
    String isEnabled = PSServer.getServerProps().getProperty("enableDebugTools");

    if(isEnabled == null)
        isEnabled="false";

    if(isEnabled.equalsIgnoreCase("false")){
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
    String fullrolestr = PSRoleUtilities.getUserRoles();

    if (!fullrolestr.contains("Admin"))
        response.sendError(HttpServletResponse.SC_NOT_FOUND);

%>
<%
    //Checking for vulnerability
    String str = request.getQueryString();
    if(str != null && str != ""){
        response.sendError(response.SC_FORBIDDEN, "Invalid token!");
    }
    String refresh = request.getParameter("refresh");
    //Checking for vulnerability
    if(!SecureStringUtils.isValidString(refresh)){
        response.sendError(response.SC_FORBIDDEN, "Invalid refresh!");
    }
    String warningstr = request.getParameter("warning");
    //Checking for vulnerability
    if(!SecureStringUtils.isValidString(warningstr)){
        response.sendError(response.SC_FORBIDDEN, "Invalid warningStr!");
    }
    boolean warning = !(warningstr == null || warningstr.trim().length() == 0);
    if (refresh == null) {
        // Default value for warning
        warning = true;
    }
    SimpleDateFormat dfmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date limit = new Date();
    limit.setTime(limit.getTime() - 3600000); // One hour ago
    String jspPath = PSServer.getRxDir().toString();
    String txtFilePath = null;
    txtFilePath = jspPath + "/jetty/base/logs/";


    File velocityLog = new File(txtFilePath, "velocity.log");
    String logdata = null;
    if (velocityLog.exists()) {
        Reader r = new InputStreamReader(new FileInputStream(velocityLog), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(r);
        StringBuilder b = new StringBuilder();
        String line;
        b.append("<p style='font-family: monospace'>");
        while ((line = br.readLine()) != null) {
            Date ldate = null;
            try {
                ldate = dfmt.parse(line);
            } catch (ParseException e) {
                // Skip any bad lines from Avalon logger
                continue;
            }
            if (ldate.after(limit)) {
                if (warning && line.contains("Warning!")) continue;
                b.append("<br>");
                b.append(line);
            }
        }

        logdata = b.toString();
    } else {
        logdata = "No velocity log file found";
    }

%>
<html>
<head>
    <title>Retrieve velocity logs from server for assembly debugging</title>
    <link rel="stylesheet" href="/sys_resources/css/rxcx.css" type="text/css" media="screen"/>
    <link href="/sys_resources/css/templates.css" rel="stylesheet" type="text/css">
    <link href="/rx_resources/css/templates.css" rel="stylesheet" type="text/css">
    <link href="../rx_resources/css/en-us/templates.css" rel="stylesheet" type="text/css">
</head>
<body>
<div style="background-color: white; margin: 10px; padding: 10px">
    <csrf:form method="POST" action="/test/velocitylog.jsp">
        <p><img src="../sys_resources/images/banner_bkgd.jpg"></p>
        <input type="submit" id=refresh" name="refresh" value="Refresh">&nbsp;&nbsp;&nbsp;
        <label for="warning">Hide warnings:</label><input type="checkbox" id="warning" name="warning" <%= warning ? "checked" : "" %> value="on"/>
        <h3>Velocity log after <%= dfmt.format(limit) %>
        </h3>
        <%= logdata %>
    </csrf:form>
</div>
</body>
</html>
