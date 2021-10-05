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
package com.percussion.cms;

import com.percussion.cms.handlers.PSContentEditorHandler;
import com.percussion.cms.handlers.PSEditCommandHandler;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSLiteral;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.server.IPSServerErrors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class is a container for information that is needed when building the
 * output document, but cannot be derived from the editor definition. It is
 * just a container, it doesn't implement any behaviors.
 * <p>Each content editor has a page map that contains a graph of all editors
 * used to enter/modify content for the item, and their relationships. Each
 * editor has a unique page id. Each editor also has a 'childid', which is the
 * id of the mapper that defines the editor.
 * <p>Most of the values must be set by the creator to specify the appropriate
 * context.
 */
public class PSEditorDocumentContext
{
   /**
    * The EDITOR_TYPE... constants must have a delta of 1 and fill a range
    * defined by ..._FIRST and _LAST, inclusive. First and last are used for
    * validation purposes.
    * Must equal the smallest value of the public EDITOR_TYPE... constants.
    */
   private static final int EDITOR_TYPE_FIRST = 1;

   /**
    * The type that means this is a row editor, or an editor that deals with
    * a single content item at a time.
    */
   public static final int EDITOR_TYPE_ROW_EDITOR = 1;

   /**
    * The type that means this is a summary editor, or an editor that deals with
    * multiple content items at a time.
    */
   public static final int EDITOR_TYPE_SUMMARY_EDITOR = 2;

   /**
    * The type that means this is not really an editor, it is used for
    * displaying the content item only.
    */
   public static final int EDITOR_TYPE_READ_ONLY = 3;

   /**
    * Used for validation purposes. Must equal the largest value of the public
    * EDITOR_TYPE... values.
    */
   private static final int EDITOR_TYPE_LAST = 3;


   /**
    * Construct a new one with all default values.
    *
    * @param ceh The handler that delegates to the handler creating this
    *    context. Never <code>null</code>.
    *
    * @param app The main application that contains these editors. May be
    *    <code>null</code>, but if it is, you will get local defaults rather
    *    than values from the app.
    *
    * @param ce The definition of the editor associated with this context.
    *    Never <code>null</code>.
    *
    * @throws IllegalArgumentException if ceh or ce is <code>null</code>.
    */
   public PSEditorDocumentContext( PSContentEditorHandler ceh,
         PSApplication app, PSContentEditor ce )
   {
      if ( null == ceh || null == ce )
         throw new IllegalArgumentException( "handler and def cannot be null" );
         
      m_ceHandler = ceh;
      m_contentEditor = ce;
      if ( null != app )
      {
         m_requestTypeParamName = app.getRequestTypeHtmlParamName();
         m_requestTypeInsert = app.getRequestTypeValueInsert();
         m_requestTypeDelete = app.getRequestTypeValueDelete();
         m_requestTypeUpdate = app.getRequestTypeValueUpdate();
      }
   }


   /**
    * Get the name of the HTML parameter being used to identify the request
    * type. These are obtained from the application that owns the editor.
    *
    * @return The Html paremeter, never empty.
    */
   public String getRequestTypeHtmlParamName()
   {
      return m_requestTypeParamName;
   }

   /**
    * Get the value being used to identify the request as being a insert. This
    * should be the value of the parameter specified by <code>
    * getRequestTypeHtmlParamName</code> when asking the server to perform an
    * insert. These are obtained from the application that owns the editor.
    *
    * @return The value to request an insert, never empty.
    */
   public String getRequestTypeValueInsert()
   {
      return m_requestTypeInsert;
   }


   /**
    * Get the value being used to identify the request as being a delete. This
    * should be the value of the parameter specified by <code>
    * getRequestTypeHtmlParamName</code> when asking the server to perform an
    * delete. These are obtained from the application that owns the editor.
    *
    * @return The value to request an insert, never empty.
    */
   public String getRequestTypeValueDelete()
   {
      return m_requestTypeDelete;
   }


   /**
    * Get the value being used to identify the request as being a update. This
    * should be the value of the parameter specified by <code>
    * getRequestTypeHtmlParamName</code> when asking the server to perform an
    * update. These are obtained from the application that owns the editor.
    *
    * @return The value to request an insert, never empty.
    */
   public String getRequestTypeValueUpdate()
   {
      return m_requestTypeUpdate;
   }

   /**
    * Returns the definition of the content editor associated with this
    * context. It should be treated as a read-only object.
    *
    * @return A valid editor, never <code>null</code>.
    */
   public PSContentEditor getContentEditorDef()
   {
      return m_contentEditor;
   }

   /**
    * The name of the command used to generate the result document.
    * <p>The name of the edit command handler by default.
    *
    * @return A valid name, never empty.
    */
   public String getCommandName()
   {
      return m_commandName;
   }

