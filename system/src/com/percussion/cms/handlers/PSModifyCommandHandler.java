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

import com.percussion.cms.*;
import com.percussion.data.*;
import com.percussion.design.objectstore.*;
import com.percussion.error.PSBackEndUpdateProcessingError;
import com.percussion.error.PSErrorException;
import com.percussion.error.PSEvaluationException;
import com.percussion.error.PSException;
import com.percussion.extension.PSExtensionException;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.*;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSCms;
import com.percussion.util.PSUrlUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

/**
 * This class encapsulates behavior required to handle all Modify related
 * commands.  Performs all updates for ContentEditors.
 */
public class PSModifyCommandHandler extends PSCommandHandler
{
   /**
    * Constructor for this class.  Prepares all extensions and creates and
    * starts the application used to update backend data.
    *
    * @param appHandler The application handler for this app.  May not be
    * <code>null</code>.
    * @param ceh The content editor handler for this dataset.  May not be
    * <code>null</code>.
    * @param ds The dataset this command handler will process modify commands
    * for.  Must be a PSContentEditor.  May not be <code>null</code>.
    * @param app The application created by the PSContentEditor for each
    * command handler to add datasets to.  The app is started and stopped by the
    * ContentEditorHandler.
    *
    * @throws PSExtensionException if there is an error preparing an extension.
    * @throws PSNotFoundException if a udf or extension cannot be located.
    * @throws PSValidationException if there is a problem starting an internal
    * application.
    */
   public PSModifyCommandHandler(PSApplicationHandler appHandler,
      PSContentEditorHandler ceh, PSDataSet ds, PSApplication app)
      throws PSExtensionException, PSNotFoundException, PSValidationException
   {
      super(appHandler, ceh, (PSContentEditor)ds, app);

      // Extract all exits, and translations and prepare them.
      prepareExtensions(COMMAND_NAME);
      prepareInputTranslations();

      // prepare all validators for request mode
      prepareValidation((PSContentEditor) ds);

      // prepare redirects
      prepareRedirects(COMMAND_NAME);

      // prepare views
      m_viewEvaluator = new PSViewEvaluator(m_ce.getViewSet());

      // Get the hidden control param for validation error redirects
      PSEditorDocumentContext ctx = new PSEditorDocumentContext(m_ceHandler,
         app, m_ce);
      m_hiddenControlName = ctx.getInitParam(
         IPSConstants.HIDDEN_CONTROL_PARAM_NAME);
      if (null == m_hiddenControlName ||
         m_hiddenControlName.trim().length() == 0)
      {
         String [] args =
         {
            IPSConstants.HIDDEN_CONTROL_PARAM_NAME,
            "InitParam is empty or missing from system def"
         };
         throw new PSValidationException( IPSServerErrors.CE_INVALID_PARAM,
               args );
      }


      // get the app we will use and add the datasets we need
      try
      {
         PSContentEditorPipe pipe = (PSContentEditorPipe)m_ce.getPipe();

         // Extract and recurse fieldsets, creating necessary update resources
         PSFieldSet fieldSet = pipe.getMapper().getFieldSet();
         PSDisplayMapper mapper =
            pipe.getMapper().getUIDefinition().getDisplayMapper();

         // initialize plan builders
         m_insertPlanBuilder = new PSInsertPlanBuilder(m_ceHandler, m_ce,
            m_internalApp);
         m_updatePlanBuilder = new PSUpdatePlanBuilder(m_ceHandler, m_ce,
            m_internalApp);
         m_deletePlanBuilder = new PSDeletePlanBuilder(m_ceHandler, m_ce,
            m_internalApp);
         m_childInsertPlanBuilder = new PSChildInsertPlanBuilder(m_ceHandler,
            m_ce, m_internalApp);
         m_childDeletePlanBuilder = new PSChildDeletePlanBuilder(m_ceHandler,
            m_ce, m_internalApp);
         m_sequencePlanBuilder = new PSSequenceUpdatePlanBuilder(m_ceHandler,
            m_ce, m_internalApp);
         m_simpleDeletePlanBuilder = new PSSimpleChildDeletePlanBuilder(
            m_ceHandler, m_ce, m_internalApp);
         m_simpleInsertPlanBuilder = new PSSimpleChildInsertPlanBuilder(
            m_ceHandler, m_ce, m_internalApp);

         // create modify resources in app
         createDataSets(mapper, fieldSet);

         // prepare inline link fields
         m_inlineLinkFields = prepareInlineLinkFields(fieldSet, null);
      }
      catch (SQLException e)
      {
         StringBuffer buf = new StringBuffer(250);
         buf.append( System.getProperty( "line.separator" ));
         buf.append( e.getLocalizedMessage());
         SQLException next = e.getNextException();
         while ( null != next )
         {
            buf.append( System.getProperty( "line.separator" ));
            buf.append( next.getLocalizedMessage());
            next = next.getNextException();
         }
         throw new PSValidationException( IPSServerErrors.CE_SQL_ERRORS,
               buf.toString());
      }
   }

   /**
    * Prepare a map of fields that need special processing for inline links.
    * Walks the supplied fieldset recursivly looking for fields that may
    * contain inline links and adds them to the map being returned. This also
    * initializes a flat map mapping field names to field objects, see 
    * {@link #m_flatInlinelinkFields} for details of this map. 
    * Works in concert with {@link #prepareInlineLinkFields(PSFieldSet, List, 
    * Map)} to accomplish the recursive processing.
    *
    * @param fieldSet the fieldset to walk, assumed not <code>null</code>, may 
    * be empty.
    * 
    * @param allLinkFields a map to which all fields will be added that
    *    may have inline links, may be <code>null</code> or empty. If
    *    <code>null</code> is supplied, a new map will be created. See
    *    {@link #m_inlineLinkFields} for detailed info on the map.
    * 
    * @return a map of <code>List</code> of <code>PSField</code> objects that 
    *    need special processing for inline links, never <code>null</code>, 
    *    may be empty. See {@link #m_inlineLinkFields} for detailed info on 
    *    the map.
    */
   @SuppressWarnings("unchecked")
   private Map prepareInlineLinkFields(PSFieldSet fieldSet, Map allLinkFields)
   {
      if (allLinkFields == null)
         allLinkFields = new HashMap();
         
      String key = fieldSet.getName();
      List value = (List) allLinkFields.get(key);
      if (value == null)
      {
         value = new ArrayList();
         allLinkFields.put(key, value);
      }
      return prepareInlineLinkFields(fieldSet, value, allLinkFields);
   }
   
   
   /**
    * This method is used in concert with the {@link #prepareInlineLinkFields(
    * PSFieldSet, Map)} method to support recursive processing. This method
    * walks the supplied fieldset and calls either this method or the other
    * one if a member of the supplied set is a field set itself (depending
    * on the fieldset type) or it adds the field to the supplied list.
    * 
    * @param fs The field set to process. Assumed not <code>null</code>.
    * 
    * @param linkFields The list to which the <code>PSField</code> objects 
    * found in fs will be added if they can contain inline links. Assumed not
    * <code>null</code>.
    * 
    * @param allLinkFields This is used for recursion. If a complex child 
    * field set is found in <code>fs</code>, this is passed back to the other
    * method in a recursive call.
    *  
    * @return Always <code>allLinkFields</code>. 
    */
   @SuppressWarnings("unchecked")
   private Map prepareInlineLinkFields(PSFieldSet fs, List linkFields, 
         Map allLinkFields)
   {
      Iterator fields = fs.getAll();
      while (fields.hasNext())
      {
         Object o = fields.next();
         if (o instanceof PSFieldSet)
         {
            PSFieldSet curFieldSet = (PSFieldSet) o;
            if (curFieldSet.getType() == PSFieldSet.TYPE_COMPLEX_CHILD)
               prepareInlineLinkFields(curFieldSet, allLinkFields);
            else
            {
               prepareInlineLinkFields(curFieldSet, linkFields, allLinkFields);
            }
         }
         else
         {
            PSField field = (PSField) o;
            if (field.mayHaveInlineLinks())
            {
               linkFields.add(field);
               m_flatInlinelinkFields.put(field.getSubmitName(), field);
            }
         }
      }
      return allLinkFields;
   }

