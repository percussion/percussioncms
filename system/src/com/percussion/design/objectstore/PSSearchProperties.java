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

package com.percussion.design.objectstore;

import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A PSSearchProperties is a container for attributes that control how the
 * search interface looks and how the search engine is configured at the
 * field level. It is defined in sys_basicObjects.dtd as PSXSearchProperties.
 * <p>Originally, a few of these properties were stored in the PSField object
 * directly. However, while implementing full text search, several additional
 * properties were added and it was felt that it was time to group these props
 * together rather than having them spread throughout the field object.
 * <p>Some management of attributes in this class is required of the containing
 * class. This is documented in the package access methods.
 * 
 * @author paulhoward
 */
public class PSSearchProperties extends PSComponent
{
   //we don't override clone as all members are immutable
   
   /**
    * Convenience method that calls {@link #PSSearchProperties(String,boolean)
    * PSSearchProperties("", <code>false</code>)}.
    */
   public PSSearchProperties()
   {
      this("", false);
   }
   
   
   /**
    * Uses all default values except for enabling of search itself.
    * 
    * @param userSearchable See {@link #setUserSearchable(boolean)} for
    * details.
    */
   public PSSearchProperties(boolean userSearchable)
   {
      setUserSearchable(userSearchable);
   }
   
   /**
    * Convenience method that calls {@link #PSSearchProperties(String, boolean)
    * PSSearchProperties(defaultSearchLabel, <code>false</code>)}.
    */
   public PSSearchProperties(String defaultSearchLabel)
   {
      this(defaultSearchLabel, false);
   }
  
   /**
    * Most generic ctor. Enables search.
    * 
    * @param defaultSearchLabel See {@link #setDefaultSearchLabel(String)} for
    * allowed values.
    * 
    * @param enableTransformation See {@link #setEnableTransformation(boolean)
    * for details.
    */
   public PSSearchProperties(String defaultSearchLabel, 
         boolean enableTransformation)
   {
      setDefaultSearchLabel(defaultSearchLabel);
      setEnableTransformation(enableTransformation);
   }

   /**
    * Convenience method that constructs a default object, then calls {@link
    * #fromXml(Element, IPSDocument, ArrayList) 
    * fromXml(source, <code>null</code>, <code>null</code>)} on it.
    * <p>See that method for details on param and exception.
    */
   public PSSearchProperties(Element source)
      throws PSUnknownNodeTypeException
   {
      fromXml(source, null, null);
   }
   
   /**
    * See {@link #setTokenizeSearchContent(boolean)} for details.
    * 
    * @return The value set with <code>setTokenizeSearchContent</code> or 
    * restored from xml, or the default value if it has never been set.
    */
   public boolean isTokenizeSearchContent()
   {
      return m_tokenizeSearchContent;
   }

   /**
    * See {@link #setVisibleToGlobalQuery(boolean)} for details.
    * 
    * @return The value set with <code>setVisibleToGlobalQuery</code> or 
    * restored from xml, or the default value if it has never been set.
    */
   public boolean isVisibleToGlobalQuery()
   {
      return m_visibleToGlobalQuery;
   }

   /**
    * Is this field user searchable?
    *
    * @return <code>true</code> to indicate user searchable,
    *    <code>false</code> otherwise.
    */
   public boolean isUserSearchable()
   {
      return m_userSearchable;
   }

   /**
    * Is this field enabled for transformation.
    *
    * @return <code>true</code> to indicate enabled for transformation,
    *    <code>false</code> otherwise.
    */
   public boolean isEnableTransformation()
   {
      return m_enableTransformation;
   }

   /**
    * Is this field user customizable?
    *
    * @return <code>true</code> to indicate user customizable,
    *    <code>false</code> otherwise.
    */

   public boolean isUserCustomizable()
   {
      return m_userCustomizable;
   }

   /**
    * A user searchable field is indexed for searching and can be used to 
    * perform parametric searches. If this is <code>false</code>, all other 
    * properties are meaningless. Defaults to <code>true</code>.
    *
    * @param userSearchable <code>true</code> to set make this field available
    * for searching, <code>false</code> otherwise.
    */
   public void setUserSearchable(boolean userSearchable)
   {
      m_userSearchable = userSearchable;
   }

