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
<script>
    function toggle(index){
        $("#perc-styleTabs").tabs({ active: index });
    }

</script>
<i18n:settings lang="<%= locale %>" prefixes="perc.ui." debug="<%=debug%>"/>

<div id="ui-layout-west">
    <div>
        <div class='perc-template-container'>
            <div id="perc-styleTabs">
                <ul>
                    <div id = "perc-dropdown-actions-style" style="float:left;"></div>
                    <div id = "perc-dropdown-view-style" style="float:left;"></div>
                    <li onclick="toggle(0);"><a class="perc-style-sub-tab" href="#perc-styleTabs-1"><i18n:message key = "perc.ui.template.layout@Select Theme"/></a></li>
                    <li onclick="toggle(1);"><a class="perc-style-sub-tab" href="#perc-styleTabs-2"><i18n:message key = "perc.ui.template.layout@View Theme CSS"/></a></li>
                    <li onclick="toggle(2);"><a class="perc-style-sub-tab" href="#perc-styleTabs-3"><i18n:message key = "perc.ui.template.layout@Override Theme CSS"/></a></li>
                    <div style="text-align: right; float : right" class="ui-layout-east">
                        <button id="perc-css-editor-save" title="Save" class="btn btn-primary" name="perc_wizard_save"   ><i18n:message key ="perc.ui.button@Save"/></button>
                        <button id="perc-css-editor-cancel" title="Cancel" class="btn btn-primary" name="perc_wizard_cancel" ><i18n:message key ="perc.ui.common.label@Cancel"/></button>
                    </div>
                </ul>
                <div id="perc-styleTabs-1">
                    <div id="perc-css-gallery">
                    </div>
                </div>
                <div id="perc-styleTabs-2">
                    <div id="perc-css-theme-editor">
                    </div>
                </div>
                <div id="perc-styleTabs-3">
                    <div id="perc-css-editor">
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- <iframe id="css_preview" ></iframe> -->