   /**
    * Sets the name of the command used to generate the result document.
    *
    * @param commandName The name of the command handler. Never empty.
    *
    * @throws IllegalArgumentException If commandName is empty.
    */
   public void setCommandName( String commandName )
   {
      if ( null == commandName || commandName.trim().length() == 0 )
         throw new IllegalArgumentException( "Command name is null or empty." );

      m_commandName = commandName;
   }


   /**
    * See {@link #getRequestUrl() getRequestUrl} for details.
    *
    * @param baseUrl The fully qualified url-string of the form:
    *    http://server:port/ServerRoot/approot/resource.html. The passed in
    *    param is not validated to this form.
    */
   public void setRequestUrl( String baseUrl )
   {
      m_requestUrl = baseUrl;
   }


   /**
    * This is the URL that could be used as the base of a URL to hit this
    * editor. Only the query string should need to be added. Note that this
    * is not validated by this class, so it is encumbant upon the creator
    * to follow the guidelines mentioned in {@link #setRequestUrl(String)
    * setRequestUrl}.
    *
    * @return The url-string set with this property's mutator method. May be
    *    <code>null</code>.
    */
   public String getRequestUrl()
   {
      return m_requestUrl;
   }

   /**
    * A flag to indicate whether the returned document is for editing or
    * previewing. <code>true</code> by default.
    *
    * @return <code>true</code> if the result document was built to be edited,
    *    <code>false</code> otherwise.
    */
   public boolean isEditMode()
   {
      return m_isEditMode;
   }

   /**
    * Sets  the flag returned by <code>isEditMode</code>.
    *
    * @param isEditMode The editing state of this editor.
    */
   public void setDocumentMode( boolean isEditMode )
   {
      m_isEditMode = isEditMode;
   }

   /**
    * Interface to acquire named values from the system def.
    *
    * @param paramName The name of the param which you wish to get. Never
    *    empty. Param names typically take the form 'com.percussion.param'
    *    for system params and 'param' for user specified params.
    *
    * @return The value associated with the named parameter if found, the
    *    empty string otherwise.
    *
    * @throws IllegalArgumentException if paramName is <code>null</code> or
    *    empty.
    */
   public String getInitParam( String paramName )
   {
      if ( null == paramName || paramName.trim().length() == 0 )
         throw new IllegalArgumentException( "param can't be null or empty" );

      String result = "";
      IPSReplacementValue param =
            m_ceHandler.getInitParam( m_commandName, paramName );
      if ( param instanceof PSLiteral )
         result = ((Object) param).toString();
      return result;
   }


   /**
    * Sets the type of this editor. The value is retrieved via methods of the
    * form <code>is&lt;type&gt;Editor()</code>.
    *
    * @param type One of the EDITOR_TYPE... constants.
    *
    * @throws IllegalArgumentException if the supplied value is not one of the
    *    known types.
    */
   public void setEditorType( int type )
   {
      if ( type < EDITOR_TYPE_FIRST || type > EDITOR_TYPE_LAST )
      {
         throw new IllegalArgumentException( "Invalid type supplied: " + type );
      }

      m_editorType = type;
   }

   /**
    * Indicates if this editor is a row editor. A row editor is one of 2
    * editors that are used for a content editor. It contains all the fields
    * for a single row, allowing the user field level modification.
    */
   public boolean isRowEditor()
   {
      return m_editorType == EDITOR_TYPE_ROW_EDITOR;
   }


   /**
    * Indicates if this editor is a summary editor. A summary editor is one
    * of 2 editors that are used for a content editor. It contains a single
    * control which processes 0 or more child rows, a row at a time. Individual
    * fields cannot be modified.
    */
   public boolean isSummaryEditor()
   {
      return m_editorType == EDITOR_TYPE_SUMMARY_EDITOR;
   }

   /**
    * A map containing an entry for each editor page that can be generated
    * by the handler using the context. The key is an Integer of a pageId
    * whose value is a PSPageInfo object for that editor page. Use {@link
    * #getPageInfoMap() getPageInfoMap} to get the map back.
    *
    * @param pageMap Never <code>null</code>. Contains at least 1 entry.
    *
    * @throws IllegalArgumentException if pageMap is <code>null</code> or
    *    doesn't contain at least 1 entry.
    */
   public void setPageInfoMap( Map pageMap )
   {
      if ( null == pageMap || pageMap.isEmpty())
         throw new IllegalArgumentException( "map can't be null or empty" );
      m_pageInfo = pageMap;
   }


   /**
    * Returns the map set with {@link #setPageInfoMap(Map) setPageInfoMap}.
    *
    * @return The current map, which may be <code>null</code> if one has not
    *    been set yet.
    */
   public Map getPageInfoMap()
   {
      return m_pageInfo;
   }


