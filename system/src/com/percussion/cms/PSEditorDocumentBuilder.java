/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.cms;

import com.percussion.cms.handlers.PSCloneCommandHandler;
import com.percussion.cms.handlers.PSContentEditorHandler;
import com.percussion.cms.handlers.PSEditCommandHandler;
import com.percussion.data.*;
import com.percussion.design.objectstore.*;
import com.percussion.extension.PSExtensionException;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.i18n.tmxdom.IPSTmxDtdConstants;
import com.percussion.server.IPSCgiVariables;
import com.percussion.server.IPSServerErrors;
import com.percussion.util.PSIteratorUtils;
import com.percussion.util.PSMapPair;
import com.percussion.util.PSStringOperation;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.sql.SQLException;
import java.util.*;

/**
 * This is the base class for all document builders. It knows the basic
 * structure of the output document (based on the ContentEditor dtd). The
 * document has several sections, the editing section, and all other sections.
 * <p>The editing section is built using classes that implement the
 * {@link IPSBuildStep} interface. Each build step creates a 'row' in the
 * editing section of the output document (i.e. a &lt;DisplayField&gt; element).
 * <p>The other sections are created by making calls to methods in
 * this class that can be overridden by derived classes. These methods include
 * the following:
 * <ul>
 *   <li>CreateActionElement</li>
 *   <li>CreateActionLinkElement</li>
 *   <li>CreateControlNameSetElement</li>
 *   <li>CreateSectionLinkElement</li>
 *   <li>CreateUserStatusElement</li>
 *   <li>CreateWorkflowInfo</li>
 * </ul>
 * All of these methods return a node that is added into the document at the
 * proper location. Some of the methods have a default implementation in
 * this class and some return no element.
 * <p>Each of the build steps for the editing section has 1 of 3 options:
 * create a standard, editable field, create a hidden field, or create an
 * error field. If a hidden field is created, it should be added to this
 * object's build context using its {@link IPSBuildContext#addHiddenField(
 * Element, String) addHiddenField} method. Otherwise, it should be added using
 * its {@link IPSBuildContext#addVisibleField(Element,String) addVisibleField}
 * method.
 */
public abstract class PSEditorDocumentBuilder
{
   /**
    * A 1 letter string used as a key to obtain the content id extractor.
    * Use with {@link #getExtractor(String) getExtractor}.
    */
   public static final String CONTENT_ID_EXTRACTOR_KEY = "a";

   /**
    * A 1 letter string used as a key to obtain the revision id extractor.
    * Use with {@link #getExtractor(String) getExtractor}.
    */
   public static final String REVISION_ID_EXTRACTOR_KEY = "b";

   /**
    * A 1 letter string used as a key to obtain the child id extractor.
    * Use with {@link #getExtractor(String) getExtractor}.
    */
   public static final String CHILD_ID_EXTRACTOR_KEY = "c";

   /**
    * A 1 letter string used as a key to obtain the child row id extractor.
    * Use with {@link #getExtractor(String) getExtractor}.
    */
   public static final String ROW_ID_EXTRACTOR_KEY = "d";

   /**
    * A 1 letter string used as a key to obtain the page id extractor.
    * Use with {@link #getExtractor(String) getExtractor}.
    */
   public static final String PAGE_ID_EXTRACTOR_KEY = "e";

   /**
    * Must be all of the letters used for the xxx_ID_EXTRACTOR_KEY constants.
    * Used for validation.
    */
   private static final String EXTRACTOR_KEY_SET = "abcde";

   // public element names
   /**
    * The name of the element in the output doc that contains display text.
    */
   public static final String LABEL_NAME = "DisplayLabel";

   /**
    * The name of the attribute for the mnemonic key.
    */
   public static final String ATTR_ACCESSKEY = "accessKey";

   /**
    * The name of the element in the output doc that contains a parameter.
    */
   public static final String PARAM_NAME = "Param";

   /**
    * This is the ContentEditor attribute name that contains the url to use
    * when submitting the document for update. Never <code>null</code> or
    * empty.
    */
   public static final String FORMACTION_NAME = "submitHref";

   /**
    * The internal name of the 'Return to parent' button. Will not change. The
    * name is 'returntoroot'. This name will be set as the name attribute of
    * the ActionLink for this button in the output doc.
    */
   public static final String PARENT_RETURN_NAME = "returntoroot";