   /**
    * Executes the provided modify request using the supplied execution data.
    *
    * @param request the request to be processed, assumed not <code>null</code>.
    * @param execData the execution data to use, assumed not <code>null</code>.
    *
    * @return the number of steps executed.  If a field validation error halted
    * the execution of the modify request, <code>-1</code> is returned.
    *
    * @throws PSErrorException for any errors occurred.
    * @throws PSRequestValidationException for invalid requests.
    * @throws PSAuthorizationException if the user is not authorize to perform
    *    the request.
    * @throws PSRequestValidationException for any failed request validation.
    * @throws PSValidationException for any failed validation.
    * @throws SQLException for any failed SQL operation.
    * @throws PSInternalRequestCallException if any error occurs processing
    *    the internal request call.
    * @throws PSAuthorizationException if the user is not authorized.
    * @throws PSAuthenticationFailedException if the user failed to
    *    authenticate.
    * @throws PSCmsException if anything fails while processing inline links.
    */
   private int executeModifyRequest(PSRequest request,
      PSExecutionData execData)
      throws PSRequestValidationException, PSAuthorizationException,
         PSErrorException, PSConversionException, PSDataExtractionException,
         SQLException, PSInternalRequestCallException, IOException,
         PSAuthenticationFailedException, PSValidationException, PSCmsException
   {

      PSUserSession userSession = request.getUserSession();
      //modify handler should use user's community if sys_communityId is not present
      String communityid =
         request.getParameter(IPSHtmlParameters.SYS_COMMUNITYID, null);

      if (communityid == null || communityid.trim().length() == 0)
      {
         //it's assumed that the Authenticate user exit always sets
         //the IPSHtmlParameters.SYS_COMMUNITY in the user session.
         communityid =
            (String)userSession.getPrivateObject(IPSHtmlParameters.SYS_COMMUNITY);

         request.setParameter(IPSHtmlParameters.SYS_COMMUNITYID, communityid);
      }
      
      // Increment the hibernate version column
      String hib_ver = request.getParameter(IPSHtmlParameters.SYS_HIBERNATEVERSION, null);
      if (StringUtils.isBlank(hib_ver))
      {
         hib_ver = "0";
      }
      int hver = Integer.parseInt(hib_ver);
      hver++;
      request.setParameter(IPSHtmlParameters.SYS_HIBERNATEVERSION, Integer.toString(hver));
      
      // uses locale (sys_lang) in user's session or default
      // if not present in the request
      String locale =
         request.getParameter(IPSHtmlParameters.SYS_LANG, null);

      if (locale == null || locale.trim().length() == 0)
      {
         locale = (String) userSession.getPrivateObject(
            PSI18nUtils.USER_SESSION_OBJECT_SYS_LANG);

         // get the default locale if not exist in the session
         if (locale == null || locale.trim().length() == 0)
            locale = PSI18nUtils.DEFAULT_LANG;

         request.setParameter(IPSHtmlParameters.SYS_LANG, locale);
      }
      
      // get the mapper id
      int id = 0;
      String strId = request.getParameter(
         PSContentEditorHandler.CHILD_ID_PARAM_NAME, "0");

      String lang = PSI18nUtils.DEFAULT_LANG;
      try
      {
         lang = (String)userSession.getPrivateObject(
            PSI18nUtils.USER_SESSION_OBJECT_SYS_LANG);
         // get the param, providing parent mapper id as default
         id = Integer.parseInt(strId);
      }
      catch (NumberFormatException e)
      {
         // get the mapper by name if the number fails
         PSContentEditorPipe pipe = (PSContentEditorPipe)m_ce.getPipe();
         PSUIDefinition uiDef = pipe.getMapper().getUIDefinition();
         PSDisplayMapper mapper = uiDef.getDisplayMapper(strId);

         if (mapper != null)
         {
            id = mapper.getId();
            strId = "" + id;
         }
         else
         {
            // throw exception
            Object[] args = {PSContentEditorHandler.CHILD_ID_PARAM_NAME, strId};
            throw new PSRequestValidationException(
               IPSServerErrors.CE_MODIFY_INVALID_PARAM, args);
         }
      }
      
      // determine plan 
      int planType = getPlanType(execData);
      boolean updateFieldData = PSModifyPlan.updatesItemData(planType);

      if (updateFieldData)
      {      
         // run item translation exits
         runInputTranslations(execData);

         // process field translation
         processFieldTranslations(execData, strId); 
      }
      
      // add params to properly set up the authenticateUser exit
      int level;
      PSApplication app = m_appHandler.getApplicationDefinition();
      String dbAction = app.getRequestTypeHtmlParamName();
      String dbActionInsert = app.getRequestTypeValueInsert();
      String actualType = request.getParameter( dbAction );
      if ( null != actualType && dbActionInsert.equals( actualType ))
         level = IPSConstants.ASSIGNMENT_TYPE_READER;
      else
         level = IPSConstants.ASSIGNMENT_TYPE_ASSIGNEE;
      setMinAccessLevel( request, level );

      String contentId =
            request.getParameter( PSContentEditorHandler.CONTENT_ID_PARAM_NAME );
      String condition;
      if ( null == contentId || contentId.trim().length() == 0 )
      {
         // new item
         condition = IPSConstants.CHECKINOUT_CONDITION_IGNORE;
         request.removeParameter(PSContentEditorHandler.CONTENT_ID_PARAM_NAME);
      }
      else
         // modifying existing item
         condition = IPSConstants.CHECKINOUT_CONDITION_CHECKOUT;
      setCheckInOutCondition( request, condition );


      if (updateFieldData)
      {
         // run ds exits and cmd pre exits
         runPreProcessingExtensions(execData);

         // process validation
         PSEditCommandHandler editHandler = (PSEditCommandHandler)
            m_ceHandler.getCommandHandler(PSEditCommandHandler.COMMAND_NAME);
         if (editHandler == null)
            throw new RuntimeException(
               "The edit command handler must be initialized!");
         Integer pageId = getPageId(editHandler.getPageMap(), id);
         PSErrorCollector errorCollector = processValidation(execData, pageId, lang);
         if (errorCollector != null)
         {
            redirectToError(execData, errorCollector, pageId);
            return -1;
         }         
      }
      
      // validate or generate the other required ids
      // TODO move this into the plan set, making it an extractor?       
      prepareModifyPlan(planType, execData);

      // execute the plan
      PSModifyPlanSet modifyPlanSet = (PSModifyPlanSet)m_modifyPlanSets.get(
         new Integer(id));
      final PSModifyPlan modifyPlan = modifyPlanSet.getPlan(planType);
      
      // TODO: we temporarily don't have plans for delete as we are only 
      // generating change events
      if (modifyPlan == null && planType != PSModifyPlan.TYPE_DELETE_ITEM)
      {
         Object[] args = {PSContentEditorHandler.CHILD_ID_PARAM_NAME, strId};
         throw new PSRequestValidationException(
            IPSServerErrors.CE_MODIFY_INVALID_PARAM, args);
      }
      
      Boolean isModifyParent = (Boolean)m_modifyParent.get();
      if (isModifyParent == null)
         throw new IllegalStateException("m_modifyParent has not been set.");

      String uniqueValidated = request.getParameter("UniqueValidated");
      // Only run unique name validation if it was not run via the
      // PSValidateUniqueName system exit.
      if(uniqueValidated != null && uniqueValidated.equalsIgnoreCase("yes")
            && isModifyParent.booleanValue())
      {
         PSRequestContext reqCtx = new PSRequestContext(request);
         boolean isInsert = planType == PSModifyPlan.TYPE_INSERT_PLAN; 
         PSServerFolderProcessor.validateUniqueDepName(reqCtx, isInsert);
      }
      
      String test = request.getParameter(
         IPSHtmlParameters.SYS_INLINELINK_DATA_UPDATE);
      boolean inlineLinkDataUpdate = (test != null && 
         test.equalsIgnoreCase("yes"));
      
      if (updateFieldData && !inlineLinkDataUpdate)
         preProcessInlineLinks(request, id);

      int stepsExecuted=0;
      
      try
      {   
         if (planType == PSModifyPlan.TYPE_DELETE_ITEM)
         {
            String val = request.getParameter("sys_changeEventOnly");
            // has a purge resource, just generate change event
            if ( val != null && val.equals("yes") )
               stepsExecuted = 1;
            else 
            {
               stepsExecuted = modifyPlan.execute(execData, 
                                          m_internalApp.getName());
            }
         }
         else
         {
            stepsExecuted = modifyPlan.execute(execData, 
                                          m_internalApp.getName());
         }
      }
      finally
      {
         //reset content item status cache
         request.clearContentItemStatusCache();
      }

      if (updateFieldData && !inlineLinkDataUpdate)
         postProcessInlineLinks(request);

      // notify listeners of any change
      if (stepsExecuted > 0)
      {
         // create processor to add any modified binary fields before event
         // is sent
         final PSExecutionData finData = execData;
         PSChangeEventProcessor proc = null;
         if (updateFieldData)
         {
            proc = new PSChangeEventProcessor()
            {
               @SuppressWarnings("unchecked")
               @Override
               public PSEditorChangeEvent processEvent(PSEditorChangeEvent e)
               {
                  // determine which binary fields have been modified and set
                  // on the event
                  Set modified = new HashSet();
                  Map binFields = modifyPlan.getBinaryFields();
                  Iterator entries = binFields.entrySet().iterator();
                  while (entries.hasNext())
                  {
                     Map.Entry entry = (Entry)entries.next();
                     String field = (String)entry.getKey();
                     
                     /*
                      * see if there are conditionals.  If not, then consider
                      * the field modified.  If so, evaluate them to 
                      * determine if modified.
                      */
                     List condList = (List)entry.getValue();
                     boolean hasChanged = true;
                     if (condList != null)
                     {
                        Iterator conds = condList.iterator();
                        hasChanged = false;
                        while (conds.hasNext() && !hasChanged)
                        { 
                           PSConditionalEvaluator eval = 
                              (PSConditionalEvaluator)conds.next();
                           if (eval.isMatch(finData))
                              hasChanged = true;                     
                        }
                     }
                     
                     if (hasChanged)                     
                        modified.add(field);
                  }
                  
                  e.setBinaryFields(modified);
                  
                  return e;
               }
            };
         }
         
         final int action = getAction(planType, request);
         if (action != PSEditorChangeEvent.ACTION_UNDEFINED)
         {
            // call notify on base class - this will use the processor to add
            // the binary fields
            notifyEditorChangeListeners(execData, action, proc);
         }
      }

      // TODO: validate that the expected # of inserts and updates occurred
      // perhaps each step can do it!

      return stepsExecuted;
   }