   /**
    * Uses the map in this context to determine the page id of the parent of
    * the page passed in. The parent of the root page is itself. If a page
    * has more than 1 parent, the summary editor parent is returned. If more
    * than 1 summary editor parent is found, an exception is thrown.
    *
    * @param pageId The page id of the editor for which you want to find the
    *    parent.
    *
    * @return The page id of the parent.
    *
    * @throws PSNotFoundException if the parent can't be found.
    */
   public int getParentPageId( int pageId )
         throws PSNotFoundException
   {
      Map pageInfo = getPageInfoMap();
      Iterator entries = pageInfo.entrySet().iterator();
      List parentIds = new ArrayList();
      while ( entries.hasNext())
      {
         Map.Entry entry = (Map.Entry) entries.next();
         PSPageInfo info = (PSPageInfo) entry.getValue();
         Iterator pageIds = info.getPageIdList();
         while ( pageIds.hasNext())
         {
            Integer idObj = (Integer) pageIds.next();
            if ( idObj.intValue() == pageId )
               parentIds.add( entry );
         }
      }

      int parentId = -1;
      /* use flag rather than special value so we don't have to worry about
         conflict with possible ids */
      boolean found = false;
      if ( parentIds.size() == 1 )
      {
         found = true;
         Map.Entry entry = (Map.Entry) parentIds.get(0);
         parentId = ((Integer) entry.getKey()).intValue();
      }
      else if ( parentIds.size() > 0 )
      {
         Iterator parentIdIter = parentIds.iterator();
         while ( parentIdIter.hasNext() && !found )
         {
            Map.Entry entry = (Map.Entry) parentIdIter.next();
            PSPageInfo info = (PSPageInfo) entry.getValue();
            if ( info.isSummaryEditor())
            {
               if ( found )
               {
                  throw new PSNotFoundException(
                        IPSServerErrors.CE_AMBIGUOUS_PAGEID, ""+pageId );
               }
               else
                  found = true;
               parentId = ((Integer) entry.getKey()).intValue();
            }
         }
      }
      if ( !found )
         throw new PSNotFoundException( IPSServerErrors.CE_NO_PARENT, ""+pageId );
      return parentId;
   }


   /**
    * Scans the page map to find the first entry that contains the supplied id
    * and returns the key for that entry. Note that there may be more than 1
    * entry for a particular id, but they would all return the same parent.
    *
    * @param childId The child (mapper) id for which you want to find the
    *    the page.
    *
    * @return The page id that uses the mapper with mapperId.
    *
    * @throws PSNotFoundException if the id can't be found.
    */
   public int getPageId( int childId )
      throws PSNotFoundException
   {
      int pageId = -1;  // return value
      boolean found = false;
      Iterator entries = m_pageInfo.entrySet().iterator();
      while ( entries.hasNext() && !found)
      {
         Map.Entry entry = (Map.Entry) entries.next();
         PSPageInfo info = (PSPageInfo) entry.getValue();
         if ( info.getChildId() == childId )
         {
            found = true;
            pageId = ((Integer) entry.getKey()).intValue();
         }
      }
      if ( !found )
      {
         String [] args =
         {
            "child",
            ""+childId
         };
         throw new PSNotFoundException(
               IPSServerErrors.CE_MISSING_PAGEMAP_ENTRY, args );
      }
      return pageId;
   }


   /**
    * Pulls the child id for the supplied page id from the page map.
    *
    * @param pageId A unique identifier for one of the editors created by
    *    this builder.
    *
    * @return A valid child id.
    *
    * @throws PSNotFoundException if there is no entry in the page map for the
    *    supplied id.
    */
   public int getChildId( int pageId )
      throws PSNotFoundException
   {
      PSPageInfo info = (PSPageInfo) m_pageInfo.get( new Integer( pageId ));
      if ( null == info )
      {
         String [] args =
         {
            "page",
            ""+pageId
         };
         throw new PSNotFoundException(
               IPSServerErrors.CE_MISSING_PAGEMAP_ENTRY, args );
      }

      return info.getChildId();
   }


   /**
    * Scans the page map looking for all editors that match this child id.
    *
    * @param childId The child (mapper) id for which you want to find the
    *    the set of pages.
    *
    * @return A set of Map.Entry objects for all of the found editors. Each
    *    entry has a key with is the page id (as an Integer) and the value is
    *    a PSPageInfo object. Never <code>null</code>. Empty if non found.
    */
   public Iterator getPageInfoList( int childId )
   {
      List results = new ArrayList();
      Iterator entries = m_pageInfo.entrySet().iterator();
      while ( entries.hasNext())
      {
         Map.Entry entry = (Map.Entry) entries.next();
         PSPageInfo info = (PSPageInfo) entry.getValue();
         if ( info.getChildId() == childId )
            results.add( entry );
      }
      return results.iterator();
   }

