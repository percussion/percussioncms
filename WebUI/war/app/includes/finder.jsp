<%@ page import="com.percussion.services.utils.jspel.PSRoleUtilities" %>
<%@ taglib uri="/WEB-INF/tmxtags.tld" prefix="i18n" %>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>

<%
    String debug = request.getParameter("debug");     
    if(debug == null)
        debug = "false";
    String site = request.getParameter("site");
    if (site == null)
        site = "";
    String openedObject = request.getParameter("openedObject");
    boolean showPage = "PERC_PAGE".equals(openedObject);
    String finderMode = request.getParameter("finderMode");
    boolean isLibraryMode = "library".equals(finderMode);
    String view = request.getParameter("view");
    String className = "perc-ui-component-ready";
    if(view.equals("editor")){
    	className = "perc-ui-component-processing";
    }
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
<i18n:settings lang="<%= locale %>" prefixes="perc.ui." debug="<%= debug %>"/>
<div class='perc-finder-outer <%=className%>' perc-ui-component="perc-ui-component-finder" collapsed="true">
    <table class="perc-finder-header">
        <tr>
            <td scope = "row" class="perc-finder-view-options" <%if(isLibraryMode){%>style="display:none"<%}%>>
                <a tabindex="8" role="button" id='perc-finder-expander' class="perc-font-icon icon-plus-sign ui-state-enabled fas fa-plus"></a>
                <a tabindex="9" role="button" id="perc-finder-choose-columnview" class="perc-font-icon icon-columns ui-active fas fa-columns" title="<i18n:message key="perc.ui.finder@Column View"/>"></a>
                <a tabindex="10" role="button" id="perc-finder-choose-listview" class="perc-font-icon icon-list fas fa-list" title="<i18n:message key= "perc.ui.finder@List View"/>"></a>
                <a tabindex="11" role="button" id="perc-finder-choose-mypagesview" class="perc-font-icon icon-star fas fa-star" title="<i18n:message key ="perc.ui.finder@My Pages"/>"></a>
            </td>
            <td class="perc-finder-goto-or-search">
                <div class="perc-wrapper <%if(!isLibraryMode){%>perc-wrapper-bg<%}%>">
                    <table>
                        <tr>
                            <td scope = "row" class="perc-flex">
                                <input  tabindex="12" id="mcol-path-summary" class="perc-finder-goto-or-search" type="text" aria-label="Enter Path or Search"/>
                            </td>
                            <td class="perc-fixed">
                                <%if(!isLibraryMode){%>
	                                <a  tabindex="13" class="perc-action perc-action-goto-or-search perc-font-icon icon-search fas fa-search" href="#" role="button" title="Click to Search"></a>
	                                <div class="perc-hide" style="display:none;" aria-hidden="true">
	                                    <!-- using this old ui behind the scenes to minimize code changes -->
	                                    <a id="perc-finder-go-action" title="Go" href="#"></a>
	                                    <a id="perc-finder-search-submit" title="Search Submit" href="#"></a>
	                                    <input id="perc-finder-item-search" type="text" title="Search"/>
	                                </div>
                                <%}%>
                            </td>
                        </tr>
                    </table>
                </div>
            </td>
            <td class="perc-finder-menu"><!-- finder actions buttons will magically show up here! --></td>
        </tr>
    </table>
    <div class="perc-finder-message" style="display: none;" <%if(isLibraryMode){%>style="width:150px"<%}%>>
        <!-- finder messages will be appended to this div -->
    </div>
    <div class="perc-finder-body perc-resize">
        <div class="perc-finder perc-resize-height">
            <!-- path explorer is appended to this div -->
        </div>
        <!-- list-view paging toolbar is appended here -->
    </div>
</div>

