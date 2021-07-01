/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.rx.publisher.servlet;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.rx.publisher.IPSRxPublisherService;
import com.percussion.rx.publisher.PSRxPublisherServiceLocator;
import com.percussion.rx.publisher.data.PSDemandWork;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherException;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

/**
 * Invokes demand publishing and presents a user interface to allow the user to
 * see the current status of the publishing job associated with the demand
 * request.
 * <p>
 * After queuing the request, this servlet invokes a JSP to present the progress
 * data.
 * 
 * @author dougrand 
 */
public class PSDemandPublishServlet extends HttpServlet
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   private static final Logger log = LogManager.getLogger("publish-jsp");

   /*
    * (non-Javadoc)
    * 
    * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest,
    *      javax.servlet.http.HttpServletResponse)
    */
   @Override
   protected void service(HttpServletRequest request, HttpServletResponse resp)
         throws ServletException, IOException
   {
      String ids[] = request.getParameterValues(IPSHtmlParameters.SYS_CONTENTID);
      String edition = request.getParameter(IPSHtmlParameters.SYS_EDITIONID);
      String folder = request.getParameter(IPSHtmlParameters.SYS_FOLDERID);
      String site = request.getParameter(IPSHtmlParameters.SYS_SITEID);
      String gen = request.getParameter("sys_demandPublishingGenerator");
      
      int editionid;
      int folderid = convertInteger(folder, "folder");
      int contentids[] = convertArray(ids, "content ids");

      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      IPSGuid folderg = gmgr.makeGuid(new PSLocator(folderid));
      
      /* The last part after the . is used in the output, don't want all jsp pages
       * to show up as [jsp].
       */


      // attempt to figure one out
      final String DEFAULT_GENERATOR = 
         "Java/global/percussion/system/sys_SelectedItemsGenerator";
      String clistGenerator = StringUtils.isBlank(gen) ? DEFAULT_GENERATOR : gen;
      
      if (StringUtils.isBlank(edition))
      {
         if (StringUtils.isBlank(site) && !StringUtils.isNumeric(site))
         {
            throw new RuntimeException(
               "Either the edition Id or site Id must be specified when executing "
               + "demand publishing.");
         }
         
         IPSGuid siteGuid = gmgr.makeGuid(Integer.parseInt(site), PSTypeEnum.SITE);
         IPSPublisherService svc = PSPublisherServiceLocator.getPublisherService();
         List<IPSGuid> editionIds;
         try
         {
            editionIds = svc.findEditionsBySiteAndContentListGenerator(
               siteGuid, clistGenerator);
         }
         catch (PSPublisherException e)
         {
            throw new ServletException(e);
         }
         if (editionIds.isEmpty())
         {
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
         //if there is more than 1, they are effectively equivalent, so pick one
         editionid = editionIds.get(0).getUUID();
         log.info("Demand publishing with resolved edition " + editionid); 
      }
      else
      {
         editionid = convertInteger(edition, "edition");
         log.info("Demand publishing with supplied edition " + editionid); 
      }
      
      PSDemandWork work = new PSDemandWork();
      for(int i = 0; i < contentids.length; i++)
      {
         IPSGuid contentg = gmgr.makeGuid(new PSLocator(contentids[i]));
         work.addItem(folderg, contentg);
      }
      
      IPSRxPublisherService pubsvc = PSRxPublisherServiceLocator
            .getRxPublisherService();
      long requestid;
      try
      {
         requestid = pubsvc.queueDemandWork(editionid, work, clistGenerator);
      }
      catch (Exception e)
      {
         throw new ServletException(e);
      }

      request.setAttribute("requestid", requestid);
      RequestDispatcher dispatcher 
         = request.getRequestDispatcher("/ui/pubruntime/DemandPublish.jsp");
      dispatcher.forward(request, resp);
   }

   /**
    * Convert an array of values
    * 
    * @param ids the values, never <code>null</code>
    * @param typename the name of the type, assumed never <code>null</code> or
    *            empty.
    * @return the converted array, never <code>null</code>.
    */
   private int[] convertArray(String[] ids, String typename)
   {
      if (ids == null)
      {
         throw new IllegalArgumentException("Ids array must be non-null");
      }
      int rval[] = new int[ids.length];
      for (int i = 0; i < ids.length; i++)
      {
         rval[i] = convertInteger(ids[i], typename);
      }
      return rval;
   }

   /**
    * Convert a single value
    * 
    * @param value the value, never <code>null</code> or empty
    * @param typename the typename, never <code>null</code> or empty
    * @return
    */
   private int convertInteger(String value, String typename)
   {
      if (StringUtils.isBlank(value))
      {
         throw new IllegalArgumentException("value may not be null or empty");
      }
      if (StringUtils.isBlank(typename))
      {
         throw new IllegalArgumentException("typename may not be null or empty");
      }
      try
      {
         return Integer.parseInt(value);
      }
      catch (NumberFormatException ex)
      {
         throw new IllegalArgumentException("Cannot convert value " + value
               + " for type " + typename);
      }
   }
}
