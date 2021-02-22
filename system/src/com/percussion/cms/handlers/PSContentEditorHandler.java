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

import com.percussion.cms.IPSConstants;
import com.percussion.cms.IPSEditorChangeListener;
import com.percussion.cms.IPSRelationshipChangeListener;
import com.percussion.cms.PSApplicationBuilder;
import com.percussion.cms.PSEditorChangeEvent;
import com.percussion.cms.objectstore.PSCmsObject;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.data.IPSInternalRequestHandler;
import com.percussion.data.IPSInternalResultHandler;
import com.percussion.data.PSExecutionData;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.data.PSMimeContentResult;
import com.percussion.design.objectstore.IPSBackEndMapping;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSApplicationFlow;
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSCommandHandlerStylesheets;
import com.percussion.design.objectstore.PSConditional;
import com.percussion.design.objectstore.PSConditionalView;
import com.percussion.design.objectstore.PSContainerLocator;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSContentEditorMapper;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSContentEditorSharedDef;
import com.percussion.design.objectstore.PSContentEditorSystemDef;
import com.percussion.design.objectstore.PSDataSet;
import com.percussion.design.objectstore.PSDisplayMapper;
import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSInputTranslations;
import com.percussion.design.objectstore.PSOutputTranslations;
import com.percussion.design.objectstore.PSParam;
import com.percussion.design.objectstore.PSSharedFieldGroup;
import com.percussion.design.objectstore.PSSingleHtmlParameter;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.design.objectstore.PSUIDefinition;
import com.percussion.design.objectstore.PSUrlRequest;
import com.percussion.design.objectstore.PSValidationRules;
import com.percussion.design.objectstore.PSView;
import com.percussion.design.objectstore.PSViewSet;
import com.percussion.design.objectstore.PSWorkflowInfo;
import com.percussion.design.objectstore.server.PSServerXmlObjectStore;
import com.percussion.error.PSException;
import com.percussion.log.PSLogManager;
import com.percussion.log.PSLogServerWarning;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestHandler;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSApplicationHandler;
import com.percussion.server.PSConsole;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.server.PSServerLogHandler;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.util.PSCollection;
import com.percussion.util.PSIteratorUtils;
import com.percussion.util.PSStringOperation;
import com.percussion.util.PSUniqueObjectGenerator;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Request handler for all content editor requests.  Dispatches all requests to
 * the appropriate command handler.
 */
