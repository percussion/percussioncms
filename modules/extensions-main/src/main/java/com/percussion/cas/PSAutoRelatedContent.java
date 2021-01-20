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

import com.percussion.cms.PSCmsException;
import com.percussion.data.macro.PSMacroUtils;
import com.percussion.error.PSException;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSCms;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.StringTokenizer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A Rhythmyx post exit. This makes a query for related content and adds the
 * resulting snippets to the Related Content list of a variant.
 * <p>
 * There are 5 exit parameters:
 * <code>LinkURL</code> - The name of an attribute where the query url will be
 *    found. This must be an attribute of the root element of the caller's
 *    result document XML.
 * 
 * <code>slotNameOverride</code> - This optional parameter allows the caller to
 *    place the results in any slot. The slotname can be specified in the 
 *    mapper of the query, but this parameter allows the same query resource 
 *    to be used in different slots.
 * 
 * <code>publishableTokens</code> - This optional parameter allows a caller to
 *    specify a comma delimited string with all publishable tokens. If not
 *    provided or empty, this defaults to <code>y,i</code>.
 * 
 * <code>revisionCorrectionTokens</code> - This optional parameter allows a 
 *    caller to specify a comma delimited string of publishable tokens 
 *    (such as i,x) for which the revision needs to be replaced with the last 
 *    public revision. If not provided or empty, this defaults to 'i'.
 * 
 * <code>maxLinks</code> - This optional parameter allows a caller to limit
 *    the maximum number of linkUrl elements returned. The value must be a 
 *    parsable integer. If not provided, &lt; 0 or empty, all linkUrl elements
 *    will be returned. 
 * 
 * @version 1.0
 */
