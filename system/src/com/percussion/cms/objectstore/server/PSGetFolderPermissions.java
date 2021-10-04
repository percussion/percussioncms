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
package com.percussion.cms.objectstore.server;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSObjectPermissions;
import com.percussion.error.PSExceptionUtils;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This is a system exit which given contenid calculates permissions
 * for the folder. This permission value is then set as an
 * attribute value of the root OR if supplied optional element name 
 * then gets contentid from each of the matching element, gets contentid
 * attribute from that element then calculates and sets folder permissions. 
 *
 * See the description of <code>processResultDocument()</code> method for
 * details of its functioning.
 */
public class PSGetFolderPermissions extends PSDefaultExtension
   implements IPSResultDocumentProcessor
{
   /**
   * Implementation of the method in the interface
   * <code>com.percussion.extension.IPSResultDocumentProcessor</code>
   * <p>
   * See {@link IPSResultDocumentProcessor#processResultDocument(Object[],
   *      IPSRequestContext, Document) processResultDocument} for details.
   * <p>
   * If <code>resultDoc</code> is <code>null</code> or the root element of
   * <code>resultDoc</code> is <code>null</code> then no processing is done
   * and no exception is thrown.
   * <p>
   * @param params An array with elements as defined below.
   * <p>
   * Required Params
   * <p>
   * param[0] is required. It is the content id of the folder whose permissions
   * are to be obtained and then set as an attribute value. If the specified
   * folder does not exist, <code>PSExtensionProcessingException</code> is
   * thrown. This parameter is specified using the "contentid" parameter name
   * in the workbench.
   * 
   * <p>
   * param[1] is required. It is the name of the attribute whose value must
   * be set with the folder permission value. This parameter is specified
   * using the "attributeName" parameter name in the workbench.
   * 
   * <p>
   * param[2] is optional. It is the name of the element from which to get
   * contentId and calculate permissions from. Defaults to 'PSXComponentSummary'.
   * 
   * <p>
   * param[3] is optional. It is the name of the attribute of the above element
   * that contains contentid value from which to calculate folder permissions.
   * Defaults to 'contentId'. 
   * <p>
   * 
   * @param request the request context for this request,
   * never <code>null</code> (specified by the interface)
   *
   * @param resultDoc The supplied document. Guaranteed not <code>null</code>
   * by the interface.
   *
   * @return the supplied document with the attribute value set for the
   * specified node, never <code>null</code>
   *
   * @throws PSParameterMismatchException if any required paramater is missing
   * or is <code>null</code> or empty.
   * @throws PSExtensionProcessingException if the specified folder does not
   * exist
   */
   public Document processResultDocument(Object[] params,
         IPSRequestContext request, Document resultDoc)
      throws PSParameterMismatchException, PSExtensionProcessingException
   {
      request.printTraceMessage(ms_className + "#processResultDocument");

      if (resultDoc == null)
      {
         request.printTraceMessage(ms_className + "#resultDoc is null");
         return resultDoc;
      }

      Element resultDocRoot = resultDoc.getDocumentElement();
      if (resultDocRoot == null)
      {
         request.printTraceMessage(ms_className + "#resultDoc root is null");
         return resultDoc;
      }

      try
      {
         // check the number of parameters provided is correct
         if ((params.length < EXPECTED_NUMBER_OF_PARAMS) ||
            (params[0] == null) ||
            (params[0].toString().trim().length() < 1) ||
            (params[1] == null) ||
            (params[1].toString().trim().length() < 1))
         {
            throw new PSParameterMismatchException(
               EXPECTED_NUMBER_OF_PARAMS, params.length);
         }

         String strContentId = params[0].toString().trim();
         int contentId = convertContentId(strContentId);
         String permAttrName = params[1].toString().trim();
         
         if (contentId==-1)
         {  
            //defaults
            String elementName = "PSXComponentSummary";
            String contentIdAttrName = "contentId";
            
            /*contentid = -1 means that we need to go through a doc
             looking for a given element name and if found and has
             'contentId' attribute, then calculate and set folder
             permissions as an attribute of that element.
            */  
            
            if (params.length >= 3 && params[2]!=null &&
                params[2].toString().trim().length() > 0)
            {
               elementName = params[2].toString();
            }
            if (params.length >= 4 && params[3]!=null &&
                params[3].toString().trim().length() > 0)
            {
               contentIdAttrName = params[3].toString();
            }
            
            //get element from which to get contentId attribute, from which calculate
            //folder permissions and set them as a new attribute of this element.
         
            PSXmlTreeWalker walker = new PSXmlTreeWalker(resultDocRoot);
            Element el = walker.getNextElement(elementName, 
               PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
            
            while (el != null)
            {
               if (el.getNodeName().equalsIgnoreCase(elementName))
               {
                  //get contentid
                  int contentid2 =
                     convertContentId(el.getAttribute(contentIdAttrName));
                  
                  if (contentid2 == -1)
                  {
                     throw new PSParameterMismatchException(ms_className +
                        ": Invalid content id: -1");
                  }
                     
                  //calculate and set folder permissions on this element 
                  setFolderPermissions(request, el, contentid2, permAttrName);
               }
               
               el = walker.getNextElement(elementName, 
                  PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
            }
           
         }
         else
         {
            //default case: given contentId set permissions on the root elem.
            setFolderPermissions(request, resultDocRoot, contentId, permAttrName);
         }
         
         return resultDoc;
      }
      catch(Exception ex)
      {
          log.error(PSExceptionUtils.getMessageForLog(ex));
          log.debug(PSExceptionUtils.getDebugMessageForLog(ex));
          throw new PSExtensionProcessingException(ms_className, ex);
      }
   }

   /**
    * Given contentid calculate folder permissions and set it as
    * a new attribute with a given attribute name.
    * @param request assumed not <code>null</code>.
    * @param element assumed not <code>null</code>.
    * @param contentId assumes valid contentid.
    * @param permAttrName name of the new permissions attribute to set.
    * @throws PSCmsException
    */
   private void setFolderPermissions(
      IPSRequestContext request,
      Element element,
      int contentId,
      String permAttrName)
      throws PSCmsException
   {
      PSObjectPermissions objPermissions = PSFolderSecurityManager.getPermissions(contentId);
      int permissions = objPermissions.getPermissions();
      
      request.printTraceMessage(
         ms_className + ": Folder Permissions = " + permissions);
      
      element.setAttribute(permAttrName, "" + permissions);
   }

   /**
    * Converts string contentid into int contentid, 
    * handles any conversion exceptions and returns -1 on any error.  
    * @param strContentId
    * @return content id or -1 if fails to convert.
    */
   private int convertContentId(String strContentId)
   {
      if (strContentId==null)
         return -1;
      
      int contentId = -1;
      try
      {
         contentId = Integer.parseInt(strContentId);
      }
      catch (NumberFormatException nfe)
      {
         return -1;
      }
      
      return contentId;
   }

   /**
    * See interface for description.
    *
    * @return Always <code>false</code>.
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * The function name used for error handling, never <code>null</code>
    * or empty
    */
   private static final String ms_className = "PSGetFolderPermissions";

   /**
    * The number of expected parameters, intialized to 2
    */
   private static final int EXPECTED_NUMBER_OF_PARAMS = 2;

   private static final Logger log = LogManager.getLogger(IPSConstants.SECURITY_LOG);

}




