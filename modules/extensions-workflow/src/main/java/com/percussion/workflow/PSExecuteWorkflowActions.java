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
package com.percussion.workflow;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.extension.IPSExtension;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.IPSWorkFlowContext;
import com.percussion.extension.IPSWorkflowAction;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSExtensionRef;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSServer;

import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
/**
 * The PSExecuteWorkflowActions class implements extension handling for the
 * workflow action extensions (Extensions that implement
 * <CODE>IPSWorkflowAction</CODE>). This extension loads (if necessary) and
 * executes all workflow action extensions in the list
 * <CODE>requestContext.getSessionPrivateObject</CODE>.
 * <CODE>(IPSWorkflowAction.WORKFLOW_ACTIONS_PRIVATE_OBJECT)</CODE>.
 */
public class PSExecuteWorkflowActions implements IPSResultDocumentProcessor
{

   /**
    * The fully qualified name of this extension.
    */
   static private String m_fullExtensionName = "";

   /*
    * Flag to indicate indicating that this exit has not been initialized yet.
    */
   static private boolean  m_extensionInitialized = false;

   /* *************  IPSExtension Interface Implementation ************* */

   /**
    * Caches the extension manager instance, and creates an empty hash map for
    * which the full workflow action extension name will be the key, and the
    * executable workflow action extension instance is the value.
    */
   public void init(IPSExtensionDef extensionDef, File codeRoot)
      throws PSExtensionException
   {
      if (!m_extensionInitialized)
      {
       ms_extensionMgr = PSServer.getExtensionManager(null);
       m_wfActionExtensions = new Hashtable();
       m_extensionInitialized = true;
       m_fullExtensionName = extensionDef.getRef().toString();
      }
   }

   /* *******  IPSResultDocumentProcessor Interface Implementation ******* */

