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

package com.percussion.cms.handlers;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.server.PSRelationshipDbProcessor;
import com.percussion.data.IPSDataErrors;
import com.percussion.data.IPSInternalRequestHandler;
import com.percussion.data.PSExecutionData;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.design.objectstore.PSValidationException;
import com.percussion.error.PSBackEndUpdateProcessingError;
import com.percussion.error.PSException;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.extension.PSExtensionException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSApplicationHandler;
import com.percussion.server.PSConsole;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestValidationException;
import com.percussion.server.PSUserSession;
import com.percussion.util.IPSHtmlParameters;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class encapsulates behavior required to handle Clone commands.  Creates
 * a copy of a specified item and all of it's child and shared data, inserting
 * it as the initial revision of a new item.
 */
public class PSCloneCommandHandler extends PSCommandHandler
{
   /**
    * Creates a handler to process requests for cloning existing content
    * items.
    *
    * @param ah The application handler for this app.  May not be 
    * <code>null</code>.
    * @param ceh The content editor handler for this dataset.  May not be
    * <code>null</code>.
    * @param app The application created by the PSContentEditor for each
    * command handler to add datasets to.  The app is started and stopped by the
    * ContentEditorHandler.  May not be <code>null</code>.
    *
    * @throws PSIllegalArgumentException if there is any invalid data
    * @throws PSExtensionException if there is an error preparing an exception.
    * @throws PSNotFoundException if a udf or extension cannot be located.
    * @throws PSValidationException if there is a problem starting an internal
    * application.
    * @throws IllegalArgumentException if any param is <code>null</code>.
    */
   public PSCloneCommandHandler(PSApplicationHandler ah,
      PSContentEditorHandler ceh, PSContentEditor ce,
         PSApplication app)
      throws PSIllegalArgumentException, PSExtensionException,
         PSNotFoundException, PSValidationException
   {
      super(ah, ceh, ce, app);

      // save the param names we use
      m_contentIdParamName = ceh.getParamName(
         PSContentEditorHandler.CONTENT_ID_PARAM_NAME);
      m_revisionIdParamName = ceh.getParamName(
         PSContentEditorHandler.REVISION_ID_PARAM_NAME);

      // Extract all exits and prepare them.
      prepareExtensions(COMMAND_NAME);

      // prepare redirects
      prepareRedirects(COMMAND_NAME);

      // initialize the copy handler to do our work for us
      m_copyHandler = new PSCopyHandler(ceh, ce, app, this, true);
   }


   /**
    * Process a content editor clone request using the input context
    * information and data.  Creates a new content item with a new content id
    * and a revision of <code>1</code>.
    *
    * @param request the request object containing all context data associated
    * with the request.
    *
    * @throws IllegalArgumentException if request is <code>null</code>.
    */
   public void processRequest(PSRequest request)
   {
      if (request == null)
         throw new IllegalArgumentException("request cannot be null");

      PSExecutionData execData = null;
      try
      {
         execData = new PSExecutionData(m_appHandler, this, request);

         int newContentId = executeCloneRequest(request, execData);
         int newRevisionId = 1;

         // set new content and revision id params
         request.setParameter(m_contentIdParamName, String.valueOf(
            newContentId));
         request.setParameter(m_revisionIdParamName, String.valueOf(
            newRevisionId));

         // run cmd and ds post exits
         runPostProcessingExtensions(execData, null);

         // do redirect
         processRedirect(execData);
      }
      catch (PSAuthorizationException e)
      {
         m_appHandler.handleAuthorizationException(request, e);
      }
      catch (Throwable t)
      {
         /* catch anything that comes our way */
         PSConsole.printMsg("Cms", t);
         String source = COMMAND_NAME;

         String sessId = "";
         PSUserSession sess = request.getUserSession();
         if (sess != null)
            sessId = sess.getId();

         int errorCode;
         Object[] errorArgs;

         PSException e = null;

         if (t instanceof PSException) {
            e = (PSException)t;
            errorCode = e.getErrorCode();
            errorArgs = e.getErrorArguments();
         }
         else {
            errorCode = IPSServerErrors.RAW_DUMP;
            errorArgs = new Object[] { getExceptionText(t) };
         }

         PSBackEndUpdateProcessingError err =
            new PSBackEndUpdateProcessingError(
            m_appHandler.getId(), sessId, errorCode, errorArgs, source);
         m_appHandler.reportError(request, err);
      }
      finally
      {
         if (execData != null)
            execData.release();
      }
   }

