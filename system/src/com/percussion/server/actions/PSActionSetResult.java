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

package com.percussion.server.actions;

import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Tracks the results of the execution of a single action set.  Each action
 * in the set has its status tracked: success, failure, or skipped.  The results
 * can be reported as XML.
 *
 * @see PSActionSet
 */
public class PSActionSetResult
{
   /**
    * Constructs a new <code>PSActionSetResult</code> to track the execution
    * of the provided action set.  Initializes each action's status to
    * {@link #SKIPPED_STATUS}.
    *
    * @param actionSet set whose results will be tracked, not <code>null</code>.
    * @param contentEditorUrl the URL of the content editor receiving the
    * actions, not <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if <code>actionSet</code> is <code>null
    * </code>, or if <code>contentEditorUrl</code> is <code>null</code> or
    * empty.
    */
   public PSActionSetResult(PSActionSet actionSet, String contentEditorUrl)
   {
      if (actionSet == null)
         throw new IllegalArgumentException( "actionSet may not be null" );
      if (contentEditorUrl == null || contentEditorUrl.trim().length() == 0)
         throw new IllegalArgumentException(
            "contentEditorUrl may not be null or empty" );

      m_actionSetName = actionSet.getName();
      m_originalHref = contentEditorUrl;
      m_actionNames = new ArrayList();
      m_actionResults = new HashMap();

      Iterator actions = actionSet.getActions();
      while (actions.hasNext())
      {
         PSAction action = (PSAction) actions.next();
         m_actionNames.add( action.getName() );
         setSkipped( action.getName() );
      }
   }


