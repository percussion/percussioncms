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
package com.percussion.services.publisher.impl;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.rx.publisher.PSPublisherUtils;
import com.percussion.server.PSServer;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.memory.IPSCacheAccess;
import com.percussion.services.memory.PSCacheAccessLocator;
import com.percussion.services.publisher.IPSContentList;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.IPSPublisherServiceErrors;
import com.percussion.services.publisher.PSPublisherException;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.services.publisher.data.PSContentListItem;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSBaseHttpUtils;
import com.percussion.util.PSUrlUtils;
import com.percussion.utils.exceptions.PSExceptionHelper;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.timing.PSStopwatch;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Takes parameters and constructs a content list suitable for the publisher. It
 * does this by running the content list execution in the publisher service and
 * translates the resulting assembly items into an xml document.
 * 
 * @author dougrand
 * 
 */
public class PSContentListServlet extends HttpServlet
{
   /**
    * Cache region in use
    */
   private static final String ms_region = "contentlist";

   /**
    * Logger for content list servlet
    */
   private static Log ms_log = LogFactory.getLog(PSContentListServlet.class);

   /**
    * Date format used in content lists
    */
   private static final SimpleDateFormat ms_datefmt = new SimpleDateFormat(
         "yyyy-MM-dd HH:mm:ss");

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   /*
    * (non-Javadoc)
    * 
    * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest,
    *      javax.servlet.http.HttpServletResponse)
    */
   @SuppressWarnings("unchecked")
   @Override
   protected void service(HttpServletRequest request,
         HttpServletResponse response)
   {
      IPSPublisherService pub = PSPublisherServiceLocator.getPublisherService();
      IPSCacheAccess cache = PSCacheAccessLocator.getCacheAccess();
      PSStopwatch sw = new PSStopwatch();
      sw.start();
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      String siteid = request.getParameter(IPSHtmlParameters.SYS_SITEID);
      String delivery = request
            .getParameter(IPSHtmlParameters.SYS_DELIVERYTYPE);
      String publishstr = request.getParameter(IPSHtmlParameters.SYS_PUBLISH);
      String context = request.getParameter(IPSHtmlParameters.SYS_CONTEXT);
      String assemblycontext = request
            .getParameter(IPSHtmlParameters.SYS_ASSEMBLY_CONTEXT);
      String contentlistname = request
            .getParameter(IPSHtmlParameters.SYS_CONTENTLIST);
      String publicationid = request
            .getParameter(IPSHtmlParameters.SYS_PUBLICATIONID);
      String maxresultsstr = request
            .getParameter(IPSHtmlParameters.MAXRESULTSPERPAGE);
      int maxresults = !StringUtils.isBlank(maxresultsstr) ? Integer
            .parseInt(maxresultsstr) : 0;
      String pagestr = request.getParameter(IPSHtmlParameters.SYS_PAGE);
      int page = !StringUtils.isBlank(pagestr) ? Integer.parseInt(pagestr) : 0;

      String host = request.getParameter(IPSHtmlParameters.SYS_HOST);
      String protocol = request.getParameter(IPSHtmlParameters.SYS_PROTOCOL);
      String portstr = request.getParameter(IPSHtmlParameters.SYS_PORT);
      IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
      int port = 0;
      if (!StringUtils.isBlank(portstr))
      {
         port = Integer.parseInt(portstr);
      }
      boolean publish = publishstr == null
            || !publishstr.equalsIgnoreCase("unpublish");
      try
      {
         requiredParam(delivery, IPSHtmlParameters.SYS_DELIVERYTYPE);
         requiredParam(context, IPSHtmlParameters.SYS_CONTEXT);
         requiredParam(siteid, IPSHtmlParameters.SYS_SITEID);
         requiredParam(contentlistname, IPSHtmlParameters.SYS_CONTENTLIST);
         
         int assemblyContext = 0;
         if (!StringUtils.isBlank(assemblycontext))
         {
            assemblyContext = Integer.parseInt(assemblycontext);
         }
         else
         {
            assemblyContext = Integer.parseInt(context);   
         }

         StringWriter writer = new StringWriter();
         XMLOutputFactory ofact = XMLOutputFactory.newInstance();
         XMLStreamWriter f = beginDocument(delivery, context, writer, ofact);

         IPSContentList list = pub.loadContentList(contentlistname);
         Map<String, String> overrides = new HashMap<>();
         Map<String, String[]> params = request.getParameterMap();
         for (Map.Entry<String, String[]> e : params.entrySet())
         {
            if (e.getValue().length > 0)
            {
               overrides.put(e.getKey(), e.getValue()[0]);
            }
         }
         List<PSContentListItem> items = null;
         if (maxresults > 0 && !StringUtils.isBlank(publicationid))
         {
            items = (List<PSContentListItem>) cache.get(publicationid,
                  ms_region);
         }

         if (items == null)
         {
            IPSGuid deliveryContextId = new PSGuid(PSTypeEnum.CONTEXT, context);
            IPSGuid siteId = new PSGuid(PSTypeEnum.SITE, siteid);
            items = pub.executeContentList(list, overrides, publish, 
                  deliveryContextId, siteId);
            if (!StringUtils.isBlank(publicationid) && maxresults > 0)
            {
                  cache.save(publicationid, (Serializable) items, ms_region);
            }
         }

         // If we're limiting by results, create a sublist
         boolean done = false;
         if (maxresults > 0)
         {
            int start = page * maxresults;
            int end = start + maxresults;
            if (end >= items.size())
            {
               done = true;
               end = items.size();
            }
            items = items.subList(start, end);
         }

         for (PSContentListItem item : items)
         {
            formatContentListItem(pub, cms, asm, f, assemblyContext, host,
                  protocol, port, publish, list, item);
         }
         f.writeCharacters("\n");
         if (maxresults > 0)
         {
            // No next if done
            if (!done)
            {
               // Generate the element for the next page
               f.writeStartElement("PSXNextPage");
               StringBuffer urlbuf = request.getRequestURL();
               String path = PSBaseHttpUtils.getPath(urlbuf.toString());
               if (StringUtils.isBlank(path))
                  urlbuf.append(PSPublisherUtils.SERVLET_URL_PATH);
               urlbuf.append('?');
               urlbuf.append(request.getQueryString());
               String url = urlbuf.toString();
               if (url.contains(IPSHtmlParameters.SYS_PAGE))
               {
                  url = PSUrlUtils.replaceUrlParameterValue(url,
                        IPSHtmlParameters.SYS_PAGE, Integer.toString(page + 1));
               }
               else
               {
                  url += "&" + IPSHtmlParameters.SYS_PAGE + "=" + (page + 1);
               }
               f.writeCharacters(url);
            }
            else
            {
               // Clear the cached data
               if (!StringUtils.isBlank(publicationid))
               {
                  cache.evict(publicationid, ms_region);
               }
            }
            // Create new url with the right page. If
         }

         f.writeEndDocument();
         response.setContentType("text/xml; charset=utf-8");
         PrintWriter w = response.getWriter();
         f.flush();
         f.close();
         writer.close();
         w.print(writer.toString());
         sw.stop();
         String info = MessageFormat.format(
               "Created content list {0} publication id {3}\n" +
               "Site id: {1}, Delivery: {2}, Context: {4}\n" +
               "{5} result items took {6} milliseconds",
               contentlistname, siteid, delivery, publicationid, 
               context, items.size(), sw.elapsed());
         ms_log.debug(info);
      }
      catch (Exception e)
      {
         response.setContentType("text/plain");
         PrintWriter w;
         try
         {
            Throwable orig = PSExceptionHelper.findRootCause(e, true);
            ms_log.error("Content list failure", orig);
            w = response.getWriter();
            w.println(e.getLocalizedMessage());
         }
         catch (IOException e1)
         {
            throw new RuntimeException(e1);
         }
      }
   }

