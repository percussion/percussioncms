<%@ page import="com.percussion.content.ui.aa.actions.*" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="java.util.*,com.percussion.i18n.PSI18nUtils" %>
<%@ taglib uri="http://rhythmyx.percussion.com/components"
	prefix="rxcomp"%>
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
   String locale = PSI18nUtils.getSystemLanguage();
%>

<%
   PSAAClientActionFactory factory = PSAAClientActionFactory.getInstance();
   IPSAAClientAction action = factory.getAction("GetItemTemplatesForSlot");
   String objId = request.getParameter(IPSAAClientAction.OBJECT_ID_PARAM);
   String noButtons = request.getParameter("noButtons");
   Map params = new HashMap(1);
   params.put(IPSAAClientAction.OBJECT_ID_PARAM, objId);
   PSActionResponse aresponse = action.execute(params);
   String rawObj = aresponse.getResponseData();
   JSONArray temps = new JSONArray(rawObj);   
   
%>


<style>
#main {
	height: 90%;
	width: 95%;
	left: 1%;
	top: 1%;
	position: relative;
	padding: 10px 10px 10px 15px;
}

.sectionLabels {
    font-family: Arial;
    font-weight: Bold;
    font-size: 12px;
    color: #000000;
    overflow: hidden;
}

.selectOptions {
    font-family: Arial;
    font-size: 12px;
    width: 100%;
    height: 90%;
    color: #000000;
}

.buttonStyle {
    border: 1px solid black;
    width: 60px;
}

</style>
  <%
      if(StringUtils.isBlank(noButtons))
      {
  %>
  <div dojoType="LayoutContainer" id="main" layoutChildPriority="none" align="center" style="border: 0px solid black">
  <%
  }
  %>
      <div dojoType="ps:PSSplitContainer" layoutAlign="client" orientation="horizontal" sizerWidth="1"
           activeSizing="true" sizerVisible="false" persist="false" style="border: 0px solid black; width: 100%; height: 90%;">
         <div dojoType="ContentPane" sizeMin="30" sizeShare="30">            
            <span class="sectionLabels" align="left">${rxcomp:i18ntext('jsp_selecttemplate@selecttemplate',locale)}</span>
            <div dojoType="ContentPane" style="background-color: white; border: 1px solid lightgrey; width: 98%; height: 88%">
            <select class="selectOptions" size="100" id="ps.select.templates.wgtTemplates" style="border: 0px solid black;height:100%;width:100%;">
            <%
               int len = temps.length();
               for (int i = 0; i < len; i++)
               {
                 JSONObject jobj = temps.getJSONObject(i);
                 String obid = jobj.getString("objectid");
                 obid = obid.substring(2, obid.length() - 2);
                 obid = StringUtils.replace(obid, "\\", "");
                 String name = jobj.getString("name");
                 name = name.substring(2, name.length() - 2);
            %>
            <option value='<%= obid %>'><%= name %></option>
            <%
               }
            %>
            </select>
            </div>
         </div>
         <div dojoType="ContentPane" sizeMin="70" sizeShare="70" style="padding-left: 20px;">
            <span class="sectionLabels">${rxcomp:i18ntext('jsp_selecttemplate@templatepreview',locale)}</span>
            <div dojoType="ContentPane" style="overflow:auto;background-color: white; border: 1px solid lightgrey; width: 98%; height: 88%"
                 id="ps.select.templates.wgtPreviewPane">
            </div>
         </div>
      </div>
   <%
      if(StringUtils.isBlank(noButtons))
      {
   %>
   <table dojoType="ContentPane" layoutAlign="bottom" style="border: 0px light gray">
   <tr>
   <td align="right">
       <button class="buttonStyle" dojoType="ps:PSButton" id="ps.select.templates.wgtButtonSelect">${rxcomp:i18ntext('jsp_selecttemplate@select',locale)}</button>
   </td>
   <td align="left">
       <button class="buttonStyle" dojoType="ps:PSButton" id="ps.select.templates.wgtButtonCancel">${rxcomp:i18ntext('jsp_selecttemplate@cancel',locale)}</button>
   </td>
   </table>
  
</div>
 <%
     }
  %>
