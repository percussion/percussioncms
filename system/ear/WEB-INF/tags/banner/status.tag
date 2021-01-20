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
          xmlns:fn="http://java.sun.com/jsp/jstl/functions"
          xmlns:rxcomp="http://rhythmyx.percussion.com/components"
          version="1.2">
   <jsp:useBean id="status" scope="session"
                class="com.percussion.rx.ui.jsf.beans.PSUserStatus" />
   <table border="0" cellpadding="0" cellspacing="0" class="user-info" width="100%" summary="User session information section" tabindex="0">
      <tr>
         <th class="user-status-field" scope="row" id="user">${fn:escapeXml(rxcomp:i18ntext("jsp_userstatus@User",status.locale))}:</th>
         <td class="user-status-value" headers="user" tabindex="0">${status.user}</td>
      </tr>
      <tr>
         <th class="user-status-field" scope="row" id="status">${fn:escapeXml(rxcomp:i18ntext("jsp_userstatus@Roles",status.locale))}:</th>
         <td class="user-status-value" headers="status"><script language="javascript">
             var textWin = null;
             function textWindow(s)
             {
                 textWin = window.open('','UserRoles','width=500,height=100,resizable=yes');
                 textWin.document.write('<h3>${fn:escapeXml(status.fullrolestr)}</h3>');
                 textWin.document.title='${fn:escapeXml(rxcomp:i18ntext("jsp_userstatus@User Roles",param.sys_lang))}';
                 textWin.document.close();

             }
         </script><a class="banner_blue" href="javascript:void(0)"
                     title="${fn:escapeXml(status.fullrolestr)}"
                     OnClick="textWindow('${fn:escapeXml(status.fullrolestr)}')">${fn:escapeXml(status.rolestr)}</a></td>
      </tr>
      <tr>
         <th class="user-status-field" scope="row" id="community">${fn:escapeXml(rxcomp:i18ntext("jsp_userstatus@Community",status.locale))}:</th>
         <td class="user-status-value" headers="community"><a
                 href="${status.rxloginurl}"
                 title="${fn:escapeXml(status.fullcommstr)}"
                 target="_parent">${fn:escapeXml(status.commstr)}</a></td>
      </tr>
      <c:if test="${rxcomp:getLocaleCount() > 1}">
         <tr>
            <th class="user-status-field" scope="row" id="locale">${fn:escapeXml(rxcomp:i18ntext("jsp_userstatus@Locale",status.locale))}:</th>
            <td class="user-status-value" headers="locale"><a
                    href="${status.rxloginurl}"
                    target="_parent">${fn:escapeXml(status.localeDisplay)}</a></td>
         </tr>
      </c:if>
      <tr>
         <th class="user-status-field" scope="row" id="locale" valign="bottom" align="right"><a href="/rxloggingout.jsp" target="_parent"><img
                 alt="Log out" title="Log out" height="17"
                 src="/Rhythmyx/rx_resources/images/${status.locale}/logout.gif" width="62"/></a></th>
         <td rowspan="4" valign="bottom" align="right"></td>
      </tr>
   </table>
</jsp:root>