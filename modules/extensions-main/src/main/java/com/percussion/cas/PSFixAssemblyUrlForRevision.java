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
package com.percussion.cas;

import com.percussion.data.PSInternalRequestCallException;
import com.percussion.data.macro.PSMacroUtils;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestParsingException;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSParseUrlQueryString;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * This exit is intended to corrrect the related link URLs in assembly XML
 * document for last public revision and typically goes onto a authtype
 * implementation resource, for example, sys_casSupport/casSupport_1 or any
 * custom authtype resource that needs corrections for tha last public revision.
 * It takes two optional parameters:
 * <p>
 * <ol>
 * <li>Name of the element in the result XML document whose value represents of
 * the link URL to be corrected for last public revision. If the name specifed
 * starts with "@", then the link URL is taken from any element that has an
 * attribute with that name. Default is {@link #RELATEDLINKURL linkurl}</li>
 * <li>A flag ("yes" or "no" or "true" or "false") to instruct that the link
 * element has to be removed from the document if the related item never went to
 * public.</li>
 * <li>Content Valid flag string which is a comma separated list of content
 * valid flags, (e.g. i,x) for revision correction. If the current state of the
 * item has a content valid flag matching one of these then the revision is
 * corrected otherwise the URL is not modifed.</li>
 * </ol>
 * <p>
 * No exception is thrown in case of any processing error, instead the message
 * is logged to server log as well as application trace.
 */
public class PSFixAssemblyUrlForRevision extends PSDefaultExtension
      implements
         IPSResultDocumentProcessor
{
   /**
    * Implementation of the interface method, always return <code>false</code>.
    * 
    * @see com.percussion.extension.IPSResultDocumentProcessor#canModifyStyleSheet()
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * Implementation of the interface method. See class description and
    * {@link #fixRelatedContentUrl(IPSRequestContext, String, boolean)} for more
    * details.
    * 
    * @see com.percussion.extension.IPSResultDocumentProcessor#
    *      processResultDocument(java.lang.Object[],
    *      com.percussion.server.IPSRequestContext, org.w3c.dom.Document)
    */
   public Document processResultDocument(Object[] params,
         IPSRequestContext request, Document resultDoc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      if (resultDoc == null)
         return resultDoc;
      
      //Default link element name
      String elemOrAttr = RELATEDLINKURL;
      if (params.length > 0 && params[0] != null
            && params[0].toString().trim().length() > 0)
      {
         //Override it with extension parameter if non null and non-empty
         String param = params[0].toString().trim();
         elemOrAttr = param;
      }
      //Default is true
      boolean nullIfNeverPublic = true;
      if(params.length>1 && params[1] != null)
      {
         //Override it with extension parameter if non null and non-empty
         String param = params[1].toString().toLowerCase();
         if(param.startsWith("n") || param.startsWith("f"))
            nullIfNeverPublic = false;
      }
      //Default content valid flag for revision correction is "i".
      String contentValidString = "i";
      if (params.length > 2 && params[2] != null
            && params[2].toString().trim().length() > 0)
      {
         //Override it with extension parameter if non null and non-empty
         contentValidString = params[2].toString().trim().toLowerCase();
      }
      
      boolean attr = false;
      if(elemOrAttr.startsWith("@"))
      {
         attr = true;
         elemOrAttr = elemOrAttr.substring(1);
      }
      try
      {
         if (attr)
         {
            NodeList nl = resultDoc.getElementsByTagName("*");
            for (int i = nl.getLength() - 1; i >= 0; i--)
            {
               Element elem = (Element) nl.item(i);
               String url = elem.getAttribute(elemOrAttr).trim();
               if (url.length() < 1)
                  continue;
               url = fixRelatedContentUrl(request, url, nullIfNeverPublic, contentValidString);
               if (url == null)
               {
                  String msg = "Related link element is being "
                        + "removed since the fixed URL is null.";
                  ms_log.debug(msg);
                  request.printTraceMessage(msg);
                  elem.getParentNode().removeChild(elem);
                  continue;
               }
               elem.setAttribute(elemOrAttr, url);
            }
         }
         else
         {
            NodeList nl = resultDoc.getElementsByTagName(elemOrAttr);
            for (int i = nl.getLength() - 1; i >= 0; i--)
            {
               Element elem = (Element) nl.item(i);
               Node text = elem.getFirstChild();
               if(text==null || text.getNodeType()!=Node.TEXT_NODE)
               {
                  String msg = "Empty related link element " + elemOrAttr
                        + " ignored.";
                  ms_log.debug(msg);
                  request.printTraceMessage(msg);
                  continue;
               }
               String url = ((Text)text).getData();
               if (url == null || url.length() < 1)
               {
                  String msg = "Empty value for the related link element "
                        + elemOrAttr + " ignored.";
                  ms_log.debug(msg);
                  request.printTraceMessage(msg);
                  continue;
               }
               url = fixRelatedContentUrl(request, url, nullIfNeverPublic, contentValidString);
               if (url == null)
               {
                  String msg = "Related link element is being "
                        + "removed since the fixed URL is null.";
                  ms_log.debug(msg);
                  request.printTraceMessage(msg);
                  elem.getParentNode().removeChild(elem);
                  continue;
               }
               ((Text)text).setData(url);
            }
         }
      }
      catch (PSInternalRequestCallException e)
      {
         ms_log.debug(e.getMessage());
         request.printTraceMessage(e.getMessage());
      }
      catch (PSNotFoundException e)
      {
         ms_log.debug(e.getMessage());
         request.printTraceMessage(e.getMessage());
      }
      catch (PSRequestParsingException e)
      {
         ms_log.debug(e.getMessage());
         request.printTraceMessage(e.getMessage());
      }
      return resultDoc;
   }

   /**
    * Fix the revision parameter in the supplied related content URL for public
    * revision. The contentid of the related item is parsed from the supplied
    * URL. The following scheme is used to corerct teh URL:
    * <p>
    * <ul>
    * <li>If the URL does not have a
    * {@link IPSHtmlParameters#SYS_CONTENTID contentid}parameter, then the URL
    * is returned as it is.</li>
    * <li>Existence of revision {@link IPSHtmlParameters#SYS_REVISION revision}
    * parameter in the URL is checked and if it does not have revision parameter
    * at all then the the URL is returned unmodified.</li>
    * <li>If the item was ever gone public then the revision parameter in the
    * URL will be replaced with tha last public revision.</li>
    * <li>If the item was never gone public and the the nullIfNeverPublic
    * option is (third argument to this method) <code>true</code>, then the
    * return value will be <code>null</code></li>
    * <li>If the item was never gone public and the the nullIfNeverPublic
    * option is (third argument to this method) <code>false</code>, then URL
    * will be returned unmodified.</li>
    * </ul>
    * 
    * @param request the request used to make internal lookups, must not be
    *           <code>null</code>.
    * @param relatedContentUrl the related content url string that will be
    *           fixed, must not be <code>null</code>.
    * @param nullIfNeverPublic flag to indicate to return <code>null</code> if
    *           the item was never gone public.
    * @param contentValidString string which is a comma separated list of
    *           content valid flags, (e.g. i,x) for revision correction. Must
    *           not be <code>null</code> or empty.
    * @return the fixed related content url, <code>null</code> if this url
    *         should be skipped.
    * @throws PSInternalRequestCallException for errors caused doing internal
    *            lookups.
    * @throws PSNotFoundException if a required resource is not found while
    *            making lookup requests.
    * @throws PSRequestParsingException
    */
   public static String fixRelatedContentUrl(IPSRequestContext request,
         String relatedContentUrl, boolean nullIfNeverPublic,
         String contentValidString) throws PSInternalRequestCallException,
         PSNotFoundException, PSRequestParsingException
   {
      if (request == null)
      {
         throw new IllegalArgumentException("request must not be null");
      }
      if (relatedContentUrl == null || relatedContentUrl.length() < 1)
      {
         throw new IllegalArgumentException(
               "relatedContentUrl must not be null or empty");
      }
      if (contentValidString == null || contentValidString.length() < 1)
      {
         throw new IllegalArgumentException(
               "contentValidString must not be null or empty");
      }
      Map params = PSParseUrlQueryString.parseParameters(relatedContentUrl);
      String contentid = (String) params.get(IPSHtmlParameters.SYS_CONTENTID);
      if (contentid == null || contentid.length() < 1)
      {
         String msg = "Parameter " + IPSHtmlParameters.SYS_CONTENTID
               + " in the related link URL is does not exist or empty. "
               + "Revision not corrected";
         ms_log.debug(msg);
         request.printTraceMessage(msg);
         return relatedContentUrl;
      }
      String revision = (String) params.get(IPSHtmlParameters.SYS_REVISION);
      if (revision == null)
      {
         String msg = "Parameter " + IPSHtmlParameters.SYS_REVISION
               + " in the related link URL is does not exist. "
               + "Url not modified";
         ms_log.debug(msg);
         request.printTraceMessage(msg);
         return relatedContentUrl;
      }
      //Chop all commas and empty spaces
      contentValidString = contentValidString.replaceAll(",", "");
      contentValidString = contentValidString.replaceAll(" ", "");
      String lastPublicRevision = PSMacroUtils.correctRevisionForFlags(request,
            contentid, contentValidString.toCharArray(), revision);

      boolean neverPublic = lastPublicRevision.equals("-1");
      if (neverPublic)
      {
         String msg = "Item with contentid = " + contentid
         + " was never public.";
         ms_log.debug(msg);
         request.printTraceMessage(msg);
         if (nullIfNeverPublic)
         {
            msg = "Returning null since nullIfNeverPublic "
                  + "option is specified as true";
            ms_log.debug(msg);
            request.printTraceMessage(msg);
            return null;
         }
         else
         {
            msg = "Returning unmodified URL since nullIfNeverPublic "
                  + "option is specified as false";
            ms_log.debug(msg);
            request.printTraceMessage(msg);
            return relatedContentUrl;
         }
      }
      int start = relatedContentUrl.indexOf(IPSHtmlParameters.SYS_REVISION);
      String fixedUrl = relatedContentUrl.substring(0, start);
      fixedUrl += IPSHtmlParameters.SYS_REVISION + "=" + lastPublicRevision;

      int end = relatedContentUrl.indexOf("&", start);
      if (end != -1)
         fixedUrl += relatedContentUrl.substring(end);

      return fixedUrl;
   }

   /**
    * The element name returned from the rx_casSupport application for
    * related link URL's.
    */
   private static final String RELATEDLINKURL = "linkurl";

   /**
    * Reference to Log4j singleton object used to log any errors or debug info.
    */
   private static final Logger ms_log = LogManager
         .getLogger(PSFixAssemblyUrlForRevision.class);
}