   /**
    * Processes all fields that may contain inline links before the document
    * is submitted to the database.
    *
    * @param request the request for which to pre-process all fields with
    *    possible inline links, assumed not <code>null</code>.
    * @param id the id of the display mapper for which to process the inline 
    *    links. This method does nothing if an invalid mapper id is supplied. 
    * @throws IOException for any I/O errors.
    * @throws PSCmsException for all other errors.
    */
   @SuppressWarnings("unchecked")
   private void preProcessInlineLinks(PSRequest request, int id)
      throws IOException, PSCmsException
   {
      try
      {
         PSContentEditorPipe pipe = (PSContentEditorPipe) m_ce.getPipe();
         if (pipe == null)
            return;
            
         PSDisplayMapper mapper = 
            pipe.getMapper().getUIDefinition().getDisplayMapper(id);
         if (mapper == null)
            return;
            
         List fieldList = (List) m_inlineLinkFields.get(
            mapper.getFieldSetRef());

         Iterator fields = fieldList.iterator();
         PSRelationshipSet deletes = new PSRelationshipSet();
         PSRelationshipSet modifies = new PSRelationshipSet();
         
         /*
          * Word OCX posts data from the HTML form which does not include the 
          * content body. In this case we would not want to process the deletes 
          * and modifies. 
          */
         if (request.getParameter("sys_WordOCX") == null)
         {
            /*
             * Prepare all relationships which possibly need to be deleted in 
             * the post inline link process.
             */
            if (fields.hasNext())
            {
               PSRelationshipSet all = PSInlineLinkField.getInlineRelationships(
                  new PSRequestContext(request));
               Iterator relationships = all.iterator();
               while (relationships.hasNext())
               {
                  PSRelationship relationship = 
                     (PSRelationship) relationships.next();
                  String inlineRelationshipId = relationship.getProperty(
                     PSRelationshipConfig.PDU_INLINERELATIONSHIP);
                  for (int i=0; i<fieldList.size(); i++)
                  {
                     PSField field = (PSField) fieldList.get(i);
                     Object test1 = m_flatInlinelinkFields.get(
                        PSInlineLinkField.getFieldName(inlineRelationshipId));
                     if (test1 == null)
                     {
                        /*
                         * If this is not an inline link field anymore, we
                         * add it to the delete list. This cleans up orphaned
                         * inline relationships.
                         */
                        deletes.add(relationship);
                        break;
                     }
                     else
                     {
                        /*
                         * If this inline relationship is processed with this
                         * request, we add it to the dlete list. We also 
                         * test for the value 'yes', which was the old format
                         * for this property.
                         */
                        String test = PSInlineLinkField.getInlineRelationshipId(
                           request, field);
                        if (inlineRelationshipId.equals(
                              PSInlineLinkField.RS_YES))
                        {
                           deletes.add(relationship);
                           break;
                        }
                     }
                  }
               }
            }
            
            /*
             * Nothing to prepare for delete requests, all relationships will
             * be cleaned up through the delete.
             */
            String dbActionType = request.getParameter(
               m_internalApp.getRequestTypeHtmlParamName());
            if (!dbActionType.equals(m_internalApp.getRequestTypeValueDelete()))
            {                 
               while (fields.hasNext())
               {
                  PSInlineLinkField field = new PSInlineLinkField(
                     (PSField)fields.next());
                  field.preProcess(request, deletes, modifies);
               }
            }
         }

         m_tlInlineLinkDeletes.set(deletes);
         m_tlInlineLinkModifies.set(modifies);
      }
      catch (SAXException e)
      {
         throw new PSCmsException(1001, e.getLocalizedMessage());
      }
   }

   /**
    * Processes all fields that may contain inline links after the document
    * was submitted to the database.
    *
    * @param request the request for which to post-process all fields with
    *    possible inline links, assumed not <code>null</code>.
    * @throws IOException for any I/O errors.
    * @throws PSCmsException for all other errors.
    */
   private void postProcessInlineLinks(PSRequest request)
      throws IOException, PSCmsException
   {
      try
      {
         PSRelationshipSet deletes = (PSRelationshipSet) m_tlInlineLinkDeletes.get();
         PSRelationshipSet modifies = (PSRelationshipSet) m_tlInlineLinkModifies.get();
         
         PSInlineLinkField.postProcess(request, deletes, modifies);
      }
      catch (SAXException e)
      {
         throw new PSCmsException(1001, e.getLocalizedMessage());
      }
   }

   /* ************ IPSRequestHandler Interface Implementation ************ */

   /**
    * Process a content editor modify request using the input context
    * information and data.
    * <p>
    * The following steps are performed to handle the request:
    * <ol>
    * <li>Extracts the execution data</li>
    * <li>runs all preprocessing exits</li>
    * <li>runs item translation exits</li>
    * <li>performs datatype validations</li>
    * <li>performs field validations</li>
    * <li>runs item validation exits</li>
    * <li>performs the require updates to the backend database</li>
    * <li>runs post-processing exits</li>
    * <li>redirects to the appropriate URL</li>
    * </ol>
    *
    * @param request the request object containing all context   data associated
    * with the request.
    */
   public void processRequest(PSRequest request)
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");

