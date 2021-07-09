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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.services.assembly.impl;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.extension.IPSExtension;
import com.percussion.server.PSServer;
import com.percussion.services.PSMissingBeanConfigurationException;
import com.percussion.services.aaclient.PSAAStubUtil;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSAssemblyTemplate.GlobalTemplateUsage;
import com.percussion.services.assembly.IPSAssemblyTemplate.OutputFormat;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSStringTemplate.PSStringTemplateException;
import com.percussion.utils.exceptions.PSExceptionHelper;
import com.percussion.utils.jexl.IPSScript;
import com.percussion.utils.jexl.PSJexlEvaluator;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.servlet.PSServletUtils;
import com.percussion.utils.string.PSStringUtils;
import com.percussion.utils.timing.PSStopwatch;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * The assembly servlet processes all assembly requests and handles the addition
 * of active assembly decorations
 * 
 * @author dougrand
 */
public class PSAssemblyServlet extends HttpServlet
{


   /**
    * The text to search for looking for the head section
    */
   private static final String HEAD_TAG = "<head";

   /**
    * The text to search for looking for the start of the html document
    */
   private static final String HTML_TAG = "<html";

   /**
    * The text to search for looking for the body section
    */
   private static final String BODY_TAG = "<body";

   /**
    * The text to search for looking for the end of the body section
    */
   private static final String END_BODY_TAG = "</body";

   /**
    * The serial id
    */
   private static final long serialVersionUID = 123192359537611250L;

   /**
    * The logger for this class
    */
   private static final Logger ms_log = LogManager.getLogger(PSAssemblyServlet.class);

   /**
    * A preparsed expression for the inner template
    */
   private IPSScript ms_inner_template = PSJexlEvaluator
         .createStaticExpression("$sys.innertemplate");

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
      // Obtain and validate the arguments
      String template = request.getParameter(IPSHtmlParameters.SYS_TEMPLATE);
      String variantid = request.getParameter(IPSHtmlParameters.SYS_VARIANTID);
      String commands[] = request
            .getParameterValues(IPSHtmlParameters.SYS_COMMAND);
      String reset = request.getParameter("sys_reinit");
      if (!StringUtils.isEmpty(reset) && reset.equalsIgnoreCase("true"))
         PSAAStubUtil.reset();

      if (ms_log.isDebugEnabled())
      {
         ms_log.debug("Parameters to assembly:");
         Enumeration<String> names = request.getParameterNames();
         while (names.hasMoreElements())
         {
            String name = names.nextElement();
            String vals[] = request.getParameterValues(name);
            ms_log.debug(name + ": " + PSStringUtils.arrayToString(vals));
         }
      }
      PSStopwatch watch = new PSStopwatch();

