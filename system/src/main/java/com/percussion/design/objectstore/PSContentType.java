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
package com.percussion.design.objectstore;

import com.percussion.cms.objectstore.PSCmsObject;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

/**
 * Represents a content type from the content management system.
 */
public class PSContentType extends PSComponent
{
   /**
    * Initializes a newly created <code>PSContentType</code> object, from
    * an XML representation.  See {@link #toXml(Document)} for the format.
    *
    * @param sourceNode the XML element node to construct this object from.
    *    Cannot be <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException if the XML representation is not
    *    in the expected format
    */
   public PSContentType(Element sourceNode)
         throws PSUnknownNodeTypeException
   {
      if (null == sourceNode)
         throw new IllegalArgumentException("sourceNode cannot be null");
      fromXml(sourceNode, null, null);
   }

   /**
    * Initializes a newly created <code>PSContentType</code> object with the
    * specified name.  The id will be designated {@link #NOT_ASSIGNED}. The
    * object type defaults to <code>PSCmsObject.TYPE_ITEM</code>.
    *
    * @param name this string will be assigned as the content type name; should
    * be unique across all content types in the system; cannot be <code>null
    * </code> or empty.
    */
   public PSContentType(String name)
   {
      setDbId( NOT_ASSIGNED );
      if (null == name || name.trim().length() == 0)
         throw new IllegalArgumentException("name cannot be null or empty");
      setName( name );
      m_objectType = PSCmsObject.TYPE_ITEM;
   }


   /**
    * This method is called to populate an object from an XML
    * element node. An element node may contain a hierarchical structure,
    * including child objects. The element node can also be a child of
    * another element node.  See {@link #toXml(Document)} for the format.
    *
    * @param sourceNode element with name specified by {@link #XML_NODE_NAME}
    * @param parentDoc ignored.
    * @param parentComponents ignored.
    * @throws PSUnknownNodeTypeException  if an expected XML element is missing,
    *    or <code>null</code>
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
      List parentComponents) throws PSUnknownNodeTypeException
   {
      validateElementName( sourceNode, XML_NODE_NAME );

      PSXmlTreeWalker tree = new PSXmlTreeWalker( sourceNode );

      // get the required elements
      setDbId( Integer.parseInt( getRequiredElement( tree, "id" ) ) );
      setName( getRequiredElement( tree, "name" ) );
      String objectType = sourceNode.getAttribute(OBJECT_TYPE_ATTR);
      if (objectType == null)
      {
         Object[] args =
         {
            XML_NODE_NAME,
            OBJECT_TYPE_ATTR,
            "null"
         };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
      
      try
      {
         int type = Integer.parseInt(objectType);
         if (!PSCmsObject.isValidType(type))
         {
            Object[] args =
            {
               XML_NODE_NAME,
               OBJECT_TYPE_ATTR,
               "" + type
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }
         else
            m_objectType = type;
      }
      catch (NumberFormatException e)
      {
         Object[] args =
         {
            XML_NODE_NAME,
            OBJECT_TYPE_ATTR,
            "invalid number"
         };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }

      // get the optional elements
      setDescription( tree.getElementData( "description" ) );
      setNewURL( tree.getElementData( "newurl" ) );
      setQueryURL( tree.getElementData( "queryurl" ) );
      String hideFromMenu = sourceNode.getAttribute(HIDE_FROM_MENU_ATTR);
      if (hideFromMenu != null)
         m_hideFromMenu = (hideFromMenu.equals(XML_TRUE)) ? false : true;
   }


   /**
    * This method is called to create an XML element node with the
    * appropriate format for the given object. An element node may contain a
    * hierarchical structure, including child objects. The element node can
    * also be a child of another element node.
    * <p>
    * The format is defined by <code>getContentTypes.dtd</code> of the <code>
    * sys_psxContentEditorCataloger</code> Rhythmyx application:
    * <pre><code>
    * &lt;!ELEMENT PSXContentType (id, name, description, newurl, queryurl)&gt;
    * &lt;!ATTLIST PSXContentType
    *    objectType CDATA #REQUIRED
    *    hideFromMenu CDATA #IMPLIED
    * &gt;
    * &lt;!ELEMENT id (#PCDATA)&gt;
    * &lt;!ELEMENT name (#PCDATA)&gt;
    * &lt;!ELEMENT description (#PCDATA)&gt;
    * &lt;!ELEMENT newurl (#PCDATA)&gt;
    * &lt;!ELEMENT queryurl (#PCDATA)&gt;
    * </code></pre>
    *
    * @param doc The XML document being constructed, needed to create new
    *    elements.  Cannot be <code>null</code>.
    * @return the newly created XML element node
    */
   public Element toXml(Document doc)
   {
      if (null == doc)
         throw new IllegalArgumentException("Must provide a valid Document");
      
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(OBJECT_TYPE_ATTR, "" + m_objectType);
      root.setAttribute(HIDE_FROM_MENU_ATTR, 
         m_hideFromMenu ? XML_TRUE : XML_FALSE);
      PSXmlDocumentBuilder.addElement(doc, root, "id",
         String.valueOf( getDbId() ) );
      PSXmlDocumentBuilder.addElement(doc, root, "name", m_name );
      PSXmlDocumentBuilder.addElement(doc, root, "description", m_description );
      PSXmlDocumentBuilder.addElement(doc, root, "newurl", m_newURL );
      PSXmlDocumentBuilder.addElement(doc, root, "queryurl",  m_queryURL );
      
      return root;
   }


   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component.
    *
    * @param c a valid PSContentType. Cannot be <code>null</code>.
    */
   public void copyFrom(PSComponent c)
   {
      super.copyFrom( c );
      if (! (c instanceof PSContentType) )
         throw new IllegalArgumentException("invalid object for copy");

      PSContentType type = (PSContentType) c;
      setDbId( type.getDbId() );
      setName( type.getName() );
      setDescription( type.getDescription() );
      m_queryURL = type.m_queryURL;
      m_newURL = type.m_newURL;
      m_objectType = type.m_objectType;
      m_hideFromMenu = type.m_hideFromMenu;
   }


