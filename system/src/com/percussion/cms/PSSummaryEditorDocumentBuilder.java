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
package com.percussion.cms;

import com.percussion.cms.handlers.PSContentEditorHandler;
import com.percussion.cms.handlers.PSEditCommandHandler;
import com.percussion.data.IPSDataExtractor;
import com.percussion.data.PSDataExtractionException;
import com.percussion.data.PSDataExtractorFactory;
import com.percussion.data.PSExecutionData;
import com.percussion.data.PSViewExtractor;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSActionLink;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSCustomActionGroup;
import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSHtmlParameter;
import com.percussion.design.objectstore.PSLocation;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSParam;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.extension.PSExtensionException;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.server.IPSServerErrors;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSMapPair;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;

/**
 * The output document is built up over a number of steps. Most of the work
 * is performed by the base class. The main purpose of this class is to create
 * the appropriate build step needed when creating a summary editor.
 * A summary editor is basically a child table editor, which allows actions
 * on whole rows, but not modifying fields within the row.
 */
public class PSSummaryEditorDocumentBuilder extends PSModifyDocumentBuilder
{
   /**
    * The display text for the 'add child' main form button. By default, this
    * action causes the row editor for this child to be displayed. Never
    * <code>null</code> or empty.
    */
   public static final String ADDITEM_ACTION_LABEL = "Add new item";

   /**
    * This is the internal name that can be used by Custom Action Groups to
    * replace the standard button. Never <code>null</code> or empty.
    */
   public static final String ADD_ITEM_ACTION_NAME = "addnewchild";

   /**
    * Processes the supplied editor definition, creating an executable plan
    * that will be used when requests are made. Adds a single build step which
    * creates a table view of the child data. See {@link
    * PSModifyDocumentBuilder#PSModifyDocumentBuilder(PSContentEditor,
    * PSEditorDocumentContext, int, boolean ) base} class for params and their
    * requirements that aren't described below.
    *
    * @param mapping A mapping that contains a complex child.
    */
   public PSSummaryEditorDocumentBuilder( PSContentEditor ce,
         PSEditorDocumentContext ctx, PSDisplayMapping mapping,
         int pageId, boolean isError )
      throws PSExtensionException, PSNotFoundException, PSSystemValidationException
   {
      super( ce, ctx, pageId, isError );
      if ( null == mapping )
         throw new IllegalArgumentException( "mapping can't be null" );

      PSFieldSet fields = ((PSContentEditorPipe) ce.getPipe()).getMapper().
            getFieldSet( mapping.getDisplayMapper().getFieldSetRef());
      if ( null == fields )
         throw new PSNotFoundException( IPSServerErrors.CE_MISSING_FIELDSET,
               mapping.getDisplayMapper().getFieldSetRef());

      addBuildStep( new PSTableValueBuilder( fields,
            mapping.getUISet(), mapping.getDisplayMapper(), this ));

      // add the hidden fields
      String contentIdParam =
            ctx.getSystemParam( PSContentEditorHandler.CONTENT_ID_PARAM_NAME );
      String revisionIdParam =
            ctx.getSystemParam( PSContentEditorHandler.REVISION_ID_PARAM_NAME );
      Object [][] hiddenParamSet =
      {
         {
            contentIdParam,
            new PSHtmlParameter( contentIdParam )
         },
         {
            revisionIdParam,
            new PSHtmlParameter( revisionIdParam )
         },
      };
      String controlName =
            ctx.getInitParam( IPSConstants.HIDDEN_CONTROL_PARAM_NAME );
      if ( null == controlName || controlName.trim().length() == 0 )
      {
         String [] args =
         {
            IPSConstants.HIDDEN_CONTROL_PARAM_NAME,
            "InitParam is empty or missing from system def"
         };
         throw new PSSystemValidationException( IPSServerErrors.CE_INVALID_PARAM,
               args );
      }

      for ( int i = 0; i < hiddenParamSet.length; i++ )
      {
         addBuildStep( new PSSingleValueBuilder( controlName,
               (String) hiddenParamSet[i][0],
               (IPSReplacementValue) hiddenParamSet[i][1], this ));
      }


      /* The page id must be that of the row editor for this summary editor,
         which happens to be the only child of this editor. */
      String childPageId = "-99"; //arbitrary, invalid id (they typically start at 0)
      PSPageInfo info = (PSPageInfo) m_docContext.getPageInfoMap().get(
            new Integer( getPageId()));
      Iterator childrenPageIds = info.getPageIdList();
      String rowEditorPageId = childrenPageIds.next().toString();

      PSCustomActionGroup group = ce.getCustomActionGroup(
            PSLocation.PAGE_SUMMARY_VIEW, PSLocation.TYPE_FORM,
            mapping.getFieldRef());
      m_submitActions = createActionLinkList( group,
             ""+mapping.getDisplayMapper().getId(), rowEditorPageId,
             ""+getPageId(), ctx.getRequestUrl());
   }


