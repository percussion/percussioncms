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
package com.percussion.server.webservices;

import com.percussion.cms.IPSCmsErrors;
import com.percussion.cms.IPSConstants;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.handlers.PSContentEditorHandler;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.data.IPSInternalRequestHandler;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.data.PSQueryHandler;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.error.PSException;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.PSConsole;
import com.percussion.server.PSInternalRequest;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This is a helper class, which contains utility methods for all the classes
 * that related to <code>PSWebServicesRequestHandler</code> class. Those
 * classes are typically package protected and the name is in the format of
 * <code>PSWS...Handler</code> (which is not required, just a convention).
 */

public abstract class PSWebServicesBaseHandler implements IPSPortActionHandler
{
   /**
    * This is the implementation of the interface for executing all service 
    * actions within the specified port.
    *
    * @param port The name of the port to be handling this action, only used if
    *    there is an error, must not be <code>null</code> or empty.
    * @param action The name of the action to be exectuded, must not be <code>
    *    null</code> or empty.
    * @param request The original request for the operation, 
    *    assumed not <code>null</code>
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response
    * 
    * @throws PSException
    */
   public void processAction(
      String port,
      String action,
      PSRequest request,
      Document parent)
      throws PSException
   {
      /* set up the args once in case an exception occurs below */
      String[] args = new String[4];
      args[0] = action;
      args[1] = port;
      
      Logger l = LogManager.getLogger(getClass());

      Method method = null;
      // process the specified request, this will set the data in
      // the parent doc or throw an error
      try
      {
            // done through reflection, since we already have the proper
            // port handler, now get the method based on the action
            method = getClass().getDeclaredMethod(
                  action + "Action",
                  new Class[] { PSRequest.class, Document.class });
            
            method.invoke(this, new Object[] { request, parent });
      }
      catch (NoSuchMethodException e)
      {
         throw new PSException(
            IPSWebServicesErrors.WEB_SERVICE_ACTION_NOT_FOUND,
            new Object[] { action, port, e.getLocalizedMessage()});
      }
      catch (SecurityException e)
      {
         args[2] = e.getClass().getName();
         args[3] = e.getLocalizedMessage();
         throw new PSException(IPSWebServicesErrors.WEB_SERVICE_DISPATCH_ERROR, 
               args);
      }
      catch (IllegalArgumentException e)
      {
         args[2] = e.getClass().getName();
         args[3] = e.getLocalizedMessage();
         throw new PSException(IPSWebServicesErrors.WEB_SERVICE_DISPATCH_ERROR, 
               args);
      }
      catch (IllegalAccessException e)
      {
         args[2] = e.getClass().getName();
         args[3] = e.getLocalizedMessage();
         throw new PSException(IPSWebServicesErrors.WEB_SERVICE_DISPATCH_ERROR, 
               args);
      }
      catch (InvocationTargetException e)
      {
         l.error(
               "WSBaseHandler InvocationTargetException," + " action: "
                     + action + "Action" + " port: " + port + " request: "
                     + request == null ? "null" : request.getRequestFileURL()
                     + " doc: " + parent == null
                     ? "null"
                     : PSXmlDocumentBuilder.toString(parent) + " method: "
                           + method == null ? "null" : method.toString(), e
                     .getTargetException());
         
         if (e.getTargetException() instanceof PSException)
         {
            throw (PSException)e.getTargetException();
         }
         else
         {
            args[2] = "ivt: " + e.getTargetException().getClass().getName();
            args[3] = e.getTargetException().getLocalizedMessage();
            throw new PSException(
                  IPSWebServicesErrors.WEB_SERVICE_DISPATCH_ERROR, 
                  args);
         }
      }
   }

   /**
    * This is a generic processing routine that does the following:
    *    1) validates that the request has a <code>sys_contentid</code> and a
    *       <code>sys_revision</code> to an item that exists
    *    2) creates a path to the resource that exists in the WEB_SERVICES_APP
    *    3) processes the request and puts the return data in the parent doc
    *
    * @param action The action (or operation) of the request. It may not be
    *    <code>null</code> or empty.
    * @param request The request object. It may not be <code>null</code>.
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response
    * 
    * @throws PSException if any error occurs.
    */
   public void processContentIdAction(
      String action,
      PSRequest request,
      Document parent)
      throws PSException
   {
      // check for existence first, throws error if not found
      // also sets the request with the content id and revision
      validateContentKey(request, true, false);

      String path = WEB_SERVICES_APP + "/" + action;

      processInternalRequest(request, path, parent);
   }