   /**
    * @return a string representation of the object, its name
    * @see #getName
    */
   public String toString()
   {
      return getName();
   }


   /**
    * @return unique database identifier (primary key) for this content type,
    * or {@link #NOT_ASSIGNED}
    */
   public int getDbId()
   {
      return m_dbId;
   }


   /**
    * Sets the unique database identifier (primary key) for this content type.
    * This method is protected because it is expected that the database will
    * assign this id.
    *
    * @param dbId identifier to use
    */
   protected void setDbId(int dbId)
   {
      m_dbId = dbId;
   }


   /**
    * @return the description assigned to this content type.
    *         May be <code>null</code>.
    */
   public String getDescription()
   {
      return m_description;
   }


   /**
    * @return the name assigned to this content type.  Never <code>null</code>
    *         or empty.
    */
   public String getName()
   {
      return m_name;
   }


   /**
    * Sets the description of this content type.
    *
    * @param description may be <code>null</code> or empty
    */
   public void setDescription(String description)
   {
      m_description = description;
   }


   /**
    * Sets the name of this content type.  This name should be unique across
    * all content types.
    *
    * @param name a unique string to assign
    */
   public void setName(String name)
   {
      if (null == name || name.trim().length() == 0)
         throw new IllegalArgumentException("name cannot be null or empty");
      m_name = name;
   }


   /**
    * Sets the relative URL to the Rhythmyx resource (usually a content editor)
    * that will be used to query content items registered as this content type.
    *
    * @param queryURL relative URL of a Rhythmyx resource that can query items
    * of this type; may be <code>null</code> or empty.
    */
   public void setQueryURL(String queryURL)
   {
      m_queryURL = queryURL;
   }


   /**
    * Gets the relative URL of the Rhythmyx resource (usually a content editor)
    * used to query content items registered as this content type.
    *
    * @return relative URL of a Rhythmyx resource that can query items
    * of this type; may be <code>null</code> or empty.
    */
   public String getQueryURL()
   {
      return m_queryURL;
   }


   /**
    * Sets the relative URL to the Rhythmyx resource (usually a content editor)
    * that will be used to create content items for this content type.
    *
    * @param newURL relative URL of a Rhythmyx resource that can create items
    * of this type; may be <code>null</code> or empty.
    */
   public void setNewURL(String newURL)
   {
      m_newURL = newURL;
   }


   /**
    * Gets the relative URL of the Rhythmyx resource (usually a content editor)
    * used to create content items for this content type.
    *
    * @return relative URL of a Rhythmyx resource that can create items
    * of this type; may be <code>null</code> or empty.
    */
   public String getNewURL()
   {
      return m_newURL;
   }

   /**
    * Get the object type.
    * 
    * @return one of the <code>PSCmsObject.TYPE_xxx</code> values.
    */
   public int getObjectType()
   {
      return m_objectType;
   }
   
   /**
    * Set the object type.
    * 
    * @param type the new object type, must be one of the 
    *    <code>PSCmsObject.TYPE_xxx</code> values.
    */
   public void setObjectType(int type)
   {
      if (!PSCmsObject.isValidType(type))
         throw new IllegalArgumentException(
            "type must be one of PSCmsObject.TYPE_xxx values");
      
      m_objectType = type;
   }
   
   /**
    * Is this content type hidded from the menu?
    * 
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isHideFromMenu()
   {
      return m_hideFromMenu;
   }

   /**
    * Name of parent XML element
    */
   public static final String XML_NODE_NAME = "PSXContentType";

   /**
    * Indicates that this content type has not been assigned an ID.
    */
   public static final int NOT_ASSIGNED = -1;

   /**
    * Description of this content type (optional).
    */
   private String m_description = null;

   /**
    * Unique name of this content type.  Set in the constructor and never
    * <code>null</code> or empty after that.
    */
   private String m_name;

   /**
    * Relative URL to Rhythmyx application that can query and edit content
    * items of this content type.  Format is
    * "../&lt;<i>application_name</i>>/&lt;<i>resource_name</i>>.html"
    */
   private String m_queryURL = null;

   /**
    * Relative URL to Rhythmyx application that can create content items of
    * this content type.  Format is
    * "../&lt;<i>application_name</i>>/&lt;<i>resource_name</i>>.html"
    */
   private String m_newURL = null;

   /**
    * Unique database identifier for this content type.
    */
   private int m_dbId;
   
   /**
    * The object type of this content type, initialized during construction,
    * never changed after that. Must be one of the 
    * <code>PSCmsObject.TYPE_xxx</code> values.
    */
   private int m_objectType = -1;
   
   /**
    * A flag that indicates if this content type is hidden or not from menues.
    * Initialized during construction, defaults to <code>false</code> if not
    * supplied.
    */
   private boolean m_hideFromMenu = false;
   
   // XML document constants
   private static final String OBJECT_TYPE_ATTR = "objectType";
   private static final String HIDE_FROM_MENU_ATTR = "hideFromMenu";
   private final static String XML_TRUE = "1";
   private final static String XML_FALSE = "0";
}