   // see IPSRequestHandler interface for description
   public void shutdown()
   {
   }

   /**
    * See {@link PSCommandHandler#isUpdate()} for a description of this method.
    *
    * @return <code>true</code> always.
    */
   public boolean isUpdate()
   {
      return true;
   }

   /**
    * Executes the provided clone request using the supplied execution data.
    *
    * @param request the request to be processed, assumed not <code>null</code>.
    * @param execData the execution data to use, assumed not <code>null</code>.
    * @return the new contentid created.
    * @throws PSRequestValidationException for invalid requests.
    * @throws PSAuthorizationException if the user is not authorize to perform
    *    the request.
    * @throws PSRequestValidationException for any failed request validation.
    * @throws PSValidationException for any failed validation.
    * @throws SQLException for any failed SQL operation.
    * @throws PSNotFoundException for any file not found.
    * @throws PSInternalRequestCallException if any error occurs processing
    *    the internal request call.
    * @throws PSAuthorizationException if the user is not authorized.
    * @throws PSAuthenticationFailedException if the user failed to
    *    authenticate.
    * @throws IOException for any IO operation that failed.
    * @throws PSCmsException if anything goes wrong looking up existing
    *    relationships.
    * @throws PSUnknownNodeTypeException for objectstore XML parsing errors.
    */
   private int executeCloneRequest(PSRequest request,
      PSExecutionData execData)
      throws PSRequestValidationException, PSAuthorizationException,
         PSInternalRequestCallException, PSValidationException, SQLException,
         PSAuthenticationFailedException, PSNotFoundException, IOException,
         PSUnknownNodeTypeException, PSCmsException
   {
      // Extract the params
      String strContentId = null;
      String strRevisionId = null;

      strContentId = request.getParameter(m_contentIdParamName);
      strRevisionId = request.getParameter(m_revisionIdParamName);
      if (strContentId == null)
      {
         Object[] args = {m_contentIdParamName, "null"};
         throw new PSRequestValidationException(
            IPSServerErrors.CE_MODIFY_INVALID_PARAM, args);
      }
      else if (strRevisionId == null)
      {
         Object[] args = {m_revisionIdParamName, "null"};
         throw new PSRequestValidationException(
            IPSServerErrors.CE_MODIFY_INVALID_PARAM, args);
      }

      int contentId;
      int revisionId;
      try
      {
         contentId = Integer.parseInt(strContentId);
      }
      catch (NumberFormatException e)
      {
         Object[] args = {m_contentIdParamName, strContentId};
         throw new PSRequestValidationException(
            IPSServerErrors.CE_MODIFY_INVALID_PARAM, args);
      }

      try
      {
         revisionId = Integer.parseInt(strRevisionId);
      }
      catch (NumberFormatException e)
      {
         Object[] args = {m_revisionIdParamName, strRevisionId};
         throw new PSRequestValidationException(
            IPSServerErrors.CE_MODIFY_INVALID_PARAM, args);
      }

      // create new ids
      int newContentId = getNextId(NEXT_CONTENT_ID_KEY);
      int newRevisionId = 1;

      // do we need to checkin after the copy is made?
      String wfAction = request.getParameter(SYS_WFACTION);
      boolean checkin = 
         (wfAction != null && wfAction.equalsIgnoreCase(WF_ACTION_CHECKIN));

      // make the copy
      m_copyHandler.createCopy(new PSLocator(contentId, revisionId), 
         new PSLocator(newContentId, newRevisionId, false), execData, checkin);

      return newContentId;
   }