   /**
    * Generates an XML representation of the results of the action set's
    * execution in the following format:
    *
    * <code><pre>
    * &lt;!--
    * This element is a container for the results of all actions specified in an
    * ActionSet. There will be 1 child for each action in the original set with
    * the status set to 'success', 'error' or 'skipped'.
    * -->
    * &lt;!ELEMENT StoredActionResults (ActionResult+)>
    * &lt;!ATTLIST StoredActionResults
    *    actionSetName CDATA #REQUIRED
    *    originalHref CDATA #REQUIRED
    * >
    *
    * &lt;!--
    * This element contains the results from a single action, either the
    * statistics or the error information. If the status is 'skipped', then this
    * element will have no child.
    *
    * Attributes:
    * actionName - The name of the action associated with this result. If no
    *    name was supplied, defaults to the text 'unnamed'.
    *
    * status - A flag to indicate whether the action was successful or not. If
    *    the value is 'success', the child element will be Stats. If the value
    *    is 'error', the child element will be Error.  All actions after an
    *    'error' action have the status 'skipped'.
    * -->
    * &lt;!ELEMENT ActionResult ((PSXExecStatistics | Error)?)>
    * &lt;!ATTLIST ActionResult
    *    actionName CDATA #REQUIRED
    *    status (succeeded|skipped|failed) "succeeded"
    * >
    *
    * &lt;!--
    * This element contains the result statistics from a successful update. This
    * tag is a placeholder for future enhancements.
    * -->
    * &lt;!ELEMENT PSXExecStatistics EMPTY>
    *
    * &lt;!--
    * This element contains all of the available information about an error that
    * occurred during the processing of a single action. If the Description or
    * Callstack is not available, it will be empty.
    *
    * Attributes:
    * className - The fully-qualified class name of the exception that occurred.
    * -->
    * &lt;!ELEMENT Error (Description, Callstack)>
    * &lt;!ATTLIST Error
    *    className CDATA #REQUIRED
    * >
    *
    * &lt;!--
    * The message text associated with the exception. For chained exceptions,
    * the text from all of them is concatenated. May be empty.
    * -->
    * &lt;!ELEMENT Description (#PCDATA)>
    *
    * &lt;!--
    * A textual representation of the callstack of the thread when the error
    * occurred. This may not be available for all errors.
    * -->
    * &lt;!ELEMENT Callstack (#PCDATA)>
    * </pre><code>
    *
    * @return a new document in the specified format.  never <code>null</code>.
    */
   public Document toXml()
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot( doc, "StoredActionResults" );
      root.setAttribute( "actionSetName", m_actionSetName );
      root.setAttribute( "originalHref", m_originalHref );
      for (Iterator results = m_actionNames.iterator(); results.hasNext();)
      {
         String actionName = (String) results.next();
         ActionResult result = (ActionResult) m_actionResults.get( actionName );
         Element actionEl = doc.createElement( "ActionResult" );
         actionEl.setAttribute( "actionName", actionName );
         actionEl.setAttribute( "status", result.getStatusString() );
         Object resultObject = result.getResult();
         if (resultObject instanceof Exception)
         {
            Exception e = (Exception) resultObject;
            Element errorEl = doc.createElement( "Error" );
            errorEl.setAttribute( "className", e.getClass().toString() );
            PSXmlDocumentBuilder.addElement( doc, errorEl, "Description",
               e.getLocalizedMessage() );
            StringWriter callstack = new StringWriter();
            e.printStackTrace( new PrintWriter( callstack ) );
            PSXmlDocumentBuilder.addElement( doc, errorEl, "Callstack",
               callstack.toString() );
            actionEl.appendChild( errorEl );
         }
         else if (resultObject instanceof Document)
         {
            // for future use, when update statistics can be tracked
            actionEl.appendChild( ((Document) resultObject).getDocumentElement() );
         }
         root.appendChild( actionEl );
      }
      return doc;
   }


   /**
    * @return the XML representation of these results as a string, may be
    * <code>null</code> if any IO exception occurs
    *
    * @see #toXml()
    */
   public String toString()
   {
      StringWriter out = new StringWriter();
      PSXmlTreeWalker tw = new PSXmlTreeWalker( toXml() );
      try
      {
         tw.write( out );
      }
      catch (IOException ioe)
      {
         return null;
      }

      return out.toString();
   }


   /**
    * Assigns the specified action to {@link #FAILED_STATUS}.
    *
    * @param actionName name of the action that failed, not <code>null</code>
    * or empty.
    * @param error the error generated by the failed action, may be
    * <code>null</code> if unknown.
    * @throws IllegalArgumentException if actionName is <code>null</code> or
    * empty.
    */
   public void setFailed(String actionName, Exception error)
   {
      if (actionName == null || actionName.trim().length() == 0)
         throw new IllegalArgumentException( "action name may not be null" );
      ActionResult result = new ActionResult( FAILED_STATUS, error );
      m_actionResults.put( actionName, result );
   }


   /**
    * Assigns the specified action to {@link #SKIPPED_STATUS}.
    *
    * @param actionName name of the action was skipped, not <code>null</code>
    * or empty.
    * @throws IllegalArgumentException if actionName is <code>null</code> or
    * empty.
    */
   public void setSkipped(String actionName)
   {
      if (actionName == null || actionName.trim().length() == 0)
         throw new IllegalArgumentException( "action name may not be null" );
      ActionResult result = new ActionResult( SKIPPED_STATUS, null );
      m_actionResults.put( actionName, result );
   }


   /**
    * Assigns the specified action to {@link #SUCCESS_STATUS}.
    *
    * @param actionName name of the action that succeeded, not <code>null</code>
    * or empty.
    * @param statistics XML describing the number of rows inserted, updated,
    * etc.  May be <code>null</code> if not supported.
    * @throws IllegalArgumentException if actionName is <code>null</code> or
    * empty.
    */
   public void setSuccess(String actionName, Document statistics)
   {
      if (actionName == null || actionName.trim().length() == 0)
         throw new IllegalArgumentException( "action name may not be null" );
      ActionResult result = new ActionResult( SUCCESS_STATUS, statistics );
      m_actionResults.put( actionName, result );
   }


   /**
    * Gets the result assigned to the specified action.  Package protection so
    * this method can be called by the unit test.
    *
    * @param actionName name of the action, not <code>null</code> or empty.
    *
    * @return the results associated with the specified action, not
    * <code>null</code>.
    *
    * @throws IllegalArgumentException if actionName is <code>null</code> or
    * empty or if no results are registered for that name.
    */
   ActionResult getResult(String actionName)
   {
      if (actionName == null || actionName.trim().length() == 0)
         throw new IllegalArgumentException( "action name may not be null" );
      if (!m_actionResults.containsKey( actionName ))
         throw new IllegalArgumentException( "action '" + actionName +
            "' is not part of this result set." );

      return (ActionResult) m_actionResults.get( actionName );
   }


   /**
    * Inner-class to be used as the value of the action results Map since
    * two values need to be rememebered -- a status flag (success, skipped,
    * or failed) and an object associated with that status (an Exception if
    * failed, for example).
    */
   public class ActionResult
   {
      /**
       * Construct a new result object to track the specified status and
       * object.
       *
       * @param status the type of result, one of the
       * <code>{SUCCESS,SKIPPED,FAILED}_STATUS</code> values.
       * @param result an object that elaborates on the status, an
       * <code>Exception</code> if the status is failed, or a
       * <code>Document</code> if the status is success.
       * May be <code>null</code>.
       *
       * @throws IllegalArgumentException if <code>status</code> is not one of
       * the <code>{SUCCESS,SKIPPED,FAILED}_STATUS</code> values.
       */
      public ActionResult(int status, Object result)
      {
         if (status != SUCCESS_STATUS && status != FAILED_STATUS &&
             status != SKIPPED_STATUS)
            throw new IllegalArgumentException(
               "must use one of the _STATUS codes");

         m_status = status;
         m_result = result;
      }


      /**
       * Gets the (optional) status object assigned to this result.
       *
       * @return the status object assigned to this result, an <code>Exception
       * </code> if the status is failed, <code>null</code> if the status is
       * skipped, and a <code>Document</code> if the status is success.
       * May be <code>null</code> if no object has been assigned.
       */
      public Object getResult()
      {
         return m_result;
      }


      /**
       * Gets the status assigned to this result.
       *
       * @return the status assigned to this result, one of the
       * <code>{SUCCESS,SKIPPED,FAILED}_STATUS</code> values.
       */
      public int getStatus()
      {
         return m_status;
      }


      /**
       * Gets the string representation of the status flag assigned to this
       * result.
       *
       * @return the string representation of the status flag assigned to this
       * result, never <code>null</code> or empty.
       */
      public String getStatusString()
      {
         String status = null;
         switch (m_status)
         {
            case SUCCESS_STATUS:
               status = "succeeded";
               break;

            case FAILED_STATUS:
               status = "failed";
               break;

            case SKIPPED_STATUS:
               status = "skipped";
               break;

            default:
               throw new RuntimeException( "BUG unknown status: " + m_status );
         }
         return status;
      }


      /**
       * Result could be PSExecStatistics (success) or Exception (failure) or
       * <code>null</code> (skipped).
       */
      private Object m_result;

      /**
       * One of the {SUCCESS,SKIPPED,FAILED}_STATUS flags
       */
      private int m_status;
   }

   /** Value that indicates an action has successfully been processed. */
   public static final int SUCCESS_STATUS = 0;

   /** Value that indicates an action has been skipped. */
   public static final int SKIPPED_STATUS = 1;

   /** Value that indicates an action has failed. */
   public static final int FAILED_STATUS = 2;

   /**
    * Name of the action set these results are for.  Used in generating the
    * result XML.  Assigned in the ctor, and never <code>null</code> after.
    */
   private String m_actionSetName;

   /**
    * The URL of the content editor to which the action set is being applied.
    * Assigned in the ctor, and never <code>null</code> or empty after.
    */
   private String m_originalHref;

   /**
    * Maintains the result for each action, keyed by the action's name
    * (<code>String</code>).  The value is the inner-class <code>ActionResult
    * </code>, as both a status flag and a status object need to be maintained.
    * Assigned in the ctor, and never <code>null</code> after.
    */
   private Map m_actionResults;

   /**
    * Maintains the correct order of the actions (since a <code>Map</code> does
    * not) so the results can be generated in the correct order.  Assigned in
    * the ctor, and never <code>null</code> after.
    */
   private List m_actionNames;
}
