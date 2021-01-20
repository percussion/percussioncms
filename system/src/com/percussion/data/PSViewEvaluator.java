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

import com.percussion.cms.IPSConstants;
import com.percussion.cms.handlers.PSContentEditorHandler;
import com.percussion.cms.handlers.PSModifyCommandHandler;
import com.percussion.cms.handlers.PSQueryCommandHandler;
import com.percussion.design.objectstore.PSConditionalView;
import com.percussion.design.objectstore.PSView;
import com.percussion.design.objectstore.PSViewSet;
import com.percussion.server.PSRequest;
import com.percussion.util.IPSHtmlParameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * This class is used to process view information at run time, determining if
 * a specified field should appear in the current view, and to determine the
 * next view to use (for example when building links in a result document).
 */
public class PSViewEvaluator
{
   /**
    * Creates an evaluator for the supplied view set.
    *
    * @param viewSet The set of views.  May not be <code>null</code>.
    */
   public PSViewEvaluator(PSViewSet viewSet)
   {
      if (viewSet == null)
         throw new IllegalArgumentException("viewSet may not be null");

      m_viewSet = viewSet;

      Iterator condViewNames = m_viewSet.getConditionalViewNames();
      while (condViewNames.hasNext())
      {
         String viewName = (String)condViewNames.next();
         List evals = new ArrayList();
         Iterator condViews = m_viewSet.getCondtionalViews(viewName);
         while (condViews.hasNext())
         {
            PSConditionalView condView = (PSConditionalView)condViews.next();
            evals.add(new PSConditionalViewEvaluator(condView));
         }
         if (!evals.isEmpty())
            m_viewEvaluators.put(viewName, evals);
      }
   }

   /**
    * Determines if the specified field should be visible in the current view.
    * Comparison is case insensitive.
    *
    * @param fieldName The name of the field to check.  May not be
    * <code>null</code> or empty.
    * @param data The execution data.  May not be <code>null</code>.
    *
    * @return <code>true</code> if the specified <code>fieldName</code> can be
    * found in the current view's list of fields.  <code>false</code> otherwise.
    */
   public boolean isFieldVisible(String fieldName, PSExecutionData data)
      throws PSDataExtractionException
   {
      if (fieldName == null || fieldName.trim().length() == 0)
         throw new IllegalArgumentException(
            "fieldName may not be null or empty");

      if (data == null)
         throw new IllegalArgumentException("data may not be null");

      // get the current view
      PSView curView = getCurrentView(data);

      return isFieldVisible(fieldName, curView.getFields());
   }

   /**
    * Returns the view name to use for subsequent requests when creating links
    * for the current result document.  Current logic is hard-coded based on the
    * following rules:
    *
    * <ol>
    * <li>If the modify command was specified and the current view is not
    * the default view, return <code>sys_All</code></li>
    * <li>If on a child page and the current view is not
    * <code>sys_Default</code>, return <code>sys_All</code></li>
    * <li>Otherwise, return the current view.</li>
    * </ol>
    *
    * @param data The execution data.  May not be <code>null</code>.
    * @param pageId The page id of the current page.
    *
    * @return The next view name, never <code>null</code> or empty.
    */
   public String getNextView(PSExecutionData data, int pageId)
      throws PSDataExtractionException
   {
      if (data == null)
         throw new IllegalArgumentException("data may not be null");

      PSView curView = getCurrentView(data);
      String nextView = curView.getName();

      /* if on a modify request, then we are making an internal request to get
       * the error page, so make sure they get back all fields.  If current view
       * is not sys_Default, return sys_All.
       */
      if (PSModifyCommandHandler.COMMAND_NAME.equals(
         data.getRequest().getParameter(
            PSContentEditorHandler.COMMAND_PARAM_NAME)) &&
         !nextView.equals(IPSConstants.DEFAULT_VIEW_NAME))
      {
         nextView = IPSConstants.SYS_ALL_VIEW_NAME;
      }
      // if on a child page, force view to sys_All if not sys_Default
      else if (pageId != PSQueryCommandHandler.ROOT_PARENT_PAGE_ID &&
         !nextView.equals(IPSConstants.DEFAULT_VIEW_NAME))
      {
         nextView = IPSConstants.SYS_ALL_VIEW_NAME;
      }

      return nextView;
   }

