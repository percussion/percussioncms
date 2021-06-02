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
package com.percussion.fastforward.utils;

import com.percussion.cms.IPSCmsErrors;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.IPSServerErrors;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * A collection of useful utility methods for Rhythmyx exits.
 */
public class PSUtils
{

   private static final Logger log = LogManager.getLogger(PSUtils.class);

   /**
    * Tokenizes a string of comma-separated values into a Set.
    * 
    * @param sourceCSV must not be <code>null</code>.
    * @return set of strings parsed of of the source string of comma separated
    *         values. Never <code>null</code>, may be empty.
    */
   public static Set tokenizeCommaSeparatedValues(String sourceCSV)
   {
      if (sourceCSV == null)
         throw new IllegalArgumentException("sourceCSV may not be null");

      Set values = new HashSet();
      StringTokenizer tok = new StringTokenizer(sourceCSV, ",");
      while (tok.hasMoreTokens())
      {
         String value = tok.nextToken().trim();
         values.add(value);
      }
      return values;
   }

   /**
    * Safely gets the specified index of the parameter array as a String. The
    * default value will be returned if parameter array is null, or does not
    * contain a non-empty string at the specified index.
    * 
    * @param params array of parameter objects from the calling function. if
    *           <code>null</code> or the value requested for the index is
    *           <code>null</code> or empty, the default value is returned.
    * @param index specifies which parameter from the array will be returned
    * @param defaultValue returned if array does not have a non-empty string at
    *           the specified index.
    * 
    * @return the parameter at the specified index (converted to a String and
    *         trimmed), or the defaultValue.
    */
   public static String getParameter(Object[] params, int index,
         String defaultValue)
   {
      if (params == null || params.length < index + 1 || params[index] == null
            || params[index].toString().trim().length() == 0)
      {
         return defaultValue;
      }
      else
      {
         return params[index].toString().trim();
      }
   }

   /**
    * Convience method that calls
    * {@link PSUtils#getParameter(Object[], int, String)}with <code>null</code>
    * as the default value.
    * 
    * @param params
    * @param index
    * @return
    */
   public static String getParameter(Object[] params, int index)
   {
      return getParameter(params, index, null);
   }

   /**
    * Safely gets the specified index of the parameter array. The default value
    * will be returned if parameter array is null, or contains a
    * <code>null</code> value at the specified index.
    * 
    * @param params array of parameter objects from the calling function.
    * @param index specifies which parameter from the array will be returned
    * @param defaultValue returned if array does not have a non-null value at
    *           the specified index.
    * @return the parameter at the specified index, or the defaultValue.
    */
   public static Object getParameterObject(Object[] params, int index,
         Object defaultValue)
   {
      if (params == null || params.length < index + 1 || params[index] == null)
      {
         return defaultValue;
      }
      else
      {
         return params[index];
      }
   }

   /**
    * Convience method that calls
    * {@link PSUtils#getParameterObject(Object[], int, Object)}with
    * <code>null</code> as the default value.
    * 
    * @param params
    * @param index
    * @return
    */
   public static Object getParameterObject(Object[] params, int index)
   {
      return getParameterObject(params, index, null);
   }

   /**
    * Logs debug messages to the Rhythmyx application trace log and possibily to
    * standard output.
    * 
    * @param request the current request object, used for accessing the Rhythmyx
    *           application trace log, not <code>null</code>.
    * @param msg the message to be logged, not <code>null</code>.
    */
   public static void printTraceMessage(IPSRequestContext request, String msg)
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");
      if (msg == null)
         throw new IllegalArgumentException("message may not be null");

