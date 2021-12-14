<%@ include file="/ui/admin/AdminAuthentication.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout"%>
<%@ taglib tagdir="/WEB-INF/tags/nav" prefix="rxnav"%>
<%@ taglib uri="http://myfaces.apache.org/trinidad" prefix="tr"%>
<%@ taglib uri="http://rhythmyx.percussion.com/components" prefix="rxcomp"%>

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

<c:set var="page_title" scope="request" value="RxFix Tool"/>

<layout:admin>
   <jsp:body>
      <rxcomp:menubar> 
			<rxcomp:menuitem value="Help" 
		   	onclick="openHelpWindow('#{sys_rxfix.helpFile}', true)"/>
    	</rxcomp:menubar>
   
      <rxnav:adminbreadcrumbs/>
      <tr:panelHeader text="RxFix - Choose Actions">
         <f:verbatim>
            <p>Choose which of the following fixes to preview. Once the preview
            has run, you will be able to confirm that you want to run the fix.</p> 
         </f:verbatim>

         <h:dataTable var="e" value="#{sys_rxfix.entries}">
            <h:column>
               <h:selectBooleanCheckbox value="#{e.dofix}"></h:selectBooleanCheckbox>
            </h:column>
            <h:column>
               <h:outputText value="#{e.fixname}"/>
            </h:column>
         </h:dataTable>

         <f:verbatim><br/></f:verbatim>
         <f:verbatim>
            <p><i>Please note:</i> The preview may take a few minutes to run,
            please be patient.</p>
         </f:verbatim>
         <h:commandButton value="Preview" action="#{sys_rxfix.preview}" />
      </tr:panelHeader>
   </jsp:body>
</layout:admin>