   /**
    * Returns an iterator over actions that are default and or custom.
    * Default actions will have the following params:
    * <ul>
    *    <li>sys_command - edit</li>
    *    <li>sys_pageid - of target row editor</li>
    * </ul>
    * Custom actions will have the following params:
    * <ul>
    *    <li>{@link #FORMACTION_NAME} - target url</li>
    *    <li>custom params</li>
    *    <li>sys_pageid - of this editor</li>
    *    <li>sys_childid - mapper id of this fieldset</li>
    * </ul>
    *
    * @return A valid iterator over 0 or more actions. Except for exceptional
    *    circumstances, there should be 1 entry.
    */
   Iterator getSubmitActions()
   {
      return m_submitActions.iterator();
   }


   /**
    * See base class for full description. This method varies because it adds
    * a parameter defined by {@link #FORMACTION_NAME} which is not URL encoded.
    * This parameter, when placed in the first position, has special meaning
    * to the Javascript that modifies the action when the user clicks the
    * button. It is used by the stylesheet as the return action override
    * in the FORM. It is not encoded to save a step in the Javascript (which
    * would need to decode it before assigning it to the action of the form).
    */
   protected Iterator getActionLinks( Document doc, PSExecutionData data )
      throws PSDataExtractionException
   {
      if ( null == doc || null == data )
         throw new IllegalArgumentException( "One or more params is null." );

      List actions = new ArrayList();
      List params = new ArrayList();

      Iterator actionSet = getSubmitActions();

      while ( actionSet.hasNext())
      {
         PSMapPair actionEntry = (PSMapPair) actionSet.next();
         String label = (String) actionEntry.getKey();
         List extractorPairs = (List) actionEntry.getValue();
         Iterator extractors = extractorPairs.iterator();
         while ( extractors.hasNext())
         {
            PSMapPair paramEntry = (PSMapPair) extractors.next();
            String name = (String) paramEntry.getKey();
            IPSDataExtractor extractor = (IPSDataExtractor) paramEntry.getValue();
            String value = extractor.extract( data ).toString();
            if ( name != FORMACTION_NAME )
               value = URLEncoder.encode( value );
            params.add( new PSMapPair( name, value ));
         }

         if(label.equalsIgnoreCase(ADDITEM_ACTION_LABEL))
         {
            String lang = getUserLocaleString(data);

            label = PSI18nUtils.getString(PSI18nUtils.PSX_CE_ACTION +
               PSI18nUtils.LOOKUP_KEY_SEPARATOR_LAST +
               ADDITEM_ACTION_LABEL, lang);
         }

         actions.add( createActionElement( doc, label,
               params.iterator(), true ));
         params.clear();   // get ready for next iteration
      }
      return actions.iterator();
   }