   /**
    * The purpose of this flag is to indicate that the contents of the field
    * are either binary (such as pdf or gif) or contain significant formatting 
    * that should not be indexed (such as html). If the underlying data type
    * is binary, this flag will be set to <code>true</code> and locked 
    * (see {@link #isEnableTransformationLocked()}. 
    * <p>This is somewhat RetrievalWare specific because they don't perform 
    * such transformations on data in what they call meta fields and we have
    * no way of knowing whether a text field should be converted or not.
    * <p>If the field is not {@link #isUserSearchable() user searchable},
    * this flag is ignored.
    * <p>Originally added for full text search.
    *   
    * @param enableTransformation <code>true</code> to enable transformation, 
    * <code>false</code> otherwise. If <code>true</code> is supplied, the
    * {@link #isVisibleToGlobalQuery()} flag will also be set.
    * 
    * @throws IllegalStateException If this property is locked due to the 
    * underlying data type. Use {@link #isEnableTransformationLocked()} to
    * determine if this method can be successfully called. 
    */
   public void setEnableTransformation(boolean enableTransformation)
   {
      if (m_enableTransformationLocked)
      {
         throw new IllegalStateException("The transformation flag is locked.");
      }
      m_enableTransformation = enableTransformation;
      if (enableTransformation)
         setVisibleToGlobalQuery(true);
   }

   /**
    * The external search engine is word based. Typically, whitespace and
    * punctuation is used to separate words. However, sometimes it is 
    * undesirable to use punctation as a word separator, such as for file paths
    * or possibly an identifier (such as 123:456). This flag is used to 
    * control parsing on this field. Defaults to <code>false</code>. 
    * <p>If the field is not {@link #isUserSearchable() user searchable},
    * this flag is ignored.
    * <p>Originally added for external search engine use.
    * 
    * @param isToken If <code>true</code>, when the contents of this field are 
    * indexed into the external search engine, punctuation is not treated
    * as a word separator, it is left as part of the word. Otherwise, 
    * punction will separate words and it is not indexed.  
    */
   public void setTokenizeSearchContent(boolean isToken)
   {
      m_tokenizeSearchContent = isToken;
   }

   /**
    * The search engine has the concept of the main data and meta data. The
    * global query searches the main data always, plus any 'meta data' fields
    * that are visible to the global query. 'Meta data' is used loosely here
    * to mean all the fields that aren't considered the main content of the
    * item. Defaults to <code>true</code>. 
    * <p>If the field is not {@link #isUserSearchable() user searchable},
    * this flag is ignored.
    * <p>Originally added for external search engine use.
    * 
    * @param isVisible If this is <code>true</code>, when a full text global 
    * query is performed, this field will be visible. If <code>false</code>, 
    * the contents of this field will only match queries executed against 
    * this specific field. Must be <code>true</code> if <code>
    * isEnableTransformation()</code> returns <code>true</code> or an
    * exception will be thrown.
    * 
    * @throws IllegalStateException If {@link #isEnableTransformation()
    * returns <code>true</code> and <code>isVisible</code> is <code>false
    * </code>.
    */
   public void setVisibleToGlobalQuery(boolean isVisible)
   {
      m_visibleToGlobalQuery = isVisible;
   }

   /**
    * Set this field to user customizable.
    *
    * @param userCustomizable <code>true</code> to set this field to user
    * customizable, <code>false</code> otherwise.
    */
   public void setUserCustomizable(boolean userCustomizable)
   {
      m_userCustomizable = userCustomizable;
   }

   /**
    * See {@link #setDefaultSearchLabel(String)} for details.
    *
    * @return Never <code>null</code>.
    */
   public String getDefaultSearchLabel()
   {
      return m_defaultSearchLabel;
   }

   /**
    * If a field is searchable (meaning {@link #isUserSearchable()} returns
    * <code>true</code>), when that field is displayed to the end user, it
    * will have an associated text label. First, if there is a UIDef, the
    * label from that will be used, if there isn't one, then this label.
    * If it hasn't been specifically set, defaults to the submit name of the
    * field. (This behavior is managed by the containing <code>PSField</code>.)
    *
    * @param label May be <code>null</code> or empty. White space is trimmed.
    */
   public void setDefaultSearchLabel(String label)
   {
      if (null == label || label.trim().length() == 0)
         m_defaultSearchLabel = "";
      else
         m_defaultSearchLabel = label.trim();
   }