   /**
    * Processes the supplied editor definition, creating an executable plan
    * that will be used when requests are made. This is the base class for all
    * document builders.
    *
    * @param ce The editor definition for the result document. Never <code>null
    *    </code>
    *
    * @param ctx A set of properties that are needed to build the document but
    *    cannot be derived from the editor definition. Never <code>null</code>.
    *
    * @param pageId The unique id for this particular editor within the
    *    collection of editors that make up a content editor.
    *
    * @throws PSSystemValidationException If anything is not kosher with the editor
    *    def.
    *
    * @throws PSExtensionException If an extension is used by the definition
    *    and it can't be loaded.
    */
   protected PSEditorDocumentBuilder( PSContentEditor ce,
         PSEditorDocumentContext ctx, int pageId )
      throws PSExtensionException, PSSystemValidationException
   {
      if ( null == ce || null == ctx )
         throw new IllegalArgumentException( "One or more params was null." );

      m_pageId = pageId;
      m_contentTypeId = ""+ce.getContentType();
      m_isRelatedContentEnabled = ce.isRelatedContentEnabled();
      m_objectType = ce.getObjectType();

      Iterator tableSetIter =
            ((PSContentEditorPipe) ce.getPipe()).getLocator().getTableSets();
      while ( tableSetIter.hasNext())
         m_tableSets.add( tableSetIter.next());

      try
      {
         // create extractors for section link list links
         Iterator links = ce.getSectionLinkList();
         List linkList = new ArrayList(5);
         while ( links.hasNext())
         {
            PSUrlRequest req = (PSUrlRequest) links.next();
            IPSDataExtractor extractor =
                  PSDataExtractorFactory.createReplacementValueExtractor( req );
            linkList.add(extractor);
         }
         m_sectionLinkExtractors = new IPSDataExtractor[linkList.size()];
         linkList.toArray( m_sectionLinkExtractors );

         // todo: for future enhancement
         m_actionLinkExtractors = new IPSDataExtractor[0];

         // create extractor for contentid
         PSHtmlParameter contentId = new PSHtmlParameter(
               ctx.getSystemParam( PSContentEditorHandler.CONTENT_ID_PARAM_NAME ));
         m_contentIdExtractor =
               PSDataExtractorFactory.createReplacementValueExtractor( contentId );
         
         // create extractor for the item locale
         PSContentItemStatus itemStatus = 
            new PSContentItemStatus("CONTENTSTATUS", "LOCALE");
         m_itemLocaleExtractor = 
            PSDataExtractorFactory.createReplacementValueExtractor(itemStatus);
         
         // create extractors for user status info
         PSCgiVariable userAgent =
               new PSCgiVariable( IPSCgiVariables.CGI_REQUESTOR_SOFTWARE );
         m_userStatusExtractors.put( USERAGENT_NAME,
               PSDataExtractorFactory.createReplacementValueExtractor( userAgent ));

         PSCgiVariable acceptLang = new PSCgiVariable( "HTTP_ACCEPT_LANGUAGE" );
         m_userStatusExtractors.put( ACCEPTLANG_NAME,
               PSDataExtractorFactory.createReplacementValueExtractor( acceptLang ));

         PSUserContext sessionId = new PSUserContext( "SessionId" );
         m_userStatusExtractors.put( SESSIONID_NAME,
               PSDataExtractorFactory.createReplacementValueExtractor( sessionId ));

         PSUserContext userName = new PSUserContext( "User/Name" );
         m_userStatusExtractors.put( USERNAME_NAME,
               PSDataExtractorFactory.createReplacementValueExtractor( userName ));

         PSUserContext roleSet = new PSUserContext( "Roles/RoleName" );
         m_userStatusExtractors.put( ROLESET_NAME,
               PSDataExtractorFactory.createReplacementValueExtractor( roleSet ));

         PSUserContext language =
            new PSUserContext( PSI18nUtils.USER_CONTEXT_VAR_SYS_LANG );
         m_userStatusExtractors.put( LANGUAGE,
               PSDataExtractorFactory.createReplacementValueExtractor( language ));

         m_isEditMode = ctx.isEditMode();
         m_commandName = ctx.getCommandName();

         PSRequestor requestor = ce.getRequestor();
         if ( null == requestor )
         {
            throw new PSSystemValidationException(
                  IPSServerErrors.CE_MISSING_REQUESTOR, ce.getName());
         }
         m_formAction = requestor.getRequestPage();
         if ( null == m_formAction || m_formAction.trim().length() == 0 )
         {
            throw new PSSystemValidationException(
                  IPSServerErrors.CE_MISSING_FORMACTION, ce.getName());
         }
         m_formAction += ".html";

         m_docContext = ctx;

         // create view evaluator
         PSViewSet viewSet = ce.getViewSet();
         if (viewSet == null)
         {
            throw new PSSystemValidationException(
               IPSServerErrors.CE_VIEW_SET_MISSING);
         }
         m_viewEvaluator = new PSViewEvaluator(viewSet);

         // build extractors needed for button params if they haven't yet
         if ( null != ms_paramExtractors )
            return;

         // sync in case we start parellizing app inits
         synchronized ( PSEditorDocumentBuilder.class)
         {
            // use double check idiom
            if ( null != ms_paramExtractors )
               return;
            ms_paramExtractors = new HashMap();
            String [][] paramInfo =
            {
               {
                  ctx.getSystemParam( PSContentEditorHandler.CONTENT_ID_PARAM_NAME ),
                  CONTENT_ID_EXTRACTOR_KEY
               },
               {
                  ctx.getSystemParam( PSContentEditorHandler.REVISION_ID_PARAM_NAME ),
                  REVISION_ID_EXTRACTOR_KEY
               },
               {
                  ctx.getSystemParam( PSContentEditorHandler.CHILD_ID_PARAM_NAME ),
                  CHILD_ID_EXTRACTOR_KEY
               },
               {
                  ctx.getSystemParam( PSContentEditorHandler.CHILD_ROW_ID_PARAM_NAME ),
                  ROW_ID_EXTRACTOR_KEY
               },
               {
                  ctx.getSystemParam( PSContentEditorHandler.PAGE_ID_PARAM_NAME ),
                  PAGE_ID_EXTRACTOR_KEY
               }
            };
            Iterator params = PSIteratorUtils.iterator( paramInfo );
            while ( params.hasNext())
            {
               String [] paramPair = (String []) params.next();
               PSSingleHtmlParameter value = new PSSingleHtmlParameter( paramPair[0] );
               Map tmp = new HashMap();
               tmp.put( paramPair[0], PSDataExtractorFactory
                        .createReplacementValueExtractor( value ));
               ms_paramExtractors.put( paramPair[1],
                     (Map.Entry) tmp.entrySet().iterator().next());
            }
         }
      }
      catch ( IllegalArgumentException iae )
      {
         // we're trying to eliminate this exception in new code
         throw new IllegalArgumentException( iae.getLocalizedMessage());
      }
   }

   /**
    * If a DisplayField element created by a sub-step should be hidden, it
    * should be added using this method after the node is created.
    *
    * @param ctx The build context that contains the list of hidden fields and.
    * control names.  May not be <code>null</code>.
    *
    * @param dispNode A DisplayField element that should not be visible when
    *    the editor is rendered. Must not be <code>null</code>.
    *
    * @param controlName name of the control that should render this node. Must not
    *    be <code>null</code> or empty.
    */
   private void addHiddenField( PSEditorDocumentBuildContext ctx,
      Element dispNode, String controlName )
   {
      if ( null == ctx || null == dispNode || null == controlName
            || controlName.trim().length() == 0 )
      {
         throw new IllegalArgumentException(
               "One or more of the params is null or empty." );
      }
      ctx.getHiddenFields().add(dispNode);
   }



   /**
    * If a DisplayField element created by a sub-step should be visible when
    * rendered, it should be added using this method after the node is created.
    *
    * @param ctx The build context that contains the list of hidden fields and.
    * control names.  May not be <code>null</code>.
    *
    * @param dispNode A DisplayField element that should be visible when
    *    the editor is rendered. Must not be <code>null</code>.
    *
    * @param controlName name of the control that should render this node. Must not
    *    be <code>null</code> or empty.
    */
   private void addVisibleField( PSEditorDocumentBuildContext ctx,
      Element dispNode, String controlName )
   {
      if ( null == ctx || null == dispNode || null == controlName
            || controlName.trim().length() == 0 )
      {
         throw new IllegalArgumentException(
               "One or more of the params is null or empty." );
      }
      ctx.getVisibleFields().add(dispNode);
   }


   /**
    * Each content editor builds 1 or more editors. There is a builder for
    * each one of these editors. The page id uniquely identifies the page
    * within the context of this content editor.
    *
    * @return The page id, greater than or = ROOT_PARENT_PAGE_ID.
    */
   public int getPageId()
   {
      return m_pageId;
   }

   /**
    * A method to determine if the request is for a new document or an
    * existing item. Builders may behave differently in these 2 contexts.
    *
    * @param data The execution data, used to obtain information needed to
    *    perform the check.
    *
    * @param isRowEditor A flag that indicates whether this builder is a
    *    row editor. [Todo: looking at this again, it is hokey that the base
    *    class knows something about a derived class. Can this be reworked
    *    to remove this knowledge?]
    *
    * @return <code>true</code> if this request is for an editor to create
    *    new content for the parent or any child item, <code>false</code>
    *    otherwise.
    */
   public static boolean isNewDocument( PSExecutionData data,
         boolean isRowEditor )
      throws PSDataExtractionException
   {
      Object o = null;
      try
      {
         o = getExtractor( CONTENT_ID_EXTRACTOR_KEY ).extract( data );
         if ( null == o )
            return true;

         o = getExtractor( PAGE_ID_EXTRACTOR_KEY ).extract( data );
         int pageId;
         if ( null == o || o.toString().trim().length() == 0 )
            pageId = PSEditCommandHandler.ROOT_PARENT_PAGE_ID;
         else
            pageId = Integer.parseInt( o.toString());

         o = getExtractor( ROW_ID_EXTRACTOR_KEY ).extract( data );
         if ( null == o && pageId != PSEditCommandHandler.ROOT_PARENT_PAGE_ID
               && isRowEditor)
         {
            return true;
         }
         return false;
      }
      catch ( NumberFormatException nfe )
      {
         String [] args =
         {
            o.toString(),
            "page id",
            nfe.getLocalizedMessage()
         };
         throw new PSDataExtractionException(
               IPSServerErrors.CE_BAD_NUMBER_FORMAT, args );
      }
   }

