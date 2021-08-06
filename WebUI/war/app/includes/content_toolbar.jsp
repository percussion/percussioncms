<%@ page import="com.percussion.services.utils.jspel.PSRoleUtilities" %>
<%@ taglib uri="/WEB-INF/tmxtags.tld" prefix="i18n" %>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>

<%
    String debug = request.getParameter("debug");
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
%>
<style>
    .perc-page-edit-action-bar .button {
        text-shadow: 0px -1px 1px rgba(0,0,0,0.20);
        font-family: percOpenSans, sans-serif;
        text-transform: uppercase;
        font-size: 14px;
        text-decoration: none;
        display: block;
        color: rgba(255, 255, 255, 0.8);
        padding: 5px 20px;
        border-radius: 5px;
        border: solid thin #979696;
        background: #133c55 !important;
        /* background-image: linear-gradient(-179deg, #30E1FF 0%, #0C87A3 100%); */
        -webkit-transition: all 250ms;
        transition: all 250ms;
    }
</style>
<i18n:settings lang="<%= locale %>" prefixes="perc.ui." debug="<%= debug %>"/>

<div id="perc-content-menu">

    <div id="perc-dropdown-actions" style="float : left">
    </div>
    <div id="perc-dropdown-view" style="float : left">
    </div>

    <div id="perc-dropdown-publish-now" style="float : left">
    </div>
    <!-- Explore Orphaned Assets-->
    <div class="perc-lib-expander-div" >
        <span id="perc_orphan_assets_expander" style="display:none">
            <a id="perc_orphan_assets_maximizer" href="#" aria-label="Web Accessibility Initiative" style="float: left;"></a>
            
            <span><i18n:message key = "perc.ui.content.toolbar@Unused Assets"/></span>
        </span>
    </div>

    <div class="percussion-service" data-control="page-edit-action-bar" style="float: left">
    </div>

    <div id="perc-dropdown-page-workflow" style="float : right" role="presentation">
    </div>

</div>

<div id='perc_asset_library' class='perc-template-container perc-hidden' style="float:unset;">
    <div class="perc-orphan-assets-menu perc-orphan-assets-menu-left" style="z-index: 4330; float: left; margin-left: -27px; border-right: 1px solid rgb(210, 209, 205);"></div>
    <div class="perc-orphan-assets-menu perc-orphan-assets-menu-right">
        <img src="/cm/images/icons/editor/delete.png" class="perc-ui-menu-icon perc-ui-delete-asset" alt="Delete local content" title="Delete Asset" style="padding-right:4px; margin-top:-4px; float:right; cursor:pointer; padding-left: 1px;"></img>
        <img src="/cm/images/icons/editor/edit.png" class="perc-ui-menu-icon perc-ui-edit-asset" alt="Edit local content" title="Edit Asset" style="margin-top:-4px;float:right; cursor:pointer; padding-left: 1px;"></img>
    </div>
    <!-- Explore Orphan Assets Tray-->
    <div class='perc-orphan-assets-list'>
        <!-- This is generated dynamically from the controller -->
    </div>
</div>
