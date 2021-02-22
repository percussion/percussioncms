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

package com.percussion.data;

import com.percussion.debug.PSDebugLogHandler;
import com.percussion.debug.PSTraceMessageFactory;
import com.percussion.design.objectstore.IPSDocumentMapping;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSDataMapper;
import com.percussion.design.objectstore.PSDataMapping;
import com.percussion.design.objectstore.PSDataSet;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSExtensionParamValue;
import com.percussion.design.objectstore.PSHtmlParameter;
import com.percussion.design.objectstore.PSPageDataTank;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.design.objectstore.PSXmlField;
import com.percussion.error.PSBackEndUpdateProcessingError;
import com.percussion.error.PSErrorException;
import com.percussion.error.PSException;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSApplicationHandler;
import com.percussion.server.PSInvalidRequestTypeException;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestStatistics;
import com.percussion.server.PSUserSession;
import com.percussion.util.PSSqlHelper;
import com.percussion.xml.PSXmlTreeWalker;

import java.sql.Connection;
import java.sql.SQLException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * The PSTransactionSet class is used to group connections into a single
 * transaction. When statements are executed against each connection,
 * failure in any of the statements will cause a roll back in all the
 * connections in the transaction group. Success causes the data to be
 * committed across the transaction group.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSTransactionSet implements IPSExecutionStep
{
   /**
    * Transactional integrity will not be honored.
    */
   public static final int XACT_NONE = 0;

   /**
    * Transactional integrity will be honored at the row level.
    */
   public static final int XACT_EACH_ROW = 1;

   /**
    * Transactional integrity will be honored across all rows.
    */
   public static final int XACT_ALL_ROWS = Integer.MAX_VALUE;


   /**
    * Create a transaction set to handle update, insert and delete
    * processing for the specified data set.
    *
    * @param      ah         the application containing the data set
    *
    * @param      ds         the data set containing the update pipe(s) this
    *                        object will handle
    *
    * @exception   PSInvalidRequestTypeException
    *                        if ds contains no update pipes
    */
   public PSTransactionSet(PSApplicationHandler ah, PSDataSet ds)
      throws PSInvalidRequestTypeException, PSIllegalArgumentException,
            java.sql.SQLException, PSSystemValidationException
   {
      super();

      m_appHandler = ah;

      if (ds.isTransactionForRow())
         m_type = XACT_EACH_ROW;
      else if (ds.isTransactionForAllRows())
         m_type = XACT_ALL_ROWS;
      else // if (ds.isTransactionDisabled())
         m_type = XACT_NONE;

      IPSExecutionStep[] insertPlan = null;
      IPSExecutionStep[] updatePlan = null;
      IPSExecutionStep[] deletePlan = null;

      fixupDataMappings(ah, ds);

      /* build the execution plans for inserting, updating and/or deleting.
       * This is done by creating transaction sets to group sets of
       * related statements. Statements are also built, but they're
       * associated with their corresponding transaction sets. They're not
       * used directly by the handler.
       */
      try {
         insertPlan = PSUpdateOptimizer.createInsertExecutionPlan(ah, ds);
         updatePlan = PSUpdateOptimizer.createUpdateExecutionPlan(ah, ds);
         deletePlan = PSUpdateOptimizer.createDeleteExecutionPlan(ah, ds);
      } catch (PSIllegalArgumentException e) {
         if (e.getErrorCode() == IPSBackEndErrors.EXEC_PLAN_NO_UPDATE_PIPES)
            throw new PSInvalidRequestTypeException(
               e.getErrorCode(), e.getErrorArguments());
         else
            throw e;
      } catch (RuntimeException e) {
         // for unknown exceptions, it's useful to log the stack trace
         Object[] args = { ah.getName(),
            com.percussion.error.PSException.getStackTraceAsString(e) };
         throw new PSSystemValidationException(
            IPSServerErrors.APPLICATION_INIT_EXCEPTION, args,
            ah.getApplicationDefinition(), ds);
      }

      fixupExecutionPlans(ds, insertPlan, updatePlan, deletePlan);

      /* go through the execution plans to see if we're extracting from
       * XML data only or HTML data only. If we're using a mix, we will
       * walk the XML document and assume HTML only contains single values.
       */
      prepareDataWalker(ds);
   }


   /* ************  IPSExecutionStep Interface Implementation ************ */
      
   /**
    * Execute the data modification statements associated with this
    * transaction set as a step in the execution plan.
    *
    * @param      data           the execution data associated with this plan
    *
    * @exception   SQLException   if a SQL error occurs
    */
   public void execute(PSExecutionData data)
      throws java.sql.SQLException,
         com.percussion.error.PSErrorException
   {
      PSRequest request = data.getRequest();
      PSRequestStatistics stats = request.getStatistics();
      PSBackEndUpdateProcessingError beError = null;
      java.lang.Exception encounteredException = null;

      /* for each back-end:
       *
       * 1. login to the back-end
       *
       * 2. set the appropriate transaction model. This may be:
       *
       *    a. no transactions - auto-commit will be used and each error
       *    error will be logged separately
       *
       *    b. any other, auto-commit is disabled
       */

      // perform all the logins and set the transaction model
      for (int i = 0; i < m_logins.length; i++) {
         m_logins[i].execute(data);
         try {
            data.getDbConnection(i).setAutoCommit((m_type == XACT_NONE));
         } catch (PSIllegalArgumentException e) {
            throw new java.sql.SQLException(e.toString());
         }
      }

      /* for each statement:
       *
       * 1. go through the incoming data and execute the statements
       *
       * 2. if row-level transactions are enabled, perform the checkpoint
       *    (commit). Otherwise, process all data.
       *
       * 3. perform the final commit on success or rollback on error
       */
      int onRow = 0;
      IPSExecutionStep[] curPlan = null;
      int curExec = -1;
      final int firstRunFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
      final int nextRunFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;

      /* Is the request using XML data as the source or HTML?
       * At this time, only one is permitted.
       */
      PSXmlTreeWalker walker = null;
      Element curNode = null;
      String rowIterator = m_rootElement;
      Document inDoc = request.getInputDocument();
      if (!m_isXmlDataSource) {
         // we need to build an XML tree from HTML data when this is true
         inDoc = PSHtmlParameterTree.generateHtmlParameterTree(request);
         rowIterator = HTML_BASE_NODE;
      } else if (inDoc == null) {
         /* Throw if no input xml doc, and we are in xmlDataSource mode...
            addresses bug id Rx-00-09-0035 */
         throw new java.sql.SQLException("Xml Document Expected, none supplied");
      }

      if (inDoc != null) {
         walker = new PSXmlTreeWalker(inDoc);
         data.setInputDocumentWalker(walker);

         curNode = (Element)walker.getCurrent();   // should be the root node
         if (rowIterator != null) {
            if ((curNode != null) && !rowIterator.equals(curNode.getNodeName()))
            {   // if the iterator is not the root node, we need to move to
               // the first occurrence of the iterator node
               curNode = walker.getNextElement(rowIterator, firstRunFlags);
            }

            /* if this has any base parts, we need to strip them off
              * for subsequent iterations (eg, if we have x/y and
              * we're now on y, we can't find x/y from here, only more y's
              */
            int pos = rowIterator.lastIndexOf('/');
            if (pos != -1)
            {
               rowIterator = rowIterator.substring(pos+1);
            }
         }
      }

      int requestType = PSApplicationHandler.REQUEST_TYPE_UNKNOWN;
      /* If the designer didn't specify where to get the action type, then
         get it from the HTML param up front and use the same action for all
         rows. */
      if ( null == m_actionTypeXmlField )
         requestType = m_appHandler.getRequestType(request);

      /* we have at least one row of data if we found a node in the tree
       * or we are not doing a tree based traversal (eg, only CGI variables
       * are being used.
       */
      boolean hasMoreRows = (curNode != null) || (rowIterator == null);
      for (; hasMoreRows;
         hasMoreRows = (rowIterator != null) &&
            ((curNode = walker.getNextElement(rowIterator, nextRunFlags)) != null))
      {
         // bump the row count
         onRow++;

         /* If the designer specified an XML element that contains the action
            type, we need to go through the data row by row, figuring out
            if it is an insert, update or delete then executing the
            appropriate plan for each set of data. */
         if ( m_actionTypeXmlField != null )
         {
            String nodeString = walker.getElementData(m_actionTypeXmlField, false);
            requestType = m_appHandler.getRequestType(nodeString);
         }

         // verify this is a request type we know about
         switch (requestType)
         {
            case PSApplicationHandler.REQUEST_TYPE_UPDATE:
               curPlan = m_updatePlan;
               traceRequestType("Update");
               break;

            case PSApplicationHandler.REQUEST_TYPE_INSERT:
               curPlan = m_insertPlan;
               traceRequestType("Insert");
               break;

            case PSApplicationHandler.REQUEST_TYPE_DELETE:
               curPlan = m_deletePlan;
               traceRequestType("Delete");
               break;
            default:
               stats.incrementRowsSkipped();
               traceRequestType("Skip row");
               continue;
         }

         // trace the request type

         /* verify they're permitted to perform this type of action.
          * This is done before checking we have a valid plan as we don't
          * want hackers to know what types of actions this request supports.
          */
         if (!m_appHandler.hasAccess(request, requestType, false)) {
            com.percussion.security.PSAuthorizationException ae
               = new com.percussion.security.PSAuthorizationException(
               m_appHandler.getRequestTypeName(requestType),
               request.getRequestPage(),
               request.getUserSessionId());
            beError = recordFailure(beError, data, null, curNode, ae);
            continue;
         }

         /* this happens if they want to perform an action which is not
          * defined for this set (eg, update request but updating is
          * disabled)
          */
         if (curPlan == null) {
            stats.incrementRowsSkipped();
            continue;
         }

         /* run the execution plan (IPSExecutionStep[]) */
         for (curExec = 0; curExec < curPlan.length; curExec++)
         {
            IPSExecutionStep step = curPlan[curExec];
            String objectIterator = null;
            if (step instanceof PSStatement) {
               PSStatement stmt = (PSStatement)step;
               objectIterator = stmt.getIteratorNode();
            }

            // we always have at least one item for non-tree data
            boolean hasNext = true;

            if (objectIterator != null) {
               // get the first iterator node
               Element firstIteNode = walker.getNextElement(objectIterator, firstRunFlags);
               hasNext = (firstIteNode != null);

               /* if this has any base parts, we need to strip them off
                  * for subsequent iterations (eg, if we have x/y and
                  * we're now on y, we can't find x/y from here, only
                  * more y's
                  */
               int pos = objectIterator.lastIndexOf('/');
               if (pos != -1)
               {
                  objectIterator = objectIterator.substring(pos+1);
               }
            }

            for (   ;
                  hasNext;
                  hasNext = (objectIterator == null) ? false :
                     (walker.getNextElement(objectIterator, nextRunFlags) != null))
            {
               try {
                   step.execute(data);
               } catch (Exception e) {
                  /* we have three possible scenarios here.
                   *
                   * 1. no transactional capabilities are enabled. In this
                   *    case we will move on to the next row as if nothing
                   *    happened.
                   *
                   * 2. row level transactions are supported. We will roll
                   *    back all transactions within this "row". We define
                   *    a row as being a grouping within the XML tree, as
                   *    noted by our rowIterator XML node.
                   *
                   * 3. document level transactions are supported. We must
                   *    roll everything back in this model.
                   *
                   * in any of these cases, we do log the failure
                    */
                  String sqlSource = curPlan[curExec].toString();
                  beError = recordFailure(beError, data, sqlSource, curNode, e);

                  if (m_type == XACT_NONE) {
                     // we carry on business as usual, applying what we can
                     continue;
                  }

                  // for anything else, we're breaking out at least one level
                  encounteredException = e;
                  break;
               }

               // stop processing entries within this row on error
               if (encounteredException != null) {
                  break;
               }
            }  // end of for (; hasNext; ...)

            if (walker != null)
               walker.setCurrent(curNode);   // reset for next block

            // break out of the current execution plan on error
            if (encounteredException != null)
               break;
         } // end of for (curExec = 0; ...)

         if (encounteredException != null) {
            if (m_type == XACT_EACH_ROW) {
               // we rollback everything in the current transaction and
               // jump to the next row in the document
               rollback(data);
               encounteredException = null;   // don't do this again

               // trace rollback
               PSDebugLogHandler dh = m_appHandler.getLogHandler();
               if (dh.isTraceEnabled(
                  PSTraceMessageFactory.RESOURCE_HANDLER_FLAG))
               {
                  Object[] args = {"traceResourceHandler_rollbackRow"};
                  dh.printTrace(
                     PSTraceMessageFactory.RESOURCE_HANDLER_FLAG, args);
               }

               continue;
            }

            // for XACT_ALL_ROWS we will deal with the rollback at
            // the end of this method (when we do standard cleanup)
            break;
         }

          /* if row-level transactions are enabled, perform the
          * checkpoint (commit).
          */
         checkpoint(data, onRow);
      } // end of for (; hasMoreRows; ...)

      // perform the final commit on success or rollback on error
      if (m_type != XACT_NONE)
      {
         for (int i = 0; i < m_logins.length; i++)
         {
            try {
               Connection conn = data.getDbConnection(i);
               if (encounteredException == null)
                        PSSqlHelper.commit(conn); // no error, commit
               else
               {
                        PSSqlHelper.rollback(conn); // error, rollback changes

                  // trace rollback
                  PSDebugLogHandler dh = m_appHandler.getLogHandler();
                        if (dh
                              .isTraceEnabled(PSTraceMessageFactory.RESOURCE_HANDLER_FLAG))
                  {
                           Object[] args =
                           {"traceResourceHandler_rollbackAll"};
                           dh.printTrace(PSTraceMessageFactory.RESOURCE_HANDLER_FLAG,
                                 args);
                  }

               }
               conn.setAutoCommit(true);   // reset this for other users
            } catch (Exception e) {
               if (encounteredException == null)
                  encounteredException = e;
               else if ((encounteredException instanceof SQLException) &&
                        (e instanceof SQLException))
               {
                  ((SQLException)encounteredException).setNextException(
                     (SQLException)e);
               }

               if (i > 0) {
                  /* an exception at this point is a serious problem!!!
                   * if (i == 0), we're on the first back-end. Since the
                   * first one failed, we can simply rollback all the
                   * remaining back-ends. On subsequent back-end, we really
                   * need to un-commit what we've committed. This is
                   * not supported, which is bad news as we've left the data
                   * in an inconsistent state.
                   */
               }
            }
         }
      }

      if (beError != null)
         throw new PSErrorException(beError);
   }



   private void fixupExecutionPlans(
      PSDataSet ds,
      IPSExecutionStep[] insertPlan, IPSExecutionStep[] updatePlan,
      IPSExecutionStep[] deletePlan)
      throws PSInvalidRequestTypeException
   {
      if ((updatePlan != null) && (updatePlan.length == 0))
         updatePlan = null;
      if ((insertPlan != null) && (insertPlan.length == 0))
         insertPlan = null;
      if ((deletePlan != null) && (deletePlan.length == 0))
         deletePlan = null;

      IPSExecutionStep[] loginSource =
         ((updatePlan != null) ? updatePlan :
         ((insertPlan != null) ? insertPlan : deletePlan));

      if (loginSource == null) {
         // ?! no update, insert or delete statements!!!
         Object[] args = { m_appHandler.getName(), ds.getName() };
         throw new PSInvalidRequestTypeException(
            IPSBackEndErrors.EXEC_PLAN_NO_UPDATE_PIPES, args);
      }

      // we'll first get the count of logins then build the login array
      int loginCount = getLoginCount(loginSource);
      m_logins = new PSBackEndLogin[loginCount];
      int index = 0;
      for (int i = 0; i < loginSource.length; i++) {
         if (loginSource[i] instanceof com.percussion.data.PSBackEndLogin)
            m_logins[index++] = (PSBackEndLogin)loginSource[i];
      }

      // now we'll go through each piece and build it's components
      if (updatePlan != null)
         m_updatePlan = removeLoginsFromPlan(updatePlan);

      if (insertPlan != null)
         m_insertPlan = removeLoginsFromPlan(insertPlan);

      if (deletePlan != null)
         m_deletePlan = removeLoginsFromPlan(deletePlan);
   }

   private int getLoginCount(IPSExecutionStep[] plan)
   {
      int loginCount = 0;
       for (int i = 0; i < plan.length; i++) {
         if (plan[i] instanceof com.percussion.data.PSBackEndLogin)
            loginCount++;
      }

      return loginCount;
   }

   private IPSExecutionStep[] removeLoginsFromPlan(IPSExecutionStep[] plan)
   {
      IPSExecutionStep[] retPlan;

      retPlan = new IPSExecutionStep[plan.length - getLoginCount(plan)];
      int index = 0;
       for (int i = 0; i < plan.length; i++) {
         if (!(plan[i] instanceof com.percussion.data.PSBackEndLogin))
            retPlan[index++] = plan[i];
      }

      return retPlan;
   }

   private void checkpoint(PSExecutionData data, int rowsProcessed)
      throws java.sql.SQLException
   {
      // if no transactions, we've dealt with this through auto-commit
      if (m_type == XACT_NONE)
         return;

      // one transaction set, the whole set will be handled at the end of
      // data processing (no checkpoint)
      if (m_type == XACT_ALL_ROWS)
         return;

      // we're either doing it for each row or based on a set of rows
      if ((m_type != XACT_EACH_ROW) &&
         ((rowsProcessed % m_type) != 0))
         return;   // not on a row set boundary

      // we're on a boundary (eg, 100 row set and we're on row 100)
      commit(data);
   }

   private void commit(PSExecutionData data)
      throws java.sql.SQLException
   {
      // if no transactions, we've dealt with this through auto-commit
      if (m_type == XACT_NONE)
         return;

      for (int i = 0; i < m_logins.length; i++)
      {
         try {
               PSSqlHelper.commit(data.getDbConnection(i));
         } catch (PSIllegalArgumentException e) {
            throw new java.sql.SQLException(e.toString());
         }
      }
   }

   private void rollback(PSExecutionData data)
      throws java.sql.SQLException
   {
      // if no transactions, we've dealt with this through auto-commit
      if (m_type == XACT_NONE)
         return;

      for (int i = 0; i < m_logins.length; i++)
      {
         try {
               PSSqlHelper.commit(data.getDbConnection(i));
         } catch (PSIllegalArgumentException e) {
            throw new java.sql.SQLException(e.toString());
         }
      }
   }

   private PSBackEndUpdateProcessingError recordFailure(
      PSBackEndUpdateProcessingError err,
      PSExecutionData data, String sqlSource,
      Element curInDocNode, Exception e)
   {
      PSRequest request = data.getRequest();

      // first we'll log this error
      String sessId = "";
      PSUserSession sess = request.getUserSession();
      if (sess != null)
         sessId = sess.getId();

      int errorCode;
      Object[] errorArgs;

      if (e instanceof PSException) {
         PSException pse = (PSException)e;
         errorCode = pse.getErrorCode();
         errorArgs = pse.getErrorArguments();
      }
      else {
         errorCode = IPSServerErrors.RAW_DUMP;
         errorArgs = new Object[] { PSDataHandler.getExceptionText(e) };
      }

      PSBackEndUpdateProcessingError newErr
         = new PSBackEndUpdateProcessingError(
            m_appHandler.getId(), sessId, errorCode, errorArgs,
            sqlSource, curInDocNode);

      // then we'll append the error to our previous error
      if (err != null)
         err.setNext(newErr);
      else
         err = newErr;

      request.getStatistics().incrementRowsFailed();

      return err;
   }

   private void prepareDataWalker(PSDataSet ds)
      throws PSSystemValidationException
   {
      /* before we start building the plan, we need to check if the action
       * type field was set. By having at least two elements in the tree,
       * we fix bug id TGIS-4BUPSU
       */
      // set the XML field we'll be determining the action type from
      PSPageDataTank pageTank = ds.getPageDataTank();
      if (pageTank != null)
         m_actionTypeXmlField = pageTank.getActionTypeXmlField();

      /* if we have no action type XML field, we'll use the HTML action
       * field. This is also required to fix bug id TGIS-4BUPSU
       */
      if (!m_isXmlDataSource && m_actionTypeXmlField == null )
      {   // in this case, the XML action field is really the HTML param
         PSApplication app = m_appHandler.getApplicationDefinition();
         m_actionTypeXmlField = app.getRequestTypeHtmlParamName();
         // need to give it the proper base
         if (m_actionTypeXmlField != null)
            m_actionTypeXmlField = HTML_BASE_NODE + "/" + m_actionTypeXmlField;
      }

      /* go through the execution plans to see if we're extracting from
        * XML data only or HTML data only. If we're using a mix, we will
        * walk the XML document and assume HTML only contains single values.
        */
      determineNodeIteratorForPlan(m_insertPlan);
      determineNodeIteratorForPlan(m_updatePlan);
      determineNodeIteratorForPlan(m_deletePlan);

      m_appHandler.getLogHandler().write(
         new com.percussion.log.PSLogExecutionPlan(
            m_appHandler.getId(),
            IPSDataErrors.EXEC_PLAN_LOG_UPDATE_XML_WALKER_ROOT,
            new Object[] { m_rootElement }));

      rebaseNodeIteratorForPlan(m_insertPlan);
      rebaseNodeIteratorForPlan(m_updatePlan);
      rebaseNodeIteratorForPlan(m_deletePlan);

      if (m_isXmlDataSource)
      {
         if (m_actionTypeXmlField != null)
         {
            m_actionTypeXmlField = PSXmlTreeWalker.getRelativeFieldName(
               m_rootElement, m_actionTypeXmlField);
         }
      }
      else
      {   // in this case, the XML action field is really the HTML param
         PSApplication app = m_appHandler.getApplicationDefinition();
         m_actionTypeXmlField = app.getRequestTypeHtmlParamName();
      }

      // finally, we need to strip off the root node name to simplify walking
      m_rootElement = stripRootName(m_rootElement);
   }

   private void determineNodeIteratorForPlan(IPSExecutionStep[] plan)
   {
      if (plan == null)
         return;

      java.util.List tempElements = new java.util.ArrayList();
      for (int i = 0; i < plan.length; i++)
      {
         IPSExecutionStep step = plan[i];
         if (step instanceof PSStatement)
         {
            tempElements.clear();

            /* as per bug id TGIS-4BUPSU, we need to consider the action
             * type to avoid basing the tree iterator wrong
             */
            if (m_actionTypeXmlField != null)
               tempElements.add(m_actionTypeXmlField);

            java.util.List extractors
               = ((PSStatement)step).getReplacementValueExtractors();
            int size = (extractors == null) ? 0 : extractors.size();
            for (int j = 0; j < size; j++)
            {
               addXmlFieldNames(
                  tempElements, (IPSDataExtractor)extractors.get(j) );
            }

            if (tempElements.size() > 0) {
               //m_isXmlDataSource = true;
               String baseElement
                  = PSXmlTreeWalker.getLowestLevelElement(tempElements);

               // we can't iterate on attributes, so move up to their parent
               int atPos = baseElement.indexOf('@');
               if (atPos > 0) {
                  if (baseElement.charAt(atPos-1) == '/')
                     baseElement = baseElement.substring(0, atPos-1);
                  else
                     baseElement = baseElement.substring(0, atPos);
               }

               ((PSStatement)step).setIteratorNode(baseElement);

               m_appHandler.getLogHandler().write(
                  new com.percussion.log.PSLogExecutionPlan(
                     m_appHandler.getId(),
                     IPSDataErrors.EXEC_PLAN_LOG_UPDATE_XML_STMT_WALKER,
                     new Object[] { baseElement, step.toString() }));

               for (int j = 0; j < size; j++)
               {
                  IPSDataExtractor extr = (IPSDataExtractor)extractors.get(j);
                  if (extr instanceof PSXmlFieldExtractor)
                  {
                     ((PSXmlFieldExtractor)extr).setXmlFieldBase(baseElement);
                  }
                  else if (extr instanceof PSUdfCallExtractor)
                  {   // taking UDFs into consideration as they may also
                     // contain XML fields (bug id TGIS-4BTW25)
                     ((PSUdfCallExtractor)extr).setXmlFieldBase(baseElement);
                  }
               }

               /* to get the root node, we look at the lowest level element
                * which is shared between the root and the base for this
                * statement. we used to use the base shared by the root
                * and data base element, which usually ended up being the
                * root. This caused problems, as noted in bug id TGIS-4BUPSU
                */
               if (m_rootElement == null)
               {
                  m_rootElement = baseElement;
               }
               else
               {
                  /* TG: removing this code (99-09-22) as it has side effects
                   * when there are multiple statements which cause the
                   * root to get re-based again. This still doesn't feel like
                   * a complete fix, but the QA suite works with this change
                   */
                  // tempElements.clear();
                  // tempElements.add(m_rootElement);
                  // tempElements.add(baseElement);
                  // 
                  // m_rootElement
                  //    = PSXmlTreeWalker.getLowestLevelElement(tempElements);

                  m_rootElement
                     = PSXmlTreeWalker.getBaseElement(m_rootElement, baseElement);
               }
            }
         }
      }
   }

   private void addXmlFieldNames(java.util.List xmlList, IPSDataExtractor extr)
   {
      IPSReplacementValue[] values = extr.getSource();

      for (int k = 0; k < values.length; k++)
      {
         IPSReplacementValue rv = values[k];
         if (rv instanceof PSXmlField)
         {
            String paramName = rv.getValueText();
            if (!xmlList.contains(paramName))
               xmlList.add(paramName);
         }
         else if (rv instanceof PSExtensionCall)
         {   // taking UDFs into consideration as they may also
            // contain XML fields (bug id TGIS-4BTW25)
            PSExtensionParamValue[] paramValues = ((PSExtensionCall)rv).getParamValues();
            int arrayLength = (paramValues == null) ? 0 : paramValues.length;

            for (int i = 0; i < arrayLength; i++){
               IPSReplacementValue rv2 = paramValues[i].getValue();
               if ((rv2 instanceof PSXmlField) || (rv2 instanceof PSExtensionCall))
                  addXmlFieldNames(xmlList, extr);
            }
         }
      }
   }


   private void rebaseNodeIteratorForPlan(IPSExecutionStep[] plan)
   {
      if (plan == null)
         return;

      for (int i = 0; i < plan.length; i++)
      {
         IPSExecutionStep step = plan[i];
         if (step instanceof PSStatement)
         {
            PSStatement stmt = (PSStatement)step;
            // if (m_isXmlDataSource) {
               String node = stmt.getIteratorNode();
               if (node != null) {
                  node = PSXmlTreeWalker.getRelativeFieldName(m_rootElement, node);
                  stmt.setIteratorNode(node);

                  m_appHandler.getLogHandler().write(
                     new com.percussion.log.PSLogExecutionPlan(
                        m_appHandler.getId(),
                        IPSDataErrors.EXEC_PLAN_LOG_REBASED_STMT_WALKER,
                        new Object[] { node, stmt.toString() }));
               }
            // }
         }
      }
   }

   private String stripRootName(String name)
   {
      if (name != null) {
         int pos = name.indexOf('/');
         if (pos == 0)   // in case they're using / before the root name
            pos = name.indexOf('/', 1);
         if (pos != -1)
            name = name.substring(pos+1);
      }

      return name;
   }

   /**
    * Determine whether an XML data source is being used, if not, then try to convert
    * the current data source to an XML data source. The real purpose is to force the
    * data extractor being a PSXmlFieldExtractor.
    */
   private void fixupDataMappings(PSApplicationHandler ah, PSDataSet ds)
      throws PSIllegalArgumentException
   {
      PSDataMapper mapper = ds.getPipe().getDataMapper();
      int mapperSize = (mapper == null) ? 0 : mapper.size();

      PSDataMapping mapping = null;
      IPSDocumentMapping docMapping = null;
      for (int i = 0; i < mapperSize; i++){
         mapping = (PSDataMapping)mapper.get(i);
         docMapping = mapping.getDocumentMapping();
         determineXmlDataSource(docMapping, mapping);
         if (m_isXmlDataSource)
            break;
      }

      if (!m_isXmlDataSource){
         for (int i = 0; i < mapperSize; i++){
            mapping = (PSDataMapping)mapper.get(i);
            docMapping = mapping.getDocumentMapping();
            convertToXmlDataSource(docMapping, mapping);
         }
      }
   }

   private void determineXmlDataSource(Object obj, PSDataMapping mapping)
   {
      m_isXmlDataSource = false;

      if (obj instanceof PSXmlField){
         m_isXmlDataSource = true;
      }
      else if (obj instanceof PSExtensionCall){
         PSExtensionParamValue[] paramValues = ((PSExtensionCall)obj).getParamValues();
         int arrayLength = (paramValues == null) ? 0 : paramValues.length;

         IPSReplacementValue repValue = null;
         for (int i = 0; i < arrayLength; i++){
            repValue = paramValues[i].getValue();
            if (repValue instanceof PSXmlField){
               m_isXmlDataSource = true;
               break;
            }
            else if (repValue instanceof PSExtensionCall){
               determineXmlDataSource(repValue, mapping);
               if (m_isXmlDataSource)
                  break;
            }
         }
      }
   }

   private void convertToXmlDataSource(Object obj, PSDataMapping mapping)
      throws PSIllegalArgumentException
   {
      if (obj instanceof PSHtmlParameter) {
         /* when we're at the mapping level, we can switch the HTML
          * params to XML fields
          */
         mapping.setDocumentMapping(
            makeHtmlParamXmlField((PSHtmlParameter)obj) );
      }
      else if (obj instanceof PSExtensionCall){
         PSExtensionCall udfCall = (PSExtensionCall)obj;

         PSExtensionParamValue[] paramValues = udfCall.getParamValues();
         int arrayLength = (paramValues == null) ? 0 : paramValues.length;

         IPSReplacementValue repValue = null;
         for (int i = 0; i < arrayLength; i++){
            repValue = paramValues[i].getValue();

            if (repValue instanceof PSHtmlParameter)
            {
               /* when we're in the UDF, it's the param value we need to set
                * (part of fix for bug id TGIS-4BTW25)
                */
               paramValues[i].setValue(
                  makeHtmlParamXmlField((PSHtmlParameter)repValue) );
            }
            else if (repValue instanceof PSExtensionCall)
            {
               /* if we're in a UDF which is within a UDF, it's safe to
                * call this recursively on the UDF (UDFs don't change
                * the mapping object
                * (part of fix for bug id TGIS-4BTW25)
                */
               convertToXmlDataSource(repValue, mapping);
            }
         }  // end of for loop
      }
   }

   private PSXmlField makeHtmlParamXmlField(PSHtmlParameter htmlParam)
   {
      return new PSXmlField(htmlParam.getParamValueText());
   }

   private void traceRequestType(String type)
   {
      PSDebugLogHandler dh = m_appHandler.getLogHandler();
      if (dh.isTraceEnabled(PSTraceMessageFactory.RESOURCE_HANDLER_FLAG))
      {
         Object[] args = {type,
            "traceResourceHandler_reqType"};
         dh.printTrace(PSTraceMessageFactory.RESOURCE_HANDLER_FLAG, args);
      }
   }

   private static final String   HTML_BASE_NODE = "PSXParam";


   /**
    * This contains the update execution plan. It is an array containing
    * the steps we must perform, in their appropriate order. It may
    * contain INSERT statements (PSUpdateStatement)
    * and exits (PSExitHandler).
    */
   private IPSExecutionStep[]      m_insertPlan;

   /**
    * This contains the update execution plan. It is an array containing
    * the steps we must perform, in their appropriate order. It may
    * contain UPDATE statements (PSUpdateStatement)
    * and exits (PSExitHandler).
    */
   private IPSExecutionStep[]      m_updatePlan;

   /**
    * This contains the update execution plan. It is an array containing
    * the steps we must perform, in their appropriate order. It may
    * contain DELETE statements (PSUpdateStatement)
    * and exits (PSExitHandler).
    */
   private IPSExecutionStep[]      m_deletePlan;

   /**
    * The XACT_xxx transaction type. This tells us whether or not we need
    * to support transactions, and if so, what the checkpoint is, etc.
    */
   private int                     m_type;

   /**
    * We only need to execute logins once per transaction set. Furthermore,
    * there's only one set of logins regardless of execution plan. That is,
    * insert, update and delete all use the same login plan.
    */
   private PSBackEndLogin[]      m_logins;

   /**
    * The application handler we belong to. This gives us ways to figure
    * out what type of request we're handling (eg, insert).
    */
   private PSApplicationHandler   m_appHandler;

   /**
    * Is the data source XML or HTML (as defined by the mappings)
    */
   private boolean               m_isXmlDataSource;

   /**
    * We build a primary tree walker for the input document traversing the
    * specified root element. This root is considered the "row" of input
    * data (which may be many back-end rows).
    */
   private String                  m_rootElement;

   /**
    * The name of the XML field containing the action type we are performing.
    */
   private String                  m_actionTypeXmlField;
}