   /**
    * Validates that the specified request has both <code>sys_contentid</code>
    * and <code>sys_revision</code> and that they point to an existing item.
    * 
    * @param request The request object. It may not be <code>null</code>.
    * 
    * @return An <code>Element</code> that is the <code>EL_CONTENTKEY</code> of
    *    input document of the specified request.
    * 
    * @throws PSException if the content item can not be found or the request 
    *    does not specify the proper data.
    */
   public Element validateContentKey(PSRequest request) throws PSException
   {
      return validateContentKey(request, true, true);
   }

   /**
    * Validates that the specified request has both <code>sys_contentid</code>
    * and <code>sys_revision</code> and that they point to an existing item.
    * 
    * @param request The request object. It may not be <code>null</code>.
    * @param contentIdRequired Set to <code>true</code> to require a content id.
    * @param revisionRequired Set to <code>true</code> to require a revision.
    * 
    * @return An <code>Element</code> that is the <code>EL_CONTENTKEY</code> of
    *    input document of the specified request.
    * 
    * @throws PSException if the content item can not be found or the request 
    *    does not specify the proper data.
    */
   public Element validateContentKey(
      PSRequest request,
      boolean contentIdRequired,
      boolean revisionRequired)
      throws PSException
   {
      // get the required parameters from the input document
      Document inputDoc = request.getInputDocument();

      Element root = inputDoc.getDocumentElement();
      Element el = PSXMLDomUtil.getFirstElementChild(root, EL_CONTENTKEY);
      int id =
         PSXMLDomUtil.checkAttributeInt(el, ATTR_CONTENTID, contentIdRequired);
      int rev =
         PSXMLDomUtil.checkAttributeInt(el, ATTR_REVISION, revisionRequired);

      request.setParameter(IPSHtmlParameters.SYS_CONTENTID, "" + id);
      request.setParameter(IPSHtmlParameters.SYS_REVISION, "" + rev);

      // check for existance throws an exception if
      // the content id does not exist
      getContentEditorURL(request);

      return el;
   }
   
   /**
    * Validates all purge keys in the requests input document and sets the
    * request parameters <code>sys_contentid</code> accordingly. At this
    * point all purge keys must supply the contentid. No revisions are expected
    * or used since purging of specific item revisions is not supported.
    * 
    * @param request the request that contains the input document for which to
    *    validate the purge keys and to which the request parameter 
    *    <code>sys_contentid</code> will be set, not <code>null</code>.
    * @throws PSException for any error.
    */
   public void validatePurgeKeys(PSRequest request) throws PSException
   {
      // get the required parameters from the input document
      Document inputDoc = request.getInputDocument();

      List contentIds = new ArrayList();
      Element root = inputDoc.getDocumentElement();
      Element element = PSXMLDomUtil.getFirstElementChild(root, EL_PURGEKEY);
      while (element != null)
      {
         int id = PSXMLDomUtil.checkAttributeInt(element, ATTR_CONTENTID, true);
         contentIds.add(Integer.toString(id));
         
         element = PSXMLDomUtil.getNextElementSibling(element);
      }

      request.setParameter(IPSHtmlParameters.SYS_CONTENTID, contentIds);

      getContentEditorURL(request);
   }

   /**
    * Operation to check in or check out an item.
    * 
    * @param request The request object. It may not be <code>null</code>.
    * @param trigger A string representing which operation to execute, should be
    *    one of the following: <code>IPSConstants.TRIGGER_CHECKIN</code> or 
    *    <code>IPSConstants.TRIGGER_CHECKOUT</code>.
    * 
    * @throws PSException
    */
   public void executeCheckInOut(PSRequest request, String trigger)
      throws PSException
   {
      String path =
         getContentEditorURL(request)
            + "?"
            + IPSHtmlParameters.SYS_COMMAND
            + "=workflow"
            + "&"
            + IPSConstants.DEFAULT_ACTION_TRIGGER_NAME
            + "="
            + trigger;

      // since request has now been updated with the proper
      // revision, update the current request in case
      // other calls will be made with this request
      PSRequest newReq = makeInternalRequest(request, path).getRequest();
      request.setParameter(
         IPSHtmlParameters.SYS_REVISION,
         newReq.getParameter(IPSHtmlParameters.SYS_REVISION));
   }