   /**
    * This class maintains a set of extractors for system parameters. They
    * can be obtained by passing in one of the public keys. The map contains
    * the parameter name (String) as the key with an IPSDataExtractor for that
    * param as the value.
    *
    * @param key Must be one of the ...EXTRACTOR_KEY constants.
    *
    * @return A map containing the system HTML param name and it's extractor.
    *    Never <code>null</code>.
    */
   public static Map.Entry getExtractorSet( String key )
   {
      if ( null == key || key.length() > 1
            || EXTRACTOR_KEY_SET.indexOf(key) < 0 )
      {
         throw new IllegalArgumentException( "incorrect key supplied" );
      }
      return (Map.Entry) ms_paramExtractors.get( key );
   }


   /**
    * Returns an extractor to get a system parameter using the supplied key.
    *
    * @param key One of the xxx_ID_EXTRACTOR_KEY constants. No validation is
    *    performed on the key.
    *
    * @return A valid extractor if the key is one of the defined constants,
    *    otherwise, <code>null</code> is returned.
    */
   public static IPSDataExtractor getExtractor( String key )
   {
      Map.Entry entry = getExtractorSet( key );

      return (IPSDataExtractor) entry.getValue();
   }

   /**
    * Returns the context in which this editor was created.
    *
    * @return A valid document context. Never <code>null</code>.
    */
   public PSEditorDocumentContext getDocContext()
   {
      //todo implement read only interface to return
      return m_docContext;
   }

   /**
    * This method creates the main document, adding all fields added to this
    * object's build context with its {@link IPSBuildContext#addHiddenField(
    * Element, String) addHiddenField} and {@link
    * IPSBuildContext#addVisibleField(Element, String) addVisibleField} methods
    * in the correct location and order.
    *
    * @param data Contains the data associated with this request, used to
    *    calculate any replacement values. Never <code>null</code>.
    *
    * @return The final document, ready for return to the requestor. Never
    *    <code>null</code>
    *
    * @throws PSDataExtractionException If any problems occur while getting
    *    values from the execution data.
    */
   public Document createResultDocument( PSExecutionData data )
      throws PSDataExtractionException
   {
      if ( null == data )
         throw new IllegalArgumentException( "Execution data cannot be null" );

         // must create the result doc before running the build steps
         Document resultDoc = PSXmlDocumentBuilder.createXmlDocument();
         PSXmlDocumentBuilder.createRoot( resultDoc, ROOT_NAME );

         /*
          * We must create the user info before running the build steps,
          * otherwise we will get it from the wrong application.
          */
         Node userStatusElement = createUserStatusElement(resultDoc, data);

         // create the build context to pass to the build steps
         PSEditorDocumentBuildContext ctx = new PSEditorDocumentBuildContext(
            this, resultDoc);

         boolean isNewDoc =
               isNewDocument( data, getDocContext().isRowEditor());
         Iterator<IPSBuildStep> buildSteps = m_buildSteps.iterator();
         while ( buildSteps.hasNext())
         {
            IPSBuildStep step = buildSteps.next();
            step.execute(ctx, data, isNewDoc );
         }
         
         // Get the item locale
         String itemLocale = (String)m_itemLocaleExtractor.extract(data);
         
         Element root = resultDoc.getDocumentElement();
         //todo: determine whether normal or error
         root.setAttribute( DOCTYPE_NAME, DOCTYPE_NORMAL );
         root.setAttribute( MODE_NAME, m_isEditMode ? MODE_EDIT : MODE_PREVIEW );
         root.setAttribute( COMMANDNAME_NAME, m_commandName );
         root.setAttribute( FORMACTION_NAME, m_formAction );
         root.setAttribute( CONTENTTYPEID_NAME, m_contentTypeId );
         root.setAttribute( ENABLERELATEDCONTENT_ATTR,
            m_isRelatedContentEnabled ? "yes" : "no");
         root.setAttribute(CURRENT_TIME_STAMP, "" + new Date().getTime());
         root.setAttribute(PSContentEditor.OBJECT_TYPE_ATTR,
            Integer.toString(m_objectType));
         root.setAttribute(ITEM_LOCALE_ATTR, itemLocale);

         Element itemContent = resultDoc.createElement( ITEM_NAME );
         itemContent.setAttribute(NEW_DOC_ATTRIB, isNewDoc ?
            IPSConstants.BOOLEAN_TRUE : IPSConstants.BOOLEAN_FALSE);
         Object o = getExtractor( ROW_ID_EXTRACTOR_KEY ).extract( data );
         if (o != null)
            itemContent.setAttribute(CHILDKEY_ATTRIB, o.toString());
         root.appendChild( itemContent );
         addFieldElements( resultDoc, itemContent,
            ctx.getHiddenFields().iterator());
         addFieldElements( resultDoc, itemContent,
            ctx.getVisibleFields().iterator());

         NodeList nl =
            root.getElementsByTagName(PSDisplayFieldElementBuilder.CONTROL_NAME);
         Set<String> controls = new HashSet<>(nl.getLength());
         for (int i = 0; i < nl.getLength(); i++)
         {
            Element elem = (Element)nl.item(i);
            controls.add(
               elem.getAttribute(PSDisplayFieldElementBuilder.CONTROLNAME_NAME));
         }
         root.insertBefore(
            createControlNameSetElement(resultDoc, controls.iterator()),
            itemContent);

         root.appendChild(userStatusElement);

         Node node = createWorkflowInfo( resultDoc, data );
         if ( null != node )
            root.appendChild( node );

         node = createVariantList( resultDoc, data );
         if ( null != node )
         {
            Node importNode = resultDoc.importNode(node, true);
            root.appendChild( importNode );
         }

         node = createSectionLinkElement( resultDoc,
               PSIteratorUtils.iterator( m_sectionLinkExtractors ), data );
         if ( null != node )
            root.appendChild( node );

         node = createActionLinkElement( resultDoc, data );
         if ( null != node )
            root.appendChild( node );

         return resultDoc;
      
   }

   /**
    * Two general forms of documents are created: those generated as the
    * result of an agent request, and those generated due to validation
    * errors.
    *
    * @return <code>true</code> if this document is being generated because
    * validation failed, <code>false</code> otherwise.
    */
   public boolean isErrorDoc()
   {
      return false;
   }

   /**
    * Most builders use data from result sets. This method is called during the
    * processing of the result document. Any derived class that uses result
    * sets should override this method to set the result sets and their
    * associated context in the execution data. The default behavior is to do
    * nothing. The result sets in the incoming object have not been set up in
    * any way.
    * <p>Derived classes do not need to worry about cleaning up result sets
    * in the execution data; this will be done by the handler.
    *
    * @throws PSConversionException If the appropriate data was not present
    *    (e.g. too many or too few rows or result sets).
    *
    * @throws SQLException If any errors occur while preparing the data.
    *
    * @see PSExecutionData#getNextResultSet()
    * @see PSExecutionData#getCurrentResultRowData()
    * @see PSExecutionData#readRow()
    */
   public void prepareExecutionData( PSExecutionData data )
      throws PSConversionException, SQLException, PSDataExtractionException
   {
      // no op
   }