public class PSContentEditorHandler implements IPSRequestHandler,
   IPSInternalCommandRequestHandler, IPSInternalResultHandler
{
   /**
    * Construct a content editor handler to manage the querying for the
    * specified data set.
    * <p>
    * The steps performed during construction:
    * <ol>
    * <li>Load the Content Editor System def properties</li>
    * <li>Load the Content Editor Shared def properties</li>
    * <li>Merge all appropriate data from the System and Shared defs into the
    * dataset</li>
    * <li>Create all command handlers</li>
    * </ol>
    * @param appHandler the application handler managing this data request.
    * Never <code>null</code>.
    * @param ds the data set containing the query pipe(s) this
    * object will handle. Never <code>null</code>.
    *
    * @throws PSSystemValidationException if there are any errors
    */
   public PSContentEditorHandler(PSApplicationHandler appHandler, PSDataSet ds)
           throws PSException
   {
      if (appHandler == null)
         throw new IllegalArgumentException("appHandler may not be null");

      if (ds == null)
         throw new IllegalArgumentException("dataset may not be null");

      if ( !(ds instanceof PSContentEditor ))
         throw new IllegalArgumentException("Dataset must be a Content Editor.");

      m_appHandler = appHandler;

      m_dataSet = (PSContentEditor) ds;
      PSContentEditor ce = (PSContentEditor)ds;
      PSContentEditorPipe pipe = (PSContentEditorPipe)ce.getPipe();
      m_cmsObject = PSServer.getCmsObjectRequired(ce.getObjectType());

      // load system and shared def
      m_systemDef = PSServer.getContentEditorSystemDef();
      m_sharedDef = PSServer.getContentEditorSharedDef();
      m_sysFieldExcludes = new PSCollection(
         pipe.getMapper().getSystemFieldExcludes());
      m_sharedFieldIncludes = new PSCollection(
         pipe.getMapper().getSharedFieldIncludes());
      m_sharedFieldExcludes = new PSCollection(
         pipe.getMapper().getSharedFieldExcludes());


      /* The server may have given us null def's if there was an error
       * instantiating them.  Throw an exception, but the real problem will
       * already have been logged by the server and displayed on the console.
       */
      if (null == m_systemDef)
      {
         throw new PSSystemValidationException(IPSServerErrors.CE_SYSTEM_DEF_INVALID);
      }

      if (null == m_sharedDef)
      {
         // must have at least one group
         throw new PSSystemValidationException(IPSServerErrors.CE_SHARED_DEF_INVALID);
      }

      PSContentEditorPipe cePipe = (PSContentEditorPipe)ce.getPipe();
      PSContentEditorMapper ceMapper = cePipe.getMapper();

      /* validate that the shared includes exist (system excludes are validated
       * while merging the ce mapper), and that the shared excludes are
       * actual fields in these groups.
       */
      Iterator warnings = ceMapper.validateSharedGroups( m_sharedDef );

      // Send warnings to console and log
      while(warnings.hasNext())
      {
         PSSystemValidationException e = (PSSystemValidationException)warnings.next();
         PSConsole.printMsg(SUBSYSTEM_NAME, e);
      }

      /* now that they are ok, need to uppercase the list of shared group names
       * so that they can be compared case insensitive
       */
      for (int i = 0; i < m_sharedFieldIncludes.size(); i++)
      {
         String groupName = (String)m_sharedFieldIncludes.get(i);
         m_sharedFieldIncludes.set(i, groupName.toUpperCase());
      }

      // validate all specified workflow info
      validateWorkflow(m_dataSet);

      // promote common values from the defs to the dataset including
      // fields, pre and post exits, stylesheet, and app flow
      promoteTableSets(ce);

      //promote fieldsets and uidefs
      cePipe.setMapper(
         cePipe.getMapper().getMergedMapper(m_systemDef, m_sharedDef, true) );

      /*make sure that the combined list of shared fields defined by all
      the INCLUDED SHARED groups DOESN'T have a duplicate field name. This
      rule is enforced in order to be able to correctly disambiguate an origin
      of a shared field. If the ambiguity is detected than this method throws
      PSMinorValidationException, which will cause server not to start such CE.
      */
      cePipe.getMapper().validateSharedFieldDuplication(m_sharedDef,
         m_sharedFieldIncludes.iterator());

      promoteInputTranslations(ce);
      promoteOutputTranslations(ce);
      promoteValidations(ce);
      promoteStyleSheets(ce);
      promoteAppFlow(ce);
      promoteSectionLinkList(ce);
      promoteViewSet(ce);

      //TODO: call the method to resolve alias' etc. on the datasets locator
      // passing in the app's credentials


      // create the application all command handlers will use
      PSApplication myApp = appHandler.getApplicationDefinition();
      String myAppName = PSUniqueObjectGenerator.makeUniqueName(
         ".sys_CEHandler");

      // TODO: Pass backend credential collection once the resolveAliases
      // method is ready on the PSContainerLocator
      m_app = PSApplicationBuilder.createApplication(myAppName,
         myApp);

      // fix up all PSBackEndTable refs so it doesn't have to be done by each
      // handler
      pipe.getMapper().getFieldSet().fixupBackEndColumns(
            pipe.getLocator().getBackEndTables());

      /* validate that all fields have a valid locator, as it is optional to
       * allow easy overridding of field definitions, but is required in each
       * field contained in the content editor dataset that we pass to the
       * handlers.  Also validate that there are no duplicate field, fieldset, 
       * or column names.
       */
      validateFields(pipe.getMapper().getFieldSet(), null, null);

      // adds/validates properties such as data type and mime type
      try
      {
         PSServerXmlObjectStore.getInstance().fixupFields(
               pipe.getMapper().getFieldSet(),
               pipe.getLocator().getTableSets(),
               pipe.getLocator().getBackEndTables());
      }
      catch (SQLException se)
      {
         throw new PSException(se);
      }

      //todo: debug, add trace here
      //TODO: debug - need way to optionally turn this on (command line param?)

      try(FileOutputStream os = new FileOutputStream(new File(PSServer.getRxDir(),
      "mergedEditor.xml")))
      {
         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         PSXmlDocumentBuilder.createRoot(doc, "root" ).appendChild( ce.toXml(doc));
         PSXmlDocumentBuilder.write( doc, os);
      }
      catch ( Exception e )
      {
         PSConsole.printMsg(SUBSYSTEM_NAME, e);
      }
      // end debug

      // create command handlers and init them
      m_commandHandlers = new HashMap();

      // TODO: Pass handlers the backEnd credentials retrieved from the call
      // to the locators resolve refs
      boolean abnormalExit = true;
      try
      {
         PSEditCommandHandler eh =
            new PSEditCommandHandler( appHandler, this, m_dataSet, m_app );
         m_commandHandlers.put( PSEditCommandHandler.COMMAND_NAME, eh );
         m_commandHandlers.put( PSPreviewCommandHandler.COMMAND_NAME,
            new PSPreviewCommandHandler( appHandler, this, m_dataSet, m_app,
            eh ));
         m_commandHandlers.put( PSModifyCommandHandler.COMMAND_NAME,
            new PSModifyCommandHandler( appHandler, this, m_dataSet, m_app ));
         m_commandHandlers.put( PSWorkflowCommandHandler.COMMAND_NAME,
            new PSWorkflowCommandHandler( appHandler, this, m_dataSet, m_app ));
         m_commandHandlers.put( PSBinaryCommandHandler.COMMAND_NAME,
            new PSBinaryCommandHandler( appHandler, this, m_dataSet, m_app ));
         m_commandHandlers.put( PSCloneCommandHandler.COMMAND_NAME,
            new PSCloneCommandHandler( appHandler, this, m_dataSet, m_app ));
         m_commandHandlers.put( PSRelationshipCommandHandler.COMMAND_NAME,
            new PSRelationshipCommandHandler( appHandler, this, m_dataSet, m_app ));
         m_commandHandlers.put( PSSearchCommandHandler.COMMAND_NAME,
            new PSSearchCommandHandler( appHandler, this, m_dataSet, m_app ));

         //TODO: debug - need way to optionally turn this on (command line param?)
         PSApplicationBuilder.write(m_app);

         //TODO: end debug

         // TODO: figure out why this is needed.
         try
         {
            /* I don't understand why, but when there are joins in the app,
               (this is true for other things as well) if
               we don't do this, then the app doesn't work at run time. Apparently,
               when the ObjStore loads an app, it does something that doesn't get
               done when building the app. I don't have time to look into it
               right now. */
            m_app = new PSApplication( m_app.toXml());
         }
         catch ( Exception e )
         {
            throw new RuntimeException( e.getLocalizedMessage());
         }

         // start the app
         m_contentTypeid = PSItemDefManager.getInstance().registerDef(
            appHandler.getName(), m_dataSet);

         m_internalAppHandler = PSServer.startApplication(m_app);
         abnormalExit = false;
      }
      catch (PSException e)
      {
         throw new PSSystemValidationException(e.getErrorCode(),
            e.getErrorArguments());
      }
      finally
      {
         if (abnormalExit)
            shutdown();
      }

   }

   /**
    * Get the cms object for the content editor of this handler.
    *
    * @return the cms object, never <code>null</code>.
    */
   public PSCmsObject getCmsObject()
   {
      return m_cmsObject;
   }

   /**
    * Returns the system def.
    * @return The system def object, never <code>null</code>.
    */
   public PSContentEditorSystemDef getSystemDef()
   {
      return m_systemDef;
   }

   /**
    * Get the command handler for the provided command name.
    *
    * @param name the command handler name we are looking for, might be
    *    <code>null</code> or empty.
    * @return the command handler for the provided name or <code>null</code>
    *    if not found.
    */
   public PSCommandHandler getCommandHandler(String name)
   {
      return (PSCommandHandler)lookupCommandHandler(name);
   }

   /**
    * Get the content editor name.
    *
    * @return the name of this content editor, never <code>null</code> or
    *    empty.
    */
   public String getName()
   {
      return m_appHandler.getName();
   }

   /**
    * Returns the shared def.
    * @return The shared def object, never <code>null</code>.
    */
   public PSContentEditorSharedDef getSharedDef()
   {
      return m_sharedDef;
   }

   /**
    * Returns this handler's dataset.
    *
    * @return The dataset, never <code>null</code>.
    */
   public PSContentEditor getContentEditor()
   {
      return m_dataSet;
   }

   /**
    * Returns this handler's internal application handler
    *
    * @return The internal application handler, never <code>null</code>.
    */
   public PSApplicationHandler getInternalAppHandler()
   {
      return m_internalAppHandler;
   }

   /**
    * Returns a param name, given the internal name.
    *
    * @param internalName The key used to locate the param name.  May not be
    * <code>null</code> or empty.
    *
    * @return The param name if defined, or else the internalName.
    */
   public String getParamName(String internalName)
   {
      if (internalName == null || internalName.trim().length() == 0)
         throw new IllegalArgumentException(
            "internalName may not be null or empty");

      String paramName = (String)m_systemDef.getParamNames().get(internalName);
      if (paramName == null)
         paramName = internalName;

      return paramName;
   }

   /**
    * Returns the InitParams for the specified command handler name.
    *
    * @param cmdName The command name, may not be <code>null</code>.
    *
    * @return An Iterator over <code>zero</code> or more PSParam objects
    */
   public Iterator getInitParams(String cmdName)
   {
      if (cmdName == null)
         throw new IllegalArgumentException("cmdName may not be null");

      Iterator params = null;
      List paramList = (List)m_systemDef.getInitParams().get(cmdName);
      if (paramList != null)
         params = paramList.iterator();
      else
         params = PSIteratorUtils.emptyIterator();

      return params;
   }

   /**
    * Returns the specified init param value for the specified command handler
    * name.
    *
    * @param cmdName The command name, may not be <code>null</code>.
    * @param paramName The init param name, may not be <code>null</code>.
    *
    * @return The param value, or <code>null</code> if not found.
    */
   public IPSReplacementValue getInitParam(String cmdName, String paramName)
   {
      if (cmdName == null || cmdName.trim().length() == 0)
         throw new IllegalArgumentException("cmdName may not be null or empty");

      if (paramName == null || paramName.trim().length() == 0)
         throw new IllegalArgumentException(
            "paramName may not be null or empty");

      IPSReplacementValue value = null;

      Iterator params = getInitParams(cmdName);
      while (params.hasNext())
      {
         PSParam param = (PSParam)params.next();
         if (param.getName().equals(paramName))
         {
            value = param.getValue();
            break;
         }
      }

      return value;
   }

   /* ************ IPSRequestHandler Interface Implementation ************ */

   /**
    * Process a content editor request using the input context information and
    * data.
    * <p>
    * The following steps are performed to handle the request:
    * <ol>
    * <li>Determine the command handler to forward to by checking the request
    * param.</li>
    * <li>Forward the request to that handler.</li>
    * </ol>
    *
    * @param request the request object containing all context data associated
    * with the request.
    */
   public void processRequest(PSRequest request)
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");

      // Get the command parameter value from the request
      String commandParam = request.getParameter(
         getParamName(COMMAND_PARAM_NAME));

      if ( null == commandParam || commandParam.trim().length() == 0 )
         commandParam = PSEditCommandHandler.COMMAND_NAME;

      IPSRequestHandler rh = lookupCommandHandler(commandParam);

      // if we have a handler, delegate the reqeust processing to it
      if (rh != null)
         rh.processRequest(request);
      else
      {
         // have the server log the error and return an appropriate response
         PSServerLogHandler.handleDataSetHandlerNotFound(
            request, m_appHandler.getId(), m_appHandler.getName(),
               m_dataSet.getName(),
                  (commandParam == null ? "null" : commandParam));
      }

   }

   /**
    * Get the content editor request handler for the supplied request.
    *
    * @param request the request to get the handler for, assumed not
    *    <code>null</code>.
    * @return IPSRequestHandler the request handler or <code>null</code> if
    *    not found. Will be one of IPSInternalRequestHandler or
    *    IPSInternalResultHandler.
    */
   private IPSRequestHandler getRequestHandler(PSRequest request)
   {
      // Get the command parameter value from the request
      String commandParam = request.getParameter(
         getParamName(COMMAND_PARAM_NAME));

      return lookupCommandHandler(commandParam);
   }

   // see IPSMakeInternalRequest for documentation
   public PSExecutionData makeInternalRequest(PSRequest request)
      throws PSInternalRequestCallException, PSAuthorizationException,
      PSAuthenticationFailedException
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");

      IPSRequestHandler rh = getRequestHandler(request);

      // if we have a handler, delegate the reqeust processing to it
      PSExecutionData data = null;
      if (rh instanceof IPSInternalRequestHandler)
         data = ((IPSInternalRequestHandler) rh).makeInternalRequest(request);
      else
      {
         // have the server log the error and return an appropriate response
         String commandParam = request.getParameter(
            getParamName(COMMAND_PARAM_NAME));
         PSServerLogHandler.handleDataSetHandlerNotFound(
            request, m_appHandler.getId(), m_appHandler.getName(),
            m_dataSet.getName(), (commandParam == null ? "null" : commandParam));
      }

      return data;
   }

   // See IPSInternalResultHandler interface for description
   public ResultSet getResultSet(PSExecutionData data)
      throws PSInternalRequestCallException
   {
      if (data == null)
         throw new IllegalArgumentException("data may not be null");

      IPSRequestHandler rh = getRequestHandler(data.getRequest());

      // if we have a handler, delegate the reqeust processing to it
      ResultSet rs = null;
      if (rh instanceof IPSInternalResultHandler)
         rs = ((IPSInternalResultHandler) rh).getResultSet(data);
      else
      {
         throw new UnsupportedOperationException(
            "getResultSet not supported");
      }

      return rs;
   }


   // See IPSInternalResultHandler interface for description
   public Document getResultDocument(PSRequest request)
      throws PSInternalRequestCallException, PSAuthorizationException,
      PSAuthenticationFailedException
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");

      IPSRequestHandler rh = getRequestHandler(request);
      if (rh instanceof IPSInternalRequestHandler)
         return ((IPSInternalRequestHandler)rh).getResultDocument( request );
      else
         throw new UnsupportedOperationException( "getResultDocument");
   }


   // see IPSInternalResultHandler interface for description
   public ByteArrayOutputStream getMergedResult(PSExecutionData data)
      throws PSInternalRequestCallException, PSAuthorizationException,
      PSAuthenticationFailedException
   {
      if (data == null)
         throw new IllegalArgumentException("Execution data may not be null");

      IPSRequestHandler rh = getRequestHandler(data.getRequest());
      if (rh instanceof IPSInternalResultHandler)
      {
         return ((IPSInternalResultHandler)rh).getMergedResult(data);
      }
      else
         throw new UnsupportedOperationException( "getMergedResult");
   }

   // See IPSInternalResultHandler interface for description
   public Document getResultDoc(PSExecutionData data)
      throws PSInternalRequestCallException
   {
      if (data == null)
         throw new IllegalArgumentException("data may not be null");

      IPSRequestHandler rh = getRequestHandler(data.getRequest());

      // if we have a handler, delegate the reqeust processing to it
      Document doc = null;
      if (rh instanceof IPSInternalResultHandler)
         doc = ((IPSInternalResultHandler) rh).getResultDoc(data);
      else
      {
         throw new UnsupportedOperationException(
            "getResultDoc not supported");
      }

      return doc;
   }
   
   public boolean isBinary(PSRequest req)
   {
      if (req == null)
      {
         throw new IllegalArgumentException("req may not be null");
      }
      IPSRequestHandler rh = getRequestHandler(req);

      if (rh instanceof IPSInternalResultHandler)
         return ((IPSInternalResultHandler) rh).isBinary(req);
      else
         return false;
   }

   // See IPSInternalResultHandler interface for description
   public PSMimeContentResult getMimeContent(PSExecutionData data,
      boolean setResponse) throws PSInternalRequestCallException
   {
      if (data == null)
         throw new IllegalArgumentException("data may not be null");

      IPSRequestHandler rh = getRequestHandler(data.getRequest());

      // if we have a handler, delegate the reqeust processing to it
      PSMimeContentResult mcs = null;
      if (rh instanceof IPSInternalResultHandler)
         mcs = ((IPSInternalResultHandler) rh).getMimeContent(data, setResponse);
      else
      {
         throw new UnsupportedOperationException(
            "getMimeContent not supported");
      }

      return mcs;
   }

   /**
    * Returns <code>IPSInternalRequest.REQUEST_TYPE_CONTENT_EDITOR</code>.
    *
    * see {@link com.percussion.data.IPSInternalRequestHandler#getRequestType()}
    * for details.
    */
   public int getRequestType()
   {
      return IPSInternalRequest.REQUEST_TYPE_CONTENT_EDITOR;
   }


   // see IPSInternalCommandRequestHandler for documentation
   public Document makeInternalRequest(PSRequest request, String command)
   {
      try
      {
         if (request == null || command == null)
            throw new IllegalArgumentException(
               "request and command cannot be null");

         // create a copy of the request and add the command parameter
         PSRequest clonedReq = request.cloneRequest();
         clonedReq.setParameter( COMMAND_PARAM_NAME, command );

         IPSRequestHandler rh = lookupCommandHandler(command);

         if (rh == null || !(rh instanceof IPSInternalCommandRequestHandlerEx))
         {
            // have the server log the error and return an appropriate response
            PSServerLogHandler.handleDataSetHandlerNotFound(
               clonedReq, m_appHandler.getId(), m_appHandler.getName(),
                  m_dataSet.getName(),
                     (command == null ? "null" : command));
         }

         Document doc =
            ((IPSInternalCommandRequestHandlerEx) rh).makeInternalRequestEx(
               clonedReq);

         return doc;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e.getLocalizedMessage());
      }
   }

   /**
    * Shutdown the request handler, freeing any associated resources.  Also need
    * to shutdown all commandHandlers.
    */
   public void shutdown()
   {
      try
      {
         PSItemDefManager.getInstance().unRegisterDef(m_dataSet);
         // walk commandhandlers and shut them down
         Iterator i = m_commandHandlers.values().iterator();
         while (i.hasNext())
            ((IPSRequestHandler)i.next()).shutdown();

         m_commandHandlers = null;

         // shutdown and dispose of the app
         // Server may have already shut down the app
         if (PSServer.isApplicationActive(m_app.getName()))
            PSServer.shutdownApplication(m_app.getName());
         File appDir = new File(PSServer.getRxDir(), m_app.getRequestRoot());
         if ( appDir.exists() && appDir.isDirectory())
            deleteDirectory(appDir);

         m_app = null;
         m_appHandler = null;
         m_dataSet = null;
         m_systemDef = null;
         m_sharedDef = null;
      }
      catch (Throwable t)
      {
         PSConsole.printMsg(SUBSYSTEM_NAME, t);
      }
   }

   /**
    * Registers the supplied listener for editor change events.  Listener will
    * be notified of any modify or workflow requests.
    *
    * @param listener The listener to notify, may not be <code>null</code>.
    */
   public void addEditorChangeListener(IPSEditorChangeListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("listener may not be null");

      // add to modify and workflow command handlers
      ((PSCommandHandler)m_commandHandlers.get(
         PSModifyCommandHandler.COMMAND_NAME)).addEditorChangeListener(
            listener);

      ((PSCommandHandler)m_commandHandlers.get(
         PSWorkflowCommandHandler.COMMAND_NAME)).addEditorChangeListener(
            listener);
      
      m_changeListeners.add(listener);
   }

   
   /**
    * The will trigger a delete notification for this content editor.  
    * This is to be used when the regular modify handler is not being used
    * to purge the content and is a work around to make sure the correct listeners
    * are notified.  usually this will notify PSSearchIndexEventQueue, PSAssemblerCacheHandler
    * and PSContentRepository.  These classes listen to any content editors starting up 
    * this class and
    * add themselves to the listener list of each.  This whole notification structure could
    * be simplified and decoupled.
    * 
    * be notified of any modify or workflow requests.
    *
    * @param contentId The content id to notify for</code>.
    */
   public void notifyPurge(int contentId) throws PSSystemValidationException, PSValidationException {
        for (IPSEditorChangeListener listener : m_changeListeners)
        {
            PSEditorChangeEvent e = new PSEditorChangeEvent(PSEditorChangeEvent.ACTION_DELETE, contentId, -1, -1, -1,
                    m_contentTypeid);

            listener.editorChanged(e);
        }
   }
   
   /**
    * Get the id of the content type for which this editor is used.
    *  
    * @return The content type id.
    */
   public long getContentTypeId()
   {
      return m_contentTypeid;
   }

   /**
    * Registers the supplied listener for relationship change events.  Listener
    * will be notified of any add, remove or modify relationship events.
    *
    * @param listener The listener to notify, may not be <code>null</code>.
    *
    * @throws IllegalArgumentException if listener is <code>null</code>.
    */
   public void addRelationshipChangeListener(IPSRelationshipChangeListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("listener may not be null");

      // add to relationship command handler
      ((PSRelationshipCommandHandler)m_commandHandlers.get(
         PSRelationshipCommandHandler.COMMAND_NAME)).addRelationshipChangeListener(
            listener);
   }

   /**
    * Recursively validates that all fields and fieldSets in the supplied
    * fieldset are valid. Currently that means they must have a locator, and
    * that there are no duplicate names among the names used by all fields and
    * fieldsets combined, and no duplicate backend column names used as well.
    *
    * @param fieldSet The fieldset to validate.  May not be <code>null</code>.
    * @param fieldNames The names of any fields or fieldsets that have already
    * been validated.  The first call to this method should pass
    * <code>null</code>.
    * @param colNames Map of names of any backend columns of fields that have 
    * already been validated.  The first call to this method should pass
    * <code>null</code>. Key is the column name as a <code>String</code>, value
    * is a set of the field names using that back end column.
    *
    * @throws PSSystemValidationException if the fieldSet or any of it's fields or
    * fieldSets are invalid
    */
   private void validateFields(PSFieldSet fieldSet, Set fieldNames, 
      Map colNames) throws PSSystemValidationException
   {
      if (fieldSet == null)
         throw new IllegalArgumentException("fieldSet may not be null");

      if (fieldNames == null)
         fieldNames = new HashSet();

      boolean executeColNameWarning = (colNames == null);
      if (executeColNameWarning)
         colNames = new HashMap();
      
      String checkFieldName = fieldSet.getName().toLowerCase();
      if (fieldNames.contains(checkFieldName))
         handleNonUniqueFieldName(fieldSet.getName());
      fieldNames.add(checkFieldName);
      
      Iterator fields = fieldSet.getAll();
      while(fields.hasNext())
      {
         Object o = fields.next();
         if (o instanceof PSFieldSet)
         {
            PSFieldSet childFieldSet = (PSFieldSet)o;
            // validate dupe colnames in complex children w/out regard for 
            // parent fields
            if (childFieldSet.getType() == PSFieldSet.TYPE_COMPLEX_CHILD)               
               validateFields(childFieldSet, fieldNames, null);
            else
               validateFields(childFieldSet, fieldNames, colNames);
         }
         else if (o instanceof PSField)
         {
            PSField field = (PSField)o;
            checkFieldName = field.getSubmitName().toLowerCase();
            if (fieldNames.contains(checkFieldName))
               handleNonUniqueFieldName(checkFieldName);
            fieldNames.add(checkFieldName);

            IPSBackEndMapping locator = field.getLocator();
            if (locator == null)
            {
               // throw validation error!
               throw new PSSystemValidationException(
                  IPSObjectStoreErrors.CE_MISSING_FIELD_ELEMENT,
                  new Object[] {field.getSubmitName(),
                  PSField.DATA_LOCATOR_ELEM });
            }
            
            if (locator instanceof PSBackEndColumn)
            {
               PSBackEndColumn col = (PSBackEndColumn)locator;
               String checkColName = col.getColumn();
               // use a set here, as dupe field names with dupe backend cols
               // will be handled by the dupe field name validation above, so
               // here we only care about different fields using the same
               // backend column name.
               Set colFieldNames = (Set) colNames.get(checkColName);
               if (colFieldNames == null)
               {
                  colFieldNames = new HashSet();
                  colNames.put(checkColName, colFieldNames);                  
               }
               colFieldNames.add(checkFieldName);
            }
         }
      }
      
      // now check for any dupe col names discovered
      Iterator entries = colNames.entrySet().iterator();
      while (entries.hasNext())
      {
         Map.Entry entry = (Map.Entry) entries.next();
         Set colFieldNames = (Set) entry.getValue();
         if (colFieldNames.size() > 1)
         {
            handleNonUniqueColName(new ArrayList(colFieldNames), 
               (String) entry.getKey());
         }
      }
   }

   /**
    * Handles a unique field name violation.  If server requires unique names,
    * then an exception is thrown, otherwise a warning is written to the console
    * and log
    *
    * @param fieldName The duplicated name.  Assumed not <code>null</code> or
    * empty.
    *
    * @throws PSSystemValidationException if the server requires unique field names.
    */
   private void handleNonUniqueFieldName(String fieldName)
      throws PSSystemValidationException
   {
      Object[] args = {fieldName, m_dataSet.getName(), m_appHandler.getName()};
      if (PSServer.requireUniqueFieldNames())
      {
         // throw error
         throw new PSSystemValidationException(
            IPSServerErrors.CE_DUPLICATE_FIELD_NAME_ERROR, args);
      }
      else
      {
         // just write a warning to console and log
         PSLogManager.write(new PSLogServerWarning(
            IPSServerErrors.CE_DUPLICATE_FIELD_NAME_WARNING, args,
            true, SUBSYSTEM_NAME));
      }
   }
   
   /**
    * Handles non-unique backend column names.  Prints a warning to the console
    * and log if multiple fields use the backend column name, even if from 
    * different tables, as this will cause problems when editing the item.
    * 
    * @param fieldNames A list of field names using the same backend column 
    * as <code>String</code> objects, assumed not <code>null</code> or empty.
    * @param colName The name of the backend column, assumed not 
    * <code>null</code> or empty.
    */
   private void handleNonUniqueColName(List fieldNames, String colName)
   {
      // just write a warning to console and log
      String fields = PSStringOperation.append(fieldNames, ", ");
      Object[] args = {colName, fields, m_dataSet.getName(), 
         m_appHandler.getName()};
      PSLogManager.write(new PSLogServerWarning(
         IPSServerErrors.CE_DUPLICATE_COL_NAME_WARNING, args,
         true, SUBSYSTEM_NAME));      
   }

   /**
    * This validates two parts of the content editor. If a workflowInfo was
    * provided, this checks that the specified default workflow IS NOT in
    * the exclusion list or IS in the inclusion list to pass this
    * validation.
    * TODO: check that all used workflows really exist. Therfore we can make an
    * internal request to the sys_wfLookups application. But therefore we must
    * implement a dependendy list first to tell the server in which order the
    * applications must be started.
    *
    * @param ce the content editor to validate the workflow information for,
    *    assumed not <code>null</code>.
    * @throws PSSystemValidationException if any validation fails.
    */
   private void validateWorkflow(PSContentEditor ce)
      throws PSSystemValidationException
   {
      int defaultWfId = ce.getWorkflowId();
      PSWorkflowInfo wfInfo = ce.getWorkflowInfo();
      if (wfInfo != null)
      {
         Iterator ids = wfInfo.getValues();
         if (wfInfo.isExclusionary())
         {
            // make sure the default IS NOT in the excluded list
            while (ids.hasNext())
            {
               int id = ((Integer) ids.next()).intValue();
               if (id == defaultWfId)
                  throw new PSSystemValidationException(
                     IPSServerErrors.CE_DEFAULT_WF_EXCLUDED,
                        Integer.toString(id));
            }
         }
         else
         {
            // make sure the default IS in the included list
            int id = -1;
            boolean found = false;
            while (!found && ids.hasNext())
            {
               id = ((Integer) ids.next()).intValue();
               found = (id == defaultWfId);
            }
            if (!found)
               throw new PSSystemValidationException(
                  IPSServerErrors.CE_DEFAULT_WF_NOT_INLUDED,
                     Integer.toString(id));
         }
      }
   }

   /**
    * Recursively deletes all contents of a directory and then the directory
    * itself.
    *
    * @param dir The directory.  Must be a directory, may not be
    * <code>null</code>.
    */
   private void deleteDirectory(File dir)
   {
      if (dir == null || !dir.isDirectory())
         throw new IllegalArgumentException("dir must be a valid directory");

      File[] files = dir.listFiles();
      for (int i = 0; i < files.length; i++)
      {
         if (files[i].isDirectory())
            deleteDirectory(files[i]);
         else
            files[i].delete();
      }

      dir.delete();
   }

   /**
    * Adds all tablesets from the system and shared defs to the contentEditor.
    * @param contentEditor The definition of the editor.  May not be
    * <code>null</code>.
    * @throws PSSystemValidationException if there are any errors.
    */
   private void promoteTableSets(PSContentEditor contentEditor)
      throws PSSystemValidationException
   {
      if (contentEditor == null)
         throw new IllegalArgumentException("contentEditor may not be null");

      PSContentEditorPipe pipe = (PSContentEditorPipe)contentEditor.getPipe();
      PSContainerLocator pipeLocator = pipe.getLocator();
      
      pipeLocator.mergeTableSets(m_systemDef, m_sharedDef, 
         m_sharedFieldIncludes);
   }


   /**
    * Adds all item validation conditional exits from the system and shared def
    * to the contentEditor, adding system first, followed by any from the
    * shared groups included.
    * @param contentEditor The Content Editor.  May not be
    * <code>null</code>.
    */
   private void promoteValidations(PSContentEditor contentEditor)
   {
      if (contentEditor == null)
         throw new IllegalArgumentException("contentEditor may not be null");

      PSValidationRules rules = new PSValidationRules();
      Iterator ruleWalker;

      // Add system validations
      ruleWalker = m_systemDef.getValidationRules();
      while (ruleWalker.hasNext())
      {
         rules.add(ruleWalker.next());
      }

      // add validations from any shared groups included
      if (m_sharedDef != null)
      {
         Iterator groups = m_sharedDef.getFieldGroups();
         while (groups.hasNext())
         {
            PSSharedFieldGroup group = (PSSharedFieldGroup)groups.next();
            if (isIncludedSharedGroup(group))
            {
               ruleWalker = group.getValidationRules();
               while (ruleWalker.hasNext())
               {
                  rules.add(ruleWalker.next());
               }
            }
         }
      }

      // add the validations from the content editor
      ruleWalker = contentEditor.getValidationRules();
      while (ruleWalker.hasNext())
      {
         rules.add(ruleWalker.next());
      }

      // add them all back
      contentEditor.setValidationRules(rules);
   }

   /**
    * Adds all item input translation conditional exits from the system and
    * shared def to the contentEditor, adding system first, followed by any from
    * the shared groups included.
    * @param contentEditor The Content Editor.  May not be
    * <code>null</code>.
    */
   private void promoteInputTranslations(PSContentEditor contentEditor)
   {
      if (contentEditor == null)
         throw new IllegalArgumentException("contentEditor may not be null");

      PSInputTranslations translations = new PSInputTranslations();
      Iterator translationWalker;

      // Add system translations
      translationWalker = m_systemDef.getInputTranslations();
      while (translationWalker.hasNext())
      {
         translations.add(translationWalker.next());
      }

      // add translations from any shared groups included
      if (m_sharedDef != null)
      {

         Iterator groups = m_sharedDef.getFieldGroups();
         while (groups.hasNext())
         {
            PSSharedFieldGroup group = (PSSharedFieldGroup)groups.next();
            if (isIncludedSharedGroup(group))
            {
               translationWalker = group.getInputTranslations();
               while (translationWalker.hasNext())
               {
                  translations.add(translationWalker.next());
               }
            }
         }
      }

      // add the translations from the content editor
      translationWalker = contentEditor.getInputTranslations();
      while (translationWalker.hasNext())
      {
         translations.add(translationWalker.next());
      }

      // add them all back
      contentEditor.setInputTranslation(translations);
   }

   /**
    * Adds all item input translation conditionalexits from the system and
    * shared def to the contentEditor, adding system first, followed by any from
    * the shared groups inlcuded.
    * @param contentEditor The Content Editor.  May not be
    * <code>null</code>.
    */
   private void promoteOutputTranslations(PSContentEditor contentEditor)
   {
      if (contentEditor == null)
         throw new IllegalArgumentException("contentEditor may not be null");

      PSOutputTranslations translations = null;
      translations = new PSOutputTranslations();
      Iterator translationWalker;

      // Add system translations
      translationWalker = m_systemDef.getOutputTranslations();
      while (translationWalker.hasNext())
      {
         translations.add(translationWalker.next());
      }

      // add translations from any shared groups included
      if (m_sharedDef != null)
      {

         Iterator groups = m_sharedDef.getFieldGroups();
         while (groups.hasNext())
         {
            PSSharedFieldGroup group = (PSSharedFieldGroup)groups.next();
            if (isIncludedSharedGroup(group))
            {
               translationWalker = group.getOutputTranslations();
               while (translationWalker.hasNext())
               {
                  translations.add(translationWalker.next());
               }
            }
         }
      }

      // add the translations from the content editor
      translationWalker = contentEditor.getOutputTranslations();
      while (translationWalker.hasNext())
      {
         translations.add(translationWalker.next());
      }

      // add them all back
      contentEditor.setOutputTranslation(translations);
   }


   /**
    * Adds stylesheet items from the shared and system defs.  The following logic
    * is performed:
    * <ol>
    * <li>Add all stylesheets from the shared def StylesheetSet whose
    * commandName is not found in the content editors stylesheet.</li>
    * <li>Add all redirects from the system def stylesheet whose commandName is
    * not found in the content editor's stylesheet (that has the shared
    * stylesheets already filled in).</li>
    * </ol>
    *
    * @param contentEditor The Content Editor pipe.  May not be
    * <code>null</code>.
    * @throws PSSystemValidationException if there are any errors
    */
   private void promoteStyleSheets(PSContentEditor contentEditor)
      throws PSSystemValidationException
   {
      if (contentEditor == null)
         throw new IllegalArgumentException("contentEditor may not be null");

      // get the editor's stylesheet set if any
      PSCommandHandlerStylesheets ceStyleSheets =
         contentEditor.getStylesheetSet();

      // get shared and system def's stylesheets if any
      PSCommandHandlerStylesheets sysStyleSheets =
         m_systemDef.getStyleSheetSet();
      PSCommandHandlerStylesheets sharedStyleSheets =
         (m_sharedDef == null ? null : m_sharedDef.getStylesheetSet());


      if (ceStyleSheets == null)
      {
         // in this case just add the shared if exists, otherwise the sys
         if (sharedStyleSheets != null)
         {
            // add the shared stylesheetset and merge in the sys stylesheets
            ceStyleSheets = new PSCommandHandlerStylesheets(
               sharedStyleSheets);
            mergeStyleSheets(sysStyleSheets, ceStyleSheets);
         }
         else
         {
            // add the sys stylesheets
            ceStyleSheets = new PSCommandHandlerStylesheets(sysStyleSheets);
         }
      }
      else
      {
         if (sharedStyleSheets != null)
         {
            mergeStyleSheets(sharedStyleSheets, ceStyleSheets);
         }
         mergeStyleSheets(sysStyleSheets, ceStyleSheets);
      }

      contentEditor.setStylesheetSet(ceStyleSheets);

   }

   /**
    * Adds appflow items from the shared and system defs.  The following logic
    * is performed:
    * <ol>
    * <li>Add all redirects from the shared def appflow whose commandName is
    * not found in the content editors appflow.</li>
    * <li>Add all redirects from the system def appflow whose commandName is
    * not found in the content editors appflow (that has the shared redirects
    * already filled in).</li>
    * </ol>
    *
    * @param contentEditor The Content Editor pipe.  May not be
    * <code>null</code>.
    * @throws PSSystemValidationException if there are any errors
    */
   private void promoteAppFlow(PSContentEditor contentEditor)
      throws PSSystemValidationException
   {
      if (contentEditor == null)
         throw new IllegalArgumentException("contentEditor may not be null");

      // get editor's appflow if any
      PSApplicationFlow ceAppFlow = contentEditor.getApplicationFlow();

      // get shared and system def's appflow if any
      PSApplicationFlow sysAppFlow = m_systemDef.getApplicationFlow();
      PSApplicationFlow sharedAppFlow =
         (m_sharedDef == null ? null : m_sharedDef.getApplicationFlow());


      if (ceAppFlow == null)
      {
         // in this case just add the shared if exists, otherwise the sys
         if (sharedAppFlow != null)
         {
            // add the shared appflow and merge in the sys appflow
            ceAppFlow = new PSApplicationFlow(sharedAppFlow);
            mergeAppFlow(sysAppFlow, sharedAppFlow);
         }
         else
         {
            // add the sys appflow
            ceAppFlow = new PSApplicationFlow(sysAppFlow);
         }
      }
      else
      {
         if (sharedAppFlow != null)
         {
            mergeAppFlow(sharedAppFlow, ceAppFlow);
         }
         mergeAppFlow(sysAppFlow, ceAppFlow);
      }

      contentEditor.setApplicationFlow(ceAppFlow);
   }



   /**
    * Adds SectionLinkList items from the system def.  If the same name exists
    * in multiple defs, the priority will be local is highest, then shared (once
    * it is supported), then system.  Names are compared case insensitively.
    *
    * @param contentEditor The Content Editor pipe.  May not be
    * <code>null</code>.
    * @throws PSSystemValidationException if there are any errors
    */
   private void promoteSectionLinkList(PSContentEditor contentEditor)
      throws PSSystemValidationException
   {
      if (contentEditor == null)
         throw new IllegalArgumentException("contentEditor may not be null");

      /* build a collection from the local def, and build list of uppercased
       * names as well
       */
      Iterator localLinkList = contentEditor.getSectionLinkList();
      PSCollection localLinks = new PSCollection(PSUrlRequest.class);
      List localLinkNames = new ArrayList();

      while (localLinkList.hasNext())
      {
         PSUrlRequest link = (PSUrlRequest)localLinkList.next();
         localLinks.add(link);
         localLinkNames.add(link.getName().toUpperCase());
      }

      // walk sys def links and add only if no name conflict
      Iterator sysLinks = m_systemDef.getSectionLinkList();
      while (sysLinks.hasNext())
      {
         PSUrlRequest sysLink = (PSUrlRequest)sysLinks.next();
         if (localLinkNames.contains(sysLink.getName().toUpperCase()))
            continue;
         localLinks.add(sysLink);
      }

      // set composite collection back on the content editor
      contentEditor.setSectionLinkList(localLinks);
   }



   /**
    * Creates the system views and sets them on the supplied
    * <code>contentEditor</code>.  This method assumes that the content editor
    * mapper is already merged with system and shared definition.
    *
    * @param contentEditor The Content Editor.  Assumed not <code>null</code>.
    * @throws PSSystemValidationException if there are any errors
    */
   private void promoteViewSet(PSContentEditor contentEditor)
      throws PSSystemValidationException
   {
      PSViewSet viewSet = new PSViewSet();

      // start with content editor fields
      PSContentEditorPipe pipe = (PSContentEditorPipe)contentEditor.getPipe();
      PSUIDefinition uiDef = pipe.getMapper().getUIDefinition();
      PSDisplayMapper displayMapper = uiDef.getDisplayMapper();

      createSystemViews(viewSet, displayMapper, pipe,
         PSQueryCommandHandler.ROOT_PARENT_PAGE_ID);


      // set the view set on the content editor
      contentEditor.setViewSet(viewSet);
   }

   /**
    * Update the content editor with the specified hidden view if the view
    * does not exist in the content editor; otherwise do nothing.
    * 
    * @param editor the Content Editor in question, never <code>null</code>.
    * @param viewName the name of the hidden view, never blank. 
    * It must start with {@link IPSConstants#SYS_HIDDEN_FIELDS_VIEW_NAME} and
    * followed by <code>0</code> or more hidden field names, where the names
    * are delimited by commas. 
    * See {@link IPSConstants#SYS_HIDDEN_FIELDS_VIEW_NAME} for detail.
    */
   @SuppressWarnings({"unchecked", "cast"})
   public static void addHiddenFieldsView(PSContentEditor editor, String viewName)
   {
      if (editor == null)
         throw new IllegalArgumentException("editor may not be null.");
      if (StringUtils.isBlank(viewName))
         throw new IllegalArgumentException("viewName may not be blank.");
      
      if (!viewName.startsWith(IPSConstants.SYS_HIDDEN_FIELDS_VIEW_NAME))
         throw new IllegalArgumentException("the new name must start with \""
               + IPSConstants.SYS_HIDDEN_FIELDS_VIEW_NAME + "\"");
      
      PSViewSet vset = editor.getViewSet();
      if (vset.getView(viewName) != null)
         return; // the hidden view already exist
      
      PSView allView = vset.getView(IPSConstants.SYS_ALL_VIEW_NAME);
      
      if (allView == null)
      {
         throw new IllegalStateException(IPSConstants.SYS_ALL_VIEW_NAME
               + " view does not exist in " + editor.getName()
               + " content editor.");
      }

      // get a list of hidden fields from the viewName
      String[] hiddenFields = new String[0];
      if (viewName.length() > IPSConstants.SYS_HIDDEN_FIELDS_VIEW_NAME.length())
      {
         String fields = viewName.substring(IPSConstants.SYS_HIDDEN_FIELDS_VIEW_NAME.length());
         hiddenFields = fields.split(",");
      }
      
      // create a view with all fields, except the hidden ones.
      List<String> allFields = (List<String>) IteratorUtils.toList(allView.getFields());
      List<String> visableFields = new ArrayList<String>(allFields);
      for (String name : hiddenFields)
      {
         visableFields.remove(name);
      }
      PSView hiddenView = new PSView(viewName, visableFields.iterator());
      vset.addView(hiddenView);
   }   

   /**
    * Creates default views for the supplied mapper.  Will recurse any complex
    * child mappers as well, and create conditional views based on the page ids.
    *
    * @param viewSet The viewset to which the views are added. Assumed not
    * <code>null</code>.
    * @param displayMapper The mapper for which the views are created.  Assumed
    * not <code>null</code>.
    * @param cePipe The content editor pipe, used to get field sets for the
    * specified <code>displayMapper</code>.  Assumed not <code>null</code>.
    * @param pageId If the parent mapper is supplied, then this will be
    * the value of {@link PSQueryCommandHandler#ROOT_PARENT_PAGE_ID}.  If
    * supplied a complex child mapper, this will be the value of the higher of
    * the two page ids produced for each child mapper, so (pageId - 1) and
    * pageId will be the two page id's used to create the conditional views.
    *
    * @throws PSSystemValidationException if there are any errors
    */
   private void createSystemViews(PSViewSet viewSet, PSDisplayMapper displayMapper,
      PSContentEditorPipe cePipe, int pageId)
        throws PSSystemValidationException
   {
      // Create each list of fields, walk the mapper, and set them as we go
      // for each field, create a single field view and set it on the viewSet
      List allFields = new ArrayList();  // Contains all fields
      List metaFields = new ArrayList();  // Contains all system fields
      List contentFields = new ArrayList();  // Contains all non-system fields
      List singleFields = new ArrayList();  // A view containing a single field

      PSFieldSet fieldSet =
            cePipe.getMapper().getFieldSet(displayMapper.getFieldSetRef());
      int nextPageId = pageId;

      // prepare conditionals if on a child page
      PSCollection conds = null;
      if (pageId != PSQueryCommandHandler.ROOT_PARENT_PAGE_ID)
      {
         conds = new PSCollection(PSConditional.class);
         try
         {
            // add condition for summary page
            PSConditional cond1 = new PSConditional(new PSSingleHtmlParameter(
               PAGE_ID_PARAM_NAME), PSConditional.OPTYPE_EQUALS,
                  new PSTextLiteral(String.valueOf(pageId - 1)),
                     PSConditional.OPBOOL_OR);
            conds.add(cond1);
            // add condition for child item page
            PSConditional cond2 = new PSConditional(new PSSingleHtmlParameter(
               PAGE_ID_PARAM_NAME), PSConditional.OPTYPE_EQUALS,
                  new PSTextLiteral(String.valueOf(pageId)));
            conds.add(cond2);
         }
         catch (IllegalArgumentException e)
         {
            throw new IllegalArgumentException(e.getLocalizedMessage());
         }
      }

      /* first see if this is a complex child - if so add the fieldset itself
       * to all views but SingleField so it will be in the conditional views for
       * this page id and show up on the summary editor page - there will
       * already be a single field view for it with no conditions from when the
       * parent item was processed.
       */
      if (fieldSet.getType() == PSFieldSet.TYPE_COMPLEX_CHILD)
      {
         String fieldSetName = fieldSet.getName();
         allFields.add(fieldSetName);

         // get first field in the set and see what its type is
         int fieldType = PSField.TYPE_LOCAL;  // default to local if empty
         Iterator fields = fieldSet.getAll();
         while (fields.hasNext())
         {
            Object o = fields.next();
            if (o instanceof PSField)
            {
               fieldType = ((PSField)o).getType();
               break;
           }
         }

         if (fieldType == PSField.TYPE_SYSTEM)
            metaFields.add(fieldSetName);
         else
            contentFields.add(fieldSetName);
      }

      Iterator mappings = displayMapper.iterator();
      while(mappings.hasNext())
      {
         // get the field
         PSDisplayMapping mapping = (PSDisplayMapping)mappings.next();
         String fieldRef = mapping.getFieldRef();

         // determine the type
         int fieldType = PSField.TYPE_LOCAL;  // default to local
         Object o = fieldSet.get(fieldRef);
         // the second condition below handles the case where the fieldset name
         // is the same as the field name
         if (o == null || (o instanceof PSFieldSet &&
            ((PSFieldSet)o).getType() ==
               PSFieldSet.TYPE_MULTI_PROPERTY_SIMPLE_CHILD))
         {
            o = fieldSet.getChildField(fieldRef,
               PSFieldSet.TYPE_MULTI_PROPERTY_SIMPLE_CHILD);
         }

         if (o != null)
         {
            if (o instanceof PSField)
            {
               fieldType = ((PSField)o).getType();
            }
            else
            {
               PSFieldSet fs = (PSFieldSet)o;

               // for simple child, need to add the simple field to the view
               if ( fs.getType() == PSFieldSet.TYPE_SIMPLE_CHILD )
               {
                  PSField field = fs.getSimpleChildField();
                  fieldType = field.getType();
                  fieldRef = field.getSubmitName();
               }
               else  // it's a complex child
               {
                  // get first field in the set to use its type
                  Iterator fields = fs.getAll();
                  while (fields.hasNext())
                  {
                     o = fields.next();
                     if (o instanceof PSField)
                     {
                        fieldType = ((PSField)o).getType();
                        break;
                    }
                  }

                  // if a complex child, need to set up views for it
                  PSDisplayMapper childMapper = mapping.getDisplayMapper();
                  // all but the main mapper produce 2 pageids, summary and edit
                  nextPageId += 2;
                  createSystemViews(viewSet, childMapper, cePipe, nextPageId);

               }
            }

            allFields.add(fieldRef);

            /* handle single field by creating a view for each field containing
             * only that field.  For complex children it's the fieldset name,
             * for simple children it's the simple child field name set above.
             */
            singleFields.clear();
            singleFields.add(fieldRef);
            String viewName = IPSConstants.SYS_SINGLE_FIELD_VIEW_NAME +
               fieldRef;

            /* if we are on a child page, we'll add a conditional view based on
             * pageid - conditions have already been prepared
             */
            if (conds == null)
               viewSet.addView(new PSView(viewName, singleFields.iterator()));
            else
               viewSet.addConditionalView(new PSConditionalView(viewName,
                  singleFields.iterator(), conds));

            if (fieldType == PSField.TYPE_SYSTEM)
               metaFields.add(fieldRef);
            else
               contentFields.add(fieldRef);
         }
      }

      // lists have been built, add the views
      if (conds == null)
      {
         // we are on the parent page
         viewSet.addView(new PSView(IPSConstants.DEFAULT_VIEW_NAME,
            allFields.iterator()));
         viewSet.addView(new PSView(IPSConstants.SYS_ALL_VIEW_NAME,
            allFields.iterator()));
         viewSet.addView(new PSView(IPSConstants.SYS_ITEM_META_VIEW_NAME,
            metaFields.iterator()));
         viewSet.addView(new PSView(IPSConstants.SYS_CONTENT_VIEW_NAME,
            contentFields.iterator()));
      }
      else
      {
         // we are on a child page, add conditional views based on page id
         viewSet.addConditionalView(new PSConditionalView(
            IPSConstants.DEFAULT_VIEW_NAME, allFields.iterator(), conds));
         viewSet.addConditionalView(new PSConditionalView(
            IPSConstants.SYS_ALL_VIEW_NAME, allFields.iterator(), conds));
         viewSet.addConditionalView(new PSConditionalView(
            IPSConstants.SYS_ITEM_META_VIEW_NAME, metaFields.iterator(),
            conds));
         viewSet.addConditionalView(new PSConditionalView(
            IPSConstants.SYS_CONTENT_VIEW_NAME, contentFields.iterator(),
            conds));
      }

   }


   /**
    * Adds all redirects from the source to the target only if a redirect with
    * the same command name is not already present (does not overwrite)
    *
    * @param source The source app flow.  Assumed not to be <code>null</code>.
    * @param target The target app flow.  Assumed not to be <code>null</code>.
    *
    * @throws PSSystemValidationException if there is a problem with the application
    * flow being added.
    */
   private void mergeAppFlow(PSApplicationFlow source, PSApplicationFlow target)
      throws PSSystemValidationException
   {
      Iterator cmdNames = source.getCommandHandlerNames();
      while (cmdNames.hasNext())
      {
         String cmdName = (String) cmdNames.next();
         PSCollection coll = source.getRedirectCollection(cmdName);
         if (target.getRedirectCollection(cmdName) == null)
            target.addRedirects(cmdName, coll);
      }
   }


   /**
    * Adds all stylesheets from the source to the target only if one with
    * the same command name is not already present (does not overwrite)
    *
    * @param source The source stylesheet set.  Assumed not to be
    * <code>null</code>.
    * @param target The target stylesheet set.  Assumed not to be
    * <code>null</code>.
    *
    * @throws PSSystemValidationException if there is a problem with the stylesheet
    * being added.
    */
   private void mergeStyleSheets(PSCommandHandlerStylesheets source,
      PSCommandHandlerStylesheets target)
         throws PSSystemValidationException
   {
      Iterator cmdNames = source.getCommandHandlerNames();
      while (cmdNames.hasNext())
      {
         String cmdName = (String)cmdNames.next();
         if (!target.getStylesheets(cmdName).hasNext() &&
            source.getStylesheets(cmdName).hasNext())
         {
            target.addStylesheets(cmdName, new PSCollection(
               source.getStylesheets(cmdName)));
         }
      }
   }

   /**
    * Determines if a shared group has been included by the Content Editor
    * dataset.
    *
    * @param group The group to check, its name will be compared case
    * insensitive.  May not be <code>null</code>.
    *
    * @return <code>true</code> if the content editor has included this group,
    * <code>false</code> if not.
    */
   private boolean isIncludedSharedGroup(PSSharedFieldGroup group)
   {
      if (group == null)
         throw new IllegalArgumentException("group may not be null");

      return m_sharedFieldIncludes.contains(group.getName().toUpperCase());
   }


   /**
    * Determines whether the handler that will be used to process the supplied
    * request is an update or query handler.
    *
    * @param request determines which handler will be used, not
    * <code>null</code>.
    *
    * @return <code>true</code> if the request will be processed with an
    * update handler; <code>false</code> if the request will be processed
    * with a query handler or if the request is invalid (unknown
    * {@link #COMMAND_PARAM_NAME}).
    */
   public boolean isUpdateRequest(PSRequest request)
   {
      if (request == null)
         throw new IllegalArgumentException("Request may not be null");

      IPSRequestHandler rh = getRequestHandler( request );
      if (rh instanceof PSCommandHandler)
      {
         PSCommandHandler handler = (PSCommandHandler) rh;
         return handler.isUpdate();
      }
      else
      {
         // default sys_command is edit (a query)
         return false;
      }
   }

   /**
    * This private method should be used whenever the <code>m_commandHandlers
    * </code> Map is referenced to do a lookup for a command handler. We now
    * have sub commands using the following format:
    * <code>command/subcommand</code>
    *
    * @param commandParam the name of the command including the sub command
    * seperated by a "/"
    *
    * @return IPSRequestHandler a reference to the request handler based on
    * the command only, ignoring the subcommand for the lookup, returns
    * <code>null</code> if the command cannot be found.
    */
   private IPSRequestHandler lookupCommandHandler(String commandParam)
   {
      if (commandParam == null)
         return null;

      IPSRequestHandler rh = null;

      // strip off subcommands for handler lookup
      int pos = commandParam.indexOf("/");
      if (pos >= 0)
         commandParam = commandParam.substring(0, pos);

      rh = (IPSRequestHandler)m_commandHandlers.get(commandParam);

      return rh;
   }

   /**
    * Constant for the HTML parameter name that will be used by the server when
    * selecting the command handler.
    */
   public static final String COMMAND_PARAM_NAME = "sys_command";

   /**
    * Constant for the HTML parameter name that will be used by the server when
    * retrieving a previously cached document.
    */
   public static final String CACHE_ID_PARAM_NAME = "sys_cacheid";

   /**
    * The HTML parameter name that will be used by the server when specifying
    * the content id part of the content item key.
    */
   public static final String CONTENT_ID_PARAM_NAME = "sys_contentid";

   /**
    * The HTML parameter name that will be used by the server when specifying
    * the revision part of the content item key.
    */
   public static final String REVISION_ID_PARAM_NAME = "sys_revision";

   /**
    * The HTML parameter name that will be used by the
    * server when specifying the id of the active child on the current
    * request.
    */
   public static final String CHILD_ID_PARAM_NAME = "sys_childid";

   /**
    * The HTML parameter name that will be used by the
    * server when specifying the sysId of the active child on the current
    * request.
    */
   public static final String CHILD_ROW_ID_PARAM_NAME = "sys_childrowid";

   /**
    * The HTML parameter name that specifies the id of the page the new or edit
    * command handlers should display.
    */
   public static final String PAGE_ID_PARAM_NAME = "sys_pageid";

   /**
    * The HTML parameter name that specifies the next command to request.
    */
   public static final String NEXT_COMMAND_PARAM_NAME = "sys_nextcommand";

   /**
    * The HTML parameter name that specifies the sort rank.
    */
   public static final String SORT_RANK_PARAM_NAME = "sys_sortrank";

   /**
    * The HTML parameter name that specifies the stylesheet url-string
    * to use.
    */
   public static final String USE_STYLESHEET = "sys_stylesheet";

   /**
    * The DBActionType parameter value to increment the sortrank for a child
    * row
    */
   public static final String DB_ACTION_SEQUENCE_INCREMENT
      = "SEQUENCE_INCREMENT";

   /**
    * The DBActionType parameter value to decrement the sortrank for a child
    * row
    */
   public static final String DB_ACTION_SEQUENCE_DECREMENT
      = "SEQUENCE_DECREMENT";

   /**
    * The DBActionType parameter value to set the sortrank for a child
    * row, this is used to allow any number of child ids to be set to specific
    * sort locations.
    */
   public static final String DB_ACTION_RESEQUENCE = "RESEQUENCE";

   /**
    * Name to use for subsystem when writing console messages
    */
   private static final String SUBSYSTEM_NAME = "cms.handlers";

   /**
    * Map of command handlers used to process a request.  Command name is
    * used as the key, and the instance of the handler (which must be derived
    * from <code>PSCommandHandler</code>) is stored as the entry's value.  Map
    * is initialized and handlers are added in the constructor, immutable
    * after that.
    */
   private Map m_commandHandlers = null;

   /**
    * The system def, requested from the server in constructor
    */
   private PSContentEditorSystemDef m_systemDef = null;

   /**
    * The system def, requested from the server in constructor
    */
   private PSContentEditorSharedDef m_sharedDef = null;

   /**
    * The appHandler passed to the constructor.
    */
   private PSApplicationHandler m_appHandler = null;

   /**
    * The handler for the dynamic application used to manage resources generated
    * by command handlers either during initialization or on the fly. Initialized
    * during constructor, never <code>null</code> after that.
    */
   private PSApplicationHandler m_internalAppHandler = null;

   /**
    * The dataset passed to the constructor.
    */
   private PSContentEditor m_dataSet = null;

   /**
    * The cms object for the conent editor of this handler. Initialized by the
    * ctor, never <code>null</code> after that.
    */
   private PSCmsObject m_cmsObject;

   /**
    * The list of system field excludes from the dataset.  Initialized during
    * constructor, never <code>null</code> after that.
    */
   private PSCollection m_sysFieldExcludes = null;

   /**
    * The list of names of shared group includes from the dataset.  Initialized
    * during constructor, never <code>null</code> after that. Once the list is
    * validated against the shared def, the entries in the list are uppercased
    * so that we can do case insensitive comparisions after that.
    */
   private PSCollection m_sharedFieldIncludes = null;

   /**
    * The list of fields that should be excluded from the field set of any
    * shared groups that are included.  Initialized during construction, never
    * <code>null</code> after that.
    */
   private PSCollection m_sharedFieldExcludes = null;

   /**
    * The PSApplication that is created and passed to each command handler.
    * The command handlers are responsible for adding their own resources during
    * their construction.  After they are initialized, the app is started.  This
    * handler is also responsible for shutting down the app and cleaning up and
    * resources on disk.  Initialized in the constructor, disposed of in the
    * {@link #shutdown()} method.
    */
   private PSApplication m_app = null;
   
   /**
    * The content type id of this editor. Initialized during construction, never
    * modified after that.
    */
   private long m_contentTypeid;
   
   /**
    * List of {@link IPSEditorChangeListener} objects to notify when item is
    * modified or its state changes.  Never <code>null</code>, may be empty.
    * Listeners are added using
    * {@link #addEditorChangeListener(IPSEditorChangeListener)}.
    */
   protected List<IPSEditorChangeListener> m_changeListeners = new ArrayList<IPSEditorChangeListener>();
}