      request.printTraceMessage(msg);
      if (false) // switch to true when debugging for easier access to the msgs
         System.out.println(msg);
   }

   /**
    * Returns true if this content item valid for incremental publishing.
    * Content items which have already been published will have a row in the
    * <code>RXSITEITEMS</code> table. These items are <b>INVALID </b> for
    * publishing in an incremental edition.
    *
    * @param request
    * @param requestName
    * @param contentid
    * @param variantid
    * @param context
    * @param siteid
    * @param pubOperation
    * @param pubDate
    * @param location
    * @return
    */
   public static boolean isValid(IPSRequestContext request, String requestName,
                                 String contentid, String variantid, String context, String siteid,
                                 String pubOperation, String pubDate, String location)
   {
      if (request == null)
         throw new IllegalArgumentException(
                 "isValid(): request may not be null");
      if (requestName == null)
         throw new IllegalArgumentException(
                 "isValid(): requestName may not be null");

      request.printTraceMessage("testing content id: " + contentid);

      Map internalRequestParams = new HashMap(7);
      internalRequestParams.put(IPSHtmlParameters.SYS_CONTENTID, contentid);
      internalRequestParams.put(IPSHtmlParameters.SYS_VARIANTID, variantid);
      internalRequestParams.put(IPSHtmlParameters.SYS_CONTEXT, context);
      internalRequestParams.put(IPSHtmlParameters.SYS_SITEID, siteid);
      internalRequestParams.put("puboperation", pubOperation);
      internalRequestParams.put("pubdate", pubDate);
      internalRequestParams.put("location", location);

      IPSInternalRequest ir = request.getInternalRequest(requestName,
              internalRequestParams, false);
      if (ir != null)
      {
         try
         {
            Document iresult = ir.getResultDoc();

            /*
             * a null result, or empty document, means the query failed to find
             * a row in RXSITEITEMS, so the item has not been published and
             * should be included in an incremental content list
             */
            if (iresult == null)
            {
               return true;
            }
            else
            {
               Element irRoot = iresult.getDocumentElement();
               return irRoot == null;
            }
         }
         catch (PSInternalRequestCallException e)
         {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
            request.printTraceMessage("error making internal request to "
                    + requestName);
         }
      }
      else
      {
         request.printTraceMessage("ERROR: internal request " + requestName
                 + " not found");
      }

      /*
       * if the filter query cannot be found, or generates an error, default to
       * including the item in incremental content lists
       */
      return true;
   }

   /**
    * Is this content item valid for publishing. Content items which have
    * already been published will have a row in the <code>RXSITEITEMS</code>
    * table. These items are <b>INVALID </b> for publishing in an incremental
    * edition.
    *
    * @param elem the <code>contentitem</code> to check
    * @param req the request context from the caller
    * @param reqName the name of the internal request. Must be in
    *           <code>&lt;AppName&gt;/&lt;ReqName&gt;</code> format.
    * @param context the sys_context of this edition
    * @return <code>true</code> if no matching row is found.
    * @throws PSInternalRequestCallException when the internal request fails
    */
   public static boolean isValid(Element elem, IPSRequestContext req, String reqName,
                                 String context) throws PSInternalRequestCallException
   {
      PSXmlTreeWalker wlk = new PSXmlTreeWalker(elem);

      String contentid = elem.getAttribute(XML_ATTR_CONTENTID);
      req.printTraceMessage("Content id: " + contentid);

      String variantid = elem.getAttribute(XML_ATTR_VARIANTID);
      String siteid = req.getParameter(IPSHtmlParameters.SYS_SITEID);
      String pubOpAttr = elem.getAttribute(XML_ATTR_UNPUBLISH);
      String pubOperation = OPERATION_PUBLISH;
      if (pubOpAttr != null && pubOpAttr.equalsIgnoreCase("yes"))
         pubOperation = OPERATION_UNPUBLISH;

      String pubDate = wlk.getElementData(XML_ELEM_MODIFY_DATE, true);
      String location = wlk.getElementData(XML_ELEM_LOCATION, false);

      return isValid(req, reqName, contentid, variantid, context, siteid,
              pubOperation, pubDate, location);
   }

   /**
    * Get the site base URL from the system (as registered) for the given
    * siteid.
    *
    * @param siteId SiteId for which the base URL is sought, must not be
    *           <code>null</code> or empty.
    * @param request Request context object used to make an internal request for
    *           site lookup, must not be <code>null</code>.
    * @return Site base URL string as registered in the system, never
    *         <code>null</code>, may be empty.
    * @throws PSNotFoundException if the required application resource is
    *            missing or not running.
    * @throws PSInternalRequestCallException if there is any error while
    *            executing internal request.
    */
   static public String getbaseUrl(String siteId, IPSRequestContext request)
           throws PSNotFoundException, PSInternalRequestCallException
   {
      if (siteId == null || siteId.length() < 1)
      {
         throw new IllegalArgumentException("siteId must not be null or empty");
      }
      if (request == null)
      {
         throw new IllegalArgumentException("request must not be null");
      }
      Map<String, String> params = new HashMap<String, String>();
      params.put(IPSHtmlParameters.SYS_SITEID, siteId);
      IPSInternalRequest ir = request.getInternalRequest(SITE_LOOKUP_RESOURCE,
              params, false);
      if (ir == null)
      {
         Object[] args =
                 {SITE_LOOKUP_RESOURCE, "No request handler found."};
         throw new PSNotFoundException(
                 IPSServerErrors.MISSING_INTERNAL_REQUEST_RESOURCE, args);
      }
      Document doc = ir.getResultDoc();

      NodeList nl = doc.getElementsByTagName("Site");
      if (nl.getLength() < 1)
      {
         Object[] args =
                 {SITE_LOOKUP_RESOURCE, siteId};
         throw new PSNotFoundException(IPSCmsErrors.SITE_LOOKUP_FAILED, args);
      }
      Element site = (Element) nl.item(0);

      return site.getAttribute("BaseUrl");
   }

   /**
    * The XML attribute for content id
    */
   private static final String XML_ATTR_CONTENTID = "contentid";

   /**
    * The XML attribute for variant id
    */
   private static final String XML_ATTR_VARIANTID = "variantid";

   /**
    * The XML attribute for unpublish
    */
   private static final String XML_ATTR_UNPUBLISH = "unpublish";

   /**
    * The XML element for modify date
    */
   private static final String XML_ELEM_MODIFY_DATE = "modifydate";

   /**
    * The XML element for modify date
    */
   private static final String XML_ELEM_LOCATION = "location";

   /**
    * The publish operation keyword
    */
   private static final String OPERATION_PUBLISH = "publish";

   /**
    * The unpublish operation keyword
    */
   private static final String OPERATION_UNPUBLISH = "unpublish";

   /**
    * Rhythmyx resource name to fetch the site info given the sited id.
    */
   private static final String SITE_LOOKUP_RESOURCE =
           "sys_casSupport/SiteLookup";


}