   /**
    * See {@link IPSInternalRequestHandler#makeInternalRequest(PSRequest)
    * IPSInternalRequestHandler} interface for method and parameter 
    * descriptions.
    *
    * @return <code>null</code> always.  The execution data created by this
    * request is released before returning to caller.
    */
   public PSExecutionData makeInternalRequest(PSRequest request)
      throws PSInternalRequestCallException, PSAuthorizationException,
         PSAuthenticationFailedException
   {
      if (request == null)
         throw new IllegalArgumentException("Request must be specified.");

      checkInternalRequestAuthorization(request);

      PSExecutionData execData = new PSExecutionData(m_appHandler, this,
         request);

      try
      {
         int newContentId = executeCloneRequest(request, execData);
         int newRevisionId = 1;

         // set new content and revision id params
         request.setParameter(m_contentIdParamName, String.valueOf(
            newContentId));
         request.setParameter(m_revisionIdParamName, String.valueOf(
            newRevisionId));

         // run cmd and ds post exits
         runPostProcessingExtensions(execData, null);
      }
      catch (PSException e)
      {
         throw new PSInternalRequestCallException(e.getErrorCode(),
            e.getErrorArguments());
      }
      catch (Exception e)
      {
         throw new PSInternalRequestCallException(
            IPSDataErrors.INTERNAL_REQUEST_CALL_EXCEPTION, getExceptionText(e));
      }
      finally
      {
         if (execData != null)
            execData.release();
      }

      return null;
   }