   /**
    * Creates a node that conforms to the dtd specified for PSXSearchProperties 
    * in sys_basicObjects.dtd.     
    * See base class for description of params.    
    */
   public Element toXml(Document doc)
   {
      if (null == doc)
      {
         throw new IllegalArgumentException("doc cannot be null");
      }
      Element root = doc.createElement(XML_NODE_NAME);
      // base class data
      super.toXml(root);
      
      root.setAttribute(USER_SEARCHABLE_ATTR,
            BOOLEAN_ENUM[isUserSearchable() ? 0 : 1]);
      root.setAttribute(GLOBAL_QUERY_VISIBLE_ATTR,
            BOOLEAN_ENUM[isVisibleToGlobalQuery() ? 0 : 1]);
      root.setAttribute(SEARCH_TOKEN_ATTR,
            BOOLEAN_ENUM[isTokenizeSearchContent() ? 0 : 1]);
      root.setAttribute(ENABLE_TRANSFORMATION_ATTR,
            BOOLEAN_ENUM[isEnableTransformation() ? 0 : 1]);
      root.setAttribute(USER_CUSTOMIZABLE_ATTR,
            BOOLEAN_ENUM[isUserCustomizable() ? 0 : 1]);
      String label = getDefaultSearchLabel();
      if (label.length() > 0)
         root.setAttribute(DEF_SEARCH_LABEL_ATTR, label);
      return root;
   }

   /**
    * Expects a node that conforms to the dtd specified for PSXSearchProperties 
    * in sys_basicObjects.dtd. 
    * See base class for description of params.    
    */
   public void fromXml(Element source, IPSDocument parentDoc,
         ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, XML_NODE_NAME);