   /**
    * Assemble a single content list item
    * 
    * @param pub the publishing service, never <code>null</code>
    * @param cms the legacy content service, never <code>null</code>
    * @param asm the assembly service, never <code>null</code>
    * @param formatter the xml output formatter, never <code>null</code>
    * @param assemblyContext the context ID, which is the ID of assembly context
    *    if defined; otherwise it is the ID of delivery context.
    * @param host the host, if <code>null</code> or empty, defaults to the
    *    server's host.
    * @param protocol the protocol as a string, defaults to server's protocol
    * @param port the port, defaults to server's port
    * @param publish the publish flag
    * @param list the content list that is being processed, never
    *    <code>null</code>
    * @param item the current item being run, never <code>null</code>
    * @throws XMLStreamException
    * @throws PSPublisherException
    * @throws PSAssemblyException
    */
   protected void formatContentListItem(IPSPublisherService pub,
         IPSCmsObjectMgr cms, IPSAssemblyService asm,
         XMLStreamWriter formatter, int assemblyContext, String host,
         String protocol, int port, boolean publish, IPSContentList list,
         PSContentListItem item)
      throws XMLStreamException, PSPublisherException, PSAssemblyException
   {
      if (pub == null)
      {
         throw new IllegalArgumentException("pub may not be null");
      }
      if (cms == null)
      {
         throw new IllegalArgumentException("cms may not be null");
      }
      if (asm == null)
      {
         throw new IllegalArgumentException("asm may not be null");
      }
      if (StringUtils.isBlank(host))
      {
         host = PSServer.getHostAddress();
      }
      if (StringUtils.isBlank(protocol))
      {
         protocol = "http";
      }
      if (list == null)
      {
         throw new IllegalArgumentException("list may not be null");
      }
      if (item == null)
      {
         throw new IllegalArgumentException("item may not be null");
      }
      if (port == 0)
      {
         if (protocol.equals("http"))
         {
            port = PSServer.getListenerPort();
         }
         else
         {
            port = PSServer.getSslListenerPort();
         }
      }
      formatter.writeCharacters("\n  ");
      formatter.writeStartElement("contentitem");
      PSLegacyGuid cid = (PSLegacyGuid) item.getItemId();
      formatter.writeAttribute("contentid", Integer
            .toString(cid.getContentId()));
      formatter.writeAttribute("revision", Integer.toString(cid.getRevision()));
      formatter.writeAttribute("unpublish", publish ? "no" : "yes");
      formatter.writeAttribute("variantid", Long.toString(item.getTemplateId()
            .longValue()));
      formatter.writeCharacters("\n    ");
      List<Integer> ids = new ArrayList<>();
      ids.add(new Integer(cid.getContentId()));
      PSComponentSummary s = cms.loadComponentSummaries(ids).get(0);
      formatter.writeCharacters("\n    ");
      formatter.writeStartElement("title");
      formatter.writeCharacters(s.getName());
      formatter.writeEndElement();
      formatter.writeCharacters("\n    ");
      formatter.writeStartElement("contenturl");

      IPSGuid folderguid = item.getFolderId();

      String tid = Long.toString(item.getTemplateId().longValue());
      if (tid == null)
      {
         throw new PSPublisherException(IPSPublisherServiceErrors.RUNTIME_ERROR,
               "no template id found");
      }
      IPSAssemblyTemplate template = asm.loadUnmodifiableTemplate(new PSGuid(
            PSTypeEnum.TEMPLATE, tid));
      String url = pub.constructAssemblyUrl(host, port, protocol, item
            .getSiteId(), item.getItemId(), folderguid, template, list
            .getFilter(), assemblyContext, publish);
      formatter.writeCharacters(url);
      formatter.writeEndElement();
      formatter.writeCharacters("\n    ");
      formatter.writeStartElement("delivery");
      formatter.writeCharacters("\n      ");
      formatter.writeStartElement("location");
      formatter.writeCharacters(item.getLocation());
      formatter.writeEndElement();
      formatter.writeEndElement();
      formatter.writeCharacters("\n    ");
      formatter.writeStartElement("modifydate");
      if (s.getContentLastModifiedDate() != null)
         formatter.writeCharacters(ms_datefmt.format(s
               .getContentLastModifiedDate()));
      formatter.writeEndElement();
      formatter.writeCharacters("\n    ");
      formatter.writeStartElement("modifyuser");
      formatter.writeCharacters(s.getContentLastModifier());
      formatter.writeEndElement();
      formatter.writeCharacters("\n    ");
      formatter.writeStartElement("contenttype");
      formatter.writeCharacters(Long.toString(s.getContentTypeId()));
      formatter.writeEndElement();
      formatter.writeCharacters("\n  ");
      formatter.writeEndElement();
   }

   /**
    * Begin the output content list document
    * 
    * @param delivery
    * @param context
    * @param writer
    * @param ofact
    * @return the output STaX writer for the document, never <code>null</code>
    * @throws XMLStreamException
    */
   private XMLStreamWriter beginDocument(String delivery, String context,
         StringWriter writer, XMLOutputFactory ofact) throws XMLStreamException
   {
      XMLStreamWriter f = ofact.createXMLStreamWriter(writer);
      f.writeStartDocument();
      f.writeCharacters("\n");
      f.writeStartElement("contentlist");
      f.writeAttribute("context", context);
      f.writeAttribute("deliverytype", delivery);
      return f;
   }

   /**
    * Validate that the passed parameter value is not <code>null</code> or
    * empty.
    * 
    * @param param the param value
    * @param paramName the name of the param, assumed never <code>null</code>
    *           or empty
    */
   private void requiredParam(String param, String paramName)
   {
      if (StringUtils.isBlank(param))
      {
         throw new IllegalArgumentException(paramName
               + " is a required parameter");
      }
   }

}
