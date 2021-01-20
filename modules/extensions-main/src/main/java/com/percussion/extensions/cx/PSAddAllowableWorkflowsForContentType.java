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
package com.percussion.extensions.cx;

import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSWorkflowInfo;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * This extension adds the allowable workflows for the content type.
 * <p>
 * As a pre exit, it will determine the union of the allowed workflows for each
 * supplied content type ids.  It expects two parameters:
 * <ol>
 * <li>The content type id(s) either as a <code>String</code> or a non-empty 
 * <code>List</code> of <code>String</code> objects - optional</li>
 * <li>The name of the parameter in which the resulting workflow ids are to
 * be stored as a <code>List</code> of <code>String</code> objects - required.
 * </li>
 * </ol>
 * If the first parameter value is null or empty, this exit simply returns.
 * <p> 
 * As a post exit it expects the input doc as per the following DTD.
 * &lt;!ELEMENT ContentTypes (ContentType*) &gt;
 * &lt;!ELEMENT ContentType (#PCDATA) &gt;
 * &lt;!ATTLIST ContentType contenttypeid CDATA #IMPLIED &gt;
 * The allowable workflows will be added as per the following DTD to the root
 * element.
 * &lt;!ELEMENT workflows (workflow*) &gt;
 * &lt;!ATTLIST workflow workflowid CDATA #IMPLIED &gt;
 * It basically gets all the workflows in the system and removes the workflows
 * in the excludelist for each content type.
 */
public class PSAddAllowableWorkflowsForContentType extends PSDefaultExtension
      implements IPSResultDocumentProcessor, IPSRequestPreProcessor
{

   /**
    * Required by the interface. This exit never modifies the stylesheet.
    * @see IPSResultDocumentProcessor#canModifyStyleSheet()
    * @return boolean
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /* 
    * See interface and class header
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request) 
      throws PSParameterMismatchException, PSExtensionProcessingException
   {
      if (params == null)
         throw new IllegalArgumentException("params may not be null");

      if (request == null)
         throw new IllegalArgumentException("request may not be null");


      // validate params
      if (params.length < 2 || params[1] == null)
         throw new PSParameterMismatchException("Two params expected");
      
      List ctypeList = null;
      if (params[0] instanceof List)
      {
         ctypeList = (List)params[0];
      }
      else if (params[0] != null)
      {
         String ctype = params[0].toString();
         ctypeList = new ArrayList(1);
         ctypeList.add(ctype);
      }
      
      if (ctypeList == null || ctypeList.isEmpty())
         return;
      
      String outParamName = params[1].toString();
      if (outParamName.trim().length() == 0)      
         throw new PSParameterMismatchException(
            "Output param must be specified");
      
      List allWFList = getAllWorkflows(request);      
      List wfList = new ArrayList();
      
      if (allWFList != null)
      {
         try
         {
            // build set for non-dupes
            Set wfSet = new HashSet();
            Iterator ctypes = ctypeList.iterator();
            while (ctypes.hasNext())
            {
               String ctype = ctypes.next().toString();
               wfSet.addAll(getAllowedWorkflows(ctype, request, allWFList));
            }
         
            // add to list for return param
            wfList.addAll(wfSet);
         }
         catch (Exception e)
         {
            request.printTraceMessage("Exception:" + e.getLocalizedMessage());
         }
      }

      request.setParameter(outParamName, wfList);
   }

   /*
    * implementation of the method in the interface IPSRequestPreProcessor
    */
   public Document processResultDocument(Object[] params,
         IPSRequestContext request,
         Document doc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {

      NodeList nl = doc.getDocumentElement().getElementsByTagName("ContentType");
      if(nl == null || nl.getLength() < 1)
      {
         //This means there are no contenttypes available return the doc
         //User cannot translate the item
         return doc;
      }
      Element workflowsElem = null;
      Element workflowElem = null;
      List wfList = getAllWorkflows(request);
      
      if(wfList == null)
      {
         //Could not bring any workflows
         return doc;
      }
      
      // make copy that we don't modify to use for each type's allowed call
      List allWFList = new ArrayList(wfList);
      
      // walk each type and get allowed.  Also filter main wfList to contain
      // only allowed types as we go
      for(int i=0; i<nl.getLength(); i++)
      {
         try
         {
            String cid = ((Element)nl.item(i)).getAttribute(ATTR_CONTENTTYPEID);
            List allowed = getAllowedWorkflows(cid, request, allWFList);
            wfList.retainAll(allowed);
            
            if(!allowed.isEmpty())
            {
               Element wselem = doc.createElement(ELEM_WORKFLOWS);
               Iterator iter = allowed.iterator();
               while (iter.hasNext())               
               {
                  Element welem = doc.createElement(ELEM_WORKFLOW);
                  welem.setAttribute(ATTR_WORKFLOWID, iter.next().toString());
                  wselem.appendChild(welem);
               }
               nl.item(i).appendChild(wselem);
            }
         }
         catch(Exception e)
         {
            if(request.isTraceEnabled())
               e.printStackTrace();
            continue;
         }
      }

      workflowsElem = doc.createElement(ELEM_WORKFLOWS);
      for(int j=0; j<wfList.size(); j++)
      {
         workflowElem = doc.createElement(ELEM_WORKFLOW);
         workflowElem.setAttribute(ATTR_WORKFLOWID, wfList.get(j).toString());
         workflowsElem.appendChild(workflowElem);
      }
      doc.getDocumentElement().appendChild(workflowsElem);
      return doc;
   }

   /**
    * Helper function get all the workflows in the system
    * @param request IPSRequestContext
    * @return List of the workflows
    */
   private List getAllWorkflows(IPSRequestContext request)
   {
      Document doc = null;
      List temp = new ArrayList();

      IPSInternalRequest iReq = null;
      try
      {
         iReq = request.getInternalRequest(WORKFLOW_LOOKUP_RESOURCE);         
         doc = iReq.getResultDoc();
      }
      catch(Exception e)
      {
         return null;
      }
      finally
      {
         if(iReq != null)
            iReq.cleanUp();
      }
      if(doc == null)
         return null;
      NodeList nl = doc.getDocumentElement().getElementsByTagName(
         ELEM_WORKFLOW);
      if(nl == null || nl.getLength() < 1)
      {
         return null;
      }
      for(int i=0; i<nl.getLength(); i++)
      {
         NodeList childNodes = nl.item(i).getChildNodes();
         for(int j=0; childNodes!=null && j<childNodes.getLength(); j++)
         {
            if(!childNodes.item(j).getNodeName().equalsIgnoreCase(ELEM_ID))
               continue;
            temp.add(getElemValue((Element)childNodes.item(j)));
         }
      }
      return temp;
   }

   /**
    * Gets all workflows for the specified content id
    * 
    * @param cid The content id, assumed not <code>null</code> or empty
    * @param request The request to use, assumed not <code>null</code>.
    * @param allWorkflows A list of all available workflow ids as 
    * <code>String</code> objects, assumed not <code>null</code>.
    * 
    * @return A list of zero or more workflow ids as <code>String</code>
    * objects, never <code>null</code>.
    * @throws PSInvalidContentTypeException
    */
   private List getAllowedWorkflows(String cid, IPSRequestContext request, 
      List allWorkflows) throws PSInvalidContentTypeException
   {
      PSItemDefManager mgr = PSItemDefManager.getInstance();
      PSContentEditor cedef = mgr.getItemDef(Integer.parseInt(cid),
         -1).getContentEditor();
      PSWorkflowInfo wfinfo = cedef.getWorkflowInfo();      
      List allowedWorkflows = new ArrayList();
      if(wfinfo == null)
      {
         // all are allowed
         allowedWorkflows.addAll(allWorkflows);
         return allowedWorkflows;
      }
      if(wfinfo.isExclusionary())
      {
         allowedWorkflows.addAll(allWorkflows);
         Iterator iter = wfinfo.getValues();
         while(iter.hasNext())
         {
            allowedWorkflows.remove(iter.next().toString());
         }
      }
      else
      {
         Iterator iter = wfinfo.getValues();         
         while(iter.hasNext())
         {
            String wfId = iter.next().toString();
            if (allWorkflows.contains(wfId))
               allowedWorkflows.add(wfId);
         }
      }
      return allowedWorkflows;
   }

   /**
    * Helper function that returns the value of the given DOM Element.
    *
    * @param elem - DOM Element, can not be <code>null</code>. If
    * <code>null</code>, the return value shall be empty string.
    *
    *
    * @return String - the value of the element, can be empty string.
    *
    */
   static public String getElemValue(Element elem)
   {
      String value = "";
      if(elem == null)
         return value;
      Node temp = elem.getFirstChild();
      if(null == temp || Node.TEXT_NODE != temp.getNodeType())
         return value;

      return ((Text)temp).getData().trim();
   }
   /**
    * String constant for the element name workflows
    */
   private static final String ELEM_WORKFLOWS = "workflows";

   /**
    * String constant for the element name workflow
    */
   private static final String ELEM_WORKFLOW = "workflow";

   /**
    * String constant for the element name id
    */
   private static final String ELEM_ID = "id";

   /**
    * String constant for the attribute name contenttypeid
    */
   private static final String ATTR_CONTENTTYPEID = "contenttypeid";

   /**
    * String constant for the attribute name workflowid
    */
   private static final String ATTR_WORKFLOWID = "workflowid";

   /**
    * String constant for the workflow lookup resource
    */
   private static final String WORKFLOW_LOOKUP_RESOURCE =
      "sys_psxRelationshipSupport/workflowlookup";


}
