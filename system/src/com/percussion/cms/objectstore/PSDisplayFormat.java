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
package com.percussion.cms.objectstore;

import com.percussion.cms.PSCmsException;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.data.IPSCloneTuner;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents a single display format object defined by the system.
 * A display format lets describes how to render a search/view output.
 *
 * @see PSVersionableDbComponent
 */
public class PSDisplayFormat extends PSVersionableDbComponent implements
   IPSCatalogSummary, IPSCloneTuner
{
   /**
    * Creates a new format with default values for the label, internal name,
    * display type and community. The label defaults to something of the form
    * 'Display Format n', where n is a small integer, that increments with
    * every generated object. The type is set to TYPE_VIEWSANDSEARCHES and
    * it is visible to all communities.
    */
   public PSDisplayFormat()
      throws PSCmsException
   {
      // setup primary key
      super(PSDisplayFormat.createKey(new String [] {}));

      try
      {
         // may be empty
         m_properties = new PSDFProperties();
         // adds a dummy column for consistency
         m_columns = new PSDFColumns();
         addSystemTitle(m_columns);
      }
      catch (ClassNotFoundException neverHappen)
      {}

      // required elements for validity
      String label = "Display Format " +  ms_nameCounter++;
      setDisplayName(label);
      String name = label.replace(' ', '_');
      name = name.toLowerCase();
      setInternalName(name);

      addCommunity(null);
   }

   // implements IPSCatalogSummary#getGUID()
   public IPSGuid getGUID()
   {
      return new PSGuid(PSTypeEnum.DISPLAY_FORMAT, getDisplayId());
   }
   
   // implements IPSCatalogSummary#getName()
   public String getName()
   {
      return getInternalName();
   }
   
   // implements IPSCatalogSummary#getLabel()
   public String getLabel()
   {
      return getDisplayName();
   }
   
   /**
    * Required, if contained within collection
    */
   public PSDisplayFormat(Element src)
      throws PSUnknownNodeTypeException, PSCmsException
   {
      this();
      fromXml(src);
   }

   // see base class for description
   public static PSKey createKey(String[] values)
   {
      if (values == null || values.length == 0)
         return new PSKey(new String[] {KEY_COL_ID});

      return new PSKey(new String[] {KEY_COL_ID}, values, true);
   }

   /**
    * Convience method to check whether this display format is valid for
    * 'Related Content' Search results. A display format valid for related
    * content search views is not valid for any other views because
    * 'sys_variantid' does not apply for any other views.
    * 
    * @return <code>true</code> if this display format contains
    *         ({@link #COL_CONTENTTYPEID}or {@link #COL_CONTENTTYPENAME}) and
    *         ({@link #COL_VARIANTID}or {@link #COL_VARIANTNAME}), otherwise
    *         <code>false</code>
    */
   @SuppressWarnings("unchecked")
   public boolean isValidForRelatedContent()
   {
      boolean ctTypeExists = false;
      boolean variantExists = false;

      Iterator columns = getColumns();
      while (columns.hasNext() && (!ctTypeExists || !variantExists))
      {
         PSDisplayColumn column = (PSDisplayColumn) columns.next();
         if (!ctTypeExists
               && (column.getSource().equals(COL_CONTENTTYPEID) || column
                     .getSource().equals(COL_CONTENTTYPENAME)))
            ctTypeExists = true;
         else if (!variantExists
               && (column.getSource().equals(COL_VARIANTID) || column
                     .getSource().equals(COL_VARIANTNAME)))
            variantExists = true;
      }

      return ctTypeExists && variantExists;
   }
   
   /**
    * Get the name of the column by which to sort.
    * 
    * @return the column name by which to sort, may be <code>null</code>, never
    *    empty.
    */
   public String getSortedColumnName()
   {
      return getPropertyValue(PROP_SORT_COLUMN);         
   }
   
   /**
    * Is the supplied column sorted?
    * 
    * @param columnName the column name for which to test whether it is sorted 
    *    or not.
    * @return <code>true</code> if the supplied column is sorted, 
    *    <code>false</code> otherwise.
    */
   public boolean isColumnSorted(String columnName)
   {
      boolean isSorted = false;
      String sortedColumnName = getSortedColumnName();
      if (sortedColumnName != null)
         isSorted = sortedColumnName.equals(columnName);
         
      return isSorted;
   }
   
   /**
    * Is the sorted column for this display format sorted ascending?
    * 
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isAscendingSort()
   {
      return doesPropertyHaveValue(PROP_SORT_DIRECTION, SORT_ASCENDING);
   }
   
   /**
    * Is the sorted column for this display format sorted descending?
    * 
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isDescendingSort()
   {
      return doesPropertyHaveValue(PROP_SORT_DIRECTION, SORT_DESCENDING);
   }

   /**
    * Convenience method that calls {@link #setProperty(String strName,
    * String strValue,boolean bMulti)} with default values.
    *
    * @param strName the name of the property case-insensitive
    *    match is performed. Never <code>null</code> or empty.
    *
    * @param strValue the value to set, may be <code>null<code>
    *    to specify the empty string.
    *
    */
   public void setProperty(String strName, String strValue)
   {
      setProperty(strName, strValue, false);
   }

   /**
    * Convience method to set a property's value. If the property
    * with name <code>strName</code> does not exist, one will be added.
    *
    * @param strName the name of the property case-insensitive
    *    match is performed. Never <code>null</code> or empty.
    *
    * @param strValue the value to set, may be <code>null<code>
    *    to specify the empty string.
    *
    * @param bMulti if <code>true</code> then if the property exists this
    *    value will be added
    */
   @SuppressWarnings("unchecked")
   public void setProperty(String strName, String strValue, boolean bMulti)
   {
      if (strName == null || strName.trim().length() == 0)
         throw new IllegalArgumentException(
            "strName must not be null or empty");

      // Validate name length
      if (strName.length() > PSDFProperty.NAME_LENGTH)
         throw new IllegalArgumentException(
            "property name must not exceed " + PSDFProperty.NAME_LENGTH +
            " characters");

      Iterator iter = m_properties.iterator();
      boolean bFound = false;

      while (iter.hasNext())
      {
         PSDFMultiProperty prop = (PSDFMultiProperty) iter.next();

         if (prop.getName().equalsIgnoreCase(strName))
         {
            // Threshold - if prop already contains this value
            // do not update
            if (prop.contains(strValue))
               return;

            if (bMulti)
            {
               prop.add(strValue);
            }
            else
            {
               // Remove the values of the old property
               // add the new one and clone it to bring over
               // any other attributes (e.g. description ...)
               Iterator values = prop.iterator(); // cms property(s)
               while (values.hasNext())
               {
                  // remove each entry and add
                  prop.remove((String) values.next());
               }

               // Single value
               prop.add(strValue);

               // Remove the old one
               m_properties.remove(prop);

               // Add the new one retaining a single valued property
               // @todo Noticed multiproperty clone is not
               // impl
               PSDFMultiProperty mp = (PSDFMultiProperty) prop.clone();
               m_properties.add(mp);
            }

            bFound = true;
            break;
         }
      }

      if (bFound)
         return;

      // Add the property
      PSDFMultiProperty mp = new PSDFMultiProperty(strName);
      mp.add(strValue);
      m_properties.add(mp);
      
      resetAllowedCommunities();
   }

   /**
    * Whether or not this display format has a certain property
    *
    * @return <code>true</code> if the property exists, otherwise
    *    <code>false</code>.
    */
   @SuppressWarnings("unchecked")
   public boolean hasProperty(String strName)
   {
      if (strName == null || strName.trim().length() == 0)
         throw new IllegalArgumentException(
            "strName must not be null or empty");

      Iterator iter = m_properties.iterator();

      while (iter.hasNext())
      {
         PSDFMultiProperty prop = (PSDFMultiProperty) iter.next();

         if (prop.getName().equalsIgnoreCase(strName))
            return true;
      }

      return false;
   }

   /**
    * Convenience method that calls {@link #removeProperty(String,String,
    * boolean) removeProperty(strName, null, false)}.
    */
   public void removeProperty(String strName)
   {
      removeProperty(strName, null, false);
   }

   /**
    * Convenience method to remove a property.
    *
    * @param strName the name of the property case-insensitive
    *    match is performed. Never <code>null</code> or empty.
    *
    * @param strValue the value to set, may be <code>null<code> if not
    *    applicable.
    *
    * @param bMulti if <code>true</code> this represents a multivalued property.
    */
   @SuppressWarnings("unchecked")
   public void removeProperty(String strName, String strValue, boolean bMulti)
   {
      if (strName == null || strName.trim().length() == 0)
         throw new IllegalArgumentException(
            "strName must not be null or empty");

      // Threshold
      if (m_properties.size() < 1)
         return;

      Iterator iter = m_properties.iterator();
      boolean bFound = false;
      while (!bFound && iter.hasNext())
      {
         PSDFMultiProperty prop = (PSDFMultiProperty) iter.next();
         if (prop.getName().equalsIgnoreCase(strName))
         {
            if (bMulti)
            {
               if (strValue == null)
                  throw new IllegalArgumentException(
                     "attempting to remove a multi value property." +
                     "Value to remove must not be null");
               // Removes only this occurance of strValue
               prop.remove(strValue);
            }
            else
            {
               // removes the whole property and any contained child
               // components
               m_properties.remove(prop);
               
               resetAllowedCommunities();
            }

            bFound = true;
         }
      }
   }

   /**
    * Convience method to check whether this display format is valid for views
    * and searches to use. A display format is valid for views and searches if
    * the display format is not valid for related content. See the {@link
    * #isValidForRelatedContent()} for more details.
    *
    * @return <code>true</code> if it is valid, otherwise<code>false</code>
    */
   public boolean isValidForViewsAndSearches()
   {
      return !isValidForRelatedContent();
   }

   /**
    * Convience method to check whether this display format is valid for folders
    * to use. A display format is valid for folders if the display format is not
    * valid for related content and does not contain any categorized columns.
    * See the {@link #isValidForRelatedContent()} for more details.
    *
    * @return <code>true</code> if it is valid, otherwise<code>false</code>
    */
   @SuppressWarnings("unchecked")
   public boolean isValidForFolder()
   {
      boolean valid = !isValidForRelatedContent();

      Iterator columns = getColumns();
      while (columns.hasNext() && valid )
      {
         PSDisplayColumn column = (PSDisplayColumn) columns.next();
         if (column.isCategorized())
            valid = false;
         
         for (String name : ms_invalidFolderFields)
         {
            if (name.equals(column.getSource()))
            {
               valid = false;
               break;
            }
         }
      }

      return valid;
   }
   
   /**
    * Get a string with a list of invalid column names for display formats 
    * used for folders.
    * 
    * @return the list of invalid column names for display formats used with 
    *    folders, never <code>null</code> or empty.
    */
   public String getInvalidFolderFieldNames()
   {
      StringBuffer names = new StringBuffer();
      for (int i= 0; i<ms_invalidFolderFields.length; i++)
      {
         names.append(ms_invalidFolderFields[i]);
         if (i<ms_invalidFolderFields.length-1)
            names.append(", ");
      }
      
      return names.toString();
   }
   
   /**
    * Removes all columns which are not valid to be used with folders.
    */
   @SuppressWarnings("unchecked")
   public void removeInvalidFolderColums()
   {
      List<IPSDbComponent> deletes = new ArrayList<>();
      
      Iterator columns = getColumns();
      while (columns.hasNext())
      {
         PSDisplayColumn column = (PSDisplayColumn) columns.next();
         for (String name : ms_invalidFolderFields)
         {
            if (column.getSource().equals(name))
            {
               deletes.add(column);
            }
         }
      }
      
      for (IPSDbComponent delete : deletes)
         m_columns.remove(delete);
   }

   /**
    * Gets the display id.
    *
    * @return the display id, which may be -1 if it does not have an valid id.
    */
   public int getDisplayId()
   {
      return getKeyPartInt(KEY_COL_ID, -1);
   }

   /**
    * Convience method to determine if a given property has
    * a given value.
    *
    * @param name property name never <code>null</code> or empty.
    *
    * @param value value never <code>null</code> or empty.
    *
    * @return <code>true</code> if a property with <code>name</code>
    *    has value <code>value</code>, <code>false</code> otherwise.
    */
   @SuppressWarnings("unchecked")
   public boolean doesPropertyHaveValue(String name, String value)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException(
            "name must not be null or empty");

      if (value == null || value.trim().length() == 0)
         throw new IllegalArgumentException(
            "value must not be null or empty");

      // Threshold
      if (m_properties.size() < 1)
         return false;

      Iterator iter = m_properties.iterator();

      while (iter.hasNext())
      {
         PSDFMultiProperty prop = (PSDFMultiProperty) iter.next();

         if (prop.getName().equalsIgnoreCase(name)
            && prop.contains(value))
            return true;
      }

      return false;
   }
   
   /**
    * Gets the first value of the specified property.  If the property may have
    * multiple values, use {@link #getProperties()} to determine the values
    * directly.
    * 
    * @param name the name of the property, may not be <code>null</code> or 
    * empty.  Name comparison is case-insensitive.
    * 
    * @return The first value found for the specified property, or 
    * <code>null</code> if the property has no value or if the property is not
    * found.
    */
   @SuppressWarnings("unchecked")
   public String getPropertyValue(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");
      
      String val = null; 
      
      Iterator iter = m_properties.iterator();
      while (iter.hasNext() && val == null)
      {
         PSDFMultiProperty prop = (PSDFMultiProperty) iter.next();
         if (prop.getName().equalsIgnoreCase(name))
         {
            if (prop.iterator().hasNext())
               val = (String)prop.iterator().next();
         }
      }
      
      return val;
   }

   /**
    * Checks whether this display format is allowed for supplied community or
    * not.
    *
    * @param communityId the community id to check, may not be <code>null</code>
    * or empty.
    *
    * @return <code>true</code> if this display format is available for all
    * communities or it is available for the supplied community id.
    */
   public boolean isAllowedForCommunity(String communityId)
   {
      if(communityId == null || communityId.trim().length() == 0)
         throw new IllegalArgumentException(
            "communityId may not be null or empty.");

      boolean valid = false;
      if(doesPropertyHaveValue(PROP_COMMUNITY, communityId) ||
         doesPropertyHaveValue(PROP_COMMUNITY, PROP_COMMUNITY_ALL) )
      {
         valid = true;
      }

      return valid;
   }

   /**
    * Accessor to set the column list
    *
    * @param cols the column list, never <code>null</code>.
    */
   public void setColumnList(PSDFColumns cols)
   {
      if (cols == null)
         throw new IllegalArgumentException(
            "cols must not be null");

      m_columns = cols;
      addSystemTitle(m_columns);
   }

   /**
    * Accessor to set the property list
    *
    * @param props the property list, never <code>null</code>.
    */
   public void setPropertiesList(PSDFProperties props)
   {
      if (props == null)
         throw new IllegalArgumentException(
            "props must not be null");

      m_properties = props;
      
      resetAllowedCommunities();
   }

   /**
    * Get the display name of this display format object.
    *
    * @return display name of object, never <code>null</code> or
    *    empty.
    */
   public String getDisplayName()
   {
      return m_strDisplayName;
   }

   /**
    * Calls {@link #setInternalName(String)}
    * 
    * @see #setInternalName(String)
    */
   public void setName(String str)
   {
      setInternalName(str);
   }

   /**
    * Set the display name of the objects.
    *
    * @param str never <code>null</code> or empty.
    */
   public void setDisplayName(String str)
   {
      if (str == null || str.trim().length() == 0)
         throw new IllegalArgumentException(
            "display name must be null or empty");

      if (str.equalsIgnoreCase(m_strDisplayName))
         return;

      // Validate length
      if (str.length() > DISPLAYNAME_LENGTH)
         throw new IllegalArgumentException(
            "display name must not exceed " + DISPLAYNAME_LENGTH +
            " characters");

      setDirty();
      m_strDisplayName = str;
   }

   /**
    * Gets the description of the display format.
    *
    * @return string may be empty.
    */
   @Override
   public String getDescription()
   {
      return m_strDescription;
   }

   /**
    * Sets the description attribute of this object.
    *
    * @param description string. Never <code>null</code>
    */
   public void setDescription(String description)
   {
      if (description == null)
         throw new IllegalArgumentException(
            "description must not be null");

      if (description.equalsIgnoreCase(m_strDescription))
         return;

      // Validate length
      if (description.length() > DESCRIPTION_LENGTH)
         throw new IllegalArgumentException(
            "description must not exceed " + DESCRIPTION_LENGTH +
            " characters");

      setDirty();
      m_strDescription = description;
   }

   /**
    * Returns an iterator over
    * {@link com.percussion.cms.objectstore.PSDFMultiProperty} objects to allow
    * access to the properties.
    *
    * @return list never <code>null</code> or empty.
    */
   @SuppressWarnings("unchecked")
   public Iterator getProperties()
   {
      return m_properties.iterator();
   }

   /**
    * Returns the component collection columns for direct manipulation.
    * Useful, as opposed to just the iterator, when adding or removing is
    * necessary.
    *
    * @return component collection never <code>null</code> or empty.
    */
   public PSDFColumns getColumnContainer()
   {
      return m_columns;
   }

   /**
    * Returns the component collection of properties for direct manipulation.
    * Useful, as opposed to just the iterator, when adding or removing is
    * necessary.
    *
    * @return component collection never <code>null</code> may be empty.
    */
   public PSDFProperties getPropertyContainer()
   {
      return m_properties;
   }

   /**
    * Returns an iterator over
    * {@link com.percussion.cms.objectstore.PSDisplayColumn} objects to allow
    * access to the properties.
    *
    * @return list never <code>null</code> or empty.
    */
   @SuppressWarnings("unchecked")
   public Iterator getColumns()
   {
      return m_columns.iterator();
   }
   
   /**
    * Determine the index of the specified column name.
    * 
    * @param columnName The name of the column, may not be <code>null</code> or 
    * empty.
    * 
    * @return The index, or <code>-1</code> if the specified column is not
    * found.
    */
   public int getColumnIndex(String columnName)
   {
      if (columnName == null || columnName.trim().length() == 0)
         throw new IllegalArgumentException(
            "columnName may not be null or empty");
      
      int index = -1;      
      for (int i = 0; i < m_columns.size(); i++)
      {
         if (((PSDisplayColumn)m_columns.get(i)).getSource().equals(columnName))
         {
            index = i;
            break;
         }          
      }
            
      return index;
   }

   //see base class
   @Override
   public void setPersisted()
      throws PSCmsException
   {
      m_columns.setPersisted();
      m_properties.setPersisted();
      super.setPersisted();
   }

   // see base class for description. We further need to append to the
   // root our collection list(s).
   @Override
   public void toDbXml(Document doc, Element root, IPSKeyGenerator keyGen,
      PSKey parent)
      throws PSCmsException
   {
      int state = getState();
      if (state != DBSTATE_MARKEDFORDELETE)
         super.toDbXml(doc, root, keyGen, parent);
      PSKey key = getLocator();

      // Must be non-empty if 'this' exists
      if (m_columns.getState() != DBSTATE_UNMODIFIED)
         m_columns.toDbXml(doc, root, keyGen, key);

      if (m_properties.getState() != DBSTATE_UNMODIFIED)
         m_properties.toDbXml(doc, root, keyGen, key);

      if (state == DBSTATE_MARKEDFORDELETE)
         super.toDbXml(doc, root, keyGen, parent);
   }


   // see base class for description - We override this because we
   // also override toDbXml which will call this method. Since we
   // have multiple component lists the base class functionality
   // must be expanded.
   @Override
   protected Element toXml(Document doc, boolean includeChildComps)
   {
      if (doc == null)
         throw new IllegalArgumentException(
            "doc may not be null");

      // Base class shot
      Element root = super.toXml(doc);
      Element elDisName = PSXmlDocumentBuilder.addElement(
         doc, root, XML_NODE_DISPLAYNAME, m_strDisplayName);

      if (elDisName == null)
         throw new IllegalStateException(
            "Unable to create " + XML_NODE_DISPLAYNAME + " element.");

      Element elInternalName = PSXmlDocumentBuilder.addElement(
         doc, root, XML_NODE_INTERNALNAME, m_strInternalName);

      if (elInternalName == null)
         throw new IllegalStateException(
            "Unable to create " + XML_NODE_INTERNALNAME + " element.");

      Element elVersion = PSXmlDocumentBuilder.addElement(doc, root,
            XML_NODE_VERSION, String.valueOf(m_version));

      if (elVersion == null)
         throw new IllegalStateException("Unable to create " + XML_NODE_VERSION
               + " element.");
      
      if (m_strDescription.trim().length() > 0)
      {
         Element elDescName = PSXmlDocumentBuilder.addElement(
            doc, root, XML_NODE_DESCRIPTION, m_strDescription);

         if (elDescName == null)
            throw new IllegalStateException(
               "Unable to create " + XML_NODE_DESCRIPTION + " element.");
      }

      if (includeChildComps)
      {
         /**
          * Write out the component list(s) below
          */
         Element elCols = m_columns.toXml(doc);
         root.appendChild(elCols);

         // May be empty
         if (m_properties.size() > 0)
         {
            Element elProps = m_properties.toXml(doc);
            root.appendChild(elProps);
         }
      }

      return root;
   }

   //see interface for description
   @Override
   public Element toXml(Document doc)
   {
      return toXml(doc, true);
   }

   //see interface for description
   @Override
   public void fromXml(Element e)
      throws PSUnknownNodeTypeException
   {
      // base class crack at it with validation
      super.fromXml(e);

      PSXmlTreeWalker tree = new PSXmlTreeWalker(e);

      Element elDis = tree.getNextElement(XML_NODE_DISPLAYNAME);
      tree.setCurrent(e);

      if (elDis == null)
         throw new IllegalStateException(
            XML_NODE_DISPLAYNAME + " must exist");

      m_strDisplayName = tree.getElementData(elDis);

      Element elInternal = tree.getNextElement(XML_NODE_INTERNALNAME);
      tree.setCurrent(e);

      if (elInternal == null)
         throw new IllegalStateException(
            XML_NODE_INTERNALNAME + " must exist");

      m_strInternalName = tree.getElementData(elInternal);

      Element elVersion = tree.getNextElement(XML_NODE_VERSION);
      tree.setCurrent(e);
      
      if (elVersion == null)
         throw new IllegalStateException(
               XML_NODE_VERSION + " must exist");
      
      try
      {
         setVersion(Integer.parseInt(tree.getElementData(elVersion)));
      }
      catch(NumberFormatException ex)
      {
         Object[] args =
         {
            getNodeName(),
            XML_NODE_VERSION,
            ex.getLocalizedMessage()
         };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      
      // optional
      Element elDesc = tree.getNextElement(XML_NODE_DESCRIPTION);
      tree.setCurrent(e);

      if (elDesc != null)
         m_strDescription = tree.getElementData(elDesc);

      Element aEl = tree.getNextElement(
         PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);

      while (aEl != null)
      {
         String strNodeName = aEl.getNodeName();

         if (strNodeName.equalsIgnoreCase(XML_NODE_COLUMNS))
         {
            m_columns.fromXml(aEl);
         }
         else if (strNodeName.equalsIgnoreCase(XML_NODE_PROPERTIES))
         {
            // check the children - make sure they exist
            if (aEl.getFirstChild() != null)
            {
               // Load the properties
               m_properties.fromXml(aEl);
            }
         }

         aEl = tree.getNextElement(
            PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      }
      addSystemTitle(m_columns);

      // if a community is not set, add default:
      if (!hasProperty(PROP_COMMUNITY))
      {
         addCommunity(null);
      }
   }
   
   /**
    * Tests is the supplied columns contain the <code>sys_title</code>. If not 
    * it will be added as the first column.
    * 
    * @param columns the display format columns to test, assumed not
    *    <code>null</code>, may be empty. The supplied collection will be 
    *    modified if the <code>sys_title</code> column is not found.
    */
   @SuppressWarnings("unchecked")
   private void addSystemTitle(PSDFColumns columns)
   {
      boolean found = false;
      Iterator walker = columns.iterator();
      while (!found && walker.hasNext())
      {
         PSDisplayColumn column = (PSDisplayColumn) walker.next();
         found = column.getSource().equalsIgnoreCase(SYS_TITLE);
      }
      
      if (!found)
      {
         PSDisplayColumn titleColumn = new PSDisplayColumn(SYS_TITLE, 
            SYS_TITEL_LABEL, PSDisplayColumn.GROUPING_FLAT, null, null, true);
         columns.add(0, titleColumn);
      }
   }

   /**
    * See base class for description.
    *
    * @return Always <code>true</code>.
    */
   @Override
   protected boolean requiresActionNode()
   {
      return true;
   }

   /**
    * Add the supplied community to the set of communities which have access
    * to this object.
    *
    * @param communityId To add a particular community, supply the community id
    *    (not name), it will be added to the set of communities already
    *    associated with this object. To allow anyone to access this object,
    *    supply <code>null</code> or empty. Supplying <code>null</code> or
    *    empty will clear all entries currently associated with this object.
    */
   public void addCommunity(String communityId)
   {
      if (communityId == null || communityId.trim().length() == 0)
      {
         communityId = PROP_COMMUNITY_ALL;
      }

      if (doesPropertyHaveValue(PROP_COMMUNITY, communityId))
         return;

      if (communityId.equals(PROP_COMMUNITY_ALL))
         //remove existing communities
         removeProperty(PROP_COMMUNITY, null, false);
      else if (doesPropertyHaveValue(PROP_COMMUNITY, PROP_COMMUNITY_ALL))
      {
         removeProperty(PROP_COMMUNITY, null, false);
      }
      setProperty(PROP_COMMUNITY, communityId, true);
   }

   /**
    * Remove a supplied community.
    *
    * @param strCommunity never <code>null</code> or empty.
    *
    */
   public void removeCommunity(String strCommunity)
   {
      if (strCommunity == null || strCommunity.trim().length() == 0)
         throw new IllegalArgumentException(
               "community must not be null");

      // remove this mutlivalued property
      removeProperty(PROP_COMMUNITY, strCommunity, true);
   }

   /**
    * Get the internal name of this display format object.
    *
    * @return display name of object, never <code>null</code> or
    *    empty.
    */
   public String getInternalName()
   {
      return m_strInternalName;
   }

   /**
    * Set the internal name of the object.
    *
    * @param str never <code>null</code> or empty.
    */
   public void setInternalName(String str)
   {
      if (str == null || str.trim().length() == 0)
         throw new IllegalArgumentException(
            "display name must be null or empty");

      if (str.equalsIgnoreCase(m_strInternalName))
         return;
      if (str.length() > INTERNALNAME_LENGTH)
         throw new IllegalArgumentException(
            "internal name must not exceed " + INTERNALNAME_LENGTH +
            " characeters");

      setDirty();
      m_strInternalName = str;
   }

   // see base class for description
   @Override
   public int hashCode()
   {
      int nHash = super.hashCode();

      return  nHash +
         m_strDisplayName.hashCode() +
         m_strDescription.hashCode() +
         m_columns.hashCode() +
         m_properties.hashCode() +
         m_version.hashCode();
   }

   // see base class for description
   @Override
   public boolean equals(Object obj)
   {
      // Threshold - base class shot
      if (!super.equals(obj))
         return false;

      PSDisplayFormat coll2 = (PSDisplayFormat) obj;

      return m_strInternalName.equals(coll2.m_strInternalName) 
         && m_strDisplayName.equals(coll2.m_strDisplayName)
         && m_strDescription.equals(coll2.m_strDescription)
         && m_columns.equals(coll2.m_columns)
         && m_properties.equals(coll2.m_properties)
         && m_version.equals(coll2.m_version);
   }

   //see base class for description
   @Override
   public Object clone()
   {
      PSDisplayFormat copy = null;

      copy = (PSDisplayFormat) super.clone();

      copy.m_columns = (PSDFColumns) m_columns.clone();
      copy.m_properties = (PSDFProperties) m_properties.clone();
      copy.m_strDescription = m_strDescription;
      copy.m_strDisplayName = m_strDisplayName;
      copy.m_version = m_version;
      return copy;
   }

   /**
    * Overridden to determine if it's child list are modified as well.
    *
    * @see PSDbComponent#getState()
    */
   @Override
   public int getState()
   {
      // Threshold - if base class is mod, new, or marked for delete
      if (super.getState() != IPSDbComponent.DBSTATE_UNMODIFIED)
         return super.getState();

      // if unmodified verify that the contained lists
      // are also unmodified.
      if (m_columns.getState() == IPSDbComponent.DBSTATE_MODIFIED
         || m_columns.getState() == IPSDbComponent.DBSTATE_NEW)
         return IPSDbComponent.DBSTATE_MODIFIED;

      if (m_properties.getState() == IPSDbComponent.DBSTATE_MODIFIED
         || m_properties.getState() == IPSDbComponent.DBSTATE_NEW)
         return IPSDbComponent.DBSTATE_MODIFIED;

      return IPSDbComponent.DBSTATE_UNMODIFIED;
   }

   /**
    * Temporary method to display the display formats in dropdown boxes.
    */
   @Override
   public String toString()
   {
      return m_strDisplayName;
   }

   /**
    * Override to deal with all child components.
    */
   @Override
   public void markForDeletion()
   {
      super.markForDeletion();
      m_columns.markForDeletion();
      m_properties.markForDeletion();
   }
   
   /**
    * Sets a list of allowed communites from the supplied all communities.
    * This is a transient data and will not be saved into the persistent layer.
    * 
    * @param allCommunities a list of all communites, never <code>null</code>,
    *   but may be empty. It maps the GUID of the communities to their names.
    * 
    * @see #getAllowedCommunities()
    */
   public void setAllowedCommunities(Map<IPSGuid, String> allCommunities)
   {
      if (doesPropertyHaveValue(PROP_COMMUNITY, PROP_COMMUNITY_ALL))
      {
         m_allowedCommunities = allCommunities;
      }
      else
      {
         m_allowedCommunities = new HashMap<>();
         for (Map.Entry<IPSGuid, String> comm : allCommunities.entrySet())
         {
            if (doesPropertyHaveValue(PROP_COMMUNITY, 
                  String.valueOf(comm.getKey().longValue())))
            {
               m_allowedCommunities.put(comm.getKey(), comm.getValue());
            }
         }
      }
   }
   
   /**
    * Gets a list of allowed communities, which is a transient data and will not
    * be saved into the persistent layer. Must call 
    * {@link #setAllowedCommunities(Map)} first and must not modify the 
    * properties afterwards.
    * 
    * @return a list of allowed communities, which maps the communities
    *    GUIDs to their names. It may be <code>null</code> if has not been set
    *    by {@link #setAllowedCommunities(Map)}.
    * 
    * @see #setAllowedCommunities(Map)
    */
   public Map<IPSGuid, String> getAllowedCommunities()
   {
      return m_allowedCommunities;
   }
   
   /**
    * Resets the allowed community list. This must be called when modifying
    * the properties.
    */
   private void resetAllowedCommunities()
   {
      m_allowedCommunities = null;
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.data.IPSCloneTuner#tuneClone(java.lang.Object,
    * long)
    */
   @SuppressWarnings("unchecked")
   public Object tuneClone(long newId)
   {
      PSKey newKey = createKey(new String[]
      {
         newId + ""
      });
      setKey(newKey);
      Iterator cols = m_columns.iterator();
      while (cols.hasNext())
      {
         PSDisplayColumn col = (PSDisplayColumn) cols.next();
         col.setKey(newKey);
      }
      Iterator props = m_properties.iterator();
      while (props.hasNext())
      {
         PSDFMultiProperty prop = (PSDFMultiProperty) props.next();
         prop.setKey(newKey);
      }
      return this;
   }

   /**
    * A list of allowed communities. It is a transient object and is not
    * considered as part of the object. It is typically used by the 
    * webservice layer to get name/value pairs of the allowed communities.
    * <p>
    * It is set by {@link #setAllowedCommunities(Map)} and modified by any of
    * the property changes. It is <code>null</code> if has not set yet.
    */
   private Map<IPSGuid, String> m_allowedCommunities = null;
   
   /** property list, initialized in ctor, never <code>null</code>, may be
    * empty.
    */
   private PSDFProperties m_properties;

   /**
    * column list, initialized in ctor, never <code>null</code>, may be
    * empty.
    */
   private PSDFColumns m_columns;

   /**
    * Display name attribute of object. Never <code>null</code> or empty. Set via
    * <code>fromXml</code> and accessible via <code>getDisplayName</code> and
    * <code>setDisplayName</code>.
    */
   private String m_strDisplayName = "";

   /**
    * Description attribute of object. Never <code>null</code> but may be empty.
    * Set via <code>fromXml</code> and accessible via <code>getDisplayName</code>
    * and <code>setDisplayName</code>.
    */
   private String m_strDescription = "";

   /**
    * Internal name attribute of object. Never <code>null</code> but
    * may be empty. Set via <code>fromXml</code> and accessible via
    * <code>getInternalName</code> and <code>setInternalName</code>.
    */
   private String m_strInternalName = "";

   /**
    * This value is appended to the default name when a new instance is
    * created. Use the current value, then increment. Starts at 1.
    */
   private static int ms_nameCounter = 1;

   /**
    * An array of field names which are not valid to use as display format
    * columns for folders.
    */
   private static String[] ms_invalidFolderFields = 
   {
      IPSHtmlParameters.SYS_FOLDERID,
      IPSHtmlParameters.SYS_SITEID
   };

   // private defines
   private static final String KEY_COL_ID = "DISPLAYID";

   // public defines
   public static final String XML_TRUE = "yes";
   public static final String XML_FALSE = "no";

   public static final String PROP_COMMUNITY = "sys_community";
   public static final String PROP_COMMUNITY_ALL = "-1";
   //public static final String PROP_RELATEDCONTENT = "RelatedContent";
   //public static final String PROP_VIEWSANDSEARCHES = "ViewsAndSearches";
   //public static final String PROP_FOLDER = "folders";
   
   /**
    * Defines the column used for initial sorting of results rendered by 
    * a display format.  Value is the column name as a <code>String</code>.
    */
   public static final String PROP_SORT_COLUMN = "sortColumn";

   /**
    * Defines the direction used for initial sorting of results rendered by 
    * a display format.  Values is one of the <code>SORT_XXX</code> values.
    */
   public static final String PROP_SORT_DIRECTION = "sortDirection";

   /**
    * Value for the {@link #PROP_SORT_DIRECTION} property to indicate an 
    * ascending sort.
    */
   public static final String SORT_ASCENDING = "sortAscending";

   /**
    * Value for the {@link #PROP_SORT_DIRECTION} property to indicate a 
    * descending sort.
    */
   public static final String SORT_DESCENDING = "sortDescending";
   
   /**
    * The constant that defines the name of the 'sys_contenttypeid' column.
    */
   public static final String COL_CONTENTTYPEID = "sys_contenttypeid";

   /**
    * The constant that defines the name of the 'sys_contenttypename' column.
    */
   public static final String COL_CONTENTTYPENAME = "sys_contenttypename";

   /**
    * The constant that defines the name of the 'sys_variantid' column.
    */
   public static final String COL_VARIANTID = "sys_variantid";
   
   /**
    * The constant that defines the name of the 'sys_variantname' column.
    */
   public static final String COL_VARIANTNAME = "sys_variantname";
   
   /**
    * The default field name for the title column added if non was specified.
    */
   private static final String SYS_TITLE = "sys_title";
   
   /**
    * The default field label for the title column used if non was specified.
    */
   private static final String SYS_TITEL_LABEL = "System Title:";

   public static final String XML_NODE_INTERNALNAME = "INTERNALNAME";
   public static final String XML_NODE_DISPLAYNAME = "DISPLAYNAME";
   public static final String XML_NODE_DESCRIPTION = "DESCRIPTION";
   public static final String XML_NODE_VERSION = "VERSION";
   public static final String XML_NODE_COLUMNS =
      PSDFColumns.XML_NODE_NAME;
   public static final String XML_NODE_PROPERTIES =
      PSDFProperties.XML_NODE_NAME;
   public static final int INTERNALNAME_LENGTH = 128;
   public static final int DISPLAYNAME_LENGTH = 128;
   public static final int DESCRIPTION_LENGTH = 255;
}