   /**
    * Copy all related content relationships for the supplied execution data.
    * 
    * @param fromCid the content id to copy the relationships from.
    * @param fromRid the revision to copy the relationships from.
    * @param toCid the content id to copy the relationships for.
    * @param toRid the revision to copy the relationships for.
    * @param data the execution data to operate on, not <code>null</code>.
    * @param ceh the content editor handler to be used, not <code>null</code>.
    * @return a map with all new created inline link relationships. The map 
    *    key is the original relationship id as <code>Integer</code>, 
    *    the map value is the new cloned relationship as 
    *    <code>PSRelationship</code>. Never <code>null</code>, may be empty.
    * @throws PSRequestValidationException for any failed request validation.
    * @throws PSAuthorizationException if the user is not authorized.
    * @throws PSInternalRequestCallException if any error occurs processing
    *    the internal request call.
    * @throws PSValidationException for any failed validation.
    * @throws SQLException for any failed SQL operation.
    * @throws PSAuthenticationFailedException if the user failed to
    *    authenticate.
    * @throws PSNotFoundException for any file not found.
    * @throws IOException for any IO error occurred.
    * @throws PSUnknownNodeTypeException if the requested document does not
    *    contain a valid relationship set.
    * @throws PSCmsException if anything goes wrong looking up existing
    *    relationships.
    */
   public static Map copyRelatedContent(int fromCid, int fromRid, 
      int toCid, int toRid, PSExecutionData data, PSContentEditorHandler ceh)
      throws PSRequestValidationException, PSAuthorizationException, 
      PSInternalRequestCallException, PSValidationException, SQLException,
      PSAuthenticationFailedException, PSNotFoundException, IOException,
      PSUnknownNodeTypeException, PSCmsException
   {
      if (data == null || ceh == null)
         throw new IllegalArgumentException("data and ceh cannot be null");
         
      PSRelationshipCommandHandler rsh = (PSRelationshipCommandHandler) 
         ceh.getCommandHandler(PSRelationshipCommandHandler.COMMAND_NAME);

      HashMap params = new HashMap();
      PSRequest request = data.getRequest().cloneRequest();
      request.setParameters(params);
      
      params.put(IPSHtmlParameters.SYS_CONTENTID, Integer.toString(fromCid));
      params.put(IPSHtmlParameters.SYS_REVISION, Integer.toString(fromRid));
      
      PSRelationshipDbProcessor processor = PSRelationshipDbProcessor.getInstance();
      
      Map inlineRelationships = new HashMap();

      //Get all relationships with this item as owner
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setOwner(new PSLocator(fromCid, fromRid));
      filter.limitToOwnerRevision(true);
      Iterator relationships = processor.getRelationships(filter).iterator();
      if (!relationships.hasNext())
         return inlineRelationships;

      params.put(IPSHtmlParameters.SYS_COMMAND, 
         PSRelationshipCommandHandler.COMMAND_NAME + "/" + 
         PSRelationshipCommandHandler.COMMAND_INSERT);
      params.put(IPSHtmlParameters.SYS_CONTENTID, Integer.toString(toCid));
      params.put(IPSHtmlParameters.SYS_REVISION, Integer.toString(toRid));

      while (relationships.hasNext())
      {
         PSRelationship relationship = (PSRelationship) relationships.next();
         HashMap tempParams = new HashMap(params);
         //skip non-cloneable relationships
         //TODO: add check based on rs_allowcloning parameter to see if it
         //really cloneable - Ram: 20031007
         if(!relationship.getConfig().isCloningAllowed())
            continue;
         
         tempParams.put(IPSHtmlParameters.SYS_DEPENDENTID, 
            Integer.toString(relationship.getDependent().getId()));
         tempParams.putAll(relationship.getUserProperties());
         tempParams.put(IPSHtmlParameters.SYS_RELATIONSHIPTYPE, 
            relationship.getConfig().getName());
         
         if (relationship.getConfig().useDependentRevision()) 
         {
            tempParams.put(IPSHtmlParameters.SYS_DEPENDENTREVISION, 
                  relationship.getDependent().getRevision());
         }

         request.setParameters(tempParams);
         rsh.processRequest(request);
         
         if (relationship.isInlineRelationship())
         {
            Integer key = new Integer(relationship.getId());
            
            PSRelationship lastClone = null;
            Iterator clones = request.getRelationships();
            while(clones.hasNext())
               lastClone = (PSRelationship) clones.next();
            
            if (lastClone != null)
               inlineRelationships.put(key, lastClone);
         }
      }
      
      return inlineRelationships;
   }

   /**
    * The internal name of this handler. When handler names are used in
    * config files, this is the name that must be used.
    */
   public static final String COMMAND_NAME = "clone";
   
   /**
    * The HTML parameter used to supply the workflow action to take after the 
    * clone has been created. Currently only 'checkin' is supported. If not
    * provided, no action will be taken.
    */
   public static final String SYS_WFACTION = "sys_wfAction";
   
   /**
    * The HTML parameter value to perform a checkin.
    */
   public static final String WF_ACTION_CHECKIN = "checkin";

   /**
    * Name of the HTML parameter to pass the map of name-value pairs for the 
    * fields in the cloned item. This map is typically built in the 
    * relatinship handler and set as the HTML parameter so that the clone or 
    * copy handler can make use of this later while creating a clone.  
    */
   public static final String SYS_CLONE_OVERRIDE_FIELDSET = 
      "sys_cloneoverridefieldset";

   /**
    * Used to perform the actual copying of the item.  Initialized in the ctor,
    * never <code>null</code> after that.
    */
   private PSCopyHandler m_copyHandler = null;

   /**
    * Stores the name of the html parameter to extract the content id from.
    * Initialized in the ctor, never <code>null</code> or changed after that.
    */
   private String m_contentIdParamName = null;

   /**
    * Stores the name of the html parameter to extract the revision id from.
    * Initialized in the ctor, never <code>null</code> or changed after that.
    */
   private String m_revisionIdParamName = null;
}