      if (!XML_NODE_NAME.equals(source.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, source.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;

      String data = null;
      try
      {
         PSXmlTreeWalker tree = new PSXmlTreeWalker(source);

         // base class data
         super.fromXml(source);
         
         // OPTIONAL
         data = tree.getElementData(USER_SEARCHABLE_ATTR);
         if (data != null)
            setUserSearchable(data.equalsIgnoreCase(BOOLEAN_ENUM[0]));
         else
            //default value in case loading into a non-clean field
            setUserSearchable(true);         

         // OPTIONAL
         data = tree.getElementData(GLOBAL_QUERY_VISIBLE_ATTR);
         if (data != null)
            setVisibleToGlobalQuery(data.equalsIgnoreCase(BOOLEAN_ENUM[0]));
         else
            //default value in case loading into a non-clean field
            setVisibleToGlobalQuery(true);

         // OPTIONAL
         data = tree.getElementData(SEARCH_TOKEN_ATTR);
         if (data != null)
            setTokenizeSearchContent(data.equalsIgnoreCase(BOOLEAN_ENUM[0]));
         else
            //default value in case loading into a non-clean field
            setTokenizeSearchContent(false);

         // OPTIONAL
         data = tree.getElementData(ENABLE_TRANSFORMATION_ATTR);
         if (data != null)
            setEnableTransformation(data.equalsIgnoreCase(BOOLEAN_ENUM[0]));
         else
            //default value in case loading into a non-clean field
            setEnableTransformation(false);

         // OPTIONAL
         data = tree.getElementData(USER_CUSTOMIZABLE_ATTR);
         if (data != null)
            setUserCustomizable(data.equalsIgnoreCase(BOOLEAN_ENUM[0]));
         else
            //default value in case loading into a non-clean field
            setUserCustomizable(true);

         // OPTIONAL
         setDefaultSearchLabel(tree.getElementData(DEF_SEARCH_LABEL_ATTR));
      }
      finally
      {
         resetParentList(parentComponents, parentSize);
         m_fromXml = true;
      }

   }
   
   
   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXSearchProperties";

   /**
    * See base class for details.
    * 
    * @param c Never <code>null</code>.
    */
   public void copyFrom(PSSearchProperties c)
   {
      super.copyFrom(c);
      setUserSearchable(c.isUserSearchable());
      setEnableTransformation(c.isEnableTransformation());
      setEnableTransformationLocked(c.isEnableTransformationLocked());
      m_fromXml = c.isFromXml();
      setVisibleToGlobalQuery(c.isVisibleToGlobalQuery());
      setTokenizeSearchContent(c.isTokenizeSearchContent());
      setUserCustomizable(c.isUserCustomizable());
      m_defaultSearchLabel = c.m_defaultSearchLabel;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSSearchProperties)) return false;
      if (!super.equals(o)) return false;
      PSSearchProperties that = (PSSearchProperties) o;
      return m_userSearchable == that.m_userSearchable &&
              m_visibleToGlobalQuery == that.m_visibleToGlobalQuery &&
              m_tokenizeSearchContent == that.m_tokenizeSearchContent &&
              m_enableTransformation == that.m_enableTransformation &&
              m_userCustomizable == that.m_userCustomizable &&
              m_defaultSearchLabel.equalsIgnoreCase(that.m_defaultSearchLabel);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_userSearchable, m_visibleToGlobalQuery, m_tokenizeSearchContent, m_enableTransformation, m_userCustomizable, m_defaultSearchLabel);
   }

   /**
    * See {@link #setEnableTransformationLocked(boolean)} for details.
    * @return The value set by the mutator, or the default if the mutator
    * has never been called.
    */
   public boolean isEnableTransformationLocked()
   {
      return m_enableTransformationLocked;
   }

   /**
    * For certain db data types, the search engine requires that transformation
    * be enabled. The container of this class must make sure this flag is
    * properly set. If <code>true</code>, any calls to <code>
    * setEnableTransformation</code> that attempt to change the state result
    * in an exception. Defaults to <code>false</code>.
    * <p>This value is set by the system and cannot be changed by implementers.
    * <p>This flag is not considered for equality because it is not a 
    * fundamental property.
    * 
    * @param locked <code>true</code> to prevent changes to the <code>
    * enableTransformation</code> flag.
    */
   void setEnableTransformationLocked(boolean locked)
   {
      m_enableTransformationLocked = locked;
   }

   /**
    * This latch is required to allow us to set the <code>
    * enableTransformation</code> flag based on the underlying db type if
    * a value was not supplied in the xml. This allows us to make CLOBs into
    * transformed fields on upgrades.
    * <p>Defaults to <code>false</code>. The containing class is responsible
    * for setting this flag properly.
    * <p>This flag is not considered for equality because it is not a 
    * fundamental property.
    * 
    * @return <code>true</code> if this object was populated using <code>
    * fromXml</code>, <code>false</code> otherwise.
    */
   boolean isFromXml()
   {
      return m_fromXml;
   }

   /** 
    * See {@link #setUserSearchable(boolean)} for description.
    * 
    * <p>Read and set w/ standard boolean accessor/mutator. 
    */
   private boolean m_userSearchable = true;
   
   /**
    * See {@link #setVisibleToGlobalQuery(boolean)} for description.
    * 
    * <p>Read and set w/ standard boolean accessor/mutator only. 
    * <p>We don't bother hiding this if the external engine is not present
    * as its presence is innocuous. 
    */
   private boolean m_visibleToGlobalQuery = true;

   /**
    * See {@link #setTokenizeSearchContent(boolean)} for description. 
    * <p>Read and set w/ standard boolean accessor/mutator only. 
    * <p>We don't bother hiding this if the external engine is not present
    * as its presence is innocuous. 
    */
   private boolean m_tokenizeSearchContent = false;

   /**
    * See {@link #setEnableTransformationLocked(boolean)} for description.
    * Defaults to <code>false</code> if the fixup method was not called.
    */
   private transient boolean m_enableTransformationLocked = false;

   /**
    * See {@link #setEnableTransformation(boolean)} for description.
    * <p>Read and set w/ standard boolean accessor/mutator only. 
    */
   private boolean m_enableTransformation = false;

   /**
    * See {@link #isFromXml()} for details. Defaults to <code>false</code>.
    */
   private boolean m_fromXml = false;

   /**
    * See {@link #setUserCustomizable(boolean)} for description.
    * <p>Read and set w/ standard boolean accessor/mutator only. 
    */
   private boolean m_userCustomizable = true;
   
   /**
    * See {@link #getDefaultSearchLabel()} for details. Never <code>null</code>,
    * may be empty. Modified only by {@link #setDefaultSearchLabel(String)}. 
    * Always stored w/ no leading/trailing whitespace.
    */
   private String m_defaultSearchLabel = "";

   /**
    * An array of XML attribute values for all boolean attributes. They are
    * ordered as <code>true</code>, <code>false</code>.
    */
   private static final String[] BOOLEAN_ENUM =
   {
      "yes", "no"
   };

   /* xml node/attribute names - the first 3 are package so they can be used 
    * for backwards compatibility.
    */
   static final String USER_SEARCHABLE_ATTR = "userSearchable";
   static final String USER_CUSTOMIZABLE_ATTR = "userCustomizable";
   static final String DEF_SEARCH_LABEL_ATTR = "defaultSearchLabel";
   private static final String ENABLE_TRANSFORMATION_ATTR = 
         "enableTransformation";
   private static final String GLOBAL_QUERY_VISIBLE_ATTR = 
         "visibleToGlobalQuery";
   private static final String SEARCH_TOKEN_ATTR = "tokenizeSearchContent";
}