   /**
    * This method is called by the handler after all post exits have run.
    * The document returned from this method is either returned directly to
    * the requestor (if the request was for xml) or sent to the xsl processor.
    * The default implementation adds a 'Return to parent' button at the
    * bottom of the workflow action list. The button is enabled unless this is
    * the parent editor, in which case it is disabled.  It also adds a 'New
    * Version' just above the 'Return to parent' button that is always enabled.
    *
    * @param doc The result document after all exits have run. May be <code>
    *    null</code>.
    *
    * @param data Contains the data associated with this request, used to
    *    calculate any replacement values. Never <code>null</code>.
    *
    * @return The document to return to the user. If <code>null</code> was
    *    passed in, it is returned.
    *
    * @throws PSDataExtractionException If any problems occur while getting
    *    values from the execution data.
    */
   public Document postProcessDocument( Document doc, PSExecutionData data )
      throws PSDataExtractionException
   {
      if ( null == doc )
         return doc;

      PSXmlTreeWalker walker = new PSXmlTreeWalker( doc );

      // get the hidden params and cross-check against what we want to add
      Map hiddenParams = new HashMap();
      Node root = walker.getCurrent();
      Element hiddenParamsElem = walker.getNextElement(
            BASICINFO_PATH + HIDDENFORMPARAMS_NAME );
      if ( null != hiddenParamsElem )
      {
         walker.setCurrent( hiddenParamsElem );
         Element child = walker.getNextElement( walker.GET_NEXT_ALLOW_CHILDREN );
         do
         {
            if ( PARAM_NAME.equals( child.getNodeName()))
            {
               hiddenParams.put( child.getAttribute( NAME_ANAME ), null );
            }
            child = walker.getNextElement( walker.GET_NEXT_ALLOW_SIBLINGS );
         }
         while ( null != child );
      }

      walker.setCurrent( root );
      Element parent = walker.getNextElement(
            BASICINFO_PATH + ACTIONLINKSET_NAME );
      if ( null != parent )
      {
         String contentId = "";

         IPSDataExtractor cidExtractor = getExtractor( CONTENT_ID_EXTRACTOR_KEY );
         Object o = cidExtractor.extract( data );
         if ( null != o && o.toString().trim().length() > 0 )
            contentId = o.toString().trim();

         String revisionId = "";
         IPSDataExtractor revExtractor = getExtractor( REVISION_ID_EXTRACTOR_KEY );
         o = revExtractor.extract( data );
         if ( null != o && o.toString().trim().length() > 0 )
            revisionId = o.toString().trim();

         // create base param set
         Collection paramSet = new ArrayList();
         String [][] paramPairs =
         {
            {
               m_docContext.getSystemParam(
                     PSContentEditorHandler.CONTENT_ID_PARAM_NAME ),
               contentId
            },
            {
               m_docContext.getSystemParam(
                     PSContentEditorHandler.REVISION_ID_PARAM_NAME ),
               revisionId
            }
         };

         for ( int i = 0; i < paramPairs.length; i++ )
         {
            if ( !hiddenParams.containsKey( paramPairs[i][0] ))
            {
               PSMapPair param =
                     new PSMapPair( paramPairs[i][0], paramPairs[i][1] );
               paramSet.add( param );
            }
         }

         // now add each action item we need
         Collection tmpParamSet;
         PSMapPair param;
         String label;
         boolean enabled;

         // add clone
         enabled = revisionId.length() > 0 && contentId.length() > 0;
         label = PSI18nUtils.getString(PSI18nUtils.PSX_CE_ACTION +
          PSI18nUtils.LOOKUP_KEY_SEPARATOR_LAST +  CLONE_LABEL,
          getUserLocaleString(data));
         tmpParamSet = new ArrayList(paramSet);
         param = new PSMapPair( m_docContext.getSystemParam(
            PSContentEditorHandler.COMMAND_PARAM_NAME ),
            PSCloneCommandHandler.COMMAND_NAME);
         tmpParamSet.add(param);
         parent.appendChild( createActionElement( doc, label,
               tmpParamSet.iterator(), enabled ));

         // add return to parent
         enabled = revisionId.length() > 0 && contentId.length() > 0
               && m_pageId != PSEditCommandHandler.ROOT_PARENT_PAGE_ID;
         label = PARENT_RETURN_LABEL;
         tmpParamSet = new ArrayList(paramSet);
         param = new PSMapPair( m_docContext.getSystemParam(
            PSContentEditorHandler.COMMAND_PARAM_NAME ),
            PSEditCommandHandler.COMMAND_NAME);
         tmpParamSet.add(param);
         parent.appendChild( createActionElement( doc, label,
               PARENT_RETURN_NAME, tmpParamSet.iterator(), enabled ));

      }
      return doc;
   }

   /**
    * Appends a display field element to a content editor document.
    *
    * @param doc The document, must contain a valid <code>ContentEditor<code>
    * element, not <code>null</code>.  The element is appended to the end, and
    * its control name is added to the <code>ControlNameSet</code> element if it
    * is not already listed.
    *
    * @param dispNode A DisplayField element to add, must not be
    * <code>null</code>.
    */
   public static void appendDisplayNode(Document doc, Element dispNode)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      if (dispNode == null)
         throw new IllegalArgumentException("dispNode may not be null");

      // determine the control name
      String controlName = PSDisplayFieldElementBuilder.getControlName(
         dispNode);

      PSXmlTreeWalker walker = new PSXmlTreeWalker(doc);
      Element root = doc.getDocumentElement();
      walker.setCurrent(root);
      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
      firstFlags |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;
      nextFlags |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      // see if control is in control name set
      Element controlNameSet = walker.getNextElement(CONTROLNAMES_NAME,
         firstFlags);
      if (controlNameSet == null)
         throw new IllegalArgumentException("invalid dispNode format");

      boolean foundIt = false;
      Element controlNameEl = walker.getNextElement(CONTROLNAME_NAME,
         firstFlags);
      while (controlNameEl != null)
      {
         String name = PSXmlTreeWalker.getElementData(controlNameEl);
         if (controlName.equals(name))
         {
            foundIt = true;
            break;
         }

         controlNameEl = walker.getNextElement(CONTROLNAME_NAME, nextFlags);
      }

      // if not in set, add it
      if (!foundIt)
      {
         controlNameEl = PSXmlDocumentBuilder.addElement(doc, controlNameSet,
            CONTROLNAME_NAME, controlName);
      }

