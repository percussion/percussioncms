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
package com.percussion.cms.objectstore;

import com.percussion.cms.PSCmsException;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.data.IPSCloneTuner;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Represents a single search/view as defined within the system.
 * This class contains all header information for a specific view
 * such as the searches name, display name, parent category, type,
 * display format being applied to the result set, maximum items returned
 * be result set and a description.
 * <p>
 * This object also contains all search fields and properties that
 * apply to this search/view.
 *
 * @see PSVersionableDbComponent for base class description.
 */
public class PSSearch extends PSVersionableDbComponent
   implements IPSCatalogSummary, IPSCloneTuner
{
   /**
    * When a custom view is created, this value is set as the URL. This allows
    * the object to be saved w/o error. A custom view can be created w/ the
    * following {@link #PSSearch(String, boolean) ctor} or by calling
    * {@link #setCustom(boolean)}.
    */
   public static final String URL_PLACEHOLDER = "<enter url>";

   /**
    * Default constructor, sets up the key information. Initializes
    * collection field and property data. Sets a default display name of the
    * form  'SearchN', where N is a small, unique (within the life of this
    * class) number. It is enabled for all communities by default.
    * <p>This ctor can only be used for standard searches. To create a
    * custom search, use the {@link #PSSearch(String,boolean) 2 parameter}
    * ctor.
    * 
    * @throws PSCmsException If there are any errors. 
    */
   public PSSearch() throws PSCmsException
   {
      // setup the primary key
      super(PSSearch.createKey(new String [] {}));

      m_fields = new PSSFields();
      m_properties = new PSSProperties();

      // Default required values for db consistency
      String name = "search" + ms_nameSuffix++;
      setInternalName(name);
      setDisplayName(Character.toUpperCase(name.charAt(0)) + name.substring(1));
      setMaximumNumber(DEFAULT_MAX);
      setDisplayFormatId("1");
      setParentCategory(1);
      setShowTo(SHOW_TO_ALL_COMMUNITIES, null);
      setUserCustomizable(false);
   }

   /**
    * Required if object needs to be contained within
    * {@link com.percussion.cms.objectstore.PSDbComponentCollection}
    */
   @SuppressWarnings("unused")
   public PSSearch(Element src)
      throws PSUnknownNodeTypeException, PSCmsException
   {
      this();
      fromXml(src);
   }

   /**
    * Convenience ctor that calls {@link #PSSearch(String, boolean)
    * PSSearch(name, false)}.
    */
   public PSSearch(String name)
      throws PSCmsException
   {
      this(name, false);
   }

   /**
    * Convenience ctor taking some added parameters.
    *
    * @param name A unique textual identifier for this search. Never <code>
    *    null</code> or empty. Both the label (display name) and internal
    *    name are set to this value. May not contain whitespace and must be
    *    shorter than or equal to INTERNALNAME_LENGTH in length.
    *
    * @param bCustomApp if <code>true</code> search/view will represent a
    *    custom app, otherwise a web services search/view.
    *    
    * @throws PSCmsException If there are any errors.
    */
   public PSSearch(String name, boolean bCustomApp)
      throws PSCmsException
   {
      this();

      setDisplayName(name);
      setInternalName(name);
      setCustom(bCustomApp);
   }

   // implements IPSCatalogSummary#getGUID()
   public IPSGuid getGUID()
   {
      if (isView())
         return new PSGuid(PSTypeEnum.VIEW_DEF, getId());
      
      return new PSGuid(PSTypeEnum.SEARCH_DEF, getId());
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
    * Scans the supplied string for any whitespace characters.
    *
    * @param text May be <code>null</code> or empty.
    *
    * @return <code>true</code> if any character in text is whitespace,
    *    according to Character.isWhitespace(), <code>false</code> otherwise.
    */
   private static boolean containsWhitespace(String text)
   {
      if (text == null)
         return false;

      for (int i=0; i < text.length(); i++)
      {
         if (Character.isWhitespace(text.charAt(i)))
            return true;
      }
      return false;
   }

   /**
    * Gets the search id.
    * 
    * @return the search id, it may be <code>-1</code> if the id has not been
    *    assined. 
    */
   public int getId()
   {
      return getKeyPartInt(KEY_COL_ID, -1);
   }
   
   // see base class for description
   public static PSKey createKey(String[] values)
   {
      if (values == null || values.length == 0)
         return new PSKey(new String [] {KEY_COL_ID});

      return new PSKey(new String[] {KEY_COL_ID}, values, true);
   }

   //see base for description
   @Override
   public void setPersisted()
      throws PSCmsException
   {
      m_fields.setPersisted();
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
      if (m_fields.getState() != DBSTATE_UNMODIFIED)
         m_fields.toDbXml(doc, root, keyGen, key);
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

      Element root = super.toXml(doc);

      PSXmlDocumentBuilder.addElement(doc, root, XML_NODE_DISPLAYNAME,
            m_strDisplayName);

      PSXmlDocumentBuilder.addElement(doc, root, XML_NODE_INTERNALNAME,
            m_strInternalName);

      PSXmlDocumentBuilder.addElement(doc, root, XML_NODE_PARENTCATEGORY,
         Integer.toString(m_nParentCat));

      PSXmlDocumentBuilder.addElement(doc, root, XML_NODE_DF, m_strDisplayId);

      if (m_strType.trim().length() > 0)
      {
         PSXmlDocumentBuilder.addElement(doc, root, XML_NODE_TYPE, m_strType);
      }

      PSXmlDocumentBuilder.addElement(doc, root, XML_NODE_MAX,
            Integer.toString(m_nMaxResults));

      PSXmlDocumentBuilder.addElement(doc, root, XML_NODE_VERSION,
            String.valueOf(m_version));
      
      String caseSensitive = (m_isCaseSensitive ? "1" : "0");
      PSXmlDocumentBuilder.addElement(doc, root,
         XML_NODE_CASE_SENSITIVE, caseSensitive);

      if (m_strDescription.trim().length() > 0)
      {
         PSXmlDocumentBuilder.addElement(doc, root, XML_NODE_DESCRIPTION,
               m_strDescription);
      }

      if (m_isCustom)
      {
         PSXmlDocumentBuilder.addElement(doc, root, XML_NODE_RESOURCE, m_url);
      }

      if (includeChildComps)
      {
         /**
          * Write out the component list(s) below
          */
         Element elCols = m_fields.toXml(doc);
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

   /**
    * Test whether the property name supplied is a
    * editable property.
    *
    * @return <code>true</code> if this property is editable,
    *    otherwise <code>false</code>.
    */
   public boolean isEditableProperty(String strName)
   {
      return !m_nonEditableProps.contains(strName);
   }

   /**
    * Checks if this is a context parameter.
    *
    * @param strName never <code>null</code>
    *
    * @return <code>true</code> if a context parameter,
    *    otherwise <code>false</code>.
    */
   public boolean isAContextParam(String strName)
   {
      if (strName == null)
         throw new IllegalArgumentException(
            "strName must not be null. Checking for a context parameter");

      for (int i=0; i<CONTEXT_PARAMS_LIST.length; i++)
      {
         if (strName.equalsIgnoreCase(CONTEXT_PARAMS_LIST[i]))
            return true;
      }

      return false;
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
    * Get this search objects fields.
    *
    * @return An iterator containing one or more 
    * {@link com.percussion.cms.objectstore.PSSearchField} objects. 
    * Never <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   public Iterator getFields()
   {
      return m_fields.iterator();
   }

   /**
    * Returns the component collection fields for direct manipulation.
    * Useful, as opposed to just the iterator, when adding or removing is
    * necessary.
    *
    * @return component collection never <code>null</code> or empty.
    */
   public PSSFields getFieldContainer()
   {
      return m_fields;
   }

   /**
    * Returns the component collection of properties for direct manipulation.
    * Useful, as opposed to just the iterator, when adding or removing is
    * necessary.
    *
    * @return component collection never <code>null</code> may be empty.
    */
   public PSSProperties getPropertyContainer()
   {
      return m_properties;
   }


   /**
    * Sets the fields passed using the iterator.
    *
    * @param fields iterator over
    * {@link com.percussion.cms.objectstore.PSSearchField} objects.
    * Never <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   public void setFields(Iterator fields)
   {
      if (fields == null)
         throw new IllegalArgumentException(
            "fields must not be null");

      m_fields.setFields(fields);
   }

   /**
    * Add a single search field to an existing search. This should be used
    * with caution in order to avoid modifying a search in the catalog.
    * @param field new search field, must never be <code>null</code>
    */
   public void addField(PSSearchField field)
   {
      if (field == null)
      {
         throw new IllegalArgumentException("field must never be null");
      }
      m_fields.add(field);
   }
   
   /**
    * Removes the fields.
    *
    * @param fields iterator over
    * {@link com.percussion.cms.objectstore.PSSearchField} objects.
    * If <code>null</code> fields will not be removed.
    */
   @SuppressWarnings("unchecked")
   public void removeFields(Iterator fields)
   {
      if (fields == null || !fields.hasNext())
         return;
      m_fields.removeFields(fields);
   }
   
   /**
    * Set this search as custom. Only works if the url is <code>null</code> or
    * empty, therefore, once a search is made custom, it cannot be changed back
    * to standard.
    * 
    * @param isCustom If <code>true</code>, the url is set to the default
    * value {@link #URL_PLACEHOLDER}.
    */
   public void setCustom(boolean isCustom)
   {
      if(StringUtils.isBlank(getUrl()))
      {
         m_isCustom = isCustom;
         if(m_isCustom)
            m_url = URL_PLACEHOLDER;
      }
   }

   /**
    * Get this search objects properties.
    *
    * @return a iterator containing zero or more
    *    objects. Never <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   public Iterator getProperties()
   {
      return m_properties.iterator();
   }

   //see interface for description
   @Override
   public Element toXml(Document doc)
   {
      return toXml(doc, true);
   }

   //see interface for description
   @Override
   public void fromXml(Element e) throws PSUnknownNodeTypeException
   {
      // base class handling
      super.fromXml(e);

      PSXmlTreeWalker tree = new PSXmlTreeWalker(e);
      Element elDis = tree.getNextElement(XML_NODE_DISPLAYNAME);
      tree.setCurrent(e);

      if (elDis == null)
      {
         Object[] args =
         {
            getNodeName(),
            XML_NODE_DISPLAYNAME,
            "missing"
         };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      m_strDisplayName = PSXmlTreeWalker.getElementData(elDis);

      Element elInternal = tree.getNextElement(XML_NODE_INTERNALNAME);
      tree.setCurrent(e);

      if (elInternal == null)
      {
         Object[] args =
         {
            getNodeName(),
            XML_NODE_INTERNALNAME,
            "missing"
         };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      m_strInternalName = PSXmlTreeWalker.getElementData(elInternal);

      Element elParentCat = tree.getNextElement(XML_NODE_PARENTCATEGORY);
      tree.setCurrent(e);

      if (elParentCat == null)
      {
         Object[] args =
         {
            getNodeName(),
            XML_NODE_PARENTCATEGORY,
            "missing"
         };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      try
      {
         String strVal = PSXmlTreeWalker.getElementData(elParentCat);
         setParentCategory(Integer.parseInt(strVal), false);
      }
      catch (NumberFormatException ex)
      {
         Object[] args =
         {
            getNodeName(),
            XML_NODE_PARENTCATEGORY,
            ex.getLocalizedMessage()
         };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      Element elDisplayFormat = tree.getNextElement(XML_NODE_DF);
      tree.setCurrent(e);

      if (elDisplayFormat == null)
      {
         Object[] args =
         {
            getNodeName(),
            XML_NODE_DF,
            "missing"
         };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      m_strDisplayId = PSXmlTreeWalker.getElementData(elDisplayFormat);

      // May be nullable attributes ...
      Element elType = tree.getNextElement(XML_NODE_TYPE);
      tree.setCurrent(e);

      if (elType != null)
         m_strType = PSXmlTreeWalker.getElementData(elType);

      Element elMax = tree.getNextElement(XML_NODE_MAX);
      tree.setCurrent(e);

      m_nMaxResults = DEFAULT_MAX;
      try
      {
         if (elMax != null)
         {
            String strVal = PSXmlTreeWalker.getElementData(elMax);
            m_nMaxResults = Integer.parseInt(strVal);
         }
      }
      catch (Exception ex)
      {
         // ignore, use default
      }
      //enforce our contract
      if (m_nMaxResults < 0)
         m_nMaxResults = UNLIMITED_MAX;

      Element elCase = tree.getNextElement(XML_NODE_CASE_SENSITIVE);
      tree.setCurrent(e);

      m_isCaseSensitive = false;
      if (elCase != null)
      {
         String caseSensitive = PSXmlTreeWalker.getElementData(elCase);
         if ((caseSensitive != null) && (caseSensitive.trim().equals("1")))
            m_isCaseSensitive = true;
      }

      Element elDesc = tree.getNextElement(XML_NODE_DESCRIPTION);
      tree.setCurrent(e);

      if (elDesc != null)
         m_strDescription = PSXmlTreeWalker.getElementData(elDesc);

      Element elRes = tree.getNextElement(XML_NODE_RESOURCE);
      tree.setCurrent(e);

      if (elRes != null
         && PSXmlTreeWalker.getElementData(elRes).trim().length() > 0)
      {
         m_isCustom = true;
         m_url = PSXmlTreeWalker.getElementData(elRes);
      }
      else
      {
         m_isCustom = false;
         m_url = null;
      }

      Element elVersion = tree.getNextElement(XML_NODE_VERSION);
      tree.setCurrent(e);
      
      if (elVersion == null)
      {
         Object[] args =
         {
            getNodeName(),
            XML_NODE_VERSION,
            "missing"
         };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      
      try
      {
         String strVal = PSXmlTreeWalker.getElementData(elVersion);
         setVersion(Integer.parseInt(strVal));
      }
      catch (NumberFormatException ex)
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
      
      // Load the columns (optional)
      Element elCols = tree.getNextElement(PSSFields.XML_NODE_NAME);
      tree.setCurrent(e);
      if (elCols != null && elCols.getFirstChild() != null)
      {
         m_fields = new PSSFields(elCols);
      }
      else
      {
         try
         {
            m_fields = new PSSFields();
         }
         catch (PSCmsException ex)
         {
            Object[] args =
            {
               getNodeName(),
               PSSFields.XML_NODE_NAME,
               ex.getLocalizedMessage()
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
      }

      // Load the properties (optional)
      Element elProps = tree.getNextElement(PSSProperties.XML_NODE_NAME);
      tree.setCurrent(e);
      if (elProps != null && elProps.getFirstChild() != null)
         m_properties = new PSSProperties(elProps);
      else
         m_properties = new PSSProperties();

      // set default user customizable property
      if (!isUserCustomizable())
         setUserCustomizable(false);
   }

   /**
    * Gets the maximum number of entries this search returns.
    *
    * @return integer always > 0
    */
   public int getMaximumResultSize()
   {
      return m_nMaxResults;
   }

   /**
    * Get the internal name of this Search object.
    *
    * @return internal name of object, never <code>null</code> or
    *    empty.
    */
   public String getInternalName()
   {
      return m_strInternalName;
   }

   /**
    * Set the internal name of the object.
    *
    * @param name cannot be <code>null</code> or empty and must be smaller than
    *    <code>INTERNALNAME_LENGTH</code>. Also no whitespace is allowed in the
    *    supplied internal name.
    */
   public void setInternalName(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException(
            "display name must be null or empty");

      if (containsWhitespace(name))
         throw new IllegalArgumentException("Name cannot contain whitespace.");

      if (name.equals(m_strInternalName))
         return;

      // Validate length
      if (name.length() > INTERNALNAME_LENGTH)
         throw new IllegalArgumentException(
            "Internal name must not exceed " + INTERNALNAME_LENGTH +
            "characters");

      setDirty();
      m_strInternalName = name;
   }

   /**
    * Set the maximum number of entries to be returned.
    *
    * @param nMax int > 0, if < 0 the maximum will default
    *    to <code>DEFAULT_MAX</code>
    */
   public void setMaximumNumber(int nMax)
   {
      // Threshold if less than zero we default
      // to unlimited results.
      if (nMax < 0)
         nMax = UNLIMITED_MAX;

      // Threshold - no modification necessary
      if (m_nMaxResults == nMax)
         return;

      setDirty();
      m_nMaxResults = nMax;
   }

   /**
    * Indicates whether this object is representing a search or a view. A
    * 'search' contains the meta data that defines what needs to be located,
    * while a 'view' is the meta data and the values associated with the
    * search fields.
    * <p>Defaults to TYPE_USERSEARCH.
    *
    * @return One of the TYPE_xxx values.
    */
   public String getType()
   {
      return m_strType;
   }

   /**
    * See {@link #getType()} for details.
    *
    * @param type Must be one of the TYPE_xxx values. This can be checked
    * ahead of time by calling the <code>isValidType(String)<code> method.
    */
   public void setType(String type)
   {
      if (!isValidType(type))
         throw new IllegalArgumentException("Invalid type supplied.");

      type = type.trim();
      // Threshold
      if (type.equalsIgnoreCase(m_strType))
         return;

      // Validate length
      if (type.length() > TYPE_LENGTH)
      {
         throw new IllegalArgumentException("A new type was defined that"
               + " exceeds the maximum length allowed by the db column.");
      }

      setDirty();
      m_strType = type;
   }


   /**
    * Compares the supplied type (case insensitive) against all allowed types.
    *
    * @param type May be <code>null</code> or empty. Leading/trailing white
    *    space is trimmed before comparison.
    *
    * @return If one of the TYPE_xxx values, <code>true</code> is returned,
    *    otherwise, false is returned.
    */
   private boolean isValidType(String type)
   {
      if (type == null || type.trim().length() == 0)
         return false;

      type = type.trim();
      for (int i=0; i < TYPES_ENUM.length; i++)
      {
         if (type.equalsIgnoreCase(TYPES_ENUM[i]))
            return true;
      }
      return false;
   }


   /**
    * Get the parent category of this search.
    *
    * @return parent category, defaults to {@link ParentCategory#MY_CONTENT}.
    */
   public int getParentCategory()
   {
      return m_nParentCat;
   }

   /**
    * Set the parent category attribute.
    *
    * @param nParentCat must be one of the numeric values defined in 
    *    {@link ParentCategory}; otherwise defaults to 
    *    {@link ParentCategory#ALL_CONTENT}.
    */
   public void setParentCategory(int nParentCat)
   {
      setParentCategory(nParentCat, true);
   }

   /**
    * Set the parent category attribute.
    *
    * @param nParentCat must be one of the numeric values defined in 
    *    {@link ParentCategory}; otherwise defaults to 
    *    {@link ParentCategory#MY_CONTENT}.
    * @param isSetDirty <code>true</code> if call {@link #setDirty()}.
    */
   private void setParentCategory(int nParentCat, boolean isSetDirty)
   {
      // Threshold
      if (nParentCat == m_nParentCat)
         return;

      if (isSetDirty)
         setDirty();
      boolean isFound = false;
      for (ParentCategory cat : ParentCategory.values())
      {
         if (cat.getId() == nParentCat)
         {
            isFound = true;
            m_nParentCat = nParentCat;
            break;
         }
      }
      if (!isFound)
         m_nParentCat = ParentCategory.MY_CONTENT.getId();
   }


   /**
    * Convenience method.
    *
    * @return <code>true</code> if its a search <code>
    *    otherwise <code>false</code>
    */
   public boolean isUserSearch()
   {
      return m_strType.equalsIgnoreCase(TYPE_USERSEARCH);
   }

   /**
    * Convenience method that calls {@link #isAADNewSearch()}.
    */
   public boolean isRCSearch()
   {
      return isAADNewSearch();
   }

   /**
    * Convenience method.
    *
    * @return <code>true</code> if its a standard view <code>
    *    otherwise <code>false</code>
    */
   public boolean isStandardView()
   {
      if(m_strType.equalsIgnoreCase(TYPE_VIEW) && !isCustomApp())
         return true;

      return false;
   }

   /**
    * Convenience method.
    *
    * @return <code>true</code> if its a custom view <code>
    *    otherwise <code>false</code>
    */
   public boolean isCustomView()
   {
      if(m_strType.equalsIgnoreCase(TYPE_VIEW) && isCustomApp())
         return true;

      return false;
   }

   /**
    * Convenience method.
    *
    * @return <code>true</code> if its a view <code>
    *    otherwise <code>false</code>
    */
   public boolean isView()
   {
      return m_strType.equalsIgnoreCase(TYPE_VIEW);
   }

   /**
    * Convenience method.
    *
    * @return <code>true</code> if its a Custom Search <code>
    *    otherwise <code>false</code>
    */
   public boolean isCustomSearch()
   {
      return m_strType.equalsIgnoreCase(TYPE_CUSTOMSEARCH);
   }

   /**
    * Convenience method.
    *
    * @return <code>true</code> if its a Standard Search <code>
    *    otherwise <code>false</code>
    */
   public boolean isStandardSearch()
   {
      return m_strType.equalsIgnoreCase(TYPE_STANDARDSEARCH);
   }

   /**
    * Determines if this search is compatible with the external search engine.
    *
    * @return <code>true</code> if this search is not a custom search or view,
    * and for which {@link #doesPropertyHaveValue(String, String)
    * doesPropertyHaveValue(PROP_SEARCH_ENGINE_TYPE,
    * SEARCH_ENGINE_TYPE_EXTERNAL)} returns <code>true</code>.
    */
   public boolean useExternalSearch()
   {
      return !(isCustomSearch() || isCustomView()) &&
         doesPropertyHaveValue(PROP_SEARCH_ENGINE_TYPE,
         SEARCH_ENGINE_TYPE_EXTERNAL);
   }

   /**
    * Convenience method to determine if a given property has
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
         PSSearchMultiProperty prop = (PSSearchMultiProperty) iter.next();

         if (prop.getName().equalsIgnoreCase(name)
            && prop.contains(value))
            return true;
      }

      return false;
   }

   /**
    * Checks whether this search is allowed for supplied community or not.
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
    * Convience method that calls {@link #setProperty(String strName,
    * String strValue,boolean bMulti)} with default values.
    *
    * @param strName the name of the property case-insensitive
    *    match is performed. Never <code>null</code> or empty.
    *
    * @param strValue the value to set, never  <code>null<code>
    *    or empty.
    *
    */
   public void setProperty(String strName, String strValue)
   {
      setProperty(strName, strValue, false);
   }

   /**
    * Add the supplied user or community only to have access to this search.
    * Making a user as the owner of the search removes previous user or
    * community as owners.
    *
    * @param showTo one of the SHOW_TO_XXXX flags
    * @param userOrCommunityId name of the user or community this search is
    * visibile to. The following scheme is used to interpret this parameter:
    *
    * <ol>
    * <li>Must not be <code>null</code>or empty if showTo parameter is
    * {@link #SHOW_TO_USER}. In this case it is interpreted as user
    * name.</li>
    * <li>If showTo flag is {@link #SHOW_TO_COMMUNITY}, this value is
    * interpreted as communityid. Also in this case, if this parameter is
    * code>null</code> or empty it is assumed to be {@link #PROP_COMMUNITY_ALL}.</li>
    * <li>If showTo flag is {@link #SHOW_TO_ALL_COMMUNITIES}, this values is be
    * ignored and communityId is assumed to be {@link #PROP_COMMUNITY_ALL}.</li>
    * </ol>
    */
   public void setShowTo(int showTo, String userOrCommunityId)
   {
     switch(showTo)
     {
        case SHOW_TO_USER:
           if (userOrCommunityId == null ||
               userOrCommunityId.trim().length() == 0)
           {
              throw new IllegalArgumentException(
                 "userOrCommunityId must not be null or empty");
           }
           // Check if we have the property set to this user already
           if (doesPropertyHaveValue(PROP_USERNAME, userOrCommunityId))
              return;
           //remove existing communities
           removeProperty(PROP_COMMUNITY, null, false);
           setProperty(PROP_USERNAME, userOrCommunityId);
           break;
        case SHOW_TO_COMMUNITY:
        case SHOW_TO_ALL_COMMUNITIES:
           if (userOrCommunityId == null ||
               userOrCommunityId.trim().length() == 0)
           {
              userOrCommunityId = PROP_COMMUNITY_ALL;
           }
           // Check if we have 'all' already
           if (doesPropertyHaveValue(PROP_COMMUNITY, PROP_COMMUNITY_ALL))
              return;

           if (userOrCommunityId.equals(PROP_COMMUNITY_ALL))
              //remove existing communities
              removeProperty(PROP_COMMUNITY, null, false);

           setProperty(PROP_COMMUNITY, userOrCommunityId, true);
           break;
        default:
         throw new IllegalArgumentException(
            "userOrCommunityId is not a valid visibilty flag for the search");
     }
   }

   /**
    * Add the supplied communities only to have access to this search.
    * Making a user as the owner of the search removes previous user or
    * community as owners.
    *
    * @param communities list of communities this search is visible to. Must
    * not be <code>null</code> or empty.
    */
   @SuppressWarnings("unchecked")
   public void setShowTo(Collection communities)
   {
      Iterator iter = communities.iterator();
      while (iter.hasNext())
      {
         String element = (String) iter.next();
         setShowTo(SHOW_TO_COMMUNITY, element);
      }
   }

   /**
    * Add the supplied community to the set of communities which have access
    * to this search.
    *
    * @param communityId To add a particular community, supply the community id
    *    (not name), it will be added to the set of communities already
    *    associated with this search. To allow anyone to access this search,
    *    supply <code>null</code> or empty. Supplying <code>null</code> or
    *    empty will clear all entries currently associated with this search.
    *
    * @deprecated use {@link #setShowTo(int,String)} or {@link #setShowTo(
    * Collection)} instead.
    */
   public void addCommunity(String communityId)
   {
      setShowTo(SHOW_TO_COMMUNITY, communityId);
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
      removeProperty(PROP_AAD_NEWSEARCH, strCommunity, true);
      removeProperty(PROP_CX_NEWSEARCH, strCommunity, true);
   }

   /**
    * Set the search property that indicates if an end user can customize fields
    * in the user interface.
    * @param isUserCustomizable <code>true</code> if end user can customize the
    * search fields, otherwise <code>false</code>
    */
   public void setUserCustomizable(boolean isUserCustomizable)
   {
      String userCustomizable = BOOL_NO;
      if(isUserCustomizable)
      {
         userCustomizable = BOOL_YES;
      }
      // Check if we do not have to change the value
      if (doesPropertyHaveValue(PROP_USER_CUSTOMIZABLE, userCustomizable))
         return;

      setProperty(PROP_USER_CUSTOMIZABLE, userCustomizable);
   }

   /**
    * Can the end user customize the search fields in the user interface?
    * @return  <code>true</code> if end user can customize the search fields,
    * otherwise <code>false</code>.
    */
   public boolean isUserCustomizable()
   {
      return  doesPropertyHaveValue(PROP_USER_CUSTOMIZABLE, BOOL_YES);
   }

   /**
    * Convenience method that calls 
    * {@link #removeProperty(String, String, boolean)
    * removeProperty(PROP_CX_NEWSEARCH, null, true)}. See 
    * {@link #setAsCXNewSearch(int[])} for a description of this property.
    */
   public void clearCXNewSearch()
   {
      removeProperty(PROP_CX_NEWSEARCH, null, true);
   }

   /**
    * Convenience method that calls 
    * {@link #removeProperty(String, String, boolean)
    * removeProperty(PROP_AAD_NEWSEARCH, null, true)}. See 
    * {@link #setAsAADNewSearch(int[])} for a description of this property. 
    */
   public void clearAADNewSearch()
   {
      removeProperty(PROP_AAD_NEWSEARCH, null, true);
   }
   
   /**
    * The 'aad new search' is used to indicate that this search should be 
    * used when a user activates a new search from the "active assembly for 
    * documents" interface. The property is community specific. There can be 1 
    * search with this property for each community and there MUST be 1 search 
    * with this property assigned to all communities. If a search matching 
    * the current community is not found, then the one for all communities
    * is used.
    * <p>Sets this search as the active assembly new search for the supplied
    * communities. The method is intelligent and will only cause this 
    * object to be modified if the supplied ids don't match the current ones.
    * 
    * @param communityIds The numeric keys of the communities to set. Provide
    * {@link #ANY_COMMUNITY_ID} to make this the default search to use when
    * no other search is specified for a particular community. Never 
    * <code>null</code> or empty. Use {@link #clearAADNewSearch()} to remove
    * this setting.
    * <p>ints are used rather than <code>String</code>s as a partial validation.
    * 
    * @see #isAADNewSearch()
    */
   public void setAsAADNewSearch(int[] communityIds)
   {
      setAsNewSearch(PROP_AAD_NEWSEARCH, communityIds,
            new DefaultSearchConfigurator()
            {
               @Override boolean isNewSearch()
               {
                  return isAADNewSearch();
               }
               
               @Override void clearNewSearch()
               {
                  clearAADNewSearch();
               }
            });
   }
   
   /**
    * The 'cx new search' is used to indicate that this search should be 
    * used when a user activates a new search from the "content explorer"
    * interface. The property is community specific. There can be 1 
    * search with this property for each community and there MUST be 1 search 
    * with this property assigned to all communities. If a search matching 
    * the current community is not found, then the one for all communities
    * is used.
    * <p>Sets this search as the content explorer new search for the supplied
    * communities. The method is intelligent and will only cause this 
    * object to be modified if the supplied ids don't match the current ones.
    * 
    * @param communityIds The numeric keys of the communities to set. Provide
    * {@link #ANY_COMMUNITY_ID} to make this the default search to use when
    * no other search is specified for a particular community. Never 
    * <code>null</code> or empty. Use {@link #clearCXNewSearch()} to remove
    * this setting.
    * <p>ints are used rather than <code>String</code>s as a partial validation.
    * 
    * @see #isCXNewSearch()
    */
   public void setAsCXNewSearch(int[] communityIds)
   {
      setAsNewSearch(PROP_CX_NEWSEARCH, communityIds,
            new DefaultSearchConfigurator()
            {
               @Override boolean isNewSearch()
               {
                  return isCXNewSearch();
               }
               
               @Override void clearNewSearch()
               {
                  clearCXNewSearch();
               }
            });
   }
   
   /**
    * A simple helper class that hides the actual method names used for 
    * checking if this is a new search of a particular type (CX or AAD) and
    * clearing that property.
    *
    * @author paulhoward
    */
   private abstract class DefaultSearchConfigurator
   {
      /**
       * Forwards the call to one of the is[type]NewSearch methods in this
       * object.
       * @return The value returned by the underlying call.
       */
      abstract boolean isNewSearch();

      /**
       * Forwards the call to one of the clear[type]NewSearch methods in this
       * object.
       */
      abstract void clearNewSearch();
   }
   
   /**
    * Does the work for the {@link #setAsCXNewSearch(int[])} and 
    * {@link #setAsAADNewSearch(int[])} methods.
    * @param type The name of the search property, assumed either 
    * {@link #PROP_CX_NEWSEARCH} or {@link #PROP_AAD_NEWSEARCH}.
    * @param communityIds See aforementioned methods for description. Never
    * <code>null</code> or empty. (Not assumed so callers don't need to 
    * duplicate this check).  If {@link #ANY_COMMUNITY_ID} is supplied, it is
    * expected to be the first in the list.
    */
   @SuppressWarnings("unchecked")
   private void setAsNewSearch(String type, int[] communityIds,
         DefaultSearchConfigurator dsc)
   {
      if ( null == communityIds || communityIds.length == 0)
      {
         throw new 
               IllegalArgumentException("communityIds cannot be null or empty");  
      }
      
      Collection currentIds = new ArrayList();
      String[] vals = getPropertyValues(type);
      if (null != vals)
         currentIds.addAll(Arrays.asList(vals));
      
      boolean different = false;
      if (communityIds[0] == ANY_COMMUNITY_ID)
         // do this so we check for 'y' as well as -1 (backwards compatibility
         different = !dsc.isNewSearch();
      else
      {
         for (int i = 0; i < communityIds.length; i++) 
         {
            if (!currentIds.contains(String.valueOf(communityIds[i])))
               different = true;
         }
      }
      if (!different)
         return;
      
      dsc.clearNewSearch();
      for (int i = 0; i < communityIds.length; i++) 
      {
         setProperty(type, String.valueOf(communityIds[i]), true);
      }
   }

   /**
    * Convenience method that calls 
    * {@link #isCXNewSearch(String) isCxNewSearch(ANY_COMMUNITY_ID_STRING)}.
    *
    * @return <code>true</code> if it's a content explorer new search,
    *    otherwise <code>false</code>
    */
   public boolean isCXNewSearch()
   {
      return isCXNewSearch(PROP_COMMUNITY_ALL);
   }

   /**
    * See {@link #setAsCXNewSearch(int[])} for a description of this property.
    * 
    * @param communityId The numeric id of the community you are testing for.
    * Never <code>null</code> or empty. Use {@link #ANY_COMMUNITY_ID} 
    * to check for the default search of this type. 
    * 
    * @return <code>true</code> if a match is found, <code>false</code> 
    * otherwise.
    * 
    * @see #getNewSearchCommunities(String)
    */
   public boolean isCXNewSearch(String communityId)
   {
      if ( null == communityId)
      {
         throw new 
               IllegalArgumentException("communityId cannot be null or empty");  
      }
      boolean found = doesPropertyHaveValue(PROP_CX_NEWSEARCH, communityId);
      if (!found && communityId.equals(PROP_COMMUNITY_ALL))
         //backwards compatibility
         found = doesPropertyHaveValue(PROP_CX_NEWSEARCH, BOOL_YES);
      return found;
   }
   
   /**
    * Gets all the communities for which this search has been registered as 
    * the default cx new-search search. This method should be called rather
    * than accessing the property directly as it handles backwards compatibility
    * issues.
    * 
    * @return Never <code>null</code>. If this search is not registered as 
    * a default, an empty array is returned. Otherwise, the array contains 
    * a set of community numeric identifiers. {@link #PROP_COMMUNITY_ALL}
    * in the array means that this is used if no search exists that is
    * assigned to the user's community.
    */
   public String[] getCXNewSearchCommunities()
   {
      return getNewSearchCommunities(PROP_CX_NEWSEARCH);
   }
    
   /**
    * Gets all the communities for which this search has been registered as 
    * the default active assembly new-search search. This method should be 
    * called rather than accessing the property directly as it handles 
    * backwards compatibility issues.
    * 
    * @return Never <code>null</code>. If this search is not registered as 
    * a default, an empty array is returned. Otherwise, the array contains 
    * a set of community numeric identifiers. {@link #PROP_COMMUNITY_ALL}
    * in the array means that this is used if no search exists that is
    * assigned to the user's community.
    */
   public String[] getAADNewSearchCommunities()
   {
      return getNewSearchCommunities(PROP_AAD_NEWSEARCH);
   }
    
   /**
    * The class the does the work for {@link #getCXNewSearchCommunities()}
    * and {@link #getAADNewSearchCommunities()}. See those methods for a 
    * description.
    * 
    * @param type Assumed either {@link #PROP_AAD_NEWSEARCH} or 
    * {@link #PROP_CX_NEWSEARCH}.
    * 
    * @return See referenced methods for description.
    */
   private String[] getNewSearchCommunities(String type)
   {
      String[] comms = getPropertyValues(type);
      if (null == comms)
         comms = new String[0];
      if (comms.length == 1)
      {
         //backwards compatibility
         if (comms[0].equals(BOOL_YES))
            comms[0] = PROP_COMMUNITY_ALL;
         else if (comms[0].equals(BOOL_NO))
            comms = new String[0];
      }
      return comms;
   }
   
   /**
    * See {@link #setAsAADNewSearch(int[])} for a description of this property.
    * 
    * @param communityId The numeric id of the community you are testing for.
    * Never <code>null</code> or empty. Use {@link #ANY_COMMUNITY_ID} 
    * to check for the default search of this type. 
    * 
    * @return <code>true</code> if a match is found, <code>false</code> 
    * otherwise.
    */
   public boolean isAADNewSearch(String communityId)
   {
      if ( null == communityId)
      {
         throw new 
               IllegalArgumentException("communityId cannot be null or empty");  
      }
      boolean found = doesPropertyHaveValue(PROP_AAD_NEWSEARCH, communityId);
      if (!found && communityId.equals("-1"))
         //backwards compatibility
         found = doesPropertyHaveValue(PROP_AAD_NEWSEARCH, BOOL_YES);
      return found;
   }
   
   /**
    * Convenience method that calls
    * {@link #isAADNewSearch(String) isAADNewSearch(ANY_COMMUNITY_ID_STRING)}.
    */
   public boolean isAADNewSearch()
   {
      return isAADNewSearch(PROP_COMMUNITY_ALL);
   }

   /**
    * Convience method to set a property's value. If the property
    * with name <code>strName</code> does not exist, one will be added.
    *
    * @param strName the name of the property case-insensitive
    *    match is performed. Never <code>null</code> or empty.
    *
    * @param strValue the value to set, never <code>null<code>
    *    or empty.
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

      // Validate length
      if (strName.length() > PSSProperty.NAME_LENGTH)
         throw new IllegalArgumentException(
            "property name must not exceed " + PSSProperty.NAME_LENGTH +
            " characters");

      // Threshold - if null default to empty string
      if (strValue == null || strValue.trim().length() == 0)
         throw new IllegalArgumentException(
            "strValue must not be null or empty");


      // Validate the non-editable properties
      if (m_nonEditableProps.contains(strName))
         throw new IllegalArgumentException(
            strName + " cannot be modified");

      resetAllowedCommunities();
      
      Iterator iter = m_properties.iterator();
      boolean bFound = false;

      while (iter.hasNext())
      {
         PSSearchMultiProperty prop = (PSSearchMultiProperty) iter.next();

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
               PSSearchMultiProperty 
                     newProp = (PSSearchMultiProperty)prop.clone();

               // Remove the old one
               m_properties.remove(prop);

               // Remove the values of the old property
               // add the new one and clone it to bring over
               // any other attributes (e.g. description ...)
               Iterator values = newProp.iterator(); // cms property(s)
               while (values.hasNext())
               {
                  // remove each entry and add
                  newProp.remove((String) values.next());
               }

               // Single value
               newProp.add(strValue);

               m_properties.add(newProp);
            }
            bFound = true;
            break;
         }
      }

      if (bFound)
         return;

      // Add the property
      PSSearchMultiProperty mp = new PSSearchMultiProperty(strName);
      mp.add(strValue);
      m_properties.add(mp);
   }

   /**
    * Convenience method that calls {@link #getPropertyValues(String)
    * getPropertyValues(strName)} and converts the results to a single
    * <code>String</code>.
    * 
    * @param strName The name, may be <code>null</code> or empty.
    *  
    * @return <code>null</code> if property not found, otherwise if the 
    * property is single-valued, its value, otherwise, if the property is
    * multi-valued, all values concatenated together using a comma separator.
    */
   public String getProperty(String strName)
   {
      String[] results = getPropertyValues(strName);
      if (null == results || results.length == 0)
         return null;
      if (results.length == 1)
         return results[0];
      StringBuilder buf = new StringBuilder();
      for (int i = 0; i < results.length; i++) 
      {
         buf.append(results[i]);
         if (i < results.length-1)
            buf.append(",");
      }
      return buf.toString();
   }
   
   /**
    * Convience method to get a property's value.
    *
    * @param strName the name of the property, a case-insensitive
    *    match is performed.  May be <code>null</code> or empty.
    *
    * @return Value(s) of the property, Returns <code>null</code> if
    *    property does not exist or supplied property name is
    *    <code>null</code> or empty.
    */
   @SuppressWarnings("unchecked")
   public String[] getPropertyValues(String strName)
   {
      if (strName == null || strName.trim().length() == 0)
         return null;

      Iterator iter = m_properties.iterator();
      Collection results = null;
      while (iter.hasNext())
      {
         PSSearchMultiProperty prop = (PSSearchMultiProperty) iter.next();
         if (prop.getName().equalsIgnoreCase(strName))
         {
            Iterator propIter = prop.iterator();
            results = new ArrayList();
            while(propIter.hasNext())
            {
               results.add(propIter.next());
            }
         }
      }
      String[] retVal = null;
      if (null != results)
      {
         retVal = new String[results.size()];
         results.toArray(retVal);
      }
      return retVal;
   }

   /**
    * Whether or not this display format has a certain property
    * 
    * @param strName The property name, may not be <code>null</code> or empty. 
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
         PSSearchMultiProperty prop = (PSSearchMultiProperty) iter.next();

         if (prop.getName().equalsIgnoreCase(strName))
            return true;
      }

      return false;
   }

   /**
    * Convience method that calls {@link #removeProperty(String,String,boolean)
    * removeProperty(strName, strValue, false)}.
    */
   public void removeProperty(String strName, String strValue)
   {
      removeProperty(strName, strValue, false);
   }

   /**
    * Removes a single-valued property from this object or one value from a 
    * multi-valued property. The entire multi-valued property can be removed
    * as well by providing <code>null</code> for the value.
    *
    * @param strName the name of the property, a case-insensitive
    *    match is performed. Never <code>null</code> or empty.
    *
    * @param strValue the value to remove for multi-valued properties. May be
    * <code>null</code> or empty. Ignored unless <code>bMulti</code> is
    * <code>true</code>. 
    *
    * @param bMulti if <code>true</code> this represents a multi-valued 
    * property. In this case, if <code>strValue</code> is not <code>null</code>, 
    * a value by that name is removed. If it is <code>null</code>, then the
    * property is removed from this object (the same behavior as for single
    * valued properties). 
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

      while (iter.hasNext())
      {
         PSSearchMultiProperty prop = (PSSearchMultiProperty) iter.next();

         if (prop.getName().equalsIgnoreCase(strName))
         {
            if (bMulti)
            {
               if (strValue == null)
                  m_properties.remove(prop);
               else
                  // Removes only this occurance of strValue
                  prop.remove(strValue);
            }
            else
            {
               // removes the whole property and any contained
               // child properties
               m_properties.remove(prop);
            }
            break;
         }
      }
   }

   /**
    * Get the display name attribute.
    *
    * @return never <code>null</code> or empty.
    */
   public String getDisplayName()
   {
      return m_strDisplayName;
   }

   /**
    * Set the display name attribute.
    *
    * @param name never <code>null</code> or empty.
    */
   public void setDisplayName(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException(
            "display name must not be null or empty");

      // Threshold
      if (name.equals(m_strDisplayName))
         return;

      // Validate length
      if (name.length() > DISPLAYNAME_LENGTH)
         throw new IllegalArgumentException(
            "Display name must not exceed " + DISPLAYNAME_LENGTH +
            "characters");

      setDirty();
      m_strDisplayName = name;
   }


   /**
    * @see #setInternalName(String)
    */
   public void setName(String name)
   {
      setInternalName(name);
   }

   /**
    * Get the description attribute.
    *
    * @return description never <code>null</code>
    *    may be empty.
    */
   @Override
   public String getDescription()
   {
      return m_strDescription;
   }

   /**
    * Set the description attribute.
    *
    * @param str may be <code>null</code> to
    *    specify the empty string.
    */
   public void setDescription(String str)
   {
      if (str == null)
         str = "";

      // Threshold
      if (str.equalsIgnoreCase(m_strDescription))
         return;

      // Validate length
      if (str.length() > DESCRIPTION_LENGTH)
         throw new IllegalArgumentException(
            "Description must not exceed " + DESCRIPTION_LENGTH +
            "characters");

      setDirty();
      m_strDescription = str;
   }

   /**
    * Get the url attribute.
    *
    * @return the custom url, <code>null</code> if this is not a custom
    *    search. Otherwise its never <code>null</code> but may be empty.
    */
   public String getUrl()
   {
      return m_url;
   }

   /**
    * See {@link #setCaseSensitive(boolean)} for details.
    *
    * @return <code>true</code> if the text data shold be compared in
    * case-sensitive manner, <code>false</code> otherwise.
    */
   public boolean isCaseSensitive()
   {
      return m_isCaseSensitive;
   }

   /**
    * Sets whether the search should treat the text data in case-sensitve
    * manner or not.
    *
    * @param flag <code>true</code> if the text data shold be compared in
    * case-sensitive manner, <code>false</code> otherwise.
    */
   public void setCaseSensitive(boolean flag)
   {
      if (m_isCaseSensitive == flag)
         return;

      setDirty();
      m_isCaseSensitive = flag;
   }

   /**
    * Parses all parameters from the supplied url and fills them into the
    * supplied map as name/value pairs of <code>String</code> objects.
    *
    * @param url the url to parse, may be <code>null</code> or empty.
    * @param params the map into which to fill in the url parameters,
    *    may be <code>null</code> or empty. If <code>null</code> is supplied,
    *    a new <code>HashMap</code> will be created. After this call the
    *    supplied map is never <code>null</code> but may be empty.
    * @return the map that was supplied or a new <code>HashMap</code> if
    *    <code>null</code> was supplied with all query parameters filled in,
    *    never <code>null</code>, may be empty.
    */
   @SuppressWarnings("unchecked")
   public static Map parseParameters(String url, Map params)
   {
      if (params == null)
         params = new HashMap();

      if (url == null)
         return params;

      int queryStart = url.indexOf('?');
      if (queryStart >= 0)
      {
         StringTokenizer tokens =
            new StringTokenizer(url.substring(queryStart+1), "&");
         while (tokens.hasMoreTokens())
         {
            String parameter = tokens.nextToken();
            int valueStart = parameter.indexOf('=');
            if (valueStart >= 0)
            {
               String name = parameter.substring(0, valueStart);
               String value = parameter.substring(valueStart+1);

               params.put(name, value);
            }
         }
      }

      return params;
   }

   /**
    * Set the resource url attribute.
    *
    * @param url Never <code>null</code> or empty. Must be of the form ???
    *
    * @throws IllegalStateException If called on a non-custom search (i.e.,
    *    the isCustomApp() method returns <code>false</code>).
    */
   public void setUrl(String url)
   {
      // TODO What are the limitations on the 'url' param?
      
      if (!isCustomApp())
         throw new IllegalStateException("Cannot set url on standard search.");

      if (url == null || url.trim().length() == 0)
         throw new IllegalArgumentException("Url cannot be null or empty.");

      url = url.trim();

      if (url.equals(m_url))
         return;

      // Validate length
      if (url.length() > CUSTOMURL_LENGTH)
         throw new IllegalArgumentException(
            "Custom url must not exceed " + CUSTOMURL_LENGTH +
            "characters");

      setDirty();
      m_url = url;
   }

   /**
    * Get the display format id attribute.
    *
    * @return never <code>null</code> or empty.
    */
   public String getDisplayFormatId()
   {
      return m_strDisplayId;
   }

   /**
    * Set the display format id attribute.
    *
    * @param str never <code>null</code> or empty.
    */
   public void setDisplayFormatId(String str)
   {
      if (str == null || str.trim().length() == 0)
         throw new IllegalArgumentException(
            "display id must not be null or empty");

      // Threshold
      if (str.equalsIgnoreCase(m_strDisplayId))
         return;

      setDirty();
      m_strDisplayId = str;
   }

   // see base class for description
   @Override
   public boolean equals(Object obj)
   {
      // base class handling and validation
      if (!super.equals(obj))
         return false;

      PSSearch s2 = (PSSearch) obj;

      return m_strDescription.equals(s2.m_strDescription)
         && m_strInternalName.equals(s2.m_strInternalName)
         && m_strDisplayName.equals(s2.m_strDisplayName)
         && m_strDisplayId.equals(s2.m_strDisplayId)
         && m_nParentCat == s2.m_nParentCat
         && m_strType.equals(s2.m_strType)
         && m_nMaxResults == s2.m_nMaxResults
         && m_fields.equals(s2.m_fields)
         && m_properties.equals(s2.m_properties)
         && m_isCaseSensitive == s2.m_isCaseSensitive
         && m_version.equals(s2.m_version);
   }

   //see base class for description
   @Override
   public Object clone()
   {
      PSSearch copy = null;

      copy = (PSSearch) super.clone();

      copy.m_fields = (PSSFields) m_fields.clone();
      copy.m_properties = (PSSProperties) m_properties.clone();
      copy.m_nMaxResults = m_nMaxResults;
      copy.m_nParentCat = m_nParentCat;
      copy.m_strDescription = m_strDescription;
      copy.m_strDisplayId = m_strDisplayId;
      copy.m_strDisplayName = m_strDisplayName;
      copy.m_strType = m_strType;
      copy.m_isCaseSensitive = m_isCaseSensitive;
      copy.m_version = m_version;

      return copy;
   }

   // see base class for description
   @Override
   public int hashCode()
   {
      int nHash = super.hashCode();

      return nHash + m_strDescription.hashCode() + m_strDisplayName.hashCode() +
         m_strDisplayId.hashCode() + m_nParentCat + m_strType.hashCode() +
         m_nMaxResults + m_fields.hashCode() + m_properties.hashCode()
         + Boolean.valueOf(m_isCaseSensitive).hashCode() + m_version.hashCode();
   }

   /**
    * Overriden to determine if it's child list are modified as well.
    * See {@link PSDbComponent#getState() base class} for description.
    */
   @Override
   public int getState()
   {
      // Threshold - if base class is mod, new, or marked for delete
      if (super.getState() != IPSDbComponent.DBSTATE_UNMODIFIED)
         return super.getState();

      // if unmodified verify that the contained lists
      // are also unmodified.
      if (m_fields.getState() == IPSDbComponent.DBSTATE_MODIFIED
         || m_fields.getState() == IPSDbComponent.DBSTATE_NEW)
         return IPSDbComponent.DBSTATE_MODIFIED;

      if (m_properties.getState() == IPSDbComponent.DBSTATE_MODIFIED
         || m_properties.getState() == IPSDbComponent.DBSTATE_NEW)
         return IPSDbComponent.DBSTATE_MODIFIED;

      return IPSDbComponent.DBSTATE_UNMODIFIED;
   }

   /**
    * A flag that indicates whether the results for this search come from a
    * custom application or from web services.
    *
    * @return <code>true</code> if this search is a custom application
    *    otherwise <code>false</code>
    */
   public boolean isCustomApp()
   {
      return m_isCustom;
   }

   /**
    * Override to deal with all child components.
    */
   @Override
   public void markForDeletion()
   {
      super.markForDeletion();
      m_fields.markForDeletion();
      m_properties.markForDeletion();
   }

   /**
    * Get the visibility flag for this search object.
    * @return one of the SHOW_TO_XXXX flags to inidcate the search is visible
    * to the current user, current community or all communities.
    */
   public int getShowTo()
   {
      //If the search has a property "sys_username" means it is visible to the
      //user who created it.
      if(hasProperty(PROP_USERNAME))
         return SHOW_TO_USER;

      //If the search has a property "sys_community=-1" means it is visible to
      //a user from any community.
      if(doesPropertyHaveValue(PROP_COMMUNITY, PROP_COMMUNITY_ALL))
         return SHOW_TO_ALL_COMMUNITIES;

      //If the search has a property "sys_community!=-1" means it is visible to
      //a user from any community.
      return SHOW_TO_COMMUNITY;
   }

   /**
    * Get the property names that are used by the rhythmyx server.  All other
    * properties are passed thru to the external search engine.
    *
    * @return A read-only collection of property names, never <code>null</code>
    * or empty.
    */
   @SuppressWarnings("unchecked")
   public Collection getInternalPropertyNames()
   {
      return Collections.unmodifiableCollection(ms_internalSearchProps);
   }
   
   /**
    * Converts this search to an internal search if 
    * {@link #useExternalSearch()} returns <code>true</code>, otherwise
    * simply returns. 
    */
   @SuppressWarnings("unchecked")
   public void convertToInternal()
   {
      if (!useExternalSearch())
         return;
      
      // convert the search prop
      setProperty(PSSearch.PROP_SEARCH_ENGINE_TYPE, 
         PSSearch.SEARCH_ENGINE_TYPE_INTERNAL);
      
      // set as "advanced" or else the fields are ignored
      setProperty(PSSearch.PROP_SEARCH_MODE, 
         PSSearch.SEARCH_MODE_ADVANCED);
      
      // clear any FTS query
      removeProperty(PSSearch.PROP_FULLTEXTQUERY, null);
      
      // now convert the fields
      Iterator fields = getFields();
      while (fields.hasNext())
      {
         PSSearchField field = (PSSearchField) fields.next();
         field.setExternalOperator(null);
      }
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
      switch (getShowTo())
      {
      case SHOW_TO_USER:
         m_allowedCommunities = null;
         break;
      case SHOW_TO_ALL_COMMUNITIES:
         m_allowedCommunities = allCommunities;
         break;
      default:
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
    * @see com.percussion.services.data.IPSCloneTuner#tuneClone(long)
    */
   @SuppressWarnings("unchecked")
   public Object tuneClone(long newId)
   {
      PSKey newKey = createKey(new String[]
      {
         newId + ""
      });
      setKey(newKey);
      Iterator cols = m_fields.iterator();
      while (cols.hasNext())
      {
         PSSearchField field = (PSSearchField) cols.next();
         field.setKey(newKey);
      }
      Iterator props = m_properties.iterator();
      while (props.hasNext())
      {
         PSSearchMultiProperty prop = (PSSearchMultiProperty) props.next();
         prop.setKey(newKey);
      }
      return this;
   }
   
   /**
    * A list of allowed communities. It is a transient object and is not
    * considered as part of the object. It is typically used by the 
    * webservice layer to get name/id pairs of the allowed communities.
    * <p>
    * It is set by {@link #setAllowedCommunities(Map)} and unsetted by any of 
    * the property changes. It is <code>null</code> if has not set yet.
    */
   Map<IPSGuid, String> m_allowedCommunities = null;
   
   /**
    * Valid values and its names from the {@link #getParentCategory()}
    */
   public enum ParentCategory
   {
      MY_CONTENT        (1, "myContent"),
      COMMUNITY_CONTENT (2, "communityContent"),
      ALL_CONTENT       (3, "allContent"),
      OTHER_CONTENT     (4, "otherContent");
      
      /**
       * The numeric value of the object, starts from <code>1</code>.
       */
      private int m_id;
      
      /**
       * The name of the object, never <code>null</code> or empty.
       */
      private String m_name;
      
      /**
       * Creates an object from the given id and name.
       * 
       * @param id the numeric value of the created object.
       * @param name the name of the object, it may not be <code>null</code>
       *    or empty.
       */
      private ParentCategory(int id, String name)
      {
         if (name == null || name.trim().length() == 0)
            throw new IllegalArgumentException(
                  "name may not be null or empty.");

         m_id = id;
         m_name = name;
      }
      
      /**
       * @return the numeric value of the object, starts from <code>1</code>.
       */
      public int getId()
      {
         return m_id;
      }

      /**
       * @return the name of the object, never <code>null</code> or empty.
       */
      public String getName()
      {
         return m_name;
      }
   }

   // private defines
   private static final String KEY_COL_ID = "SEARCHID";

   // public defines
   public static final int DEFAULT_MAX = 100;
   public static final int UNLIMITED_MAX = -1;

   /**
    * String constant representing the type "View". A view is a query based on
    * a set of search fields and values (parameters ) and cannot be modified in
    * any way by the end user. The only control he has has is to change display
    * format but not the content in the view. Views are further categorized into
    * two groups, namely, Custom Views and Standard Views. The content in the
    * Custom views is the result of executing a specified Rhythmyx URL. On the
    * otherhand the content in a Standard View is the result of executing a web
    * services request known to the client system.
    */
   public static final String TYPE_VIEW = "View";

   /**
    * String constant representing the type "User Search". A user search is a
    * query based on a set of search fields and values (parameters ) in that an
    * end usr can customize to include addtional fields or to remove existing
    * fields and change the query parameter values.
    */
   public static final String TYPE_USERSEARCH = "Search";

   /**
    * String constant representing the type "Related Content Search". An RC
    * Search is a query based on a set of search fields and values (parameters )
    * in that an end usr can customize to include addtional fields or to remove
    * existing fields and change the query parameter values. This type of search
    * must inlcude two special fields,namely content type id and variant id.
    */
   public static final String TYPE_RCSEARCH = "RCSearch";

   /**
    * String constant representing the type "Standard Search". A Standard Search
    * is a query based on a set of search fields and values (parameters )
    * in that an end user can change the query parameter values. He may also be
    * able to customize search fields to include addtional fields or to remove
    * existing fields depending on the way implementer sets up the search. The
    * content in a Standard Search the result of executing a web service
    * request known to the client system.
    */
   public static final String TYPE_STANDARDSEARCH = "StandardSearch";

   /**
    * String constant representing the type "Custom Search". A Custom Search  is
    * a query based on a set of search fields and values (parameters )
    * in that an end user can never customize to include addtional fields or to
    * remove existing fields but change the query parameter values. The content
    * in a Custom Search the result of executing a specified Rhythmyx URL
    * specially built for this purpose.
    */
   public static final String TYPE_CUSTOMSEARCH = "CustomSearch";

   /**
    * Contains all of the allowed values that can be used w/ getType and
    * setType.
    */
   private static final String[] TYPES_ENUM =
   {
      TYPE_VIEW,
      TYPE_USERSEARCH,
      TYPE_RCSEARCH,
      TYPE_STANDARDSEARCH,
      TYPE_CUSTOMSEARCH
   };
   public static final String PROP_CUSTOMAPP = "isCustom";
   public static final String PROP_USERNAME = "sys_username";
   public static final String PROP_COMMUNITY = PSDisplayFormat.PROP_COMMUNITY;
   public static final String PROP_COMMUNITY_ALL = 
      PSDisplayFormat.PROP_COMMUNITY_ALL;
   /**
    * This property specifies whether a search or view is customizable by the
    * end user. Allowed values are {@link #BOOL_YES} or {@link #BOOL_NO}, 
    * defaults to {@link #BOOL_NO}.
    */
   public static final String PROP_USER_CUSTOMIZABLE = "userCustomizable";
   public static final String PROP_CX_NEWSEARCH = "cxNewSearch";
   public static final String PROP_AAD_NEWSEARCH = "aadNewSearch";
   public static final String PROP_FULLTEXTQUERY = "FullTextQuery";
   
   /**
    * Property to allow a search to override any max search results specified in
    * the global search configuration in the server configuration. If this
    * property is specified with a value of {@link #BOOL_YES}, then the max
    * results setting of this search is used, otherwise the global maximum is in
    * effect (note that the max results value specified by this search is still
    * used if it is less than the global max or if the gobal max is not
    * specified).
    */
   public static final String PROP_OVERRIDE_GLOBAL_MAX_RESULTS = 
      "overrideGlobalMaxResults";
   
   /**
    * Constant to be used with the {@link #setAsCXNewSearch(int[])} and 
    * {@link #setAsAADNewSearch(int[])}. Is the numeric identifier that 
    * indicates this search is the default search to be used if no other 
    * search has the property set for the user's community.
    */
   public static final int ANY_COMMUNITY_ID = 
         Integer.parseInt(PROP_COMMUNITY_ALL);

   /**
    * Property to define the search mode.  Current possible values are
    * {@link #SEARCH_MODE_SIMPLE} and {@link #SEARCH_MODE_ADVANCED}.
    */
   public static final String PROP_SEARCH_MODE = "searchMode";

   /**
    * Value for the {@link #PROP_SEARCH_MODE} property to indicate a simple
    * search is to be performed.  This means that any search fields are to be
    * ignored, and only the full text query string is to be used.
    */
   public static final String SEARCH_MODE_SIMPLE = "simple";

   /**
    * Value for the {@link #PROP_SEARCH_MODE} property to indicate an advanced
    * search is to be performed.  This means that any search fields are to be
    * included as well any full text query string.
    */
   public static final String SEARCH_MODE_ADVANCED = "advanced";

   /**
    * Property to define search type.  Used to indicate what search ui should be
    * used to edit the search properties.  Possible values are defined by the
    * <code>SEARCH_ENGINE_TYPE_XXX</code> constants, defaulting to
    * {@link #SEARCH_ENGINE_TYPE_INTERNAL} if not defined.
    */
   public static final String PROP_SEARCH_ENGINE_TYPE = "searchEngineType";

   
   /**
    * The name of the search property used to store the folder path or folder id 
    * if a search is to be folder limited. An empty value is equivalent to
    * absence of the property.
    */
   public static final String PROP_FOLDER_PATH = "folderPath";   
   
   /**
    * The name of the search property used to store the flag that indicates
    * whether to include the sub-folders in the search as well. Only used
    * if the {@link #PROP_FOLDER_PATH} property is present. Its value should
    * be either <code>true</code> or <code>false</code>, case insensitive. If 
    * not <code>false</code>, <code>true</code> is assumed.
    */
   public static final String PROP_FOLDER_PATH_RECURSE = "includeSubFolders";
   
   /**
    * Constant for Convera search engine property name query type
    */
   public static final String PROP_QUERYTYPE = "querytype";
   
   /**
    * Constant for boolean query type property value.
    */
   public static final String QT_BOOLEAN = "16";

   /**
    * Constant for concept query type property value.
    */
   public static final String QT_CONCEPT = "32";

   /**
    * Constant for pattern query type property value.
    */
   public static final String QT_PATTERN = "64";
   
   /**
    * Constant for Convera search eninge property name expansion level
    */
   public static final String PROP_EXPANSIONLEVEL = "expansionlevel";
   
   /**
    * This is the property name used to transfer the 'Filter with' text. 
    */
   public static final String PROP_BODYFILTER = "bodyfilter";
   
   /**
    * Property to indicate search is executed by the internal search engine.
    */
   public static final String SEARCH_ENGINE_TYPE_INTERNAL = "internal";

   /**
    * Property to indicate search is executed by the external search engine.
    */
   public static final String SEARCH_ENGINE_TYPE_EXTERNAL = "external";

   /**
    * The constant to indicate root node name.
    */
   public static final String XML_NODE_NAME = "PSXSearch";

   public static final String XML_NODE_DISPLAYNAME = "DISPLAYNAME";
   public static final String XML_NODE_DF = "DISPLAYFORMAT";
   public static final String XML_NODE_PARENTCATEGORY = "PARENTCATEGORY";
   public static final String XML_NODE_INTERNALNAME = "INTERNALNAME";
   public static final String XML_NODE_RESOURCE = "CUSTOMURL";
   public static final String XML_NODE_TYPE = "TYPE";
   public static final String XML_NODE_MAX = "MAXIMUMITEMS";
   public static final String XML_NODE_DESCRIPTION = "DESCRIPTION";
   public static final String XML_NODE_CASE_SENSITIVE = "CASESENSITIVE";
   public static final String XML_NODE_VERSION = "VERSION";

   // @todo move to common area
   public static final String XML_TRUE = "yes";
   public static final String XML_FALSE = "no";

   // validation lengths for data
   public static final int INTERNALNAME_LENGTH = 128;
   public static final int DISPLAYNAME_LENGTH = 128;
   public static final int CUSTOMURL_LENGTH = 255;
   public static final int TYPE_LENGTH = 255;
   public static final int DESCRIPTION_LENGTH = 255;

   /**
    * property list, initialized in ctor, never <code>null</code>, may be
    * empty.
    */
   private PSSProperties m_properties;

   /**
    * fields list, initialized in ctor, never <code>null</code>, may be
    * empty.
    */
   private PSSFields m_fields;

   /**
    * Description of search, initialized in definition, never <code>null
    * </code> may be empty
    */
   private String m_strDescription = "";

   /**
    * Display name of search, initialized in definition, never <code>null
    * </code> or empty
    */
   private String m_strDisplayName = "";

   /**
    * Foreign key of display id for this search, initialized in definition,
    * never <code>null</code> or empty
    */
   private String m_strDisplayId = "";

   /**
    * Parent category of this search. Initialized in definition, defaults
    * to {@link ParentCategory#MY_CONTENT}.
    */
   private int m_nParentCat = ParentCategory.MY_CONTENT.getId();

   /**
    * See {@link #getType()} for details. Always one of the TYPE_xxx values.
    */
   private String m_strType = TYPE_USERSEARCH;

   /**
    * Internal name of search, initialized in definition, never
    * <code>null</code> or empty.
    */
   private String m_strInternalName = "";

   /**
    * The url used to get the results if they are obtained from a custom
    * application. Only used/saved if <code>m_isCustom</code> is
    * <code>true</code>. Is <code>null</code> if this is a standard view, never
    * <code>null</code> or empty if a custom view.
    */
   private String m_url = null;

   /**
    * Maximum number of entries returned via this search, initialized in
    * definition, defaults to <code>DEFAULT_MAX</code>
    */
   private int m_nMaxResults = DEFAULT_MAX;

   /**
    * N.B. The following context parameters are hard coded for
    * now as they currently are not in an application.
    * @todo move the applet context parameters to an application
    */

   /**
    * The parameter to define the applet is in debug mode or not. The allowed
    * values are 'TRUE' or 'FALSE'. The default is 'FALSE'.
    */
   public static final String PARAM_DEBUG = "DEBUG";

   /**
    * The parameter that defines the view that this instance of the applet
    * should represent. The value must be one of the <code>
    * PSUiMode.TYPE_VIEW_xxx</code> values. If this parameter is not suppled, it
    * assumes 'CX' view.
    */
   public static final String PARAM_VIEW = "VIEW";

   /**
    * The parameter that defines a url to get menu xml. If this is not supplied
    * it assumes defaults for each view.
    */
   public static final String PARAM_MENU_URL = "MENU_URL";

   /**
    * The parameter that defines a url to get navigational tree xml for 'CX' or
    * 'IA' views. If this is not supplied for 'CX' view, loads the xml as
    * defined by the file <code>CE_NAV_XML</code>, for IA view raises an
    * exception.
    */
   public static final String PARAM_NAV_URL = "NAV_URL";

   /**
    * The parameter that defines a url to get all possible relations an item can
    * have. If not supplied uses
    */
   public static final String PARAM_RS_URL = "RELATIONS_URL";

   /**
    * The parameter that defines the relationship to be selected by default in
    * 'DT' view when the applet is loaded.
    */
   public static final String PARAM_INITIAL_RS = "INITIAL_RS";

   /**
    * The parameter that defines a url to get ancestors of an item for a
    * particular relationship.
    */
   public static final String PARAM_ANC_URL = "ANCESTOR_URL";

   /**
    * The parameter that defines a url to get descendants of an item for a
    * particular relationship.
    */
   public static final String PARAM_DESC_URL = "DESCENDANT_URL";

   /**
    * The parameter that defines a url to get options.
    */
   public static final String PARAM_OPTIONS_URL = "OPTIONS_URL";

   /**
    * N.B. The following context parameters are hard coded for
    * now as they currently are not in an application.
    */
   public static final String [] CONTEXT_PARAMS_LIST = new String []
      {
      // TODO move the applet context parameters to an application
         PSSearch.PARAM_DEBUG,
         PSSearch.PARAM_VIEW,
         PSSearch.PARAM_MENU_URL,
         PSSearch.PARAM_NAV_URL,
         PSSearch.PARAM_RS_URL,
         PSSearch.PARAM_INITIAL_RS,
         PSSearch.PARAM_ANC_URL,
         PSSearch.PARAM_DESC_URL,
         PSSearch.PARAM_OPTIONS_URL
      };

   /**
    * List of properties that cannot be modifed over the life cycle
    * of this object. Initialized in definition, setup with elements in ctor,
    * never <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private List m_nonEditableProps = new ArrayList();

   /**
    * A flag to indicate whether this search of type custom view that should
    * contain a url that is used to obtain the results as opposed to using
    * web-services. Set in ctor by caller, {@link #setCustom(boolean)} or in
    * fromXml based on the presence of a url in the serialized form. 
    */
   private boolean m_isCustom = false;

   /**
    * A monotonically incrementing value used to uniquify the names assigned
    * to newly created searches. After using, it should be incremented by 1.
    */
   private static int ms_nameSuffix = 1;

   /**
    * Stores the case sensitivity of the search. If <code>true</code> then
    * the text data is compared in case-sensitive manner, otherwise not.
    * Defaults to <code>false</code>. Modified in the
    * <code>setCaseSensiti()</code> method.
    */
   private boolean m_isCaseSensitive = false;

   /**
    * Constatnt for character representation of the boolean "yes".
    */
   public static final String BOOL_YES = "y";

   /**
    * Constatnt for character representation of the boolean "no".
    */
   public static final String BOOL_NO = "n";

   /**
    * Constatnt for new search context for Content Explorer.
    */
   public static final String CX_NEW_SEARCH = "CXNEWSEARCH";

   /**
    * Constatnt for new search context for Active Assembly for Documents.
    */
   public static final String AAD_NEW_SEARCH = "AADNEWSEARCH";

   /**
    * The constant to indicate that the search is visible to current user which
    * means the user who created the search.
    * @see #getShowTo()
    */
   static public final int SHOW_TO_USER = 0;

   /**
    * The constant to indicate that the search is visible to any user from
    * current community which means user's community when the search was
    * created.
    * @see #getShowTo()
    */
   static public final int SHOW_TO_COMMUNITY = 1;

   /**
    * The constant to indicate that the search is visible to the user from any
    * community.
    * @see #getShowTo()
    */
   static public final int SHOW_TO_ALL_COMMUNITIES = 3;

   /**
    * List of search properties that used directly by the server.  All other
    * properties are passed thru to the search engine.  Never <code>null</code>,
    * emtpy, or modified after construction.
    */
   static private List<String> ms_internalSearchProps = new ArrayList<>();
   static
   {
      ms_internalSearchProps.add(PSSearch.PROP_COMMUNITY);
      ms_internalSearchProps.add(PSSearch.PROP_COMMUNITY_ALL);
      ms_internalSearchProps.add(PSSearch.PROP_USER_CUSTOMIZABLE);
      ms_internalSearchProps.add(PSSearch.PROP_CX_NEWSEARCH);
      ms_internalSearchProps.add(PSSearch.PROP_AAD_NEWSEARCH);
      ms_internalSearchProps.add(PSSearch.PROP_FULLTEXTQUERY);
      ms_internalSearchProps.add(PSSearch.PROP_SEARCH_MODE);
      ms_internalSearchProps.add(PSSearch.PROP_SEARCH_ENGINE_TYPE);
      ms_internalSearchProps.add(PSSearch.PROP_USERNAME);
      ms_internalSearchProps.add(PSSearch.PROP_FOLDER_PATH);
      ms_internalSearchProps.add(PSSearch.PROP_FOLDER_PATH_RECURSE);
   }
}