   /**
    * Gets the current view as specified in the <code>data</code>.  Uses the
    * default view if the data does not specify a view name.
    *
    * @param data The execution data, assumed not <code>null</code>
    *
    * @return The view, never <code>null</code>.
    *
    * @throws PSDataExtractionException if the current view as specified in the
    * <code>data</code> is not found in this evaluator's viewset.
    */
   private PSView getCurrentView(PSExecutionData data)
      throws PSDataExtractionException
   {
      String viewName = getCurrentViewName(data);

      PSView curView = null;

      // if any conditional views, see if one matches
      List evalList = (List)m_viewEvaluators.get(viewName.toLowerCase());
      if (evalList != null)
      {
         Iterator evals = evalList.iterator();
         while (evals.hasNext())
         {
            PSConditionalViewEvaluator eval =
               (PSConditionalViewEvaluator)evals.next();
            if (eval.isMatch(data))
            {
               curView = eval.getView();
               break;
            }
         }
      }

      // no conditional matches, so get the default view for that name
      if (curView == null)
         curView = m_viewSet.getView(viewName);

      if (curView == null)
         throw new PSDataExtractionException(IPSDataErrors.VIEW_NOT_FOUND,
            viewName);

      return curView;
   }


   /**
    * Gets the name of the current view as specified in the <code>data</code>.
    * If the view is not specified, will return the default view name.  This
    * has the side effect of setting the value of the default view name in the
    * request parameters if an empty view name has been specified.
    *
    * @param data The execution data, assumed not <code>null</code>
    *
    * @return The view name, never <code>null</code> or empty.  If not specified
    * or an empty value has been specified,
    * {@link IPSConstants#DEFAULT_VIEW_NAME} is returned.
    */
   private String getCurrentViewName(PSExecutionData data)
   {
      PSRequest request = data.getRequest();
      String viewName = request.getParameter(
         IPSHtmlParameters.SYS_VIEW, IPSConstants.DEFAULT_VIEW_NAME);

      // An exit may have put an empty value in the param map
      if(viewName.trim().length() == 0)
      {
         viewName = IPSConstants.DEFAULT_VIEW_NAME;
         request.setParameter(IPSHtmlParameters.SYS_VIEW, viewName);
      }

      return viewName;
   }
   /**
    * Determines if the specified field is contained in the list of fields.
    * Comparison is case insensitive.
    *
    * @param fieldName The name of the field to check.  Assumed not
    * <code>null</code> or empty.
    * @param fields An iterator over zero or more field names as
    * <code>String</code> objects, assumed not <code>null</code> and not to
    * contain <code>null</code> or empty entries.
    *
    * @return <code>true</code> if the specified <code>fieldName</code> can be
    * found in the list of fields.  <code>false</code> otherwise.
    */
   private boolean isFieldVisible(String fieldName, Iterator fields)
   {
      boolean isVisible = false;

      while(fields.hasNext() && !isVisible)
      {
         if (fields.next().toString().equalsIgnoreCase(fieldName))
            isVisible = true;
      }

      return isVisible;
   }

   /**
    * The viewset this evaluator will handle.  Never <code>null</code> or
    * modified after construction.
    */
   private PSViewSet m_viewSet;

   /**
    * Map of conditional view evaluators for each conditional view.  Key is the
    * lowercased view name as a <code>String</code>, value is a List of
    * <code>PSConditionalViewEvaluator</code> objects. Never
    * <code>null</code>, may be empty.
    */
   private Map m_viewEvaluators = new HashMap();

   /**
    * Class to contain a <code>PSConditionalView</code> and its
    * <code>PSConditionalEvaluator</code>.
    */
   private class PSConditionalViewEvaluator
   {
      /**
       * Constuct a conditional view evaluator.
       *
       * @param view The conditional view, may not be <code>null</code>.
       *
       * @throws IllegalStateException if the view's conditions are invalid.
       */
      public PSConditionalViewEvaluator(PSConditionalView view)
      {
         if (view == null)
            throw new IllegalArgumentException("view may not be null");

         m_view = view;
         try
         {
            m_eval = new PSConditionalEvaluator(m_view.getConditions());
         }
         catch (IllegalArgumentException e)
         {
            // shouldn't happen, but in case it does...
            throw new IllegalStateException(e.getLocalizedMessage());
         }

      }

      /**
       * Evalutes the view's conditions.
       *
       * @param data The execution data, may not be <code>null</code>.
       *
       * @return <code>true</code> if all of the conditions evaluate to
       * <code>true</code>, <code>false</code> otherwise.
       */
      public boolean isMatch(PSExecutionData data)
      {
         if (data == null)
            throw new IllegalArgumentException("data may not be null");

         return m_eval.isMatch(data);
      }

      /**
       * @return The view this evaluator was constructed with,
       * never <code>null</code>.
       */
      public PSConditionalView getView()
      {
         return m_view;
      }

      /**
       * The view to evaluate, never <code>null</code> after construction.
       */
      private PSConditionalView m_view;

      /**
       * Used to evaluate the view's conditions, never <code>null</code> after
       * construction.
       */
      private PSConditionalEvaluator m_eval;
   }
}