   /**
    * Extracts the child (mapper) id list from the pageInfo map entries and
    * builds a map that just contains these lists. The generated map has the
    * same keys as the supplied map, only the values differ.
    *
    * @return A map containing all of the parent ids found in the page info,
    *    as keys, and their list of child page ids as the value for the entry.
    */
   public Map getPageIdMap()
   {
      Map idMap = new HashMap();
      Iterator pi = m_pageInfo.entrySet().iterator();
      while ( pi.hasNext())
      {
         Map.Entry entry = (Map.Entry) pi.next();
         Object parentId = entry.getKey();
         PSPageInfo info = (PSPageInfo) entry.getValue();
         Iterator childIds = info.getPageIdList();
         List childIdList = new ArrayList();
         while ( childIds.hasNext())
            childIdList.add( childIds.next());
         idMap.put( parentId, childIdList );
      }
      return idMap;
   }


   /**
    * Gets the actual name for the HTML parameter specified by the supplied
    * internal name. If there is no mapping, the internalName is returned.
    * Typically, one of the constants in <code>PSContentEditorHandler</code>
    * of the form <code>..._PARAM_NAME</code> is used for the param.
    *
    * @param internalName The parameter name to convert. Never empty.
    *
    * @return The overridden name, if present, otherwise, the passed name.
    *    Never empty.
    *
    * @throws IllegalArgumentException if internalName is <code>null</code> or
    *    empty.
    */
   public String getSystemParam( String internalName )
   {
      if ( null == internalName || internalName.trim().length() == 0 )
         throw new IllegalArgumentException( "param can't be null or empty" );

      return m_ceHandler.getParamName( internalName );
   }

   // future functionality
   /**
    * See {@link #isErrorDocument() isErrorDocument) for details.
    *
    * @param isError A flag to indicate whether the builder using this context

   public void setAsErrorDocument( boolean isError )
   {
      m_isErrorDoc = isError;
   }
    */

   /**
    * Indicates whether this is a fresh request, a request due to a validation
    * error. The builders will behave differently when building an error doc.

   public boolean isErrorDocument()
   {
      return m_isErrorDoc;
   }
    */

   /**
    * The internal name of the command handler that created this context.
    * Never empty. Defaults to edit command handler.
    */
   private String m_commandName = PSEditCommandHandler.COMMAND_NAME;

   /**
    * A flag to indicate whether this document is editing or previewing.
    * Defaults to <code>true</code>.
    */
   private boolean m_isEditMode = true;

   /**
    * One of the EDITOR_TYPE_xxx values. Defaults to row editor.
    */
   private int m_editorType = EDITOR_TYPE_ROW_EDITOR;

   /**
    * The handler that delegates to the handler that creates this context.
    * Never <code>null</code> after construction.
    */
   private PSContentEditorHandler m_ceHandler;

   /**
    * The definition of the editor associated with this object. Never <code>
    * null</code> after construction.
    */
   private PSContentEditor m_contentEditor;

   /*
    * This map must contain an entry for every editor page generated. The
    * key will be the page id of the editor and the value will be a
    * PSPageInfo object for the referenced editor. Never <code>null</code>,may
    * be empty until set by user after which it must contain at least 1 entry.
    */
   private Map m_pageInfo = new HashMap();

   /**
    * See {@link PSApplication#getRequestTypeHtmlParamName()
    * getRequestTypeHtmlParamName} for a description of this member.
    * Defaults to <code>DBActionType</code> if no app supplied to ctor.
    */
   private String m_requestTypeParamName = "DBActionType";

   /**
    * See {@link PSApplication#getRequestTypeValueInsert()
    * getRequestTypeValueInsert} for a description of this member.
    * Defaults to <code>INSERT</code> if no app supplied to ctor.
    */
   private String m_requestTypeInsert = "INSERT";

   /**
    * See {@link PSApplication#getRequestTypeValueDelete()
    * getRequestTypeValueDelete} for a description of this member.
    * Defaults to <code>DELETE</code> if no app supplied to ctor.
    */
   private String m_requestTypeDelete = "DELETE";

   /**
    * See {@link PSApplication#getRequestTypeValueUpdate()
    * getRequestTypeValueUpdate} for a description of this member.
    * Defaults to </code>UPDATE</code> if no app supplied to ctor.
    */
   private String m_requestTypeUpdate = "UPDATE";

   /**
    * The fully qualified URL to reach this editor, not including the
    * sys_command param (or any query string).
    * <p>Example: http://server:9992/Rhythmyx/editorapp/thiseditor.html
    * <p><code>null</code> until set via {@link #setRequestUrl(String)
    * setRequestUrl}.
    * <e>NOTE:</e> this class does not validate that the described form is
    * actually submitted.
    */
   private String m_requestUrl;
}

