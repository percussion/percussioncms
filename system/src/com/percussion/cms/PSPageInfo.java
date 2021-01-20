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


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class is used to group information about an item editor which is
 * represented as an xml result document.
 * It stores the following information:
 * <ol>
 *    <li>The type of the page, either row editor or summary editor.</li>
 *    <li>The childId for the page, which is passed to the modify handler
 *       so it knows which table(s) to modify. This is the mapper id.</li>
 *    <li>A sequenced list of dataset names that are used to get
 *       the data needed for the request from the backend.</li>
 *    <li>A sequenced list of page ids. Each entry in the list corresponds to
 *       a summary view in a row editor, in the same order they appear in
 *       the document from top to bottom.</li>
 * </ol>
 */
public class PSPageInfo
{
   /**
    * This value must be equal to the first editor type value. Editor type
    * values should increase monotonically by 1 so there are no unused values
    * between <code>TYPE_FIRST</code> and <code>TYPE_LAST</code>, inclusive.
    */
   private static final int TYPE_FIRST = 1;

   /**
    * One of the constants used to set the editor type.
    */
   public static final int TYPE_SUMMARY_EDITOR = 1;

   /**
    * One of the constants used to set the editor type.
    */
   public static final int TYPE_ROW_EDITOR = 2;

   /**
    * This value is used to indicate that this page is for returning data
    * only, not an editor. In fact, the data is similar to the data for the 
    * summary editor, but it includes fields that have the showInSummary or
    * showInPreview flags set to <code>false</code> .
    */
   public static final int TYPE_SUMMARY_DATA = 3;

   /**
    * This value must be equal to the last editor type value. If x is a TYPE_...
    * then x >= TYPE_FIRST && x <= TYPE_LAST.
    */
   private static final int TYPE_LAST = 3;


   /**
    * The only constructor for this class.
    *
    * @param type One of the TYPE_xxx constants. Specifies the type of editor
    *    this object identifies.
    *
    * @param childId The id of the mapper that describes the associated
    *    editor.
    *
    * @param queryHandlers A list of Strings which contain the names of
    *    datasets used to obtain data to fulfill a query. May be <code>null
    *    </code>.
    *
    * @param pageMap A list of Map.Entry objects whose key is a page id (as
    *    an Integer) and whose value is a list of complex child page ids.
    *    May be <code>null</code> or empty. If supplied, the contents are
    *    not validated.
    *
    * @throws IllegalArgumentException if the type is not a supported value.
    */
   public PSPageInfo( int type, int childId, List queryHandlers, List pageMap )
   {
      if ( type < TYPE_FIRST || type > TYPE_LAST )
         throw new IllegalArgumentException( "Unsupported type supplied." );

      m_type = type;
      m_childId = childId;
      if ( null != queryHandlers )
         m_handlers.addAll( queryHandlers );
      if ( null != pageMap )
         m_pageMap.addAll( pageMap );
   }

   /**
    * Sets the builder which can be obtained with the getBuilder() method.
    *
    * @param builder The builder for this page. Never <code>null</code>.
    *
    * @throws IllegalArgumentException if builder is <code>null</code>.
    */
   public void setBuilder( PSEditorDocumentBuilder builder )
   {
      if ( null == builder )
         throw new IllegalArgumentException( "builder cannot be null" );
      m_builder = builder;
   }

   /**
    * Returns the child id for the page described by this object. The child
    * id is the id of the mapper for the fieldset used for the associated
    * editor.
    *
    * @return The associated mapper's id.
    */
   public int getChildId()
   {
      return m_childId;
   }

   /**
    * Returns the builder set with the {@link
    * #setBuilder(PSEditorDocumentBuilder) setBuilder} method. The builder
    * generates the output document for this editor.
    *
    * @return The builder, which may be <code>null</code> until it is set
    *    once, then never <code>null</code>.
    *
    * @see #setBuilder(PSEditorDocumentBuilder) setBuilder
    */
   public PSEditorDocumentBuilder getBuilder()
   {
      return m_builder;
   }

   /**
    * One of the methods used to determine what type of editor this is. A
    * summary editor displays all rows related to the parent, allowing actions
    * at a row level, but not at a field level.
    *
    * @return <code>true</code> if this is a summary editor
    */
   public boolean isSummaryEditor()
   {
      return m_type == TYPE_SUMMARY_EDITOR;
   }


   /**
    * One of the methods used to determine what type of editor this is. A
    * row editor displays all fields for a single content item and allows
    * modifications of the fields, but not of the entire row.
    *
    * @return <code>true</code> if this is a row editor
    */
   public boolean isRowEditor()
   {
      return m_type == TYPE_ROW_EDITOR;
   }

   /**
    * Compares the supplied type to this object. The type indicates what type
    * of editor or result data this page represents.
    * 
    * @param type One of the TYPE_XXX values.
    * 
    * @return <code>true</code> if type matches the type of this object,
    *    <code>false</code> otherwise. 
    */
   public boolean isType( int type )
   {
      return m_type == type;  
   }
   
   /**
    * Returns a list of dataset names supplied in the ctor. These are the
    * names of the resources used to get the data to generate the editor
    * associate with this object.
    *
    * @return An iterator over the dataset names, never <code>null</code>,
    *    may be empty.
    */
   public Iterator getDatasetList()
   {
      return m_handlers.iterator();
   }

   /**
    * Returns the page id list set in the constructor. This list contains all
    * of the child page ids referenced by the editor associated with this
    * object in document order (from top to bottom).
    *
    * @returns An iterator over the ids. Never <code>null</code>, may be empty.
    */
   public Iterator getPageIdList()
   {
      return m_pageMap.iterator();
   }

   /**
    * One of the ..._TYPE constants. Always between TYPE_FIRST and TYPE_LAST
    * inclusive, after construction. Immutable after construction.
    */
   private int m_type;

   /**
    * The child id of the mapper for this editor. Set in ctor, then immutable.
    */
   private int m_childId;

   /**
    * The builder that will create the output document for a request. <code>
    * null</code> until initialized by the <code>setBuilder</code> method,
    * then never <code>null</code> after that.
    */
   private PSEditorDocumentBuilder m_builder;

   /**
    * A list of dataset names used to gather data for the request.
    * Never <code>null</code>, may be empty.
    */
   private List m_handlers = new ArrayList();

   /**
    * A list of Map.Entry objects whose key is a page id (as an Integer) and
    * whose value is a PSPageInfo object. Never <code>null</code>, may be
    * emtpy.
    */
   private List m_pageMap = new ArrayList();
}