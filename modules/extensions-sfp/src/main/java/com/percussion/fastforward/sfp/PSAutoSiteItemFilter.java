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
package com.percussion.fastforward.sfp;

import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.server.IPSRequestContext;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.xml.PSXmlTreeWalker;
import com.percussion.cms.objectstore.PSProcessorProxy;
import com.percussion.cms.PSCmsException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSDbComponent;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.server.IPSInternalRequest;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This exit filters out the items in the auto index that are not a part of the
 * site identified by the HTMLParameter
 * {@link com.percussion.util.IPSHtmlParameters#SYS_SITEID siteid}passed
 */
public class PSAutoSiteItemFilter extends PSDefaultExtension
      implements
         IPSResultDocumentProcessor
{

   /**
    * Implements the interface method. Modifies the result document to filter
    * out items that are not in the site with specified siteid. The request
    * context must have the HTML parameter
    * {@link IPSHtmlParameters#SYS_SITEID siteid}specified.
    * 
    * @param params Array must have one non <code>null</code> and non-empty
    *           parameter specifying the site folder lookup url.
    * @param request request context object must not be <code>null</code>.
    * @param resultDoc if <code>null</code> returned as it is.
    * @return Document result document filtered for the items that are not in
    *         the site specified, <code>null</code> only if the original
    *         document is <code>null</code>.
    * @throws PSParameterMismatchException
    * @throws PSExtensionProcessingException
    */
   public Document processResultDocument(Object[] params,
         IPSRequestContext request, Document resultDoc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      if (resultDoc == null)
         return resultDoc;

      if (params.length < MIN_REQD_PARAM_COUNT)
         throw new PSParameterMismatchException(0, "Invalid # of Parameters."
               + " Expected : " + MIN_REQD_PARAM_COUNT + " Passed : "
               + params.length);
      if (params[0] == null || params[0].toString().equals(""))
         throw new PSParameterMismatchException(0,
               "SiteFolderLookupURL cannot be null or empty");

      String siteFolderLookupURL = params[0].toString();
      String currSiteid = request.getParameter(IPSHtmlParameters.SYS_SITEID, "");
      request.printTraceMessage("Current Site Id = " + currSiteid);
      if (currSiteid.length() < 1)
      {
         request.printTraceMessage(
                     "Site Id is null or empty. No site item filtering is done");
         return resultDoc;
      }
      boolean isEmpty = true;
      try
      {
         //First get the folders for the current site...
         HashMap paramMap = new HashMap();
         paramMap.put(IPSHtmlParameters.SYS_SITEID, currSiteid);
         IPSInternalRequest ireq = request.getInternalRequest(
               siteFolderLookupURL, paramMap, false);
         Document sdoc = ireq.getResultDoc();
         Element rootEl = sdoc.getDocumentElement();
         PSXmlTreeWalker walker = new PSXmlTreeWalker(rootEl);
         String parentFolderPath = walker.getElementData("folderPath").trim();
         request
               .printTraceMessage("Parent's Folder Path = " + parentFolderPath);
         //start walking the tree
         Element assemblerInfoNode = resultDoc.getDocumentElement();
         Element relatedInfoNode = null;
         if (assemblerInfoNode != null && assemblerInfoNode.hasChildNodes())
         {
            relatedInfoNode = (Element) assemblerInfoNode.getFirstChild();
         }
         else
         {
            return resultDoc;
         }

         if (relatedInfoNode == null)
         {
            request
                  .printTraceMessage("root node is null, returning empty list");
            return resultDoc;
         }
         PSXmlTreeWalker resultWalker = new PSXmlTreeWalker(relatedInfoNode);
         Element linknode = resultWalker.getNextElement("linkurl",
               PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
         while (linknode != null)
         {
            if (isValid(linknode, request, parentFolderPath))
            {
               isEmpty = false;
               request.printTraceMessage("Keeping Item :: "
                     + linknode.getAttribute("relateditemid"));
               linknode = resultWalker.getNextElement("linkurl",
                     PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);

            }
            else
            {
               Element nextRes = resultWalker.getNextElement("linkurl",
                     PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
               request.printTraceMessage("Removing Item:: "
                     + PSXmlDocumentBuilder.toString(linknode));
               relatedInfoNode.removeChild((Node) linknode);
               request.printTraceMessage("Removed the item");
               linknode = nextRes;
            }
         }
         if (isEmpty)
         {
            request.printTraceMessage("returning empty list");
            PSXmlDocumentBuilder.addEmptyElement(resultDoc, relatedInfoNode,
                  "linkurl");
            return resultDoc;
         }
         else
         {
            
            StringWriter sw = new StringWriter();
            PSXmlDocumentBuilder.write(resultDoc, (Writer) sw);
            request.printTraceMessage("returning XML doc: \n" + sw.toString());
            return resultDoc;
         }
      }
      catch (PSCmsException ex)
      {
         request.printTraceMessage("PSCmsException Occurred!!");
         request.printTraceMessage(PSCmsException.getStackTraceAsString(ex));
      }
      catch (PSInternalRequestCallException irce)
      {
         request.printTraceMessage("PSInternalRequestCallException Occurred!!");
         request.printTraceMessage(PSInternalRequestCallException
               .getStackTraceAsString(irce));
      }
      catch (IOException ioe)
      {
         request.printTraceMessage("IOException Occurred!!");
         request.printTraceMessage(PSCmsException.getStackTraceAsString(ioe));

      }

      return resultDoc;
   }

   /**
    * Checks if the specified item (as DOM element) is valid. An item is
    * considered to be valid if it lies under site folder tree path supplied.
    * 
    * @param currElement DOM element representing one item, must not be
    *           <code>null</code>.
    * @param request request context object, must not be <code>null</code>.
    * @param parentFolderPath paranet folder path, must not be <code>null</code>
    * @return <code>true</code> if test based on the above description
    *         succeeds, <code>false</code> otherwise.
    * @throws PSCmsException
    */
   private boolean isValid(Element currElement, IPSRequestContext request,
         String parentFolderPath) throws PSCmsException
   {
      String strcontentid = currElement.getAttribute("relateditemid");
      request.printTraceMessage("Current Item Contentid = " + strcontentid);
      int contentid = Integer.parseInt(strcontentid);
      int currentrevision = computeRevision(currElement, request);
      PSLocator item = new PSLocator(contentid, currentrevision);
      PSRelationshipProcessor relProxy = PSRelationshipProcessor.getInstance();
      String[] folderpath = relProxy.getRelationshipOwnerPaths(PSDbComponent
            .getComponentType(PSFolder.class), item,
            PSRelationshipConfig.TYPE_FOLDER_CONTENT);
      if ((parentFolderPath.lastIndexOf('/') + 1) != parentFolderPath.length()) 
      {
          parentFolderPath = parentFolderPath + "/";
      }
      if (folderpath != null)
      {
         for (int i = 0; i < folderpath.length; i++)
         {
            String currPath = folderpath[i].trim();
            request.printTraceMessage("in isValid method, currPath = "
                  + currPath);
            request.printTraceMessage("in isValid method, parentFolderPath = "
                  + parentFolderPath);
            if (currPath.indexOf(parentFolderPath) >= 0)
            {
               request.printTraceMessage(currPath
                     + " contains parent folder path" + parentFolderPath);
               return true;
            }
         }
      }
      request
            .printTraceMessage(" None of the item paths have parent folder path");
      request.printTraceMessage("Remove the item " + item.getId());
      return false;
   }

   /**
    * Parses the link url in the linkurl DOM element to read the revision and
    * returns.
    * 
    * @param linkurl DOM eleemnt foe the linkurl, must not be <code>null</code>.
    * @param request request content object, must not be <code>null</code>.
    * @return revision parsed from the link url, -1 if the parameter
    *         {@link IPSHtmlParameters#SYS_REVISION revision}in the URL is
    *         missing.
    */
   private int computeRevision(Element linkurl, IPSRequestContext request)
   {
      PSXmlTreeWalker walker = new PSXmlTreeWalker(linkurl);
      Element valueEl = walker.getNextElement("Value");
      String currentURL = valueEl.getAttribute("current");
      request.printTraceMessage("currentURL = " + currentURL);
      String revstring = IPSHtmlParameters.SYS_REVISION + "=";
      int startIndex = currentURL.indexOf(revstring);
      if (startIndex < 0)
      {
         request.printTraceMessage("URL doesnot have sys_revision : "
               + currentURL);
         return -1;
      }
      int endIndex = currentURL.indexOf("&", startIndex);
      if (endIndex < 0)
      {
         request
               .printTraceMessage("sys_revision is probably the last parameter");
         endIndex = currentURL.length();
      }
      String rev = currentURL.substring(startIndex + revstring.length(),
            endIndex);
      request.printTraceMessage("Returning integer value of " + rev);
      return Integer.parseInt(rev);
   }

   /**
    * Implementation of the method required by the interface. Always returns
    * <code>false</code>.
    * 
    * @see com.percussion.extension.IPSResultDocumentProcessor
    * #canModifyStyleSheet()
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * Required number of parameters to this extension.
    */
   private static final int MIN_REQD_PARAM_COUNT = 1;
}