   /**
    * Performs the specified transition for the specified item.
    *  
    * @param request the request contains the specified item, never 
    *    <code>null</code>. The id of the item is specified by
    *    {@link IPSHtmlParameters#SYS_CONTENTID} and the revision of the
    *    item is specified by {@link IPSHtmlParameters#SYS_REVISION}.
    * @param transition the to be performed transition, which is either the 
    *    transition id or transition trigger. It may not be 
    *    <code>null</code> or empty. 
    * @param comment the comment for the transition. It may be <code>null</code>
    *    or empty.
    * @param adhocList the add hoc list for the transition. It may be 
    *    <code>null</code> or empty.
    *    
    * @throws PSException if an error occurs during the transition.
    */
   public void transitionItem(PSRequest request, String transition,
         String comment, String adhocList) throws PSException
   {
      String path = getContentEditorURL(request);

      request.setParameter(IPSHtmlParameters.SYS_COMMAND, "workflow");
      request.setParameter("WFAction", transition);

      // the above is a hack to get around the fact that it "needs" a
      // WFAction as a name, we put the transition(Id) there and over
      // in the exit we check for a numeric value, if it is numeric,
      // we call a different constructor
   
      if (comment != null && comment.trim().length() > 0)
         request.setParameter("commenttext", comment);
   
      if (adhocList != null && adhocList.trim().length() > 0)
         request.setParameter("sys_wfAdhocUserList", adhocList);
         
      resetValidationError(request);
      PSInternalRequest iReq = makeInternalRequest(request, path);
      checkValidationError(iReq.getRequest(), path);
   }

   /**
    * Operation to create an XML result Document from an error type,
    * code and message.
    *
    * @param type A String defining the style of the response valid values are 
    * "success", "partial", "failure", it may not be <code>null</code> or empty.
    * @param code An int of the internal exception code.
    * @param message A String which is the message from the exception, it may be 
    *    <code>null</code>.
    *
    * @return a result response document, never <code>null</code>
    */
   public static Document createResultResponseDoc(
      String type,
      int code,
      String message)
   {
      if (type == null || type.trim().length() == 0)
         throw new IllegalArgumentException("type may not be null or empty");

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      addResultResponseXml(type, code, message, doc);

      return doc;
   }

   /**
    * Operation to create an XML result Element from an error type,
    * code and message.
    *
    * @param type A String defining the style of the response valid values are 
    * "success", "partial", "failure", it may not be <code>null</code> or empty.
    * @param code An int of the internal exception code.
    * @param message A String which is the message from the exception, it may be 
    *    <code>null</code>.
    * @param doc A Document where to append the response to, assumed not <code>
    *    null</code>.
    */
   public static void addResultResponseXml(
      String type,
      int code,
      String message,
      Document doc)
   {
      if (type == null || type.trim().length() == 0)
         throw new IllegalArgumentException("type may not be null or empty");

      Element resultResponseEl;
      Element root = doc.getDocumentElement();
      resultResponseEl =
         doc.createElementNS(NS_COMMON, NS_COMMON_PREFIX + EL_RESULTRESPONSE);

      // FIX ME implement this when we need more than 1 error / success returned      
      // we need to determine if we have a partial failure,
      // for now we assume no "partial" failure
      resultResponseEl.setAttribute("type", type);

      if (root == null)
         doc.appendChild(resultResponseEl);
      else
         root.appendChild(resultResponseEl);

      Element resultEl =
         doc.createElementNS(NS_COMMON, NS_COMMON_PREFIX + EL_RESULT);

      resultEl.setAttribute("type", type);
      resultEl.setAttribute("errorCode", "" + code);
      if (message != null)
         resultEl.appendChild(doc.createTextNode(message));
      resultResponseEl.appendChild(resultEl);
   }
   