      PSExecutionData execData = null;
      try
      {
         execData = new PSExecutionData(m_appHandler, this,
            request);

         int result = executeModifyRequest(request, execData);
         // make sure a field validation error did not stop the modify
         if (result > -1)
         {
            // run cmd and ds post exits
            runPostProcessingExtensions(execData, null);

            // do redirect
            processRedirect(execData);
         }
      }
      catch (PSAuthorizationException e)
      {
         m_appHandler.handleAuthorizationException(request, e);
      }
      catch (PSErrorException err)
      {
         // these are pre-formatted error we've thrown
         m_appHandler.reportError(request, err.getLogError());
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

   /**
    * Process all input field translations for the provided execution data
    * and childid.
    *
    * @param data the execution data to run the translations for, assumed
    *    not <code>null</code>.
    * @param childId the childid for which we have to run the translations,
    *    assumed not <code>null</code>.
    */
   private void processFieldTranslations(PSExecutionData data, String childId)
      throws PSConversionException, PSDataExtractionException
   {
      Map<String, PSTransformRunner> runnerMap = m_inputFieldTranslations.get(
         childId);
      if (runnerMap == null)
         return;

      Iterator<String> names = runnerMap.keySet().iterator();
      while (names.hasNext())
      {
         String name = names.next();
         PSTransformRunner transform = runnerMap.get(name);
         List<PSExtensionRunner> runners = transform.getTransforms();
         String errMsg = transform.getErrorMsg();
         if (!StringUtils.isBlank(errMsg))
            errMsg += ": ";
         
         // currently only one exit supported for field translations
         PSExtensionRunner runner = runners.get(0);
         
         try
         {
            Object result = runner.processUdfCallExtractor(data);
            if (result != null)
            {
               PSRequest request = data.getRequest();
               request.setParameter(name, result);
            }
         }
         catch (Exception e)
         {
            Object[] args = {name, errMsg + e.getLocalizedMessage()};
            if (e instanceof PSDataExtractionException)
            {
               throw new PSDataExtractionException(
                  IPSServerErrors.FIELD_TRANSFORM_ERROR, args);               
            }
            
            throw new PSConversionException(
               IPSServerErrors.FIELD_TRANSFORM_ERROR, args);
         }         
      }
   }

   /**
    * Make an internal request to the edit handler of the current contentid,
    * revision and pageid to get the XML document. The result XML document is
    * updated with the parameter values provided to this request.
    * The DisplayError element will be inserted to the top of the root with
    * a generic message.
    * All DisplayField element labels of fields with errors will be replaced
    * with the error label and its type attribute set to error.
    *
    * @param data the execution data which produced the error, assumed not
    *    <code>null</code>.
    * @param errorCollector the error collector containing all information
    *    necessary to create the error page.
    * @param pageId the originating pageid, assumed not <code>null</code>.
    *
    * @throws PSDataExtractionException if there are any problems processing
    * the data.
    * @throws IOException if there are any problems sending the redirect.
    */
   @SuppressWarnings("unchecked")
   private void redirectToError(PSExecutionData data,
      PSErrorCollector errorCollector, Integer pageId)
         throws PSDataExtractionException, IOException
   {
      try
      {
         // get all html parameters and set the pageid and view to the
         // originating page
         PSRequest request = data.getRequest();

         if (request.isSavedParams()) {
            //restore previously saved params, this unwinds pre-exit changes
            request.restoreParams();
         }

         request.setParameter(PSContentEditorHandler.PAGE_ID_PARAM_NAME,
            pageId);
         // add the view so the correct fields will be hidden in the cached doc
         String nextView = m_viewEvaluator.getNextView(data, pageId.intValue());
         String currentView = request.getParameter(IPSHtmlParameters.SYS_VIEW);
         if (currentView == null || !currentView.startsWith(
               IPSConstants.SYS_HIDDEN_FIELDS_VIEW_NAME))
         {
            // only add the view for non-hidden fields views         
            request.setParameter(IPSHtmlParameters.SYS_VIEW, nextView);
         }
         else
         {
            nextView = currentView;
         }

         String reqName = createRequestName(m_ceHandler.getName(),
            m_ce.getName());
         IPSInternalCommandRequestHandler rh =
            (IPSInternalCommandRequestHandler)PSServer.getInternalRequestHandler(
               reqName);

         if (rh != null)
         {
            Document doc = rh.makeInternalRequest(request,
               PSEditCommandHandler.COMMAND_NAME);
            if (doc != null)
            {
               String psredirect = request.getParameter(
                  IPSHtmlParameters.DYNAMIC_REDIRECT_URL);
               if (psredirect!=null && psredirect.trim().length()>0)
               {
                  // add the empty psredirect control to propigate its value.
                  // The mergeParameters method will populate it for us.
                  Element dispNode = PSSingleValueBuilder.createHiddenField(doc,
                     m_hiddenControlName,
                     IPSHtmlParameters.DYNAMIC_REDIRECT_URL, "", false);
                  PSEditorDocumentBuilder.appendDisplayNode(doc, dispNode);
               }

               mergeParameters(doc, request);
               errorCollector.mergeErrors(pageId, doc);

               int cacheId = PSPageCache.addPage(doc);

               String url = request.getRequestFileURL();
               Map params = new HashMap();
               params.put(PSContentEditorHandler.COMMAND_PARAM_NAME,
                  PSEditCommandHandler.COMMAND_NAME);
               params.put(PSContentEditorHandler.CACHE_ID_PARAM_NAME,
                  Integer.toString(cacheId));
               /* need to add the view so conditional stylesheets will be
                * properly selected.
                */
               params.put(IPSHtmlParameters.SYS_VIEW, nextView);
               URL completeUrl = PSUrlUtils.createUrl(null, null, url,
                  params.entrySet().iterator(), null,
                  new PSRequestContext(request));

               sendRedirect(data, completeUrl.toExternalForm());
               request.setParameter(IPSHtmlParameters.SYS_CE_CACHED_PAGEURL,
                     completeUrl.toExternalForm());
               recordErrorToRequest(request, doc);
            }
         }
      }
      catch (MalformedURLException e)
      {
         // should never happen
         throw new PSDataExtractionException(
            IPSServerErrors.CE_NO_REDIRECT_URL);
      }
   }

   /**
    * Record the error message into the supplied request.
    *
    * @param request The request, assume not <code>null</code>.
    *
    * @param errorDoc The error document, assume not <code>null</code> and
    *    the error is in the <code>PSErrorCollector.DISPLAY_ERROR_ELEM</code>
    *    element of the document.
    */
   private void recordErrorToRequest(PSRequest request, Document errorDoc)
   {
      NodeList list = errorDoc.getElementsByTagName(
         PSErrorCollector.DISPLAY_ERROR_ELEM);

      if (list.getLength() <= 0)
         return;

      String errorMsg = "";
      for (int i=0; i < list.getLength(); i++)
      {
         Element errorElem = (Element) list.item(i);
         errorMsg = errorMsg + PSXmlDocumentBuilder.toString(errorElem);
      }

      request.setParameter(IPSHtmlParameters.SYS_VALIDATION_ERROR, errorMsg);
   }

   /**
    * Sets the value element of each control in the supplied document (which
    * must conform to sys_ContentEditor.dtd) to the value of the matching
    * parameter in the supplied request.
    * <p>
    * The provided document is the result of an internal query request made to
    * the same editor which caused the validation error.  After this method
    * merges, the document is exactly the same as the one that was submitted
    * to cause the validation exception.
    * <p>
    * If either method parameter is <code>null</code>, this method does nothing.
    *
    * @param doc the document to be updated with the actual parameter values
    *    supplied with the request, may be <code>null</code>. A document
    *    conforming to the sys_ContentEditor.dtd is expected.
    * @param request the request containing the parameters from where to
    *    update the provided document, may be <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private void mergeParameters(Document doc, PSRequest request)
   {
      // do nothing if any of the provided parameters in null
      if (doc == null || request == null)
         return;

      Map parameters = request.getParameters();
      // get all Control elements
      NodeList controls = doc.getElementsByTagName(
         PSDisplayFieldElementBuilder.CONTROL_NAME);
      for (int i=0; i<controls.getLength(); i++)
      {
         Element control = (Element) controls.item(i);
         String paramName = control.getAttributeNode(
            PSDisplayFieldElementBuilder.PARAMNAME_NAME).getValue();

         Object pv = parameters.get(paramName);
         if (pv instanceof String)
         {
            // is this for a choice list?
            Node firstChild = control.getFirstChild();
            if ((firstChild != null) &&
                firstChild.getNodeName().equals(
                  PSChoiceBuilder.DISPLAYCHOICES_NAME))
            {
               // convert to an array for choice list with single entry
               pv = new ArrayList(1);
               ((ArrayList) pv).add(parameters.get(paramName));
            }
            else
            {
               /*
                * Single values are stored as a child "Value" in the control
                * element. Replace the existing Value element or create a
                * new Value element with the current parameter value.
                */
               String paramValue = (String) pv;
               if (paramValue != null && paramValue.trim().length() > 0)
               {
                  // create the new value element
                  Text text = doc.createTextNode(paramValue);
                  Element value = doc.createElement(
                     PSDisplayFieldElementBuilder.DATA_NAME);
                  value.appendChild(text);

                  NodeList values = control.getElementsByTagName(
                     PSDisplayFieldElementBuilder.DATA_NAME);
                  if (values.getLength() == 0)
                  {
                     // add new value element
                     control.appendChild(value);
                  }
                  else
                  {
                     // replace existing element
                     Element oldValue = (Element) values.item(0);
                     control.replaceChild(value, oldValue);
                  }
               }
            }
         }

         if (pv instanceof ArrayList)
         {
            /*
             * Multiple values are stored as a child "DisplayChoices" of the
             * control element. Walk all DisplayEntry elements of the
             * DisplayChoices and set the selected attribute in the Value
             * element to the current value. If the choice is present in the
             * parameter list the selected attribute is set to yes, otherwise
             * selected is set to no.
             */
            ArrayList paramValues = (ArrayList) pv;
            NodeList choices = control.getElementsByTagName(
               PSChoiceBuilder.DISPLAYCHOICES_NAME);
            if (choices.getLength() > 0 && !paramValues.isEmpty())
            {
               Element choice = (Element) choices.item(0);
               NodeList entries = choice.getElementsByTagName(
                  PSChoiceBuilder.DISPLAYENTRY_NAME);
               for (int m=0; m<entries.getLength(); m++)
               {
                  Element entry = (Element) entries.item(m);
                  String text = PSChoiceBuilder.getFirstTagText(entry,
                     PSChoiceBuilder.DISPLAYVALUE_NAME);
                  boolean found = false;
                  for (int n=0; n<paramValues.size() && !found; n++)
                  {
                     String paramValue = (String) paramValues.get(n);
                     if (paramValue.equalsIgnoreCase(text))
                        found = true;
                  }

                  if (found)
                  {
                     entry.setAttribute(
                        PSChoiceBuilder.SELECTED_ATTRIBUTE_NAME,
                        PSChoiceBuilder.ATTRIB_BOOLEAN_TRUE);
                  }
                  else
                  {
                     entry.setAttribute(
                        PSChoiceBuilder.SELECTED_ATTRIBUTE_NAME,
                        PSChoiceBuilder.ATTRIB_BOOLEAN_FALSE);
                  }
               }
            }
         }
      }
   }

   // see IPSInternalRequestHandler interface for description
   public PSExecutionData makeInternalRequest(PSRequest request)
      throws PSInternalRequestCallException, PSAuthorizationException,
         PSAuthenticationFailedException
   {
      if (request == null)
         throw new IllegalArgumentException("Request must be specified.");

      checkInternalRequestAuthorization(request);

      PSExecutionData data = new PSExecutionData(m_appHandler, this, request);

      try
      {
         int result = executeModifyRequest(request, data);
         
         /*
          * Run post exits only if field validation did not stop the modify 
          * process.
          */
         if (result > -1)
            runPostProcessingExtensions(data, null);

         return data;
      }
      catch (PSException e)
      {
         throw new PSInternalRequestCallException(e.getErrorCode(),
            e.getErrorArguments(),e);
      }
      catch (Exception e)
      {
         throw new PSInternalRequestCallException(
            IPSDataErrors.INTERNAL_REQUEST_CALL_EXCEPTION, getExceptionText(e),e);
      }
   }

   /**
    * Shutdown the command handler, freeing any associated resources.
    */
   public void shutdown()
   {
      // nothing to do
   }

   /**
    * See {@link PSCommandHandler#isUpdate()} for a description of this method.
    *
    * @return <code>true</code> always.
    */
   @Override
   public boolean isUpdate()
   {
      return true;
   }

   /**
    * The modify handler does field validation only.
    *
    * @param data the execution data to validation against, assumed not
    *    <code>null</code>.
    * @param pageId the pageid to be validated, assumed not
    *    <code>null</code>.
    * @param lang the language/locale string to be used to produce the localized
    *    error message. This must follow the XML notation for language or locale
    *    string. See {@link PSI18nUtils#getLocaleFromString} for details. May be
    *    <code>null</code> or <code>empty</code>.
    * @return <code>null</code> if no error occurred, a PSErrorCollector
    *    object otherwise.
    * @throws PSDataExtractionException If any problems occur while getting
    *    values from the execution data.
    */
   private PSErrorCollector processValidation(PSExecutionData data,
      Integer pageId, String lang) throws PSDataExtractionException
   {
      try
      {
         PSErrorCollector errorCollector =
            new PSErrorCollector(PSErrorCollector.TYPE_FIELD,
               getMaxErrorsToStop(pageId));

         if (!processFieldValidation(pageId, errorCollector, data))
         {
            errorCollector.set(
               PSI18nUtils.getString("psx.ce.error@genericFieldError", lang));
         }

         return errorCollector.hasErrors() ? errorCollector : null;
      }
      catch (PSEvaluationException e)
      {
         throw new PSDataExtractionException(e.getErrorCode(),
            e.getErrorArguments());
      }
   }

   /**
    * Creates and adds datasets to the update application to perform all
    * necessary updates.  Builds execution plans for each type of modify
    * that may be performed, adding steps that include the required dataset
    * into each plan.
    *
    * @param mapper The parent display mapper.  Assumed not <code>null</code>.
    *
    * @param mainFieldSet The parent fieldSet.  Assumed not <code>null</code>.
    *
    * @throws SQLException if there is an error determining a column's datatype.
    * @throws PSValidationException if there is an error creating a dataset.
    * @throws PSNotFoundException if a udf or extension cannot be located.
    * @throws PSValidationException if there is a problem starting an internal
    *    application.
    */
   @SuppressWarnings("unchecked")
   private void createDataSets(PSDisplayMapper mapper,
      PSFieldSet mainFieldSet)
      throws SQLException, PSValidationException, PSExtensionException,
         PSNotFoundException
   {
      // create a plan set to hold all modify plans
      PSModifyPlanSet modifyPlanSet = new PSModifyPlanSet();

      // get the field set the display mapper uses
      int setId = mapper.getId();
      String mapperFieldSetRef = mapper.getFieldSetRef();
      PSFieldSet fieldSet = null;
      if (mapperFieldSetRef.equals(mainFieldSet.getName()))
         fieldSet = mainFieldSet;
      else
         fieldSet = getMapperFieldSet(setId);

      // prepare input field translations only
      prepareInputFieldTranslations(setId, mapper, fieldSet);

      // create update and insert plans
      PSModifyPlan updatePlan = m_updatePlanBuilder.createModifyPlan(mapper,
         fieldSet);
      modifyPlanSet.addPlan(updatePlan);

      // add delete plan
      PSModifyPlan deleteItemPlan = m_deletePlanBuilder.createModifyPlan(mapper,
         fieldSet);
      modifyPlanSet.addPlan(deleteItemPlan);
      PSModifyPlan insertPlan = null;
      if (fieldSet.getType() == PSFieldSet.TYPE_PARENT)
      {
         insertPlan = m_insertPlanBuilder.createModifyPlan(mapper,
            fieldSet);
      }
      else if (fieldSet.getType() == PSFieldSet.TYPE_COMPLEX_CHILD)
      {
         insertPlan = m_childInsertPlanBuilder.createModifyPlan(mapper,
            fieldSet);
      }

      modifyPlanSet.addPlan(insertPlan);

      if (fieldSet.getType() == PSFieldSet.TYPE_COMPLEX_CHILD)
      {
         PSModifyPlan deletePlan = m_childDeletePlanBuilder.createModifyPlan(
            mapper, fieldSet);
         modifyPlanSet.addPlan(deletePlan);

         // now set up sequence updates
         if (fieldSet.isSequencingSupported())
         {
            PSModifyPlan seqUpdatePlan = m_sequencePlanBuilder.createModifyPlan(
               mapper, fieldSet);
            modifyPlanSet.addPlan(seqUpdatePlan);

            // need to create a query to retreive current sort rank values
            String queryReqName = createSequenceQueryDataSet(mapper, fieldSet);
            m_seqQueryResources.put(new Integer(setId), queryReqName);
         }
      }

      /* if the current mapper contains any simple children mappers, need
       * to create delete and inserts for them as well, to be processed with
       * the parent data.  Otherwise they need their own datasets created.
       */
      Iterator mappings = mapper.iterator();
      while (mappings.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping)mappings.next();
         PSDisplayMapper childMapper = mapping.getDisplayMapper();
         if (childMapper == null)
            continue;

         // we have a child, see if it is simple or complex
         mapperFieldSetRef = childMapper.getFieldSetRef();
         fieldSet = getMapperFieldSet(childMapper.getId());

         if (fieldSet.getType() == PSFieldSet.TYPE_COMPLEX_CHILD)
            createDataSets(childMapper, mainFieldSet);
         else if(fieldSet.getType() == PSFieldSet.TYPE_SIMPLE_CHILD)
         {
            // create insert and delete plans
            PSModifyPlan simpleDeletePlan =
               m_simpleDeletePlanBuilder.createModifyPlan(childMapper,
               fieldSet);

            PSModifyPlan simpleInsertPlan =
               m_simpleInsertPlanBuilder.createModifyPlan(childMapper,
               fieldSet);

            // add these steps to the appropriate plan
            insertPlan.addAllSteps(simpleInsertPlan);
            updatePlan.addAllSteps(simpleDeletePlan);
            updatePlan.addAllSteps(simpleInsertPlan);
         }
         else
            throw new IllegalStateException("Found invalid fieldset <" +
            fieldSet.getName() + "> for a child mapper referring to <" +
            mapperFieldSetRef + ">.");
      }

      // Add the plan set to the map under this mapper's id
      m_modifyPlanSets.put(new Integer(setId), modifyPlanSet);
   }

   /**
    * Creates a query dataset for getting current sortrank values in a
    * complex child table.  All parameters assumed not <code>null</code>.
    *
    * @param mapper The PSDisplayMapper that references the fieldset that
    * supports sequencing.
    * @param fieldSet The fieldSet that supports sequencing.
    *
    * @return The resource name of a query resource that will query
    * for SYSID and SORTRANK, with SYSID as the key.
    *
    * @throws PSValidationException if there is an error creating the dataset.
    */
   @SuppressWarnings("unchecked")
   private String createSequenceQueryDataSet(PSDisplayMapper mapper,
      PSFieldSet fieldSet) throws PSValidationException
   {
      // get the table
      PSBackEndTable beTable = PSModifyPlanBuilder.getMapperTable(mapper,
         fieldSet);

      try
      {
         // use that to create the columns and param mappings we need
         PSDataMapper dataMapper = new PSDataMapper();
         HashMap selectionKeys = new HashMap();
         ArrayList sortCols = new ArrayList();

         // Add the sysId column
         PSBackEndColumn beCol = new PSBackEndColumn(beTable,
            IPSConstants.CHILD_ITEM_PKEY);
         PSDataMapping dataMapping = new PSDataMapping(
            PSContentEditorHandler.CHILD_ROW_ID_PARAM_NAME, beCol);
         dataMapper.add(dataMapping);

         // Add the SORTRANK column
         beCol = new PSBackEndColumn(beTable,
            IPSConstants.CHILD_SORT_KEY);
         dataMapping = new PSDataMapping(
            PSContentEditorHandler.SORT_RANK_PARAM_NAME, beCol);
         dataMapper.add(dataMapping);
         sortCols.add(new PSSortedColumn(beCol, true));

         // set up selection keys for contentid and revisionid
         beCol = new PSBackEndColumn(beTable,
            IPSConstants.ITEM_PKEY_CONTENTID);
         selectionKeys.put(beCol.getColumn(), m_ceHandler.getParamName(
            PSContentEditorHandler.CONTENT_ID_PARAM_NAME));
         beCol = new PSBackEndColumn(beTable,
            IPSConstants.ITEM_PKEY_REVISIONID);
         selectionKeys.put(beCol.getColumn(), m_ceHandler.getParamName(
            PSContentEditorHandler.REVISION_ID_PARAM_NAME));

         String resourceName = PSApplicationBuilder.createQueryDataset(
            m_internalApp, dataMapper, selectionKeys.entrySet().iterator(),
               sortCols.iterator());

         return resourceName;
      }
      catch (IllegalArgumentException e)
      {
         // won't happen
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }
   }
   
   /**
    * Determines the correct plan to execute.
    *
    * @param data The execution data, assumed not <code>null</code>.
    *
    * @return The plan type to execute, one of the 
    * <code>PSModifyPlan.TYPE_xxx</code> values.  See 
    * {@link com.percussion.cms.PSModifyPlan} for info on plan types.
    *
    * @throws PSRequestValidationException if the plan type cannot be 
    * determined.
    */
   private int getPlanType(PSExecutionData data) 
      throws PSRequestValidationException
   {
      int planType = PSModifyPlan.TYPE_INSERT_PLAN;
      PSRequest request = data.getRequest();
      
      String dbActionType = request.getParameter(
         m_internalApp.getRequestTypeHtmlParamName(), "null");

      if (dbActionType.equals(m_internalApp.getRequestTypeValueInsert()))
         planType = PSModifyPlan.TYPE_INSERT_PLAN;
      else if (dbActionType.equals(m_internalApp.getRequestTypeValueUpdate()))
         planType = PSModifyPlan.TYPE_UPDATE_PLAN;
      else if (dbActionType.equals(m_internalApp.getRequestTypeValueDelete()))
      {
         String childIdParamName = m_ceHandler.getParamName(
            PSContentEditorHandler.CHILD_ID_PARAM_NAME);         
         String strChildId = request.getParameter(childIdParamName, "0");
         if ("0".equals(strChildId))
            planType = PSModifyPlan.TYPE_DELETE_ITEM;
         else
            planType = PSModifyPlan.TYPE_DELETE_COMPLEX_CHILD;
      }
      else if (dbActionType.equals(
                 PSContentEditorHandler.DB_ACTION_SEQUENCE_DECREMENT) ||
               dbActionType.equals(
                 PSContentEditorHandler.DB_ACTION_SEQUENCE_INCREMENT) ||
               dbActionType.equals(
                 PSContentEditorHandler.DB_ACTION_RESEQUENCE))
      {
         planType = PSModifyPlan.TYPE_UPDATE_SEQUENCE;
      }
      else
      {
         Object[] args = {m_internalApp.getRequestTypeHtmlParamName(),
            dbActionType};
         throw new PSRequestValidationException(
            IPSServerErrors.CE_MODIFY_INVALID_PARAM, args);
      }

      return planType;
   }

   /**
    * Calculates a change event action from the plan type.
    * The calculated value is used as a parameter for editor change listenrs. 
    * @param planType the plan type being executed.
    * One of the <code>PSModifyPlan.TYPE_xxx</code> values.
    * @param request current request.
    * Assumed not <code>null</code>.
    * @return the action for the change event.  One of the
    * <code>PSEditorChangeEvent.ACTION_xxx</code> values.
    * If the returned value is {@link PSEditorChangeEvent#ACTION_UNDEFINED},
    * the listeners should not be notified.
    * @see #getPlanType(PSExecutionData)
    */
   static int getAction(final int planType, final PSRequest request)
   {
      // Andriy: static for testing convenience only
      switch (planType)
      {
         case PSModifyPlan.TYPE_INSERT_PLAN:
            return PSEditorChangeEvent.ACTION_INSERT;
         case PSModifyPlan.TYPE_UPDATE_PLAN:
         case PSModifyPlan.TYPE_UPDATE_NO_BIN_PLAN:
            return PSEditorChangeEvent.ACTION_UPDATE;
         case PSModifyPlan.TYPE_UPDATE_SEQUENCE:
            // when children are not specified do not notify 
            // editor change listeners
            final String param = PSContentEditorHandler.CHILD_ROW_ID_PARAM_NAME;
            return request.getParameter(param) == null
                  ? PSEditorChangeEvent.ACTION_UNDEFINED
                  : PSEditorChangeEvent.ACTION_UPDATE;

         case PSModifyPlan.TYPE_DELETE_ITEM:
         case PSModifyPlan.TYPE_DELETE_COMPLEX_CHILD:
            return PSEditorChangeEvent.ACTION_DELETE;
         default:
            throw new IllegalStateException("unknown plan type: " +
               planType);
      }
   }

   /**
    * Retreives and validates the content, revision, and child ids.  If both
    * content and revision ids are not found, assume it's an insert, generate
    * new values and add them to the params.  
    *
    * @param planType The plan type, assumed to be one of the 
    * <code>PSModifyPlan.TYPE_xxx_PLAN</code> values. 
    * @param data The execution data
    * @throws PSRequestValidationException if an id is missing or cannot be
    * converted to an integer.
    * @throws SQLException if there is an error creating a new id.
    * @throws PSAuthorizationException if performing a sequence update and the
    * user is not authorized to query the database.
    * @throws PSInternalRequestCallException if performing a sequence update and
    * an error occurs querying the database.
    * @throws PSAuthenticationFailedException if the user cannot be
    * authenticated.
    */
   @SuppressWarnings("unchecked")
   private void prepareModifyPlan(int planType, PSExecutionData data) throws
      PSRequestValidationException, SQLException,
      PSAuthorizationException, PSInternalRequestCallException,
      PSAuthenticationFailedException
   {
      PSRequest request = data.getRequest();

      String contentIdParamName = m_ceHandler.getParamName(
         PSContentEditorHandler.CONTENT_ID_PARAM_NAME);
      String revisionIdParamName = m_ceHandler.getParamName(
         PSContentEditorHandler.REVISION_ID_PARAM_NAME);
      String childIdParamName = m_ceHandler.getParamName(
         PSContentEditorHandler.CHILD_ID_PARAM_NAME);
      String childRowIdParamName = m_ceHandler.getParamName(
         PSContentEditorHandler.CHILD_ROW_ID_PARAM_NAME);

      // Retrieve keys.  If an insert, generate them and add to the request
      String strContentId = request.getParameter(contentIdParamName);
      String strRevisionId = request.getParameter(revisionIdParamName);
      String strChildRowId = request.getParameter(childRowIdParamName);
      String strChildId = request.getParameter(childIdParamName, "0");

      String dbActionType = request.getParameter(
         m_internalApp.getRequestTypeHtmlParamName(), "null");

      boolean isParent = false;

      PSContentEditorPipe pipe = (PSContentEditorPipe)m_ce.getPipe();
      PSUIDefinition uiDef = pipe.getMapper().getUIDefinition();
      PSDisplayMapper mapper;

      // get the child id, should always have this
      int childId = 0;
      // be sure it's a valid child id
      try
      {
         childId = Integer.parseInt(strChildId);
         mapper = uiDef.getDisplayMapper(childId);
      }
      catch (NumberFormatException e)
      {
         // get the mapper by name if the number fails
         mapper = uiDef.getDisplayMapper(strChildId);
         if (mapper == null)
         {
            // throw exception
            Object[] args = {PSContentEditorHandler.CHILD_ID_PARAM_NAME,
                  strChildId};
            throw new PSRequestValidationException(
               IPSServerErrors.CE_MODIFY_INVALID_PARAM, args);
         }
         childId = mapper.getId();
      }

      PSFieldSet fieldSet = pipe.getMapper().getFieldSet(
         mapper.getFieldSetRef());

      // see if we have the parent mapper
      if (pipe.getMapper().getUIDefinition().getDisplayMapper().getId() ==
         childId)
      {
         isParent = true;
      }
      m_modifyParent.set(isParent);

      /* if inserting, should not have either contentid or revision unless it's
       * a child insert, in which case we need both, but should have no
       * child row id.
       */
      if (planType == PSModifyPlan.TYPE_INSERT_PLAN)
      {
         if (isParent)
         {
            setWorkflowIdForInsert(request);

            if (strContentId != null)
            {
               Object[] args = {contentIdParamName, strContentId};
               throw new PSRequestValidationException(
                  IPSServerErrors.CE_MODIFY_INVALID_PARAM, args);
            }
            else if (strRevisionId != null)
            {
               Object[] args = {revisionIdParamName, strRevisionId};
               throw new PSRequestValidationException(
                  IPSServerErrors.CE_MODIFY_INVALID_PARAM, args);
            }
         }
         else
         {
            if (strContentId == null)
            {
               Object[] args = {contentIdParamName, "null"};
               throw new PSRequestValidationException(
                  IPSServerErrors.CE_MODIFY_INVALID_PARAM, args);
            }
            else if (strRevisionId == null)
            {
               Object[] args = {revisionIdParamName, "null"};
               throw new PSRequestValidationException(
                  IPSServerErrors.CE_MODIFY_INVALID_PARAM, args);
            }
            else if (strChildRowId != null)
            {
               Object[] args = {childRowIdParamName, strChildRowId};
               throw new PSRequestValidationException(
                  IPSServerErrors.CE_MODIFY_INVALID_PARAM, args);
            }
         }
      }
      else if (planType == PSModifyPlan.TYPE_UPDATE_PLAN ||
         planType == PSModifyPlan.TYPE_DELETE_COMPLEX_CHILD ||
         planType == PSModifyPlan.TYPE_UPDATE_SEQUENCE || 
         planType == PSModifyPlan.TYPE_DELETE_ITEM)
      {
         // should have all required keys
         if (strContentId == null)
         {
            Object[] args = {contentIdParamName, "null"};
            throw new PSRequestValidationException(
               IPSServerErrors.CE_MODIFY_INVALID_PARAM, args);
         }
         else if (strRevisionId == null && planType != 
            PSModifyPlan.TYPE_DELETE_ITEM)
         {
            Object[] args = {revisionIdParamName, "null"};
            throw new PSRequestValidationException(
               IPSServerErrors.CE_MODIFY_INVALID_PARAM, args);
         }
         else if (!isParent && strChildRowId == null)
         {
            Object[] args = {childRowIdParamName, "null"};
            throw new PSRequestValidationException(
               IPSServerErrors.CE_MODIFY_INVALID_PARAM, args);
         }
      }

      // this is an insert, create ids and set the params
      if (planType == PSModifyPlan.TYPE_INSERT_PLAN)
      {
         if (isParent)
         {
            // generate content and revision
            int contentId = getNextId(NEXT_CONTENT_ID_KEY);
            int revisionId = 1;  // start this at 1

            request.setParameter(contentIdParamName, String.valueOf(contentId));
            request.setParameter(revisionIdParamName,
               String.valueOf(revisionId));
         }
         else
         {
            /* generate sysid - if allowing multiple inserts, need to generate
             * multiple sysids and multiple sort ranks
             */
            String tableName = PSModifyPlanBuilder.getMapperTable(
               mapper, fieldSet).getTable();

            /* only allow multiple if a complex child that does not contain
             * a simple child
             */
            int listSize = 1;
            boolean isMultiple = false;
            if(PSModifyPlanBuilder.allowMultipleInserts(mapper, fieldSet)
               && (request.hasMultiValuesForAnyParameter()
               && !request.isOnlyMultiValueParam(
                     IPSHtmlParameters.REQ_XML_DOC_FLAG)))
            {
               isMultiple = true;
               listSize = request.getMaxListValuesSize();

               int[] childIds = getNextIdBlock(tableName, listSize);
               ArrayList childIdList = new ArrayList(listSize);
               for (int i = 0; i < childIds.length; i++)
                  childIdList.add(String.valueOf(childIds[i]));

               request.setParameter(childRowIdParamName, childIdList);
            }
            else
            {
               int childRowId = getNextId(tableName);
               request.setParameter(childRowIdParamName,
                  String.valueOf(childRowId));
            }


            // handle new sortrank
            if (fieldSet.isSequencingSupported())
            {
               // gen new sortrank, incrementing the highest current value
               int[][] seqRows = getSequenceValues(childId, data);
               Object sortRank;
               int firstSortRank;

               if (seqRows.length == 0)
                  firstSortRank = 1;
               else
                  firstSortRank = seqRows[seqRows.length - 1][1] + 1;

               // if inserting multiple, need to gen list of sortranks
               if (!isMultiple)
                  sortRank = String.valueOf(firstSortRank);
               else
               {
                  sortRank = new ArrayList(listSize);
                  for (int i = 0; i < listSize; i++)
                     ((ArrayList)sortRank).add(String.valueOf(firstSortRank +
                        i));
               }

               request.setParameter(PSContentEditorHandler.SORT_RANK_PARAM_NAME,
                  sortRank);
            }
         }
      }
      else
      {
         try
         {
            // validate that we have valid keys
            Integer.parseInt(strContentId);
            strContentId = null;
            
            if (planType != PSModifyPlan.TYPE_DELETE_ITEM)
               Integer.parseInt(strRevisionId);               
            strRevisionId = null;

            if (!isParent)
            {
               // handle new sortrank
               if (planType == PSModifyPlan.TYPE_UPDATE_SEQUENCE)
               {
                  if (!fieldSet.isSequencingSupported())
                  {
                     Object[] args = {m_internalApp.getRequestTypeHtmlParamName(),
                        dbActionType};
                     throw new PSRequestValidationException(
                        IPSServerErrors.CE_MODIFY_INVALID_PARAM, args);
                  }

                  // determine resequencing values
                  int[][] seqRows = getSequenceValues(childId, data);

                  if (strChildRowId.indexOf(",") != -1)
                  {
                     // must be an array of indexes, update all the sortranks
                     HashMap sysIdMap = new HashMap();
                     int sortRank = 0;
                     StringTokenizer tok = new StringTokenizer(strChildRowId, ",");
                     while (tok.hasMoreTokens())
                     {
                        sysIdMap.put(tok.nextToken(), "" + sortRank);
                        sortRank++;
                     }
                     setSequenceValues(sysIdMap, seqRows, request);
                  }
                  else
                  {
                     int sysId = Integer.parseInt(strChildRowId);
                     setSequenceValues(sysId, seqRows, dbActionType, request);
                  }
               }
            }
         }
         catch (NumberFormatException e)
         {
            String param = null;
            String value = null;
            if (strContentId != null)
            {
               param = contentIdParamName;
               value = strContentId;
            }
            else if (strRevisionId != null)
            {
               param = revisionIdParamName;
               value = strRevisionId;
            }
            else
            {
               param = childRowIdParamName;
               value = strChildRowId;
            }

            Object[] args = {param, value};
            throw new PSRequestValidationException(
               IPSServerErrors.CE_MODIFY_INVALID_PARAM, args);
         }
      }
   }

   /**
    * Set the workflow id into a given request object (as
    * <code>IPSHtmlParameters.SYS_WORKFLOWID</code> according to the object
    * type of the current content editor. Set it to
    * <code>IPSConstant.INVALIDE_WORKFLOW_ID</code> if the object type of the
    * content editor is not workflowable; otherwise if the workflow id not exist
    * in the request object, its value is set by calculating the default
    * workflow from the user's community and available workflows for this
    * content editor.
    * 
    * @param request The request object, assume not <code>null</code>.
    * @throws PSAuthenticationFailedException
    * @throws PSAuthorizationException
    * @throws PSInternalRequestCallException
    */
   private void setWorkflowIdForInsert(PSRequest request)
         throws PSInternalRequestCallException, PSAuthorizationException,
         PSAuthenticationFailedException
   {
      if (m_ceHandler.getCmsObject().isWorkflowable())
      {
         boolean useDefaultWorkflowId = false;
         String strWorkflowId = request
               .getParameter(IPSHtmlParameters.SYS_WORKFLOWID);
         if (strWorkflowId == null || strWorkflowId.length() == 0)
            useDefaultWorkflowId = true;
         else
         {
            try
            {
               Integer.parseInt(strWorkflowId);
            }
            catch (NumberFormatException e)
            {
               useDefaultWorkflowId = true;
            }
         }

         if (useDefaultWorkflowId)
         {
            int workflowId;

            if (m_ceHandler.getCmsObject().isWorkflowable())
               workflowId = PSCms.getDefaultWorkflowId(request, m_ce);
            else
               workflowId = IPSConstants.INVALID_WORKFLOW_ID;
            request.setParameter(IPSHtmlParameters.SYS_WORKFLOWID, 
                  Integer.toString(workflowId));
         }
      }
      else
      {
         request.setParameter(IPSHtmlParameters.SYS_WORKFLOWID, 
               Integer.toString(IPSConstants.INVALID_WORKFLOW_ID));
      }
   }

   /**
    * Queries the correct table and retrieves a resultset of sysid and
    * sortrank columns, sorted by sortrank ascending.
    *
    * @param mapperId The id of the display mapper that is being processed.
    * @param data The execution data.  Assumed not <code>null</code>.
    *
    * @return A two dimensional integer array, forming a two column table.  The
    * first column is sysId, the second column is sortrank.  May be
    * <code>null</code> if no handler was found for the specified mapperId.
    *
    * @throws PSAuthorizationException if the user is not authorized to execute
    * the query.
    * @throws PSAuthenticationFailedException if the user cannot be
    * authenticated.
    * @throws PSInternalRequestCallException if there is an error executing the
    * query.
    * @throws SQLException if there is an error reading the query results.
    */
   @SuppressWarnings("unchecked")
   private int[][] getSequenceValues(int mapperId, PSExecutionData data)
      throws PSAuthorizationException, PSInternalRequestCallException,
         SQLException, PSAuthenticationFailedException
   {
      int[][] result = null;
      IPSInternalResultHandler rh = null;
      ResultSet rs = null;

      String resourceName = (String)m_seqQueryResources.get(new Integer(
         mapperId));

      PSExecutionData execData = null;
      try
      {
         if (resourceName != null)
         {
            // get the request handler to execute
            rh = (IPSInternalResultHandler)PSServer.getInternalRequestHandler(
                  createRequestName(m_internalApp.getRequestRoot(),
                     resourceName));

            if (rh != null)
            {
               execData = rh.makeInternalRequest(data.getRequest());

               result = new int[0][2];
               ArrayList resultList = new ArrayList();
               rs = rh.getResultSet(execData);
               while(rs.next())
               {
                  int[] row = new int[2];
                  row[0] = rs.getInt(IPSConstants.CHILD_ITEM_PKEY);
                  row[1] = rs.getInt(IPSConstants.CHILD_SORT_KEY);
                  resultList.add(row);
               }
               result = new int[resultList.size()][2];
               resultList.toArray(result);
            }
         }
      }
      finally
      {
         if (rs != null)
            rs.close();
         if (execData != null)
            execData.release();
      }


      return result;
   }


   /**
    * Determine what rows require resequencing, and add arrays of sysid and
    * sortrank values to the param map in the request to cause multiple rows
    * to be updated.  First truncates the parameter map so that we don't
    * have any lists submitted with the original request.
    *
    * @param sysId The childRowId of the row being moved up or down.
    * @param seqRows A two dimensional integer array, representing rows of
    * two columns each, sysId and sortRank respectively.
    * @param dbActionType The value specified to either incrememnt or decrement
    * the sortRank of the row specified by the sysId.  Must be either
    * {@link PSContentEditorHandler#DB_ACTION_SEQUENCE_INCREMENT} or
    * {@link PSContentEditorHandler#DB_ACTION_SEQUENCE_DECREMENT}
    * @param request The request context, assumed not <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private void setSequenceValues(int sysId, int[][] seqRows,
      String dbActionType, PSRequest request)
   {

      /* Need to reset the param map to single values, as it comes in with
       * multiple "rows", one for each row in the summary table.  We don't
       * care about these values, so we need to truncate the map down to
       * single values, and then add lists for the resequencing updates
       * which will expand the params out to lists of the appropriate size.
       *
       * IMPORTANT: In case the interface of request.setParameters() is changed
       * to take in a Map instead of a HashMap one day, we need to construct a
       * new HashMap and pass that in instead of just casting the return of
       * getTruncatedParameters to a HashMap, so we don't get a
       * ClassCastException at runtime.  If it is ever changed, then this code
       * won't need to construct the HashMap and can be made more efficient.
       */
      HashMap newMap = new HashMap(request.getTruncatedParameters());
      request.setParameters(newMap);


      // get the sequence of the specified row
      int curRank = 1;
      int index = 0;
      for (int i = 0; i < seqRows.length; i++)
      {
         if (seqRows[i][0] == sysId)
         {
            curRank = seqRows[i][1];
            index = i;
            break;
         }
      }

      Object sysIdParamValue = null;
      Object sortRankParamValue = null;

      if (dbActionType.equals(
         PSContentEditorHandler.DB_ACTION_SEQUENCE_INCREMENT))
      {
         // see if we cannot really increment, as it is the highest
         if (index == seqRows.length - 1)
         {
            sysIdParamValue = String.valueOf(sysId);
            sortRankParamValue = String.valueOf(curRank);
         }
         else
         {
            // need to bump it up, and move the one above it down one
            sysIdParamValue = new ArrayList(2);
            sortRankParamValue = new ArrayList(2);
            ((ArrayList)sysIdParamValue).add(String.valueOf(sysId));
            ((ArrayList)sortRankParamValue).add(String.valueOf(curRank + 1));
            ((ArrayList)sysIdParamValue).add(String.valueOf(
               seqRows[index + 1][0]));
            ((ArrayList)sortRankParamValue).add(String.valueOf(curRank));
         }
      }
      else
      {
         // see if we cannot really decrement, as it is the lowest
         if (index == 0)
         {
            sysIdParamValue = String.valueOf(sysId);
            sortRankParamValue = String.valueOf(curRank);
         }
         else
         {
            // need to decrement it, and move the one below it up one
            sysIdParamValue = new ArrayList(2);
            sortRankParamValue = new ArrayList(2);
            ((ArrayList)sysIdParamValue).add(String.valueOf(sysId));
            ((ArrayList)sortRankParamValue).add(String.valueOf(curRank - 1));
            ((ArrayList)sysIdParamValue).add(String.valueOf(
               seqRows[index - 1][0]));
            ((ArrayList)sortRankParamValue).add(String.valueOf(curRank));
         }
      }

      request.setParameter(PSContentEditorHandler.CHILD_ROW_ID_PARAM_NAME,
         sysIdParamValue);
      request.setParameter(PSContentEditorHandler.SORT_RANK_PARAM_NAME,
         sortRankParamValue);
   }

   /**
    * Add arrays of sysid and sortrank values to the param map in the
    * request to cause multiple rows to be updated.  First truncates
    * the parameter map so that we don't have any lists submitted with
    * the original request.
    *
    * @param sysIdMap The childRowIds of the rows as the key(String) and
    * the sort rank as the value(String), assumed not <code>null</code>.
    * @param seqRows A two dimensional integer array, representing rows of
    * two columns each, sysId and sortRank respectively, assumed
    * not <code>null</code>.
    * @param request The request context, assumed not <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private void setSequenceValues(Map sysIdMap, int[][] seqRows,
      PSRequest request)
   {
      HashMap newMap = new HashMap(request.getTruncatedParameters());
      request.setParameters(newMap);

      ArrayList sysIdParamValue = new ArrayList();
      ArrayList sortRankParamValue = new ArrayList();

      for (int i = 0; i < seqRows.length; i++)
      {
         // if the current sortrank is different add it
         // to the list of rows to be updated
         int newSortRank = Integer.parseInt(
            (String)sysIdMap.get("" + seqRows[i][0]));
         if (seqRows[i][1] != newSortRank)
         {
            sysIdParamValue.add("" + seqRows[i][0]);
            sortRankParamValue.add("" + newSortRank);
         }
      }
      request.setParameter(PSContentEditorHandler.CHILD_ROW_ID_PARAM_NAME,
            sysIdParamValue);
      request.setParameter(PSContentEditorHandler.SORT_RANK_PARAM_NAME,
            sortRankParamValue);
   }

   /**
    * Returns the field set for a given mapper id.
    * 
    * @param mapperId The id of the mapper.
    * 
    * @return The mapper's fieldset, may be <code>null</code> if it cannot be
    *         located.
    */
   private PSFieldSet getMapperFieldSet(int mapperId)
   {
      PSFieldSet fieldSet = null;

      PSContentEditorPipe pipe =
         (PSContentEditorPipe)m_ce.getPipe();
      PSDisplayMapper mapper =
         pipe.getMapper().getUIDefinition().getDisplayMapper(
            mapperId);

      if (mapper != null)
         fieldSet = pipe.getMapper().getFieldSet(
            mapper.getFieldSetRef());

      return fieldSet;
   }   
     
   /**
    * Constant for this handler's command name.
    */
   public static final String COMMAND_NAME = "modify";

   /**
    * Map of PSModifyPlanSets to use when performing updates. The key is the
    * child id of the display mapper.
    */
   private Map m_modifyPlanSets = new HashMap();

   /**
    * Map of resourceNames to use to query for sequence ids. The key is the
    * child id of the display mapper and value is the request name to use.
    */
   private Map m_seqQueryResources = new HashMap();

   /**
    * Plan builder to create update modify plans. Initialized in the ctor, never
    * <code>null</code> after that.
    */
   private PSModifyPlanBuilder m_updatePlanBuilder = null;

   /**
    * Plan builder to create insert modify plans. Initialized in the ctor, never
    * <code>null</code> after that.
    */
   private PSModifyPlanBuilder m_insertPlanBuilder = null;

   /**
    * Plan builder to create delete modify plans. Initialized in the ctor, never
    * <code>null</code> after that.
    */
   private PSModifyPlanBuilder m_deletePlanBuilder = null;

   /**
    * Plan builder to create child insert modify plans. Initialized in the ctor,
    * never <code>null</code> after that.
    */
   private PSModifyPlanBuilder m_childInsertPlanBuilder = null;

   /**
    * Plan builder to create child delete modify plans. Initialized in the ctor,
    * never <code>null</code> after that.
    */
   private PSModifyPlanBuilder m_childDeletePlanBuilder = null;

   /**
    * Plan builder to create sequence update modify plans. Initialized in the
    * ctor, never <code>null</code> after that.
    */
   private PSModifyPlanBuilder m_sequencePlanBuilder = null;

   /**
    * Plan builder to create simple child delete modify plans. Initialized in
    * the ctor, never <code>null</code> after that.
    */
   private PSModifyPlanBuilder m_simpleDeletePlanBuilder = null;

   /**
    * Plan builder to create simple child insert modify plans. Initialized in
    * the ctor, never <code>null</code> after that.
    */
   private PSModifyPlanBuilder m_simpleInsertPlanBuilder = null;

   /**
    * Used to get correct view when redirecting to error page. Initialized
    * during construction, never <code>null</code> after that.
    */
   private PSViewEvaluator m_viewEvaluator;

   /**
    * Name of the control to use for creating hidden fields in an error document
    * before caching it. Intialized during construction, never <code>null</code>
    * or empty after that.
    */
   private String m_hiddenControlName;

   /**
    * A map of <code>List</code> of <code>PSField</code> objects that need
    * special inline link processing. The map key is the field set name and the
    * map value is a <code>List</code> of <code>PSField</code> objects found
    * in that field set which need special inline handling. The map value list
    * is never <code>null</code> but may be empty. There is one entry for the
    * parent and each complex child. All other child types are included in their
    * respective parent lists. Initialized in constructor, never
    * <code>null</code> or changed after that, may be empty.
    */
   private Map m_inlineLinkFields = null;

   /**
    * A map of fields that need special inline link processing. The map key is
    * the field name as <code>String</code> while the map value is the
    * complete field as <code>PSField</code> object. Never <code>null</code>
    * but may be empty. Initialized in {@link #prepareInlineLinkFields(
    * PSFieldSet, Map)}, never changed after that.
    */
   private Map m_flatInlinelinkFields = new HashMap();

   /**
    * A Thread local object to store a <code>Boolean</code> object,
    * <code>true</code> if it is modifying parent fields; <code>false</code>
    * if it is modifying child fields. It is set in {@link #prepareModifyPlan},
    * never <code>null</code> after that.
    */
   private static ThreadLocal m_modifyParent = new ThreadLocal();

   /**
    * Thread local object to store the to be deleted relationships,
    * <code>PSRelationshipSet</code>, which is set in the
    * {@link #preProcessInlineLinks}. This provides a thread safe way of
    * providing the preprocessed relationships to subsequent processing, e.g.
    * post processing, never code>null</code>.
    */
   private static ThreadLocal m_tlInlineLinkDeletes = new ThreadLocal();

   /**
    * Thread local object to store the to be modified relationships,
    * <code>PSRelationshipSet</code>, which is set in the
    * {@link #preProcessInlineLinks}. This provides a thread safe way of
    * providing the preprocessed relationships to subsequent processing, e.g.
    * post processing, never code>null</code>.
    */
   private static ThreadLocal m_tlInlineLinkModifies = new ThreadLocal();

}
