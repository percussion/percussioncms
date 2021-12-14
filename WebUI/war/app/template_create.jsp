<%@ page import="com.percussion.services.utils.jspel.PSRoleUtilities"
         import="com.percussion.i18n.PSI18nUtils"
         import="com.percussion.i18n.ui.PSI18NTranslationKeyValues"
%>
<%@ taglib uri="/WEB-INF/tmxtags.tld" prefix="i18n" %>
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
	
   Boolean hasSites = (Boolean)request.getAttribute("hasSites");
    if (site == null)
        site = "";
   String inlineHelpMsg = hasSites != null && hasSites
      ? PSI18nUtils.getString("perc.ui.template.create@Click Site To Work On Templates", locale)
      : PSI18nUtils.getString("perc.ui.site.architecture@Click Create Site To Create Site", locale);
%>
<i18n:settings lang="<%= locale %>" prefixes="perc.ui." debug="<%=debug%>"/>
<div class="ui-layout-west">
    <div id='perc-pageEditor-menu' class='perc-design-toolbar'>
        <div id = "perc-dropdown-actions"></div>
        <div id = "perc-dropdown-view"></div>
        <div class="perc-menu-action perc-pull-left">
            <a class = "perc-site-summary-action perc-open-dialog" role="heading" aria-level="1"><span><i18n:message key = "perc.ui.template.create@Import Summary"/></span></a>
        </div>
        <a href="#" id="perc-template-preview"  style="float:right"></a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    </div>
    <div id="perc-pageEditor-toolbar-content" class="ui-helper-clearfix"> </div>
</div>
<div class="perc-templates-layout">
    <div class="perc-unassigned-panel-expander">
        <a class="perc-panel-expander-icon perc-collapsed"></a>
    </div>
    <div class="perc-unassigned-panel perc-closed">
        <div class="perc-panel-title"><i18n:message key = "perc.ui.template.create@Unassigned"/></div> 
        <div class="perc-panel-progress">
        <span class="perc-progress-message"><i18n:message key = "perc.ui.template.create@Imported"/></span>
        <div class="perc-progress-bar-container">
            <div class="perc-progress-bar"></div>
        </div>
        <div class="perc-progress-finished">
            <span><i18n:message key = "perc.ui.template.create@Site Has Been Imported"/></span>
        </div>
        </div> 
        <div class="perc-panel-pages-list">    
        </div>
        <csrf:form class="perc-template-pages-controls" method="post" action="template_create.jsp">
            <div class="previous-disabled" style="right:56px;"><i18n:message key = "perc.ui.template.create@Prev"/></div>        
            <input type="text" autocomplete="off" name="perc-template-pages-controls-jump" class="perc-jump">
            <div class="next" style="right:0;"><i18n:message key = "perc.ui.common.label@Next"/></div>
        </csrf:form>
        <div class="perc-panel-page-range"><i18n:message key = "perc.ui.template.create@Items"/><span class="perc-panel-page-group-range"></span></div>
        <div class="perc-panel-total-item"><i18n:message key = "perc.ui.template.create@Total Items"/><span class="perc-panel-total"></span></div>
    </div>
    <div id="perc-template-view-container" style="overflow: hidden; margin-left: 38px">
        <div id="perc-activated-templates" class="perc-templates-detailed">
            <div id="perc-activated-templates-scrollable">
                <div id="perc-assigned-templates-container">
                    <div id="perc-assigned-templates">
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<div templates style="display: none;">
    <h3 class="perc-site-summary-dialog-title"><i18n:message key = "perc.ui.template.create@Site Import Summary Report"/></h3>
    <h3 class="perc-site-summary-missing-page"><i18n:message key = "perc.ui.template.create@Missing Pages"/></h3>
    <h3 class="perc-site-summary-missing-asset"><i18n:message key = "perc.ui.template.create@Missing Assets"/></h3>
    <h3 class="perc-site-summary-missing-css"><i18n:message key = "perc.ui.template.create@Missing Style Sheets"/></h3>
    <h3 class="perc-site-summary-missing-unknown"><!-- the stat name is used instead --></h3>
    <div class="perc-site-summary">
        <div class="perc-site-summary-dialog-actions perc-do-not-print">
            <a href="#" class="perc-action-refresh ui-icon ui-icon-refresh" title="Refresh"><span><i18n:message key = "perc.ui.template.create@Refresh"/></span></a>
            <a href="#" class="perc-action-print ui-icon ui-icon-print" title="Print"><span><i18n:message key = "perc.ui.template.create@Print"/></span></a>
        </div>
        <div scrollable="true">
            <div class="perc-site-summary-statistics perc-section">
                <h2><a href="#" class="perc-section-open"><i18n:message key = "perc.ui.template.create@Statistics"/></a></h2>
                <div class="perc-gadget-titlebar-shadow"></div>
                <div class="ui-widget-content"></div>
            </div>
            <div class="perc-site-summary-warnings perc-section">
                <h2><a href="#" class="perc-section-closed"><i18n:message key = "perc.ui.template.create@Missing Import Content"/></a></h2>
                <div class="perc-gadget-titlebar-shadow"></div>
                <div class="ui-widget-content perc-hide perc-log-top"></div>
                <div class="ui-widget-content perc-hide perc-log"></div>
		        <div class="ui-widget-content perc-hide perc-log-footer"></div>
            </div>
        </div>
    </div>
</div>