   /**
    * Get the content editor url for a specific content item.
    *
    * @param request the original request, assumes that <code>sys_contentid
    *    </code> is within the parameter list of the request
    *
    * @return the url where the content type editor is located, used for 
    *    internal requests, never <code>null</code>
    *
    * @throws PSInternalRequestCallException
    * @throws PSException if the content type id cannot be found
    */
   protected String getContentEditorURL(PSRequest request)
      throws PSInternalRequestCallException, PSException
   {
      PSInternalRequest ir =
         PSServer.getInternalRequest(
            CONTENT_EDITOR_CATALOGER,
            request,
            null,
            true);
      if (ir == null)
      {
         throw new PSException(
            IPSWebServicesErrors.WEB_SERVICE_INTERNAL_REQUEST_FAILED,
            CONTENT_EDITOR_CATALOGER);
      }
      else
      {
         // make the internal request and extract the URL from the result XML
         Element root = ir.getResultDoc().getDocumentElement();
         NodeList nl = root.getChildNodes();
         for (int i = 0; i < nl.getLength(); i++)
         {
            Node node = nl.item(i);
            String name = PSXMLDomUtil.getUnqualifiedNodeName(node);
            if (name.equalsIgnoreCase("query"))
            {
               String url = PSXMLDomUtil.getElementData(node);
               if (url != null && url.trim().length() != 0)
               {
                  return url;
               }
            }
         }
      }
      Object args[] = new Object[2];
      args[0] = request.getParameter(IPSHtmlParameters.SYS_CONTENTID);
      args[1] = request.getParameter(IPSHtmlParameters.SYS_REVISION);

      // content id not found
      throw new PSException(
         IPSWebServicesErrors.WEB_SERVICE_CONTENT_ITEM_NOT_FOUND,
         args);
   }

   /**
    * Determines the content type id of the specified content item.
    *
    * @param request the original request for the operation, it may not be 
    *    <code>null</code>
    * 
    * @return int the content type id as an int
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSException If the lookup fails for any reason.
    */
   protected long lookupContentTypeId(PSRequest request) throws PSException
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");

      String strContentId =
         request.getParameter(IPSHtmlParameters.SYS_CONTENTID);

      int contentId = -1;
      long contentTypeId = -1;
      try
      {
         if (strContentId != null && strContentId.trim().length() != 0)
            contentId = Integer.parseInt(strContentId);
      }
      catch (NumberFormatException e)
      {
         // not supplied, handle below
      }

      if (contentId != -1)
      {
         PSLocator loc = new PSLocator(contentId);
         contentTypeId = PSItemDefManager.getInstance().getItemContentType(loc);
      }

      if (contentTypeId == -1)
      {
         Object args[] = new Object[2];
         args[0] = request.getParameter(IPSHtmlParameters.SYS_CONTENTID);
         args[1] = request.getParameter(IPSHtmlParameters.SYS_REVISION);

         // content id not found
         throw new PSException(
            IPSWebServicesErrors.WEB_SERVICE_CONTENT_ITEM_NOT_FOUND,
            args);
      }