      // now append the element
      walker.setCurrent(root);
      Element itemEl = walker.getNextElement(ITEM_NAME);
      if (itemEl == null)
         throw new IllegalArgumentException("invalid doc format");
      itemEl.appendChild(dispNode);
   }

   /**
    * Adds an element to the supplied document that contains all of the workflow
    * information as defined in the ContentEditor dtd. This is a hook for
    * derived classes, this class does not add any workflow information.
    *
    * @param doc The document to which the element will eventually be added.
    *    Never <code>null</code>.
    *
    * @param data Contains the data associated with this request, used to
    *    calculate any replacement values. Never <code>null</code>.
    *
    * @return A Workflow element with all needed children. This class always
    *    returns <code>null</code>.
    */
   protected Node createWorkflowInfo( Document doc, PSExecutionData data )
      throws PSDataExtractionException
   {
      if ( null == doc || null == data )
         throw new IllegalArgumentException( "One or more params were null." );
      return null;
   }


   /**
    * Certain documents require a variant list. A variant list is a set of
    * objects that supply information necessary to preview the page in
    * a particular format. Each content item has a specified set of formats
    * that it can be used with. The variant list must match the format
    * specified by the ContentEditor dtd.
    * <p>The <code>createResultDocument</code> method of this class calls this
    * method at the appropriate time, only adding the node if a valid node is
    * returned.
    *
    * @param doc The document to which the element will eventually be added.
    *    Never <code>null</code>.
    *
    * @param data Contains the data associated with this request, used to
    *    calculate any replacement values. Never <code>null</code>.
    *
    * @return A VariantList element with all needed children. <code>null
    *    </code> is returned by default.
    */
   protected Node createVariantList( Document doc, PSExecutionData data )
      throws PSDataExtractionException
   {
      if ( null == doc || null == data )
         throw new IllegalArgumentException( "One or more params were null." );
      return null;
   }

   /**
    * Adds the ControlNames element to the supplied document according to the
    * ControlEditor dtd. Every name in the supplied list is added as a child
    * element.
    *
    * @param doc The document to which the element will eventually be added.
    *    Never <code>null</code>.
    *
    * @param controlNames A valid set containing 0 or more names of display
    *    controls used by the fields in this document. If no names are supplied,
    *    no element is added. Never <code>null</code>. If any entry is <code>
    *    null</code>, it is ignored. This list should contain a unique set of
    *    control names.
    *
    * @return The node, if created, otherwise <code>null</code>.
    */
   protected Node createControlNameSetElement( Document doc,
         Iterator controlNames )
   {
      if ( null == doc || null == controlNames )
         throw new IllegalArgumentException( "One or more params were null." );

      Element controlNamesNode = doc.createElement( CONTROLNAMES_NAME );
      while ( controlNames.hasNext())
      {
         String name = (String) controlNames.next();
         if ( null != name )
         {
            Element nameNode = doc.createElement( CONTROLNAME_NAME);
            nameNode.appendChild( doc.createTextNode( name ));
            controlNamesNode.appendChild( nameNode );
         }
      }
      return controlNamesNode;
   }

   /**
    * Adds an element to the supplied document that contains all of the
    * information in the UserStatus element. The element meets the ContentEditor
    * dtd.
    *
    * @param doc The document to which the element will eventually be added.
    *    Never <code>null</code>.
    *
    * @param data Contains the data associated with this request, used to
    *    calculate any replacement values. Never <code>null</code>.
    *
    * @return The node, if created, otherwise <code>null</code>.
    */
   protected Node createUserStatusElement( Document doc, PSExecutionData data )
      throws PSDataExtractionException
   {
      if ( null == doc || null == data )
         throw new IllegalArgumentException( "One or more params were null." );

      Element userStatusNode = doc.createElement( USERSTATUS_NAME );
      IPSDataExtractor extractor =
            (IPSDataExtractor) m_userStatusExtractors.get( SESSIONID_NAME );
      userStatusNode.setAttribute( SESSIONID_NAME,
            extractor.extract( data ).toString());

      extractor =
            (IPSDataExtractor) m_userStatusExtractors.get( LANGUAGE );

      Object obj = extractor.extract( data );
      String lang = "";
      if(obj != null)
         lang = obj.toString();
      userStatusNode.setAttribute(IPSTmxDtdConstants.ATTR_XML_LANG, lang);

      extractor =
            (IPSDataExtractor) m_userStatusExtractors.get( USERNAME_NAME );
      PSLiteralSet names = (PSLiteralSet) extractor.extract( data );

      String name = "";

      // if there are multiple names, we ignore the rest
      if ( null != names )
         name = names.get(0).toString();

      if ( null != name && name.trim().length() > 0 )
      {
         Element nameNode = doc.createElement( USERNAME_NAME );
         nameNode.appendChild( doc.createTextNode( name ));
         userStatusNode.appendChild( nameNode );
      }

      //todo: better interface for date format
      Element timeNode = doc.createElement( TIME_NAME );
      timeNode.appendChild( doc.createTextNode( PSStringOperation.dateFormat(
            "MMM dd, yyyy hh:mm a zzzz", new Date())));
      userStatusNode.appendChild( timeNode );

      extractor =
            (IPSDataExtractor) m_userStatusExtractors.get( ROLESET_NAME );
      PSLiteralSet roleSet = (PSLiteralSet) extractor.extract( data );
      Iterator roles;
      if ( null == roleSet )
         roles = PSIteratorUtils.emptyIterator();
      else
         roles = roleSet.iterator();
      if ( roles.hasNext())
      {
         Element roleSetNode = doc.createElement( ROLESET_NAME );
         userStatusNode.appendChild( roleSetNode );
         while ( roles.hasNext())
         {
            Element roleNode = doc.createElement( ROLE_NAME );
            roleNode.appendChild( doc.createTextNode(roles.next().toString()));
            roleSetNode.appendChild( roleNode );
         }
      }

      Element reqPropsNode = doc.createElement( REQPROPS_NAME );
      userStatusNode.appendChild( reqPropsNode );

      extractor =
            (IPSDataExtractor) m_userStatusExtractors.get( USERAGENT_NAME );
      Element userAgentNode = doc.createElement( USERAGENT_NAME );
      Object o = extractor.extract( data );
      if ( null != o )
         userAgentNode.appendChild( doc.createTextNode( o.toString()));
      reqPropsNode.appendChild( userAgentNode );

      extractor =
            (IPSDataExtractor) m_userStatusExtractors.get( ACCEPTLANG_NAME );
      Element acceptLangNode = doc.createElement( ACCEPTLANG_NAME );
      o = extractor.extract( data );
      if ( null != o )
         acceptLangNode.appendChild( doc.createTextNode( o.toString()));
      reqPropsNode.appendChild( acceptLangNode );

      return userStatusNode;
   }


   /**
    * Checks the supplied field against a cached set of information to determine
    * if this is a binary field. It also considers the 'forceBinary' flag in
    * its calculation.
    *
    * @param field A valid field that needs to be checked. Never <code>null
    *    </code>.
    *
    * @return <code>true</code> if the supplied field is a binary column
    *
    * @throws PSSystemValidationException if there are any errors
    */
   public boolean isBinaryField( PSField field ) throws PSSystemValidationException
   {
      if ( null == field )
         throw new IllegalArgumentException( "field cannot be null" );

      boolean isBinary = field.isForceBinary();

      if (!isBinary)
      {
         IPSBackEndMapping locator = field.getLocator();
         if (locator instanceof PSBackEndColumn)
         {
            // get the tableset for this field
            PSBackEndColumn col = (PSBackEndColumn)locator;
            String tableRef = col.getTable().getAlias().toUpperCase();

            // check map first
            PSTableSet tableSet = (PSTableSet)m_tableRefMap.get(tableRef);

            if (tableSet == null)
            {
               Iterator tableSets = m_tableSets.iterator();
               while (tableSets.hasNext())
               {
                  PSTableSet tmpTableSet = (PSTableSet)tableSets.next();
                  Iterator tables = tmpTableSet.getTableRefs();
                  while (tables.hasNext())
                  {
                     PSTableRef ref = (PSTableRef)tables.next();
                     if (ref.getAlias().equalsIgnoreCase(tableRef))
                     {
                        tableSet = tmpTableSet;
                        // add it to the map
                        m_tableRefMap.put(tableRef, tableSet);
                        break;
                     }
                  }
               }
            }

            if (tableSet == null)
            {
               throw new PSSystemValidationException(IPSServerErrors.CE_MISSING_TABLE,
                  col.getTable().getAlias());
            }

            // now that we have the tableset, check for binary
            try
            {
               isBinary = PSMetaDataCache.isBinaryBackendColumn(tableSet,
                  col);
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

               throw new PSSystemValidationException(IPSServerErrors.CE_SQL_ERRORS,
                  buf.toString());
            }
         }
      }

      return isBinary;
   }

   /**
    * Gets this builder's view evaluator, used to determine if fields should
    * be visible, and to get the name to use for the view parameter when
    * creating action links.
    *
    * @return The view evaluator, never <code>null</code>.
    */
   public PSViewEvaluator getViewEvaluator()
   {
      return m_viewEvaluator;
   }

   /**
    * Extracts the user's locale/language string from the execution data object
    * which in turn taken from the user's session. This string shall be in the
    * syntax of XML language attribute like "fr-ca" or "en-us".
    *
    * @param data The execution data, used to obtain locale string, must not
    * be <code>null</code>.
    * @return User's locale string as stored in the user session or as the
    * default language if not found in the session. Never <code>null</code>
    * or <code>empty</code>.
    */
   protected String getUserLocaleString(PSExecutionData data)
   {
      String lang = PSI18nUtils.DEFAULT_LANG;
      try
      {
         IPSDataExtractor extractor =
              (IPSDataExtractor) m_userStatusExtractors.get( LANGUAGE );

         Object obj = extractor.extract( data );
         if(obj != null)
           lang = obj.toString();
      }
      catch(PSDataExtractionException e)
      {
         //return default locale string
      }
      return lang;
  }


   /**
    * Determines whether this field should appear in the output document.
    * Before creating each display field, this method is called. This allows
    * derived classes an opportunity to hide a field. This class always
    * returns <code>true</code>.
    *
    * @param field A valid field for which you want to determine if it should
    *    be shown. Never <code>null</code>.
    *
    * @return <code>true</code> if the field should be placed in the output
    *    document, <code>false</code> otherwise.
    */
   boolean showField( PSField field )
   {
      if ( null == field )
         throw new IllegalArgumentException( "field can't be null" );
      return true;
   }


   /**
    * Creates an element for the supplied document that contains all of the
    * links in the supplied list. If there are no links, no element is added.
    * The element meets the ContentEditor dtd. Section links are URLs used by
    * the stylesheet.
    *
    * @param doc The document to which the element will eventually be added.
    *    Never <code>null</code>.
    *
    * @param linkExtractors A valid set containing 0 or more user defined links.
    *    If empty, no node is created. Never <code>null</code>.
    *
    * @param data Contains the data associated with this request, used to
    *    calculate any replacement values. Never <code>null</code>.
    *
    * @return The node, if created, otherwise <code>null</code>.
    */
   protected Node createSectionLinkElement( Document doc,
         Iterator linkExtractors, PSExecutionData data )
      throws PSDataExtractionException
   {
      if ( null == doc || null == linkExtractors || null == data )
         throw new IllegalArgumentException( "One or more params were null." );

      Element sectionLinksNode = null;

      if ( linkExtractors.hasNext())
      {
         sectionLinksNode = doc.createElement( SECTIONLINKSET_NAME );
         while (linkExtractors.hasNext())
         {
            IPSDataExtractor extractor =
                  (IPSDataExtractor) linkExtractors.next();
            Object link = extractor.extract(data);
            PSUrlRequest req = (PSUrlRequest) extractor.getSource()[0];

            Element linkNode = doc.createElement( SECTIONLINK_NAME );
            linkNode.setAttribute( NAME_ANAME, req.getName());
            linkNode.appendChild( doc.createTextNode( link.toString()));
            sectionLinksNode.appendChild( linkNode );
         }
      }
      return sectionLinksNode;
   }

   /**
    * Creates an element for the supplied document that contains 1 or more
    * URLs used for Action elements such as buttons (e.g. Insert). The element
    * meets the ContentEditor dtd. The set of possible actions includes those
    * set by the user, plus the default actions for the type of document.
    * <p>Action links are generated by derived classes by implmenting the
    * {@link #getActionLinks(Document,PSExecutionData) getActionLinks} method.
    *
    * @param doc The document to which the element will eventually be added.
    *    Never <code>null</code>.
    *
    * @param data Contains the data associated with this request, used to
    *    calculate any replacement values. Never <code>null</code>.
    *
    * @return The node, if created, otherwise <code>null</code>.
    */
   private Node createActionLinkElement( Document doc, PSExecutionData data )
      throws PSDataExtractionException
   {
      if ( null == doc || null == data )
         throw new IllegalArgumentException("One or more params were null.");

      Element actionLinks = doc.createElement( ACTIONLINKSET_NAME );

      // first, add all user defined links
      Iterator links = PSIteratorUtils.iterator( m_actionLinkExtractors );
      boolean addedOne = false;
      while (links.hasNext())
      {
         //todo: process these guys when supported
         addedOne = false;
      }

      links = getActionLinks( doc, data );
      while ( links.hasNext())
      {
         addedOne = true;
         actionLinks.appendChild((Node) links.next());
      }

      return addedOne ? actionLinks : null;
   }

   /**
    * Creates 0 or more ActionLink elements and returns them. These should
    * include any designer specified links. These are typically placed at the
    * bottom of the page and are used to submit the modified document. If
    * there is no submission, then the list could be empty. All params for
    * each action are URL encoded so they are ready to be made part of a url,
    * unless indicated otherwise by the overriding method.
    *
    * @param doc The document to which the element will eventually be added.
    *    Never <code>null</code>.
    *
    * @param data Contains the data associated with this request, used to
    *    calculate any replacement values. Never <code>null</code>.
    *
    * @return A list with 0 or more Element entries, never <code>null</code>.
    *
    * @throws PSDataExtractionException If any problems occur trying to get
    *    the values from the execution data.
    */
   abstract protected Iterator getActionLinks( Document doc,
         PSExecutionData data )
      throws PSDataExtractionException;


   /**
    * A convenience method when calling the 6 parameter version of this
    * method. Passes <code>null</code> for the name of the action and 
    * accesskey.
    */
   static public Element createActionElement( Document doc, String label,
         Iterator params, boolean isEnabled )
   {
      return createActionElement( doc, label, null, params, isEnabled, null);
   }

   /**
    * A convenience method when calling the 6 parameter version of this
    * method. Passes <code>null</code> for the accesskey.
    */
   static public Element createActionElement( Document doc, String label,
         String name, Iterator params, boolean isEnabled )
   {
      return createActionElement( doc, label, name, params, isEnabled, null);
   }

   /**
    * Creates an <ActionLink> element according to the ContenetEditor dtd.
    *
    * @param doc The document to which the element will eventually be added.
    *    Never <code>null</code>.
    *
    * @param label The text displayed to the user to indicate the action of
    *    the widget. Non-empty.
    *
    * @param name The internal name of the button. If not <code>null</code> or
    *    empty, then the name param is added to the generated ActionLink
    *    element.
    *
    * @param params A list containing PSMapPair objects. Each object has a
    *    key that is the name of the param and a value that is the value of
    *    the param. Each key must be a non-empty String and each value is
    *    either a String or <code>null</code>. The list is never <code>null
    *    </code>, may be empty. The params will be added in the order they
    *    appear in this list.
    *
    * @param isEnabled A flag to indicate how the widget should be rendered.
    *    If <code>true</code> it will appear normally. Otherwise it may be
    *    grayed or missing all together.
    *
    * @param accesskey the mnemonic key for the action, if <code>null</code>
    *    ignored. If length of the string is more than one after trim, then
    *    the first character will be considered. 
    * 
    * @return The generated node, never <code>null</code>.
    */
   static public Element createActionElement( Document doc, String label,
         String name, Iterator params, boolean isEnabled, String accesskey)
   {
      if ( null == doc || null == params
            || null == label || label.trim().length() == 0 )
      {
         throw new IllegalArgumentException(
               "One or more params was null or empty." );
      }

      Element actionNode = doc.createElement( ACTIONLINK_NAME );
      if ( !isEnabled )
         actionNode.setAttribute( DISABLED_NAME, IPSConstants.BOOLEAN_TRUE );
      if ( null != name && name.trim().length() > 0 )
         actionNode.setAttribute( NAME_ANAME, name );
      
      Element labelNode = doc.createElement( LABEL_NAME );
      labelNode.appendChild( doc.createTextNode( label ));
      if(null != accesskey && accesskey.trim().length() > 0)
      {
         accesskey = accesskey.charAt(0) + "";
         labelNode.setAttribute(ATTR_ACCESSKEY,accesskey);
      }
         
      actionNode.appendChild( labelNode );

      while ( params.hasNext())
      {
         Element paramNode = doc.createElement( PARAM_NAME );
         Object o = params.next();
         if ( null == o || !( o instanceof PSMapPair ))
            throw new IllegalArgumentException( "Bad type for param entry. " + o.getClass()); //todo remove
         PSMapPair entry = (PSMapPair) o;

         paramNode.setAttribute( NAME_ANAME, entry.getKey().toString());

         if ( null != entry.getValue())
         {
            paramNode.appendChild(
                  doc.createTextNode( entry.getValue().toString()));
         }
         actionNode.appendChild( paramNode );
      }
      return actionNode;
   }


   /**
    * Adds a step to the collection of steps that will be executed when a
    * request is processed. They will be processed in the order they were added.
    *
    * @param step A valid step that creates a DisplayField element while
    * building a document.
    */
   protected void addBuildStep( IPSBuildStep step )
   {
      if ( null == step )
         throw new IllegalArgumentException( "step cannot be null" );
      m_buildSteps.add(step);
   }

   /**
    * Adds all of the fields in the supplied list to the document in the
    * proper location and in the order they appear in the list.
    *
    * @param doc The document to which the element will eventually be added.
    *    Never <code>null</code>.
    *
    * @param parent The node to which the generated node will be added.
    *    Never <code>null</code>.
    *
    * @param fields The fields to add. If empty, nothing is done. Never <code>
    *    null</code>. Each entry in the list must be an Element object.
    *
    * @return <code>true</code> if any elements are added, <code>false</code>
    *    otherwise.
    */
   private boolean addFieldElements( Document doc, Element parent,
         Iterator fields )
   {
      if ( null == doc || null == parent || null == fields )
         throw new IllegalArgumentException( "One or more params were null." );

      boolean addedOne = false;
      while ( fields.hasNext())
      {
         Element elem = (Element) fields.next();
         parent.appendChild( elem );
         addedOne = true;
      }
      return addedOne;
   }

   /* These are the tag names for elements that make up the output doc.
      They are never empty. */

   /**
    * This is the doc root element tag name of the result document. Must not
    * be empty or <code>null</code>.
    */
   /** XML document element name. */
   public static final String ROOT_NAME = "ContentEditor";
   /** XML document element name. */
   public static final String ITEM_NAME = "ItemContent";
   /** XML document element name. */
   public static final String USERSTATUS_NAME = "UserStatus";
   /** XML document element name. */
   public static final String CONTROLNAMES_NAME = "ControlNameSet";
   /** XML document element name. */
   public static final String ACTIONLINKS_NAME = "ActionLinkList";
   /** XML document element name. */
   public static final String ACTION_NAME = "ActionLink";
   /** XML document element name. */
   public static final String SECTIONLINKS_NAME = "SectionLinkList";
   /** The attribute name for primary key to the row addressed. */
   public static final String CHILDKEY_ATTRIB = "childkey";

   /** XML document element name. */
   public static final String VARIANTLIST_NAME = "VariantList";

   /** XML document attribute name */
   private static final String NEW_DOC_ATTRIB = "newDocument";
   
   /** XML attribute for item locale **/
   private static final String ITEM_LOCALE_ATTR = "itemLocale";

   private static final String TIME_NAME = "Time";

   private static final String ROLE_NAME = "Role";
   private static final String REQPROPS_NAME = "RequestProperties";
   private static final String SECTIONLINKSET_NAME = "SectionLinkList";
   private static final String SECTIONLINK_NAME = "SectionLink";
   private static final String ACTIONLINKSET_NAME = "ActionLinkList";
   private static final String ACTIONLINK_NAME = "ActionLink";

   private static final String HIDDENFORMPARAMS_NAME = "HiddenFormParams";
   /** XML document attribute name. */
   public static final String DOCTYPE_NAME = "docType";
   private static final String MODE_NAME = "mode";
   private static final String COMMANDNAME_NAME = "commandName";
   private static final String CONTENTID_NAME = "contentId";
   private static final String CONTENTTYPEID_NAME = "contentTypeId";
   private static final String ENABLERELATEDCONTENT_ATTR = "enableRelatedContent";
   private static final String CURRENT_TIME_STAMP = "currentTimeStamp";


   private static final String DOCTYPE_NORMAL = "sys_normal";
   /** XML document attribute value name. */
   public static final String DOCTYPE_ERROR = "sys_error";

   private static final String MODE_PREVIEW = "sys_preview";
   private static final String MODE_EDIT = "sys_edit";

   private static final String SESSIONID_NAME = "sessionId";

   /**
    * Attribute name for action link.
    */
   private static final String DISABLED_NAME = "isDisabled";

   private static final String NAME_ANAME = "name";
   private static final String NAME_ENAME = "Name";

   /**
    * The name of the element that contains the name of an XSL control.
    */
   public static final String CONTROLNAME_NAME = "ControlName";

   private static final String USERAGENT_NAME = "UserAgent";
   private static final String ACCEPTLANG_NAME = "AcceptLanguage";
   private static final String USERNAME_NAME = "UserName";
   private static final String LANGUAGE = "language";

   /**
    * This is the xml path to the BasicInfo node in the output document,
    * not including the root. It contains a trailing slash. Never <code>null
    * </code> or empty.
    */
   private static final String BASICINFO_PATH = "Workflow/BasicInfo/";

   /**
    * The label for the clone button that is added to the workflow actions list.
    * This button is available in all parent and child
    * editors and it makes a complete copy of the current item and adds it as
    * a new item, taking the user to the parent edit screen. Never
    * <code>null</code> or empty.
    */
   private static final String CLONE_LABEL = "New Version";

   /**
    * The button label for the 'Return to parent' button that is added to the
    * workflow actions list. This button is available in all child editors
    * and it takes the user back to the root parent. Never <code>null</code>
    * or empty.
    */
   private static final String PARENT_RETURN_LABEL = "Return to Parent";

   /**
    * A concated list of role names, seperated by commas. Never empty.
    */
   private static final String ROLESET_NAME = "Roles";


   /**
    * An array of 0 or more extractors that are used to generate the URLs
    * passed through to the stylesheet for its own use. Each entry in the
    * array corresponds to a single link. Never <code>null</code> after
    * construction.
    */
   private IPSDataExtractor [] m_sectionLinkExtractors;

   /**
    * An array of 0 or more extractors that are used to generate the URLs used
    * for action events such as updating the form. Each entry in the array
    * corresponds to a single action link. Never <code>null</code> after
    * construction.
    */
   private IPSDataExtractor [] m_actionLinkExtractors;

   /**
    * Extractors to get the UserStatus information. Each entry has a key
    * whose value is the name of the element tag for which the extractor data
    * will be used as the value. Never <code>null</code>.
    */
   private Map m_userStatusExtractors = new HashMap();

   /**
    * These are the operations that will be performed each time a result
    * document is built. Each step optionally creates a DisplayField element
    * and adds it to the hidden or visible lists in this object. May be empty
    * (although unlikely in practice), never <code>null</code>.
    */
   private List<IPSBuildStep> m_buildSteps = new ArrayList<>(10);

   /**
    * This is the 'action' for the main form of the resulting editor. It is
    * relative to the original request. Never empty after construction.
    */
   private String m_formAction;

   /**
    * The name of the command handler that created this document. Never empty
    * after construction.
    */
   private String m_commandName;

   /**
    * A flag to indicate whether this document was built for editing or
    * previewing. Set at construction and never changed.
    */
   private boolean m_isEditMode;

   /**
    * Never <code>null</code> after construction.
    */
   protected PSEditorDocumentContext m_docContext;

   /**
    * This extractor can be used to get the content id from the execution
    * data. The content id may or may not be present in the data. Never
    * <code>null</code> after construction.
    */
   private IPSDataExtractor m_contentIdExtractor;
   
   /**
    * This extractor can be used to get the item locale from the execution
    * data. The item locale may or may not be present in the data. Never
    * <code>null</code> after construction.
    */
   private IPSDataExtractor m_itemLocaleExtractor;

   /**
    * A list of IPSInternalResultHandler objects. Each resource will be
    * queried, and the resulting data will be added to the Workflow element in
    * the output doc. Never <code>null</code> after construction. May be empty.
    */
   private List m_workflowHandlers;

   /**
    * Contains Map.Entry objects, keyed with the constants
    * ..._EXTRACTOR_KEY. Each entry has the HTML param as the key and an
    * extractor to get the output value as the entry's value. Never <code>null
    * </code> or empty after construction.
    */
   private static Map ms_paramExtractors;

   /**
    * The unique id for this particular builder within the context of the
    * content editor. Immutable after being set in ctor.
    */
   private int m_pageId;

   /**
    * Contains the table sets used by this editor. Set during construction,
    * then immutable. Never <code>null</code>. Each item in the list is a
    * PSTableSet.
    */
   private List m_tableSets = new ArrayList();

   /**
    * This is the unique content type identifier for this editor. Set during
    * construction, then immutable. It is stored as a String to save the
    * conversion for each document constructed.
    */
   private String m_contentTypeId;

   /**
    * This flag holds the status whether or not related content is enabled for
    * the current content editor. Initialized in ctor, never changed after that.
    */
   private boolean m_isRelatedContentEnabled = false;

   /**
    * The object type for this content editor. One of the
    * PSContentEditor.OBJECT_TYPE_xxx values. Initialized by constructor,
    * never changed after that.
    */
   private int m_objectType;

   /**
    * Map with tableRef alias uppercased as the key (a String) and the
    * PSTableSet that the table is contained by as the value.  Never <code>null
    * </code>, entries are added as fields are checked to see if backend column
    * is binary.
    */
   private Map m_tableRefMap = new HashMap();

   /**
    * Used to determine if fields should be visible at runtime, and to determine
    * the name to use for the view parameters when constructing actionlinks.
    * Initialized during construction, never <code>null</code> or modified after
    * that.
    */
   private PSViewEvaluator m_viewEvaluator;

   /**
    * This class encapsulates the members that are modified by the
    * methods in the <code>IPSBuildContext interface</code> at run time, in
    * order to avoid any threading issues.  All method calls are delegated back
    * to the {@link PSEditorDocumentBuilder}, which in turn uses this class to
    * access the members.
    */
   private class PSEditorDocumentBuildContext implements IPSBuildContext
   {
      /**
       * Creates a context object to contain the document, hidden field list and
       * visible field list.
       *
       * @param parentBuilder The builder to delegate calls to.  May not be
       * <code>null</code>.
       * @param resultDoc The result document to provide to the buildsteps.  May
       * not be <code>null</code>.
       */
      public PSEditorDocumentBuildContext(PSEditorDocumentBuilder parentBuilder,
         Document resultDoc)
      {
         if (parentBuilder == null)
            throw new IllegalArgumentException("parentBuilder may not be null");

         if (resultDoc == null)
            throw new IllegalArgumentException("resultDoc may not be null");

         m_parentBuilder = parentBuilder;
         m_resultDoc = resultDoc;
      }

      /**
       * Accessor for the list of hidden fields.
       *
       * @return The list, never <code>null</code>, may be empty.  Modifications
       * to the returned list will be reflected by the list contained in this
       * object.
       */
      public List getHiddenFields()
      {
         return m_hiddenFields;
      }

      /**
       * Accessor for the list of visible fields.
       *
       * @return The list, never <code>null</code>, may be empty.  Modifications
       * to the returned list will be reflected by the list contained in this
       * object.
       */
      public List getVisibleFields()
      {
         return m_visibleFields;
      }

      // see IPSBuildContext javadoc
      public void addHiddenField( Element dispNode, String controlName )
      {
         m_parentBuilder.addHiddenField(this, dispNode, controlName);
      }

      // see IPSBuildContext javadoc
      public void addVisibleField( Element dispNode, String controlName )
      {
         m_parentBuilder.addVisibleField(this, dispNode, controlName);
      }

      /**
       * An accessor for the result document.
       *
       * @return The result document, never <code>null</code>.
       */
      public Document getResultDocument()
      {
         return m_resultDoc;
      }

      /**
       * This is the document that will eventually be returned to the requestor.
       * Never <code>null</code> after the ctor is called.  Modified by other
       * classes that obtain this document through {@link #getResultDocument()}.
       */
      private Document m_resultDoc;

      /**
       * Contains a list of DisplayField Element objects. They will be added to
       * the output document at the top of the field list in the order they
       * appear in this list. May be empty, never <code>null</code>.  Modified
       * by calls to {@link IPSBuildContext#addHiddenField(
       * Element, String)}.
       */
      private List m_hiddenFields = new ArrayList(10);

      /**
       * Contains a list of DisplayField Element objects. They will be added to
       * the output document following the hidden fields in the order they
       * appear in this list. May be empty (although unlikely in practice),
       * never <code>null</code>.   Modified by calls to {@link
       * #addVisibleField(Element, String)}.
       */
      private List m_visibleFields = new ArrayList(10);

      /**
       * The editor document builder passed into the ctor to which
       * IPSBuildContext calls to add hidden and visible fields are delegated.
       * Never <code>null</code> or modified after that.
       */
      private PSEditorDocumentBuilder m_parentBuilder = null;
   }
}



