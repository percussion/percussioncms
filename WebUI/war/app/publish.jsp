<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.percussion.i18n.PSI18nUtils" %>
<%@ taglib uri="/WEB-INF/tmxtags.tld" prefix="i18n" %>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>

<%@ page import="com.percussion.services.utils.jspel.PSRoleUtilities" %>
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
    String currentPage = PSI18nUtils.getString("perc.ui.navMenu.publish@Publish", locale);
%>
<i18n:settings lang="<%= locale %>" prefixes="perc.ui." debug="<%=debug%>"/>
<!DOCTYPE html>
<html lang="<%=lang %>">
<head>
    <title><i18n:message key="perc.ui.navMenu.publish@Publish"/></title>
    <%@include file="includes/common_meta.jsp" %>
    <%--
         When ran in normal mode all javascript will be in one compressed file and
         the same for css (Currently just concatenated but not compressed.).
         To run from the non-compressed file simply add the query string param
         ?debug=true to the url for the page.

         Be sure that when a new javascript file is added to the page, an entry
         for each inclusion will be needed in the appropriate concat task within
         the minify target in the build.xml file. If this is not done then it won't
         get into the files used in production.
    --%>
    <link rel="stylesheet" type="text/css" href="/cm/jslib/profiles/3x/libraries/fontawesome/css/all.css"/>
    <script src="/Rhythmyx/tmx/tmx.jsp?mode=js&amp;prefix=perc.ui.&amp;sys_lang=<%=locale%>"></script>
    <script src="/JavaScriptServlet"></script>
    <% if (isDebug) { %>

    <!-- CSS Includes -->
    <link type="text/css" href="../css/minuet/perc_fonts_minuet.css" rel="stylesheet"/>
    <link rel="stylesheet" href="../jslib/profiles/3x/libraries/bootstrap/css/bootstrap.css">
    <link rel="stylesheet" href="../jslib/profiles/3x/libraries/fontawesome/css/all.css">
    <link type="text/css" href="../jslib/profiles/3x/libraries/animate.css/animate.css" rel="stylesheet" />
    <link type="text/css" href="../css/minuet/perc_common_minuet.css" rel="stylesheet"/>
    <link type="text/css" href="../css/minuet/perc_publish_minuet.css" rel="stylesheet"/>

    <!-- JavaScript Includes (order matters) -->
    <%@include file="includes/common_js.jsp" %>

    <script src="../jslib/profiles/3x/libraries/popper/popper.js"></script>
    <script src="../jslib/profiles/3x/libraries/bootstrap/js/bootstrap.js"></script>
    <script src="../jslib/profiles/3x/libraries/handlebars/handlebars-v4.0.12.js"></script>
    <script src="../jslib/profiles/3x/libraries/momentjs/moment-with-locales.js"></script>

    <script src="../views/PercCommonMinuetView.js"></script>
    <script src="../services/PercPublisherService.js"></script>
    <script src="../services/PercUtilService.js"></script>
    <script src="../services/PercSiteService.js"></script>
    <script src="../views/PercPublishMinuetView.js"></script>
    <script src="../views/PercNavigationMinuetView.js"></script>
    <script src="../views/PercPublishStatusMinuetView.js"></script>
    <script src="../views/PercPublishLogsMinuetView.js"></script>
    <script src="../services/PercUserService.js"></script>
    <script src="../services/PercWorkflowService.js"></script>
    <script src="../plugins/PercChangePasswordMinuet.js"></script>


    <!-- Include all compiled plugins (below), or include individual files as needed -->

    <%@include file="includes/finder_js.jsp" %>
    <%
    } else {
    %>
    <link rel="stylesheet" type="text/css" href="../cssMin/perc_publish.packed.min.css"/>
    <link rel="stylesheet" href="../jslib/profiles/3x/libraries/fontawesome/css/all.css">

    <script src="../jslibMin/perc_publish.packed.min.js"></script>
    <%
        }
    %>

    <!-- Set Moment.js locale -->
    <script>
        moment.locale('<%= locale %>');
    </script>
    <script>
        var csrfHeader = $("meta[name='_csrf_header']").attr("content");
        var csrfToken = $("meta[name='_csrf']").attr("content");
        var headers = {};
        headers[csrfHeader] = csrfToken;
        $.ajaxSetup({
            headers: headers
        });
    </script>
    <%@include file='includes/minuetCommonTemplates/alertTemplates.jsp'%>
    <%@include file='includes/minuetPublishTemplates/publishTemplates.jsp'%>
    <%@include file='includes/minuetPublishTemplates/publishStatusTemplates.jsp'%>
    <%@include file='includes/minuetPublishTemplates/publishLogTemplates.jsp'%>
    <%@include file='includes/minuetPublishTemplates/publishIncrementalPreviewTemplates.jsp'%>

    <!--[if lte IE 7]>
    <link rel="stylesheet" type="text/css" href="../css/IE_styles.css"/><![endif]-->
    <!--[if gte IE 8]>
    <link rel="stylesheet" type="text/css" href="../css/IE8_styles.css"/><![endif]-->
</head>
<body>
<%@include file="includes/minuetCommonTemplates/navigationTemplates.jsp" %>
<div class="perc-body-background">
    <div id="perc-publish-body-target">
    </div>
</div>
<logDetailsContainer id="percPublishLogDetailsOverlayTarget"></logDetailsContainer>
<incrementalPreviewContainer id="percIncrementalPublishPreviewOverlayTarget"></incrementalPreviewContainer>
<modalContainer id="percModalTarget"></modalContainer>
<alertContainer id="percFooterAlertTarget"></alertContainer>
<dialogContainer id="percDialogTarget"></dialogContainer>
<%@include file='includes/siteimprove_integration.html'%>

</body>
</html>