      return contentTypeId;
   }

   /**
    * convenience helper to return the item def given a contentType
    *
    * @param request the original request, may not <code>null</code>
    * @param contentType a content type id as either a numerical internal id
    *    or a string name, may not <code>null</code> or empty
    *
    * @return an item definition based on the content type, 
    *    never <code>null</code>
    *
    * @throws PSException
    */
   protected PSItemDefinition getItemDefinition(
      PSRequest request,
      String contentType)
      throws PSException
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");
      if (contentType == null || contentType.trim().length() == 0)
         throw new IllegalArgumentException("contentType may not be null or empty");

      PSSecurityToken tok = request.getSecurityToken();

      PSItemDefManager mgr = PSItemDefManager.getInstance();
      PSItemDefinition itemDef = null;

      try
      {
         int typeId = Integer.parseInt(contentType);
         itemDef = mgr.getItemDef(typeId, tok);
      }
      catch (NumberFormatException nfe)
      {
         itemDef = mgr.getItemDef(contentType, tok);
      }

      if (itemDef == null)
      {
         // content type does not exist
         throw new PSException(
            IPSWebServicesErrors.WEB_SERVICE_CONTENT_TYPE_NOT_FOUND,
            contentType);
      }
      return itemDef;
   }

   /**
    * Helper function to execute an internal request.
    *
    * @param request the original request object, assumed not <code>null</code>
    * @param path the application and resource location of the action to be 
    *    executed by the system, assumed not <code>null</code>
    *
    * @return PSInternalRequest the internal request that was generated, never
    *    <code>null</code>, may contain a modified request object
    *
    * @throws PSException if the internal request is not created
    */
   protected static PSInternalRequest makeInternalRequest(
      PSRequest request,
      String path)
      throws PSException
   {
      PSInternalRequest iReq =
         PSServer.getInternalRequest(path, request, null, true);
      if (iReq == null)
      {
         throw new PSException(
            IPSWebServicesErrors.WEB_SERVICE_INTERNAL_REQUEST_FAILED,
            path);
      }
      handleOverrideCommunity(request);      
      iReq.performUpdate();

      return iReq;
   }

   /**
    * Operation to update the parent doc from an internal request.
    *
    * @param request The original request object, assumed not <code>null</code>.
    * @param path The application and resource location of the action to be 
    *    executed by the system assumed not <code>null</code>.
    * @param parent The parent document to insert the result into, assumed not
    *    <code>nul</code>.
    * 
    * @return Returns the result document from the internal request, never 
    *    <code>null</code>.
    *
    * @throws PSException if the internal request is not created
    */
   protected void processInternalRequest(
      PSRequest request,
      String path,
      Document parent)
      throws PSException
   {
      Document doc = processInternalRequest(request, path);

      if (doc == null)
         addResultResponseXml("success", 0, null, parent);
      else
         // copy the result data to the parent doc root element
         PSXmlDocumentBuilder.replaceRoot(parent, doc.getDocumentElement());
   }

   /**
    * Protected function to get a result document from an internal request.
    *
    * @param request The original request object, assumed not <code>null</code>.
    * @param path The application and resource location of the action to be 
    *    executed by the system assumed not <code>null</code>.
    * @param isHandleOverrideCommunity if <code>true</code>, then calls
    *    {@link #handleOverrideCommunity(PSRequest)}; otherwise skips the call,
    *    assumes the caller already made the call once. 
    * 
    * @return Returns the result document from the internal request, never 
    *    <code>null</code>.
    *
    * @throws PSException if the internal request is not created
    */
   protected Document processInternalRequestEx(PSRequest request, String path,
         boolean isHandleOverrideCommunity) throws PSException
   {
      // Add a parameter to hold cached information for workflow
      // filtering later. Do this before we create the internal request
      // so it gets copied into the internal requests that are created.
      request.setParameter(PSX_WORKFLOW_STATE_INFO_CACHE, new HashMap());
            
      PSInternalRequest iReq =
         PSServer.getInternalRequest(path, request, null, true);
      if (iReq == null)
      {
         throw new PSException(
            IPSWebServicesErrors.WEB_SERVICE_INTERNAL_REQUEST_FAILED,
            path);
      }
      IPSInternalRequestHandler rh = iReq.getInternalRequestHandler();
      
      if (isHandleOverrideCommunity)
         handleOverrideCommunity(request);
      
      if (rh != null
         && ((rh instanceof PSQueryHandler)
            || (rh instanceof PSContentEditorHandler)))
      {
         return iReq.getResultDoc();
      }
      else
      {
         iReq.performUpdate();
         return null;
      }
   }
   
   /**
    * Calls {@link #processInternalRequestEx(PSRequest, String, boolean)
    * (processInternalRequestEx(request, path, true)}.
    */
   protected Document processInternalRequest(PSRequest request, String path)
      throws PSException
   {
      return processInternalRequestEx(request, path, true);
   }
   
   /**
    * Private function to get a result document from an internal request.
    *
    * @param request The original request object, assumed not <code>null</code>.
    * @param path The application and resource location of the action to be 
    *    executed by the system assumed not <code>null</code>.
    * @param parent The parent document to insert the result into, assumed not
    *    <code>nul</code>.
    * 
    * @throws PSException if the internal request is not created
    */
   protected void getMergedResultDoc(
      PSRequest request,
      String path,
      Document parent)
      throws PSException
   {
      Document retDoc = null;
      ByteArrayOutputStream out = null;
      ByteArrayInputStream in = null;

      out = processMergeResultRequest(request, path);
      if (null == out)
         return;

      in = new ByteArrayInputStream(out.toByteArray());

      try
      {
         retDoc = PSXmlDocumentBuilder.createXmlDocument(in, false);

         // copy the result data to the parent doc root element
         PSXmlDocumentBuilder.replaceRoot(parent, retDoc.getDocumentElement());
      }
      catch (IOException e)
      {
         // ignore for now
      }
      catch (org.xml.sax.SAXException e)
      {
         // ignore for now
      }
      finally
      {
         try
         {
            if (in != null)
               in.close();

            if (out != null)
               out.close();
         }
         catch (IOException e)
         {
            // ignore
         }
      }
   }

   /**
    * Function to get a result document from an internal request, also
    * has the added feature of applying the stylesheet if applicable. The
    * caller is responsible for closing the output stream.
    *
    * @param request the original request object, assumed not <code>null</code>
    * @param path the application and resource location of the action to be 
    *    executed by the system, assumed not <code>null</code>
    * @return ByteArrayOutputStream returns the result output stream from the
    *    internal request, never <code>null</code>
    * 
    * @throws PSException if the internal request is not created
    */
   protected ByteArrayOutputStream processMergeResultRequest(
      PSRequest request,
      String path)
      throws PSException
   {
      ByteArrayOutputStream out = null;

      PSInternalRequest iReq =
         PSServer.getInternalRequest(path, request, null, true);
      if (iReq == null)
      {
         throw new PSException(
            IPSWebServicesErrors.WEB_SERVICE_INTERNAL_REQUEST_NOT_FOUND,
            path);
      }
      out = iReq.getMergedResult();
      return out;
   }

   /**
    * Reset the validation error in the given request. This is used
    * in conjuction with the {@link #checkValidationError(PSRequest)}.
    * 
    * @param request The request object, it may not be <code>null</code>.
    */   
   protected void resetValidationError(PSRequest request)
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");
      
      request.setParameter(IPSHtmlParameters.SYS_VALIDATION_ERROR, null);
   }
   
   /**
    * Check the validation error that is registered in the given request.
    * 
    * @param request The request that may contains the validation error,
    *    it may not be <code>null</code>.
    *
    * @param path the resource path that is used for the request, it may 
    *    not be <code>null</code> or empty.
    *     
    * @throws PSCmsException if an validation error has occurred.
    */
   protected void checkValidationError(PSRequest request, String path)
      throws PSCmsException
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");
      if (path == null || path.trim().length() == 0)
         throw new IllegalArgumentException("path may not be null or empty");
         
      String validateError = request.getParameter(
         IPSHtmlParameters.SYS_VALIDATION_ERROR);
      if (validateError != null && validateError.trim().length() > 0)
      {
         PSConsole.printMsg(getClass().getName(),
            IPSCmsErrors.VALIDATION_ERROR,
            new Object[] {path, validateError});
         throw new PSCmsException(IPSCmsErrors.VALIDATION_ERROR,
            new Object[] {path, validateError});
      }
   }
  
   /**
    * Checks to see if the override community param is set in 
    * the request and if so overrides to that community.
    * @param request the request, cannot be <code>null</code>.
    * @throws PSException when encountering any error.
    */
   protected static void handleOverrideCommunity(PSRequest request)
      throws PSException
   {
      if(null == request)
         throw new IllegalArgumentException("Request cannot be null.");
      
      String usersComm = (String)request.getUserSession().getPrivateObject(
         IPSHtmlParameters.SYS_COMMUNITY);
      if(usersComm == null)
         usersComm = "";   
               
      // call verify community only attempting to override 
      String commOverride = request.getParameter(
         IPSHtmlParameters.SYS_OVERRIDE_COMMUNITYID);
      if (commOverride != null && commOverride.trim().length() > 0 &&
         !usersComm.trim().equals(commOverride.trim()))
      {
         PSServer.verifyCommunity(request);
      }
   }

   /**
    * Public key for private object that is used to hold workflow state info
    * across requests to the filtering exit. The filtering exit is called
    * for each row that is to be returned from the search handler. See
    * {@link PSSearchHandler} for more details.
    */
   public static final String PSX_WORKFLOW_STATE_INFO_CACHE = 
      "PSX_WORKFLOW_STATE_INFO_CACHE";
   
   /**
    * parameter element constants
    */
   protected static final String EL_CONTENTKEY = "ContentKey";
   protected static final String EL_PURGEKEY = "PurgeKey";
   protected static final String ATTR_CONTENTID = "contentId";
   protected static final String ATTR_REVISION = "revision";
   protected static final String EL_CONTENTTYPE = "ContentType";
   protected static final String EL_XMLDATA = "XMLData";

   /**
    * response elements / attributes
    */
   private static final String EL_RESULTRESPONSE = "ResultResponse";

   /**
    * Result response lives in the common schema, therefore we need to defined
    * the common namespace to be delivered when we create the result response.
    */
   private static final String NS_COMMON =
      "urn:www.percussion.com/webservices/common";

   private static final String NS_COMMON_PREFIX = "com:";

   /**
    * standard item element constants
    */
   private static final String EL_RESULT = "Result";

   /**
    * Name of the internal resource used to catalog content editor URLs.
    */
   private static final String CONTENT_EDITOR_CATALOGER =
      "sys_psxContentEditorCataloger/getUrl.xml";

   /**
    * Name of the custom application for web services.
    */
   protected static final String WEB_SERVICES_APP = "sys_psxWebServices";
}