      try
      {
         IPSAssemblyService assembly = PSAssemblyServiceLocator
               .getAssemblyService();
         watch.start();
         IPSAssemblyResult result = assembly.processServletRequest(request,
               template, variantid);

         if (result == null)
         {
            reportError(request, "No results from assembly", response);
         }
         else
         {
            PSJexlEvaluator eval = new PSJexlEvaluator(result.getBindings());
            IPSAssemblyTemplate t = result.getTemplate();
            IPSAssemblyTemplate innert = (IPSAssemblyTemplate) eval
                  .evaluate(ms_inner_template);
            if (innert != null)
            {
               t = innert;
            }
            boolean isAATemplate = t.getActiveAssemblyType().equals(
                  IPSAssemblyTemplate.AAType.Normal);
            boolean isHTML = result.getMimeType() != null && 
               result.getMimeType().startsWith("text/html");
            boolean isAACommand = commands != null && commands.length > 0
                  && commands[0].equals(IPSHtmlParameters.SYS_ACTIVE_ASSEMBLY);
            boolean isLegacyTemplate = t.getAssembler().equals(
                  IPSExtension.LEGACY_ASSEMBLER);
            boolean success = result.isSuccess();

            if (! success)
            {
               String message;
               if (result.getResultData() != null)
               {
                  message = new String(result.getResultData(), "UTF8");
               }
               else
               {
                  message = "Failure - no details available";
               }
               reportError(request, message, response);
               return;
            }
            
            if (isLegacyTemplate)
            {
               if (!t.getGlobalTemplateUsage().equals(
                     GlobalTemplateUsage.Legacy)
                     && !t.getGlobalTemplateUsage().equals(
                           GlobalTemplateUsage.None))
               {
                  // Global template will be relevant here
                  isLegacyTemplate = false;
               }
            }

            boolean doAADecorations = !isLegacyTemplate && isAATemplate
                  && isHTML && isAACommand;

            if (doAADecorations)
            {
               // need to add a page decoration when rendering a snippet
               boolean addPageDecoration = t.getOutputFormat() == OutputFormat.Snippet; 
               handleAADecorations(request, response, result, addPageDecoration);
               return;
            }

            /* Discard if the connection has closed */
            if (response.isCommitted())
               return;
            
            response.setContentType(result.getMimeType());
            if (result.getResultLength() > 0)
            {
               response.setContentLength((int) result.getResultLength());
            }
            response.setStatus(success ? 200 : 500);
            try(OutputStream os = response.getOutputStream()) {
               try (InputStream is = result.getResultStream()) {
                  IOUtils.copy(is, os);
                  os.flush();
               }
            }
         }
      }
      catch (Throwable e)
      {
         Throwable cause = PSExceptionHelper.findRootCause(e, true);
         if (PSServletUtils.isClientAbortException(e) ||
               cause.getMessage().startsWith("Software caused connection abort:") ||
               cause.getMessage().startsWith("Connection reset by peer:")) {
            ms_log.debug("Client aborted connection", e);
            return; //Client closed connection, do not throw error   
         }
         ms_log.error("Problem in assembly servlet", e);
         reportError(request, cause.getLocalizedMessage(), response);
      }
      finally
      {
         watch.stop();
         ms_log.debug("Assembling item "
               + request.getParameter(IPSHtmlParameters.SYS_CONTENTID)
               + " template " + (template != null ? template : variantid)
               + " took " + watch.toString());
      }

   }

   /**
    * Handle active assembly decorations by adding them to the result page
    * 
    * @param request the request object, assumed not <code>null</code>
    * @param response the response object, assumed not <code>null</code>
    * @param result assembly result, assume not <code>null</code>.
    * @param addPageDecoration <code>true</code> if need to add a page 
    * decoration to the rendered content (a snippet or a page without 
    * global template; <code>false</code> otherwise. 
    * 
    * @throws Exception if an error occurs.
    */
   private void handleAADecorations(HttpServletRequest request,
      HttpServletResponse response, IPSAssemblyResult result, 
      boolean addPageDecoration)
      throws Exception
   {
      Charset charset = PSStringUtils.getCharsetFromMimeType(result
            .getMimeType());
      String page = new String(result.getResultData(), charset.name());
      // Break the page into several pieces.
      String abovehead, belowbody, head, end;
      int headbreak = PSStringUtils.indexOfIgnoringCase(page, HEAD_TAG, 0);
      int headsize = HEAD_TAG.length();
      if (headbreak < 0)
      {
         headbreak = PSStringUtils.indexOfIgnoringCase(page, HTML_TAG, 0);
         headsize = HTML_TAG.length();
      }
      if (headbreak < 0)
      {
         reportError(request, "Couldn't break page into sections for AA",
               response);
         return;
      }
      // Find end > for tag
      int headend = page.indexOf('>', headbreak + headsize);
      if (headend < 0)
      {
         reportError(request, "Unterminated head or html tag found", response);
         return;
      }
      int body = PSStringUtils.indexOfIgnoringCase(page, BODY_TAG, headend);
      if (body < 0)
      {
         reportError(request, "Unterminated body tag found", response);
         return;
      }
      int bodyend = page.indexOf('>', body);
      if (bodyend < 0)
      {
         reportError(request, "Unterminated body tag found", response);
         return;
      }
      int endbody = PSStringUtils.indexOfIgnoringCase(page, END_BODY_TAG,
            bodyend);
      if (bodyend < 0)
      {
         reportError(request, "Did not find end body tag", response);
         return;
      }
      endbody = page.indexOf('>', endbody + 1);
      if (endbody < 0)
      {
         reportError(request, "Unterminated end body tag", response);
         return;
      }
      /*
       * Generally the character pointers will now look like this: <html> [head]<head>[headend]
       * ... head stuff </head> [body]<body ... >[bodyend] .... </body> </html>
       */
      abovehead = page.substring(0, headend + 1);
      head = page.substring(headend + 1, bodyend + 1);
      belowbody = page.substring(bodyend + 1, endbody + 1);
      end = page.substring(endbody + 1);

      // Now we're in business. We need to reconstruct the string and then
      // output the whole business in bytes again
      StringBuilder builder = new StringBuilder(page.length() + 200);
      builder.append(abovehead);
      addAAJavaScript(builder, getLocale(), request);
      builder.append(head);
      addAATitlebar(result, addPageDecoration, builder, request);
      builder.append(belowbody);
      if(addPageDecoration)
      {
         // insert the closing div tag for the openning page decoration div tag
         builder.insert(builder.toString().toLowerCase().indexOf(END_BODY_TAG),
               "\n</div>\n");
      }
      builder.insert(builder.toString().toLowerCase().indexOf(END_BODY_TAG),
            PSAAStubUtil.getAaPageFooter());

      builder.append(end);

      // Back to a string
      String output = builder.toString();
      // Bytes
      byte outbytes[] = output.getBytes(charset.name());

      response.setContentType(result.getMimeType());
      response.setContentLength(outbytes.length);
      response.setStatus(200);
      try(OutputStream os = response.getOutputStream()) {
         os.write(outbytes);
         os.flush();
      }
   }

   /**
    * @return the locale of the request.
    */
   private String getLocale()
   {
      String locale = (String) PSRequestInfo
            .getRequestInfo(PSRequestInfo.KEY_LOCALE);
      if (locale == null)
         locale = Locale.getDefault().toString();
      return locale;
   }

   /**
    * Add javascript for the head section of the page
    * 
    * @param builder the builder, assumed never <code>null</code>
    * @param locale the locale, assumed never <code>null</code>
    * @param request the request, assumed never <code>null</code>
    * @throws PSStringTemplateException
    */
   private void addAAJavaScript(StringBuilder builder, String locale,
         HttpServletRequest request) throws PSStringTemplateException
   {
      Locale.getDefault();
      
      Map<String, String> vars = new HashMap<>();
      vars.put("AAMODE", getAAMode(request));
      vars.put("RXROOT", getRoot(request));
      vars.put("ROOT", "..");
      vars.put("LOCALE", locale);
      vars.put("isDebug", request.getParameter("isDebug"));
      vars.put("debugAtAllCosts", request.getParameter("debugAtAllCosts"));
      builder.append(PSAAStubUtil.getAaPageHeader().expand(vars));
   }

   /**
    * Add the titlebar section. This calculates all the needed data, and sets up
    * for expanding the TITLEBAR template.
    * 
    * @param result Assembly result, assumed not <code>null</code>
    * @param addPageDecoration <code>true</code> if need to add a page 
    * decoration to the rendered content (a snippet or a page without 
    * global template; <code>false</code> otherwise. 
    * @param builder the builder, assumed never <code>null</code>
    * @param request request object, assume dnot <code>null</code>
    * @throws PSStringTemplateException
    * @throws JSONException 
    */
   private void addAATitlebar(IPSAssemblyResult result, boolean addPageDecoration,
      StringBuilder builder, HttpServletRequest request)
      throws PSStringTemplateException, PSAssemblyException,
      PSMissingBeanConfigurationException, JSONException
   {
      Map<String, String> vars = new HashMap<>();
      vars.put("RXROOT", getRoot(request));
      vars.put("ROOT", "..");
      vars.put("LOCALE", getLocale());
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      PSLegacyGuid lg = (PSLegacyGuid) result.getId();
      PSComponentSummary summary = cms.loadComponentSummary(lg.getContentId());
      String id = PSAAUtils.getPageObjectId(result);

      builder.append(PSAAStubUtil.getAaPageActionBar().expand(vars));
      boolean isIconAAMode = !getAAMode(request).equals("1");
      if(addPageDecoration)
      {
         builder.append("\n<div class=\"PsAaPage\" psAaLabel=\"" + summary.getName()
            + "\" id='" + id + "'>\n");
         if(isIconAAMode)
         {
            builder.append("<a href='javascript:void(0)' " + "id='img." + id
            + "' onclick='ps.aa.controller.activate(this)'>"
            + "<img align='absmiddle' border='0' "
            + "src='../sys_resources/images/aa/page_0.gif' "
            + "title='Activate page'/></a>");
         }
      }
   }

   /**
    * Calculate root url
    * 
    * @param request the servlet request, assumed never <code>null</code>
    * @return the root url, never <code>null</code> or empty
    * 
    */
   private String getRoot(HttpServletRequest request)
   {
      String url = request.getRequestURL().toString();
      int lastslash = url.indexOf("/assembler");
      return url.substring(0, lastslash);
   }
   
   /**
    * Helper method to return the active assembly mode or the
    * default if noe was set.
    * @param request assumed not <code>null</code>
    * @return active assembly mode, never <code>null</code>.
    */
   private String getAAMode(HttpServletRequest request)
   {
      String defaultAAMode = IPSHtmlParameters.SYS_AAMODE_ICONS;
      Properties props = PSServer.getServerProps();
      if(props != null)
         defaultAAMode = StringUtils.defaultIfEmpty(
            (String)props.get("defaultActiveAssemblyMode"),
               IPSHtmlParameters.SYS_AAMODE_ICONS);
      return 
         StringUtils.defaultIfEmpty(
            request.getParameter(IPSHtmlParameters.SYS_ACTIVE_ASSEMBLY_MODE),
            defaultAAMode   
         );
   }
   

   /**
    * Format an error response for the servlet 
    * @param request the request, assumed never <code>null</code>
    * @param string the error string, assumed never <code>null</code> 
    * @param response the response, assumed never <code>null</code>
    */
   private void reportError(HttpServletRequest request, String string,
         HttpServletResponse response)
   {
      /* Discard if the connection has closed */
      if (response.isCommitted())
      {
         ms_log.error("Could not return error information "
               + "because response is committed: " + string);
         return;
      }

      response.setStatus(500);
      response.setContentType("text/html");
      try
      {
         String id = request.getParameter(IPSHtmlParameters.SYS_CONTENTID);
         if (StringUtils.isBlank(id))
         {
            id = request.getParameter(IPSHtmlParameters.SYS_PATH);
         }
         if (StringUtils.isBlank(id))
         {
            id = "unknown";
         }
         String url = request.getRequestURI();
         url = url.replace("/render", "/debug");
         if (request.getQueryString() != null)
         {
            url = url + "?" + request.getQueryString();
         }
         ms_log.error("Problem assembling item (" + id + "): " + string);
         request.setAttribute("error", string);
         request.setAttribute("id", id);
         request.setAttribute("debugurl", url);
         RequestDispatcher disp = request
               .getRequestDispatcher("/ui/assembly/error.jsp");
         disp.forward(request, response);
      }
      catch (Exception e)
      {
         ms_log.error("Failed to report error: " + string, e);
      }
   }
}