public class PSAutoRelatedContent extends PSDefaultExtension implements
   IPSResultDocumentProcessor
{
   /**
    * Required by the interface. This exit never modifies the stylesheet.
    * 
    * @see IPSResultDocumentProcessor#canModifyStyleSheet()
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * Makes an internal request to a query resource in order to automatically
    * populate a slot. The query resource is determined by the URL in the
    * attribute name supplied as the first parameter to the exit. The query
    * resource must return an XML document compliant with
    * /Rhythmyx/DTD/sys_AssemblerInfo.dtd.
    */
   public Document processResultDocument(Object[] params,
         IPSRequestContext request, Document doc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      // first validate the exit parameters
      if (params.length < 1 || params[0] == null
            || params[0].toString().trim().length() == 0)
      {
         throw new PSParameterMismatchException(MSG_MISSING_PARAM);
      }

      // the first parameter will be an attribute name
      String attrName = params[0].toString().trim();
      request.printTraceMessage("Adding Items from @" + attrName);
      String URL = null;

      String slotNameOverride = null;
      if (params.length >= 2 && params[1] != null
            && params[1].toString().length() > 0)
      {
         slotNameOverride = params[1].toString();
         request.printTraceMessage("Slot Name Override is " + slotNameOverride);
      }

      String publishableTokens = "y,i";
      if (params.length >= 3 && params[2] != null
            && params[2].toString().trim().length() > 0)
         publishableTokens = params[2].toString().trim();

      String revisionCorrectionTokens = "i";
      if (params.length >= 4 && params[3] != null
            && params[3].toString().trim().length() > 0)
         revisionCorrectionTokens = params[3].toString().trim();
      revisionCorrectionTokens = revisionCorrectionTokens.replaceAll(" ", "");
      revisionCorrectionTokens = revisionCorrectionTokens.replaceAll(",", "");
      char[] validFlags = revisionCorrectionTokens.toCharArray();
      
      int maxLinks = -1;
      if (params.length >= 5 && params[4] != null && 
         params[4].toString().trim().length() > 0)
      {
         try
         {
            maxLinks = Integer.parseInt(params[4].toString().trim());
         }
         catch (NumberFormatException e)
         {
            throw new PSParameterMismatchException("An invalid 'maxLinks' " +
               "parameter value was supplied. Must be a parsable integer.");
         }
      }

      try
      {
         Element rootElement = doc.getDocumentElement();
         if (rootElement != null)
         {
            URL = rootElement.getAttribute(attrName);
            request.printTraceMessage("URL is: " + URL);

            if (URL == null || URL.length() == 0)
            {
               throw new PSExtensionProcessingException(0, MSG_MISSING_PARAM);
            }

            /*
             * Make sure the destination <code> sys_AssemblerInfo/RelatedContent
             * </code> element exists.
             */
            Element relatedContent = null;
            PSXmlTreeWalker walker = new PSXmlTreeWalker(rootElement);
            Element assemblerInfo = walker.getNextElement(ASSEMBLER_INFO_ELEM,
               true);
            if (assemblerInfo != null)
               relatedContent = walker.getNextElement(RELATED_CONTENT_ELEM,
                  true);

            if (relatedContent == null)
               throw new PSExtensionProcessingException(0, MSG_NO_RELATED);

            /*
             * Make an internal request to fetch all <code>
             * RelatedContent/linkurl </code> elements.
             */
            IPSInternalRequest relatedReq = request.getInternalRequest(URL, 
               null, true);
            if (relatedReq == null)
               throw new PSExtensionProcessingException(0, MSG_INVALID_REQUEST
                  + URL);
            Document results = relatedReq.getResultDoc();
            if (results == null)
            {
               request.printTraceMessage("No results returned");
               // this can be ok
               return doc;
            }
            Element resultRoot = results.getDocumentElement();
            PSXmlTreeWalker resultWalker = new PSXmlTreeWalker(resultRoot);
            request.printTraceMessage("Query result \n"
               + PSXmlDocumentBuilder.toString(results));

            Element resultLink = resultWalker.getNextElement(RELATEDLINKURL,
               resultWalker.GET_NEXT_ALLOW_CHILDREN);
            String authtype = request.getParameter(
               IPSHtmlParameters.SYS_AUTHTYPE, "0").trim();

            int resultCount = 0;
            while (resultLink != null
               && (maxLinks == -1 || resultCount < maxLinks))
            {
               if (!authtype.equals("0"))
               {
                  if (!isContentValid(resultLink, request, publishableTokens))
                  {
                     //Item is not publishable skip it and continue with next
                     // link.
                     request
                        .printTraceMessage("Skipping non-publishable url \n"
                           + PSXmlDocumentBuilder.toString(resultLink));
                     resultLink = resultWalker.getNextElement(RELATEDLINKURL,
                        resultWalker.GET_NEXT_ALLOW_SIBLINGS);
                     continue;
                  }

                  boolean correctRevision = true;
                  String contentValid = resultLink.getAttribute(
                     ATTR_CONTENTVALID).trim();
                  if (contentValid.length() > 0)
                  {
                     //If contentValid attribute exists check whether the item
                     // needs revision correction or not
                     StringTokenizer revtokens = new StringTokenizer(
                        revisionCorrectionTokens, ",");
                     if (!isFlagInToken(contentValid, revtokens))
                        correctRevision = false;
                  }
                  if (correctRevision)
                  {
                     NodeList valNodes = resultLink
                        .getElementsByTagName("Value");
                     if (valNodes != null && valNodes.getLength() > 0)
                     {
                        Element valElem = (Element) valNodes.item(0);
                        String current = valElem.getAttribute("current").trim();
                        if (current.length() > 0)
                        {
                           String correctedUrl = PSMacroUtils
                              .fixLinkUrlRevisionForFlags(request, current,
                                 validFlags);
                           if (correctedUrl == null)
                           {
                              request.printTraceMessage(
                                 "Failed to get the last public " + 
                                 "revision and skipping the linkurl element. " + 
                                 PSXmlDocumentBuilder.toString(resultLink));
                              resultLink = resultWalker.getNextElement(
                                 RELATEDLINKURL,
                                 resultWalker.GET_NEXT_ALLOW_SIBLINGS);
                              continue;
                           }
                           valElem.setAttribute("current", correctedUrl);
                        }
                        else
                        {
                           request.printTraceMessage(
                              "Empty current attribute on linkurl " + 
                              "skipping the element. " + 
                              PSXmlDocumentBuilder.toString(resultLink));
                           resultLink = resultWalker.getNextElement(
                              RELATEDLINKURL,
                              resultWalker.GET_NEXT_ALLOW_SIBLINGS);
                           continue;
                        }
                     }
                  }
               }
               request.printTraceMessage("Copying link url \n"
                  + PSXmlDocumentBuilder.toString(resultLink));
               if (slotNameOverride != null)
                  resultLink.setAttribute("slotname", slotNameOverride);
               relatedContent.appendChild(doc.importNode(resultLink, true));

               resultCount++;
               resultLink = resultWalker.getNextElement(RELATEDLINKURL,
                  resultWalker.GET_NEXT_ALLOW_SIBLINGS);
            }
         }
      }
      catch (PSExtensionProcessingException e)
      {
         throw (PSExtensionProcessingException) e.fillInStackTrace();
      }
      catch (PSException e)
      {
         throw new PSExtensionProcessingException(e.getErrorCode(), e
               .getErrorArguments());
      }
      catch (Exception e)
      {
         request.printTraceMessage("Unexpected Exception "
               + e.getLocalizedMessage() + "\n"
               + PSException.getStackTraceAsString(e));

         throw new PSExtensionProcessingException(getClass().getName(), e);
      }

      return doc;
   }

   /**
    * Utility method to check whether the given flag is in supplied tokens.
    * Comparision will be case insensitive.
    * 
    * @param flag must not be <code>null</code>.
    * @param tokens must not be <code>null</code>.
    * @return <code>true</code> if flag exists in token otherwise
    *         <code>false</code>.
    */
   private boolean isFlagInToken(String flag, StringTokenizer tokens)
   {
      if (flag == null)
         throw new IllegalArgumentException("flag cannot be null");
      if (tokens == null)
         throw new IllegalArgumentException("tokens cannot be null");

      boolean found = false;

      while (tokens.hasMoreTokens())
      {
         String token = tokens.nextToken().trim();
         if (flag.equalsIgnoreCase(token))
         {
            found = true;
            break;
         }
      }
      return found;
   }

   /**
    * Tests if the related item is publishable or not. First check If
    * contentValid exists as an attribute of linkurl element then, checks
    * whether that value exists in publishable tokens or not. If yes returns
    * <code>true</code> otherwise <code>false</code>. If contentValid does
    * not exist, then calls {@link PSCms.isRelatedItemPublishable(
    * Element, IPSRequestContext, String)} to get the publishable status of the 
    * item.
    * 
    * @param resultLink the related linkurl element, must not be
    *           <code>null</code>. The expected DTD is: &lt;!ELEMENT linkurl
    *           (Value)&gt; &lt;!ELEMENT Value EMPTY&gt; &lt;!ATTLIST Value
    *           current CDATA #REQUIRED &gt;
    * @param request the request to operate on, assumed not <code>null</code>.
    * @param publishableTokens a string with all tokens that are publishable,
    *           defaults to <code>y,i</code> if <code>null</code> or empty.
    *           The token delimiter is the comma.
    * @return <code>true</code> if the related item is publishable,
    *         <code>false</code> otherwise.
    * @throws PSCmsException if anything goes wrong processing the request.
    */
   private boolean isContentValid(Element resultLink,
         IPSRequestContext request, String publishableTokens)
         throws PSCmsException
   {

      String contentValid = resultLink.getAttribute(ATTR_CONTENTVALID).trim();
      if (contentValid.length() > 0)
      {
         StringTokenizer pubtokens = new StringTokenizer(publishableTokens, ",");
         return isFlagInToken(contentValid, pubtokens);
      }
      else
      {
         return PSCms.isRelatedItemPublishable(resultLink, request,
               publishableTokens);
      }
   }

   /**
    * Name of the XML node that contains assembler information
    */
   private static final String ASSEMBLER_INFO_ELEM = "sys_AssemblerInfo";

   /**
    * Name of the XML node that identifies related content items
    */
   private static final String RELATED_CONTENT_ELEM = "RelatedContent";

   /**
    * Name of the XML node that contains the link URL of a related content item.
    * One such node will be present in the RELATED_CONTENT_ELEM node for each
    * related content ite
    */
   private static final String RELATEDLINKURL = "linkurl";

   /**
    * Error message used when parameters are missing
    */
   private static final String MSG_MISSING_PARAM = "The URL parameter must be supplied";

   /**
    * Error message used when no related content nodes are found in the XML
    */
   private static final String MSG_NO_RELATED = "Unable to locate related content node. "
         + "Check sys_casAddAssemblerInfo.";

   /**
    * Error message used if the first (0'th) param doesn't provide a valid URL
    * for an internal request
    */
   private static final String MSG_INVALID_REQUEST = "The URL Parameter points the following invalid request: ";

   /**
    * Name of the attribute that holds the publishable flag of the item.
    */
   private static final String ATTR_CONTENTVALID = "contentValid";

}