   /**
    * Builds the list for the {@link #m_submitActions} member. See member
    * description for details.
    *
    * @param customActions If <code>null</code>, then just the standard action
    *    is added. If not <code>null</code>, then actions defined in the
    *    supplied group are added and the original action is possibly removed.
    *
    * @param mapperId Only used if <code>actions</code> is not <code>null
    *    </code>. This is the mapper id of the field set used by this editor.
    *    Assumed not <code>null</code> or empty if it is required.
    *
    * @param rowPageId This is the page id of the row editor associated with
    *    this summary editor. Assumed not <code>null</code> or empty.
    *
    * @param thisPageId Only used if <code>actions</code> is not <code>null
    *    </code>. This is the page id of the row editor associated with this
    *    summary editor. Assumed not <code>null</code> or empty if it is
    *    required.
    *
    * @param url The fully qualified url-string to reach this editor, not
    *    including the query string.
    *    <p>Example: http://server:9992/Rhythmyx/app1/resource.html
    *
    * @return A list with at least 1 member.
    */
   private List createActionLinkList( PSCustomActionGroup customActions,
         String mapperId, String rowPageId, String thisPageId, String url )
   {
      try
      {
         List actionList = new ArrayList();
         boolean addStandard = true;
         int stdPos = 0;
         PSMapPair pair;
         if ( null != customActions )
         {
            // adjust sequence to convert it to zero based
            int sequence = customActions.getLocation().getSequence() - 1;

            // if negative, they don't care, so append to end
            if ( sequence < 0 )
               sequence = 1000;     // arbitrarily large

            Iterator links = customActions.getActionLinkList();
            while ( links.hasNext())
            {
               PSActionLink link = (PSActionLink) links.next();
               Iterator params = link.getParameters();

               List extractors = new ArrayList();

               // this must be the first entry in the list
               pair = new PSMapPair( FORMACTION_NAME,
                     PSDataExtractorFactory.createReplacementValueExtractor(
                     customActions.getFormAction().getAction()));
               extractors.add( pair );

               // add custom params
               while ( params.hasNext())
               {
                  PSParam param = (PSParam) params.next();

                  pair = new PSMapPair( param.getName(),
                        PSDataExtractorFactory.createReplacementValueExtractor(
                        param.getValue()));
                  extractors.add( pair );
               }

               /* This variable was added to support related content. This
                  value is passed thru to the application doing the related
                  content searching so it can submit its results back to the
                  modify handler. The redirector can use this as a flag to
                  indicate a redirection. */
               pair = new PSMapPair( "sys_modifychildid",
                     PSDataExtractorFactory.createReplacementValueExtractor(
                     new PSTextLiteral( mapperId )));
               extractors.add( pair );

               // add the standard system params
               pair = new PSMapPair(
                     m_docContext.getSystemParam(
                     PSContentEditorHandler.PAGE_ID_PARAM_NAME ),
                     PSDataExtractorFactory.createReplacementValueExtractor(
                     new PSTextLiteral( thisPageId )));
               extractors.add( pair );
               pair = new PSMapPair(
                     m_docContext.getSystemParam(
                     PSContentEditorHandler.CHILD_ID_PARAM_NAME ),
                     PSDataExtractorFactory.createReplacementValueExtractor(
                     new PSTextLiteral( mapperId )));
               extractors.add( pair );
               pair = new PSMapPair(
                     m_docContext.getSystemParam(
                     IPSHtmlParameters.SYS_VIEW),
                     new PSViewExtractor(getViewEvaluator(), getPageId()));
               extractors.add( pair );

               // move the position of the standard button, if necessary
               if ( sequence >= 0 && sequence <= stdPos )
                  stdPos++;

               addAction( actionList, link.getDisplayText().getText(),
                     extractors, sequence++ );

            }

            Iterator removeActions = customActions.getRemoveActions();
            while ( removeActions.hasNext())
            {
               String actionName = (String) removeActions.next();
               if ( actionName.equalsIgnoreCase( ADD_ITEM_ACTION_NAME ))
                  addStandard = false;
            }
         }

         if ( addStandard )
         {
            List extractors = new ArrayList();
            pair = new PSMapPair(
                  m_docContext.getSystemParam(
                  PSContentEditorHandler.COMMAND_PARAM_NAME ),
                  PSDataExtractorFactory.createReplacementValueExtractor(
                  new PSTextLiteral( PSEditCommandHandler.COMMAND_NAME )));
            extractors.add( pair );

            pair = new PSMapPair(
                  m_docContext.getSystemParam(
                  PSContentEditorHandler.PAGE_ID_PARAM_NAME ),
                  PSDataExtractorFactory.createReplacementValueExtractor(
                  new PSTextLiteral( rowPageId )));
            extractors.add( pair );

            pair = new PSMapPair(
                  m_docContext.getSystemParam(
                  IPSHtmlParameters.SYS_VIEW),
                  new PSViewExtractor(getViewEvaluator(), getPageId()));
            extractors.add( pair );
            addAction( actionList, ADDITEM_ACTION_LABEL, extractors, stdPos );
         }
         return actionList;
      }
      catch ( IllegalArgumentException iae )
      {
         throw new IllegalArgumentException( iae.getLocalizedMessage());
      }
   }

   /**
    * Creates a Pair object using the supplied label as key and the
    * extractors as the value. This entry is then added to the supplied
    * actions list in the specified position.
    *
    * @param actions The created entry is added to this list. Assumed not
    *    <code>null</code>.
    *
    * @param label The display text for the action. Assumed not <code>null
    *    </code> or empty.
    *
    * @param paramExtractors Contains the params for this action. Each
    *    element is a Pair whose key is the name of the param, aa a
    *    String and whose value is the extractor for the param's value, as an
    *    IPSDataExtractor.
    *
    * @param pos The position to add the element to. If greater than the
    *    length of the list, the entry is appended, if less than 0, the entry
    *    is inserted at the head of the list.
    */
   private void addAction( List actions, String label, List paramExtractors,
         int pos )
   {
      PSMapPair entry = new PSMapPair( label, paramExtractors );

      if ( pos <= 0 )
         actions.add( 0, entry);
      else if ( pos > actions.size())
         actions.add( entry);
      else
         actions.add( pos, entry);
   }

   /**
    * Contains a set of entries for the submit action on this form and any
    * designer specified actions. Other actions should be included in the
    * <code>m_actions</code> member. All actions in this member and m_actions
    * are to be returned by the <code>getActionLinks</code> method. Never
    * <code>null</code> or empty after
    * construction. Each entry contains a Pair where the key is the
    * action label as a <code>String</code> and the value is a <code>List
    * </code> of extractors.
    * <p>The extractor list contains entries for all of the params needed to
    * create the action element. Each one has the param name as the key and
    * the extractor to obtain the param's value as the value of the entry.
    */
   private List m_submitActions;

   /**
    * For future use, when we have actions other than form submission actions.
    */
   // private List m_actions;
}