   /**
    * Return <CODE>false</CODE>, this extension can not modify the style sheet.
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * Loads (if necessary) and executes all workflow action extensions in the
    * request context workflow action private object. If the extension was not
    * previously loaded, the executable extension will be cached in a hash map
    * for which the extension name is the key. If any of the extensions
    * in the list is not an installed workflow action, none will be
    * executed. An empty workflow action private object will result in an
    * exception, however it is not an error if the private object does not
    * exist.
    *
    * @param  params          the parameters for this extension. Should be
    *                         <CODE>null</CODE> or of size 0, because this
    *                         extension does not have any parameters.
    * @param requestContext   the context of the request associated with this
    *                         extension
    *                         <ul>If there are transition workflow action
    *                         extensions to be performed, the following two
    *                         private objects must be present:
    *               <li>workflow context - key <CODE>
    *                         IPSWorkFlowContext.WORKFLOW_CONTEXT_PRIVATE_OBJECT
    *                         </CODE>
    *                         </li>
    *                         <li>workflow action extensions list - key <CODE>
    *                         IPSWorkflowAction.WORKFLOW_ACTIONS_PRIVATE_OBJECT
    *                         </CODE>
    *                         </li>
    *                         </ul>
    * @param  resultDoc       the result XML document (may be <CODE>null</CODE>
    *                         because it will be ignored)
    *
    * @return                 <code>resultDoc</code> is returned unchanged
    *
    * @throws                 PSParameterMismatchException
    *                         if any parameters are supplied.
    * @throws                 PSExtensionProcessingException if
    *                         <ul>
    *               <li>a list of workflow actions exists, but the
    *                         workflow context does not.
    *               </li>
    *                         <li>an extension on the list is not a workflow
    *                         action. (Does not  implement
    *                         <CODE>IPSWorkflowAction</CODE>)
    *               </li>
    *                         <li>an extension on the list is not installed
    *               </li>
    *                         <li>an extension on the list cannot be loaded
    *               </li>
    *                         <li>an exception is thrown while executing one
    *                         of the extensions.
    *               </li>
    *               </ul>
    */
   public Document processResultDocument(Object[] params,
                                         IPSRequestContext requestContext,
                                         Document resultDoc)
      throws PSParameterMismatchException, PSExtensionProcessingException
   {
      String lang = (String)requestContext.getSessionPrivateObject(
       PSI18nUtils.USER_SESSION_OBJECT_SYS_LANG);
      if (lang == null)
         lang =   PSI18nUtils.DEFAULT_LANG;

      PSWorkFlowUtils.printWorkflowMessage(requestContext,
         "\nPSExecute WorkflowActions: enter processResultDocument ");
      
       int size = (params == null) ? 0 : params.length;
       if (size != 0)
       { // no parameters are required
          throw new PSParameterMismatchException(lang, size, 0);
       }

       if (null == requestContext)
       {
          throw new PSExtensionProcessingException(
               m_fullExtensionName,
               new IllegalArgumentException("The request must not be null"));
       }

       List workflowActions = (List)
           requestContext.getPrivateObject
           (IPSWorkflowAction.WORKFLOW_ACTIONS_PRIVATE_OBJECT);
       // If there are no workflow actions we are done
       if (null == workflowActions)
       {
          PSWorkFlowUtils.printWorkflowMessage(
             requestContext,
             "No workflow actions.\n" +
             "PSExecute WorkflowActions: exit processResultDocument ");
          return resultDoc;
       }

       // If the workflow action list is empty, it is an error
       if (0 == workflowActions.size())
       {
          String key =
           Integer.toString(IPSExtensionErrors.WKFLOW_ACTIONLIST_EMPTY);
          String msg = PSI18nUtils.getString(key, lang);
          throw new PSExtensionProcessingException(lang, m_fullExtensionName,
           new Exception(msg));
       }

       PSWorkFlowContext wfContext = (PSWorkFlowContext)
           requestContext.getPrivateObject
           (IPSWorkFlowContext.WORKFLOW_CONTEXT_PRIVATE_OBJECT);

       // If there is no workflow context it is an error
       if (null == wfContext)
       {
          String key =
           Integer.toString(IPSExtensionErrors.WKFLOW_CONTEXT_NULL);
          String msg = PSI18nUtils.getString(key, lang);
          throw new PSExtensionProcessingException(
               m_fullExtensionName, new Exception(msg));
       }

       /*
        * Make sure the extensions are workflow extensions, loading them
        * if necessary and caching the executable extensions.
        */

       Iterator wfaIter = workflowActions.iterator();
       String wfActionFullName = "";

       while (wfaIter.hasNext())
       {
          try
          {
             // make stuff into separate method
             wfActionFullName = (String) wfaIter.next();
             if (null != m_wfActionExtensions.get(wfActionFullName))
             {
                continue;
             }
             else
             {
                PSExtensionRef ref = new PSExtensionRef(wfActionFullName);
                IPSExtension ext = ms_extensionMgr.prepareExtension(ref, null);
                if (!(ext instanceof IPSWorkflowAction))
                {
                   // fix except type
                   String key =
                    Integer.toString(IPSExtensionErrors.INVALID_WKFLOW_EXT);
                   String msg = PSI18nUtils.getString(key, lang);
                   throw new PSExtensionProcessingException(lang,
                    m_fullExtensionName,  new Exception(msg));
                }
                m_wfActionExtensions.put(wfActionFullName,
                                        (IPSWorkflowAction) ext);

             }
          }
          catch (PSNotFoundException e)
          {
             String language = e.getLanguageString();
             if (language == null)
                language = PSI18nUtils.DEFAULT_LANG;
             throw new PSExtensionProcessingException(language,
              m_fullExtensionName, e);
          } catch (PSExtensionException e)
          {
             String language = e.getLanguageString();
             if (language == null)
                language = PSI18nUtils.DEFAULT_LANG;
             throw new PSExtensionProcessingException(language,
              m_fullExtensionName, e);
          }
       }
       
      /*
       * Make sure that these workflow actions are not executed again with 
       * the same request.
       */
      requestContext.setPrivateObject(
        IPSWorkflowAction.WORKFLOW_ACTIONS_PRIVATE_OBJECT, null);
      
      // Now perform all the workflow actions
      wfaIter = workflowActions.iterator();
      while (wfaIter.hasNext())
      {
         wfActionFullName = (String) wfaIter.next();
         IPSWorkflowAction wfActionExtension =
            (IPSWorkflowAction) m_wfActionExtensions.get(wfActionFullName);
         /*
         * This should never happen, since we loaded the extensions
         * in the previous while loop.
         */
         if (null == wfActionExtension)
         {
            String key = Integer.toString(IPSExtensionErrors.EXEC_EXT_NOTFOUND);
            String msg = PSI18nUtils.getString(key, lang);
      
            throw new PSExtensionProcessingException(
               m_fullExtensionName,
               new Exception(msg));
         }
      
         // Perform the action!!
         wfActionExtension.performAction(wfContext, requestContext);
      }
      
      PSWorkFlowUtils.printWorkflowMessage(
         requestContext,
         "PSExecute WorkflowActions: exit processResultDocument ");
      return resultDoc;
   }

   /**
    * The unique system extension manager.
    */
    private static IPSExtensionManager ms_extensionMgr = null;

   /**
    * The map from the full extension name to the executable extension workflow
    * action extension that implements <CODE>IPSWorkflowAction<\CODE>. Shared
    * by multiple threads and HashTable is used instead of HashMap.
    * .
    */
   private Hashtable m_wfActionExtensions;
}
