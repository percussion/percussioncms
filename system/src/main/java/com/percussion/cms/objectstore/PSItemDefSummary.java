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

import com.percussion.design.objectstore.IPSDocument;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

/**
 * An item definition contains all of the design elements that define what an
 * item is. (May also loosely be called a content type. A new term was used
 * to (hopefully) avoid confusion.) This class provides some key pieces of
 * information about a type that is useful when building UIs. Once the user has
 * selected one of these, the id can be used to get further information.
 * <p>This class doesn't override the clone method because the clone provided
 * by the Object base class is satisfactory.
 */
public class PSItemDefSummary extends PSCmsComponent implements
   IPSCatalogSummary
{
   /**
    * Creates an instance.
    *
    * @param name The unique name for the associated item definition. Never
    *    <code>null</code> or empty.
    * 
    * @param label a display label for the associated item definition. Never
    *    <code>null</code>, may be empty.   
    *
    * @param typeId The unique numeric identifier for the associated item
    *    definition.
    *
    * @param editorUrl The url where the content editor lives within the 
    * system. Never <code>null</code>.
    *
    * @param description An optional message that describes what the associated
    *    item is used for. May be <code>null</code> or empty.
    */
   public PSItemDefSummary(String name, String label, int typeId,
                           String editorUrl, String description)
   {
      if (null == name || name.trim().length() == 0)
      {
         throw new IllegalArgumentException("name cannot be null or empty");
      }
      if (null == editorUrl)
      {
         throw new IllegalArgumentException("editorUrl cannot be null");
      }
      setName( name );
      setLabel(label);
      setTypeId( typeId );
      setEditorUrl( editorUrl );
      setDescription( description );
   }
   
   /**
    * Creates an instance.
    *
    * @param name The unique name for the associated item definition. Never
    *    <code>null</code> or empty.
    * 
    * @param typeId The unique numeric identifier for the associated item
    *    definition.
    *
    * @param editorUrl The url where the content editor lives within the 
    * system. Never <code>null</code>.
    *
    * @param description An optional message that describes what the associated
    *    item is used for. May be <code>null</code> or empty.
    *    
    * @deprecated use {@link #PSItemDefSummary(String, String, int, String, String)}
    * instead
    */
   public PSItemDefSummary(String name, int typeId,
                           String editorUrl, String description)
   {
      this(name, name, typeId, editorUrl, description);
   }
   
   /**
    * Creates an instance from a previously serialized (using <code>toXml
    * </code>) one.
    *
    * @param source A valid element that meets the dtd defined in the
    *    description of {@link #toXml(Document)}. Never <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException If the supplied source element does
    *    not conform to the dtd defined in the <code>fromXml<code> method.
    */
   public PSItemDefSummary(Element source)
      throws PSUnknownNodeTypeException
   {
      fromXml(source, null, null);
   }

   /**
    *  For use by derived classes. If using this method, the instance must
    *  be populated with data using the fromXml method before it is used.
    */
   protected PSItemDefSummary()
   {}


   /**
    * This is the name of the root element in the serialized version of this
    * object.
    *
    * @return The root name, never <code>null</code> or empty.
    */
   public static String getNodeName()
   {
      return "PSXItemDefSummary";
   }

   /**
    * A numeric value that uniquely identifies a content type. May be used to
    * obtain the associated definition.
    *
    * @return The id that was passed into the ctor or assigned from
    *    serialization.
    */
   public int getTypeId()
   {
      return (int) m_typeId;
   }
   
   /**
    * Get the content type id represented as a guid.
    * 
    * @return The guid, never <code>null</code>.
    */
   public IPSGuid getGUID()
   {
      return new PSGuid(PSTypeEnum.NODEDEF, m_typeId);
   }

   /**
    * The name is a unique identifier for an item definition.
    *
    * @return The name that was passed into the ctor or assigned from
    *    serialization.
    */
   public String getName()
   {
      return m_name;
   }
   
   /**
    * The display name for an item definition.
    *
    * @return The label that was passed into the ctor or assigned from
    *    serialization. Never <code>null</code>, may be empty.
    */
   public String getLabel()
   {
      return m_label;
   }

   /**
    * The url of where this content editor lives.
    *
    * @return The description that was passed into the ctor or assigned from
    *    serialization.
    */
   public String getEditorUrl()
   {
      return m_editorUrl;
   }

   /**
    * A short message that describes what this item is for.
    *
    * @return The description that was passed into the ctor or assigned from
    *    serialization or empty if one wasn't assigned.
    */
   public String getDescription()
   {
      return m_description;
   }

   /**
    * Overrides the base class to compare each of the member properties. All
    * members except the name are compared for exact matches. The name is
    * compared case insensitive.
    *
    * @return <code>true</code> if all members are equal as defined above,
    *    otherwise <code>false</code> is returned.
    */
   @Override
   public boolean equals(Object o)
   {
      if ( !(o instanceof PSItemDefSummary ))
         return false;

      PSItemDefSummary summary = (PSItemDefSummary) o;

      if (m_typeId != summary.m_typeId)
         return false;
      if (!m_name.equalsIgnoreCase(summary.m_name))
         return false;
      if (!m_editorUrl.equals(summary.m_editorUrl))
         return false;
      if (!m_description.equals(summary.m_description))
         return false;
      return true;
   }


   /**
    * Must be overridden to properly fulfill the contract in Object.
    *
    * @return A value computed by concatenating all of the properties into one
    *    string and taking the hashCode of that. The name is lowercased before
    *    it is concatenated.
    */
   @Override
   public int hashCode()
   {
      return (m_name.toLowerCase() + m_description + m_editorUrl
            + m_typeId).hashCode();
   }

   /**
    * Serializes this object into an xml element that can be attached to the
    * supplied document. It will conform to the following dtd:
    * <pre>
    * <ELEMENT PSXItemDefSummary (Description?)>
    * <ATTLIST PSXItemDefSummary
    *    id       CDATA #REQUIRED
    *    name     CDATA #REQUIRED
    *    typeId   CDATA #REQUIRED
    *    >
    * <ELEMENT Description (#PCDATA)>
    * </pre>
    *
    * @param doc Used to generate the element. Never <code>null</code>.
    *
    * @return the generated element, never <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if (null == doc)
         throw new IllegalArgumentException("doc must be supplied");

      Element root  = doc.createElement(getNodeName());

      root.setAttribute ("id", ""+getId());
      root.setAttribute ("name", getName());
      root.setAttribute ("label", getLabel());
      root.setAttribute ("typeId", ""+getTypeId());
      root.setAttribute ("editorUrl", getEditorUrl());

      //Create description element
      if ( getDescription().length() > 0 )
      {
         PSXmlDocumentBuilder.addElement(doc, root, "Description",
               getDescription());
      }
      return root;
   }

   /**
    * Replaces the data in this instance with that in a previously serialized
    * version. If any data is invalid, an exception is thrown.
    *
    * @param source An xml fragment conforming to the dtd described in
    *    {@link #toXml(Document) toXml}. Never <code>null</code>.
    *
    * @param parentDoc Unused.
    *
    * @param parentComponents Unused.
    */
   public void fromXml(Element source,
         @SuppressWarnings("unused") IPSDocument parentDoc,
         @SuppressWarnings("unused") List parentComponents)
      throws PSUnknownNodeTypeException
   {
      if (null == source)
         throw new IllegalArgumentException("sourceNode must be supplied");

      //make sure we got the correct root node tag
      if (false == getNodeName().equals(source.getNodeName()))
      {
         Object[] args = { getNodeName(), source.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      PSXmlTreeWalker walker = new PSXmlTreeWalker(source);
      String temp;
      String attrName = "name";
      temp = walker.getElementData(attrName);
      if ( null == temp || temp.trim().length() == 0 )
      {
         Object[] args = { getNodeName(), attrName, temp };
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args );
      }
      else
         m_name = temp;
      
      attrName = "label";
      temp = walker.getElementData(attrName);
      if ( null == temp )
      {
         m_label = m_name;
      }
      else
      {
         m_label = temp;
      }

      attrName = "typeId";
      temp = walker.getElementData(attrName);
      if ( null == temp || temp.trim().length() == 0 )
      {
         Object[] args = { getNodeName(), attrName, temp };
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args );
      }
      else
      {
         try
         {
            setTypeId(Integer.parseInt(temp));
         }
         catch (NumberFormatException nfe)
         {
            Object[] args = { getNodeName(), attrName, temp };
            throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args );
         }
      }

      attrName = "editorUrl";
      temp = walker.getElementData(attrName);
      if ( null == temp || temp.trim().length() == 0 )
      {
         Object[] args = { getNodeName(), attrName, temp };
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args );
      }
      else
         m_editorUrl = temp;

      String elemName = "Description";
      setDescription(walker.getElementData(elemName));
   }

   /**
    * Provides a common location for business rules associated with this
    * property. All assignment should only be done thru this method, never
    * set the property directly.
    *
    * @param name Never <code>null</code> or empty.
    */
   public void setName( String name )
   {
      if ( null == name || name.trim().length() == 0 )
         throw new IllegalArgumentException("Name must be supplied");

      m_name = name;
   }
   
   /**
    * Sets the label
    * @param label can be <code>null</code> or empty.
    * If <code>null</code> then the label will be set to an empty string.
    */
   public void setLabel(String label)
   {
      if(label == null)
         m_label = "";
      else
         m_label = label;
   }

   /**
    * Provides a common location for business rules associated with this
    * property. All assignment should only be done thru this method, never
    * set the property directly.
    *
    * @param id No validation performed at this time.
    */
   public void setTypeId( int id )
   {
      m_typeId = id;
   }

   /**
    * Provides a common location for business rules associated with this
    * property. All assignment should only be done thru this method, never
    * set the property directly.
    *
    * @param editorUrl May not be <code>null</code>.
    */
   public void setEditorUrl( String editorUrl )
   {
      m_editorUrl = editorUrl;
   }

   /**
    * Provides a common location for business rules associated with this
    * property. All assignment should only be done thru this method, never
    * set the property directly.
    *
    * @param desc May be <code>null</code> or empty. If <code>null</code>,
    *    stored as "".
    */
   public void setDescription( String desc )
   {
      if ( null == desc )
         desc = "";
      m_description = desc;
   }

   /**
    * See {@link #PSItemDefSummary(String, int, String, String) ctor}
    * for description.
    * Set in ctor or <code>toXml</code> method. Never <code>null</code> or
    * empty after set.
    */
   private String m_name;

   /**
    * See {@link #PSItemDefSummary(String, int, String, String) ctor}
    * for description.
    */
   private long m_typeId;

   /**
    * See {@link #PSItemDefSummary(String, int, String, String) ctor}
    * for description.
    * Set in ctor or <code>toXml</code> method, then never <code>null</code>
    * or changed after that (may be empty).
    */
   private String m_editorUrl;

   /**
    * See {@link #PSItemDefSummary(String, int, String, String) ctor}
    * for description.
    * Set in ctor or <code>toXml</code> method, then never <code>null</code>
    * or changed after that (may be empty).
    */
   private String m_description;
   
   /**
    * See {@link #PSItemDefSummary(String, String, int, String, String) ctor}
    * for description.
    * Set in ctor or <code>toXml</code> method, then never <code>null</code>
    * or changed after that (may be empty).
    */
   private String m_label;
}
