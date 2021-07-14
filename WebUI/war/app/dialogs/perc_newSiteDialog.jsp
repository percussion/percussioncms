<%@ page import="java.util.*,java.io.*,java.net.*" %>
<%@ page import="com.percussion.share.data.PSMapWrapper" %>
<%@ page import=" com.percussion.utils.PSSpringBeanProvider" %>
<%@ page import="com.percussion.utils.service.impl.PSUtilityService" %>
<%@ page import="com.percussion.sitemanage.service.impl.PSSiteDataService" %>
<%@ page import="com.percussion.services.utils.jspel.PSRoleUtilities" %>
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
    PSUtilityService utilityService = (PSUtilityService) PSSpringBeanProvider.getBean("utilityService");
    PSSiteDataService siteService = (PSSiteDataService) PSSpringBeanProvider.getBean("siteDataService");
    boolean isSaaS = utilityService.isSaaSEnvironment();
    List siteNames = new ArrayList();
    if(isSaaS){
        try{
	        PSMapWrapper mapW = siteService.getSaaSSiteNames(true);
	        Map map = mapW.getEntries();
	        for(Object obj:map.keySet()){
	            siteNames.add((String)obj);
	        }
        }
        catch(Exception e){
            //Server already logs the error do nothing here.
        }
    }

%>
<i18n:settings lang="<%= locale %>" prefixes="perc.ui." debug="<%= debug %>"/>
<span style="position: relative; float: right; margin-top: -28px;"><label><i18n:message key = "perc.ui.general@Denotes Required Field"/></label></span>
<div id="perc_newSiteDialog" >
    <csrf:form id="perc_newSiteDialogForm" method="get" action="">
        <div class="ui-layout-center">
            <div id="perc_wizard_step1" class="perc_wizard_step">

                <div class="perc_sitename_field">
                    <label class="perc_dialog_label perc-required-field" for="sitename"><i18n:message key="perc.ui.newsitedialog.label@Name Your Site:"/></label><br/>
                    <%if(isSaaS){ %>
                        <select class="perc_dialog_input" id="sitename-select" name="sitename-select" autofocus="true">
                            <%for(Object site:siteNames){ %>
                                <option value="<%=(String)site%>"><%=(String)site%></option>
                            <%} %>
                        </select>
                        <input class="perc_dialog_input perc_dialog_field" id="sitename" name="sitename" maxlength="80" type="hidden"/>
                    <%}else{ %>
                        <input class="perc_dialog_input perc_dialog_field" id="sitename" name="sitename" maxlength="80" type="text" autofocus="true"/>
                    <%} %>
                </div>

                <p class="hint"><strong><i18n:message key="perc.ui.newsitedialog.text@page1 summary"/></strong></p>
                
                <div class="perc_url_field">
                    <input class="perc_dialog_input" type="radio" name="site_type" id="type_url" value="type_url" checked="checked" /><label class="perc_dialog_label" for="type_url"><i18n:message key="perc.ui.newsitedialog.label@URL:"/></label>
                    <br/>
                    <input class="perc_dialog_input perc_dialog_field" id="url" name="url" maxlength="2048" type="text"/>
                </div>

                <div class="perc_percussion_template_field">
                    <input class="perc_dialog_input" type="radio" name="site_type" id="type_percussion_template" value="type_percussion_template" /><label class="perc_dialog_label" for="type_percussion_template"><i18n:message key="Percussion Templates"/></label>
                </div>
            </div>
            <div id="perc_wizard_step2" class="perc_wizard_step">
                <div>
                    <div class="perc-new-sitedlg-field">
                        <label class="perc_dialog_label perc-required-field" for="templatename"><i18n:message key="perc.ui.newsitedialog.label@Template Name:"/></label><br/>
                        <input maxlength="100" class="perc_dialog_input perc_dialog_field" id="templatename" name="templatename" type="text"/>
                    </div>
                    <div class="perc-new-sitedlg-field">
                        <label class="perc_dialog_label perc-required-field" for="perc-select-template-type"><i18n:message key="perc.ui.newsitedialog.label@Type:"/></label><br/>
                        <select id="perc-select-template-type">
                            <option value="base"><i18n:message key = "perc.ui.new.site.dialog@Base"/></option>
                            <option value="resp"><i18n:message key = "perc.ui.new.site.dialog@Responsive"/></option>
                        </select>
                    </div>
                    <div class="perc-new-sitedlg-templates-field">
                        <label id="perc_dialog_label_template" class="perc_dialog_label"><i18n:message key="perc.ui.newsitedialog.label@Template:"/></label><br/>
                        <input class="perc_dialog_field" id="selectedtemplate" name="selectedtemplate" type="hidden" aria-labelledby="perc_dialog_label_template"/>
                        <input class="perc_dialog_field" id="perc_selected_basetemplate" name="perc_selected_basetemplate" type="hidden" aria-labelledby="perc_dialog_label_template"/>
                        <input class="perc_dialog_field" id="perc_selected_resptemplate" name="perc_selected_resptemplate" type="hidden" aria-labelledby="perc_dialog_label_template"/>
                        <div id="perc-base-template-lib"></div>
                        <div id="perc-resp-template-lib" style="display:none"></div>
                    </div>
                </div>
            </div> 
        </div>
        <div class="ui-layout-south">
            <div id="perc_newSiteDialogButtons" style="z-index: 100;"></div>
        </div>
    </csrf:form>
</div>
