<!--
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
  ~      https://www.percusssion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see https://www.gnu.org/licenses/
  -->

<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page"
	xmlns:c="http://java.sun.com/jsp/jstl/core"
	xmlns:rx="http://rhythmyx.percussion.com/components"
	version="1.2">
	<jsp:scriptlet>
		request.setAttribute("servlet_path", request.getServletPath());
	</jsp:scriptlet>
	<jsp:useBean id="sys_tabs" 
		class="com.percussion.rx.ui.jsf.beans.PSTopNavigation"
		scope="page"/>
	<jsp:setProperty name="sys_tabs" property="path" 
		value="${servlet_path}"/>
	<rx:tabContainer value="${sys_tabs}" var="tab">
		<c:choose>
			<c:when test='${tab.enabled}'>
				<a href="${tab.url}" 
					class="${tab.style}" 
					target="_parent">${tab.label}</a>
			</c:when>
			<c:otherwise>
				<a class="${tab.style}">${tab.label}</a>
			</c:otherwise>
		</c:choose>
	</rx:tabContainer>
</jsp:root>
