<%@page errorPage="error.jsp" 
   import="com.percussion.services.publisher.*,com.percussion.util.*"
   import="com.percussion.utils.guid.IPSGuid"
   import="com.percussion.utils.exceptions.PSExceptionHelper"
   import="com.percussion.services.catalog.PSTypeEnum"
   import="com.percussion.services.guidmgr.IPSGuidManager"
   import="com.percussion.services.guidmgr.PSGuidManagerLocator"
   import="java.text.MessageFormat"
   import="java.util.Iterator"
   import="java.util.List"
   import="org.apache.commons.lang.StringUtils"
   import="org.apache.commons.logging.Log"
   import="org.apache.commons.logging.LogFactory"
   import="com.percussion.rx.publisher.IPSPublisherJobStatus"
   import="com.percussion.rx.publisher.IPSRxPublisherService"
   import="com.percussion.rx.publisher.PSRxPublisherServiceLocator"
   import="com.percussion.rx.publisher.data.PSDemandWork"
%>
<%@ taglib uri="http://rhythmyx.percussion.com/components"
   prefix="rxcomp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
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
   String ids[] = request.getParameterValues(IPSHtmlParameters.SYS_CONTENTID);
   String edition = request.getParameter(IPSHtmlParameters.SYS_EDITIONID);
   String folder = request.getParameter(IPSHtmlParameters.SYS_FOLDERID);
   String site = request.getParameter(IPSHtmlParameters.SYS_SITEID);
   String gen = request.getParameter("sys_demandPublishingGenerator");
   if (StringUtils.isBlank(folder) && !StringUtils.isNumeric(folder))
   {
      throw new RuntimeException(
            "Demand publishing must be launched within a folder.");
   }
   IPSPublisherService svc = PSPublisherServiceLocator.getPublisherService();
   IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
   /* The last part after the . is used in the output, don't want all jsp pages
    * to show up as [jsp].
    */
   Log log = LogFactory.getLog("publish-jsp");

   if (StringUtils.isBlank(edition))
   {
      if (StringUtils.isBlank(site) && !StringUtils.isNumeric(site))
      {
         throw new RuntimeException(
               "Either the edition Id or site Id must be specified when executing "
                     + "demand publishing.");
      }
      // attempt to figure one out
      final String DEFAULT_GENERATOR = 
         "Java/global/percussion/system/sys_SelectedItemsGenerator";
      String clistGenerator = StringUtils.isBlank(gen) ? DEFAULT_GENERATOR : gen;

      IPSGuid siteGuid = gmgr.makeGuid(Integer.parseInt(site), PSTypeEnum.SITE);
      List editionIds = svc.findEditionsBySiteAndContentListGenerator(
            siteGuid, clistGenerator);
      //if there is more than 1, they are effectively equivalent, so pick one
      if (editionIds.isEmpty())
      {
         StringBuffer buf = new StringBuffer(100);
         Iterator iter = editionIds.iterator();
         while (iter.hasNext())
         {
            IPSGuid id = (IPSGuid) iter.next();
            if (buf.length() > 0)
               buf.append(", ");
            buf.append(id.getUUID());
         }
         String msg = 
            "Your system is not properly configured to support automatic "
               + "edition resolution for demand publishing on this site. There "
               + "are no matching editions on site {0}. There needs to be an "
               + "edition that has 1 content list using the ''{1}'' generator.";
         Object[] params =
         {
            site,
            clistGenerator
         };
         throw new RuntimeException(MessageFormat.format(msg, params));
      }
      edition = String.valueOf(((IPSGuid) editionIds.get(0)).getUUID());
      log.info("Demand publishing with resolved edition " + edition);
   }
   else
      log.info("Demand publishing with supplied edition " + edition);

   IPSRxPublisherService rxsvc = 
      PSRxPublisherServiceLocator.getRxPublisherService();
   PSDemandWork work = new PSDemandWork();
   IPSGuid folderGuid = 
      gmgr.makeGuid(Integer.parseInt(folder), PSTypeEnum.LEGACY_CONTENT);
   for (int i = 0; i < ids.length; i++)
   {
      work.addItem(folderGuid, 
         gmgr.makeGuid(Integer.parseInt(ids[i]), PSTypeEnum.LEGACY_CONTENT));
   }
   long workId = -1;
   int count = -3;
   String exceptionMsg = "";
   try
   {
      workId = rxsvc.queueDemandWork(Integer.parseInt(edition), work);
   }
   catch (Exception e)
   {
     Throwable t = PSExceptionHelper.findRootCause(e, false);
     
     if(t == null)
     {
        t = e;  
     }
   
      exceptionMsg = t.getMessage();
     
     if(StringUtils.isBlank(exceptionMsg))
     {
        exceptionMsg = t.toString();
     }
   }

   if (workId != -1)
   {
      Long jobId = rxsvc.getDemandRequestJob(workId);
      final int TIMEOUT = 20000; //milliseconds
      final int WAITTIME = 100; //milliseconds
      int totalTime = 0;
      while (jobId == null && totalTime < TIMEOUT)
      {
         jobId = rxsvc.getDemandRequestJob(workId);
         if (jobId == null)
         {
            totalTime += WAITTIME;
            Thread.sleep(WAITTIME);
         }
      }
      if (jobId == null)
         count = -2;
      else
      {
         IPSPublisherJobStatus.State state;
         totalTime = 0;
         do
         {
            state = rxsvc.getDemandWorkStatus(workId);
            totalTime += WAITTIME;
            Thread.sleep(WAITTIME);
      }
      while (state == IPSPublisherJobStatus.State.QUEUEING 
               && totalTime < TIMEOUT);
         if (state == IPSPublisherJobStatus.State.QUEUEING)
            count = -1;
         else
         {
            IPSPublisherJobStatus status = rxsvc
                  .getPublishingJobStatus(jobId.longValue());
            count = status.countTotalItems();
         }
      }
   }
   pageContext.setAttribute("count", new Integer(count));
   pageContext.setAttribute("exceptionMsg", exceptionMsg);
%>
<html>
<head>
   <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
   <title>${rxcomp:i18ntext('jsp_publish@Demand Publishing',param.sys_lang)}</title>
   <%@include file="../header.jsp"%>
</head>
<body>
   <div style="background-color: white; margin: 10px; padding-top: 0px; padding: 10px">
      <p><img src="../../sys_resources/images/banner_bkgd.jpg"></p>
      <h3>${rxcomp:i18ntext('jsp_publish@Demand Publishing',param.sys_lang)}<h3>
      <p>${rxcomp:i18ntext('jsp_publish@Edition',param.sys_lang)}: <%=edition%></p>
      <c:choose>
         <c:when test="${count < 0}">
            <c:choose>
               <c:when test="${count == -1}">${rxcomp:i18ntext('jsp_publish@longqueue',param.sys_lang)}</c:when>
               <c:when test="${count == -3}">${exceptionMsg}</c:when>
               <c:otherwise>${rxcomp:i18ntext('jsp_publish@nojob',param.sys_lang)}</c:otherwise>
            </c:choose>
         </c:when>
         <c:otherwise>
            <p>${rxcomp:i18ntext('jsp_publish@Queued',param.sys_lang)}&#160;${count}&#160;
            <c:choose>
               <c:when test="${count == 1}">${rxcomp:i18ntext('jsp_publish@item',param.sys_lang)}</c:when>
               <c:otherwise>${rxcomp:i18ntext('jsp_publish@items',param.sys_lang)}</c:otherwise>
            </c:choose>
         </c:otherwise>
      </c:choose>
      </p>
   </div>
</body>
</html>
