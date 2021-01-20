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

import com.percussion.design.objectstore.server.PSDatabaseComponentLoader;
import com.percussion.xml.PSXmlTreeWalker;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The PSAttribute class is used to store attribute values.
 *
 * Attributes are named collections containing PSAttributeValue objects,
 * which are hidden using list interfaces to retrieve and set the values
 * for each specific attribute.
 *
 * @see PSDatabaseComponentCollection
 */
@SuppressWarnings(value={"unchecked"})
public class PSAttribute extends PSDatabaseComponentCollection
{
   /**
    * Construct a Java object from its XML representation. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @param   sourceNode        the XML element node to construct this
    *                               object from, never <code>null</code>
    *
    * @param   parentDoc         the Java object which is the parent of this
    *                               object, may be <code>null</code>
    *
    * @param   parentComponents  the parent objects of this object, may be
    *                               <code>null</code>
    *
    * @throws  PSUnknownNodeTypeException
    *                              if the XML element node is not of the
    *                              appropriate type or null
    */
   public PSAttribute(Element sourceNode,
      IPSDocument parentDoc, ArrayList<?> parentComponents)
      throws PSUnknownNodeTypeException
   {
      // This constructor takes an arraylist because of the specification
      // of fromXml() in IPSComponent
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Constructor for serialization, fromXml, etc.
    */
   PSAttribute()
   {
      super((new PSAttributeValue()).getClass(),
            (new PSAttributeValue()).getDatabaseAppQueryDatasetName());
   }

   /**
    * Constructs an empty attribute.
    *
    * @param   name   the name of the attribute
    *
    * @throws IllegalArgumentException if name is <code>null</code> or empty.
    */
   public PSAttribute(String name)
   {
      this();
      setName(name);
   }

   /**
    * This method is called to create one or more Action XML elements
    * containing the data described in this object that is used to update
    * the database. The Elements are appended to the root of the passed in doc.
    * This method then calls the <code>toDatabaseXml</code> method on any of
    * this object's children.
    * <p>
    * The structure of the XML element(s) appended to the document (using a
    * root element called 'root') is:
    * <pre><code>
    * &lt;!ELEMENT root (Action*)&gt;
    * &lt;!ELEMENT Action (PSXAttribute)
    * &lt;!ATTLIST Action
    * type (INSERT | UPDATE | DELETE) #REQUIRED
    * &gt;
    * &lt;!ELEMENT PSXAttribute (PSXAttributeValue*)&gt;
    * &lt;!ATTLIST PSXAttribute
    *    id CDATA #REQUIRED
    *    DbComponentId CDATA #REQUIRED
    *    name CDATA #REQUIRED
    * &gt;
    * </code></pre>
    *
    * @see PSDatabaseComponentCollection#toDatabaseXml
    */
   public void toDatabaseXml(Document doc,
      Element actionRoot,
      PSRelation relationContext) throws PSDatabaseComponentException
   {
      if (doc == null || actionRoot == null || relationContext == null)
         throw new IllegalArgumentException("one or more params is null");

      // if we are new, generate a new id
      if (isInsert())
         createDBComponentId();

      // Add action element to root
      Element actionElement = getActionElement(doc, actionRoot);
      if (actionElement != null)
      {
         // just toXml ourselves to this root
         actionElement.appendChild(toXml(doc, false));
      }

      // now add our relation to our parent
      relationContext.addKey(getComponentType(), m_databaseComponentId);
      relationContext.m_componentState = m_componentState;
      relationContext.toDatabaseXml(doc, actionRoot, relationContext);

      // now add our values
      PSRelation myCtx = (PSRelation)relationContext.clone();
      super.toDatabaseXml(doc, actionRoot, myCtx);
   }

   /**
    * Loads this object from the supplied element using {@link #fromXml},
    * then calls the super to load the values. See {@link
    * PSDatabaseComponent#fromDatabaseXml} for more information.
    */
   public void fromDatabaseXml(Element e, PSDatabaseComponentLoader cl,
      PSRelation relationContext)
      throws PSUnknownNodeTypeException
   {
      if (e == null || cl == null)
         throw new IllegalArgumentException("one or more params is null");

      // the attribute and all its values in in the supplied node
      fromXml(e, null, null);
      m_componentState = DATABASE_COMPONENT_UNCHANGED;
   }

   /**
    * Get the name of the attribute.
    *
    * @return the name of the attribute, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Set the name of the attribute.
    *
    * @param   name  the name of the attribute.
    * Can't be <code>null</code> or empty
    *
    * @throws  IllegalArgumentException if name is <code>null</code> or empty
    */
   private void setName(String name)
   {
      if ((name == null) || (name.trim().length() < 1))
         throw new IllegalArgumentException("Attribute name must be specified.");
      m_name = name;
   }
   
   /**
    * Test if this attribute is equal to the supplied attribute. Two attributes 
    * are equal if their names, and all values are equal. The order of the
    * values is not important.
    * 
    * @param obj the object to test against, may be <code>null</code>. 
    */
   public boolean equals(Object obj)
   {
      return EqualsBuilder.reflectionEquals(this, obj);
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
   }

   /**
    * Retrieve a comparator to sort and store attributes by their name.
    *
    * @return the comparator, never <code>null</code>.
    */
   public static Comparator<?> getAttributeComparator()
   {
      return new Comparator<Object> ()
      {
         /**
          * We want to sort ignoring case, and that means a Collator (may be 
          * able to improve performance by using CollatorKeys).
          */
         private Collator m_collator = Collator.getInstance();


         public int compare(Object o1, Object o2)
         {
            PSAttribute a1 = (PSAttribute) o1;
            PSAttribute a2 = (PSAttribute) o2;
            
            if (a1 == null)
            {
               if (a2 == null)
                  return 0;
               return -1;
            } else if (a2 == null)
               return 1;

            m_collator.setStrength(Collator.SECONDARY);
            int ret = compare(a1, a2);
            
            // can't return 0 if case is different (or set will think its a dup)
            if (0 == ret)
            {
               m_collator.setStrength(Collator.IDENTICAL);
               ret = compare(a1, a2);
            }
            return ret;
         }
         
         private int compare(PSAttribute a1, PSAttribute a2)
         {
            int ret = m_collator.compare(a1.getName(), a2.getName());
            
            return ret;
         }
      };
   }

   /* **************  IPSComponent Interface Implementation ************** */

   /**
    * This method is called to create a PSXAttribute XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    * &lt;!ELEMENT PSXAttribute (PSXAttributeValue*)&gt;
    * &lt;!ATTLIST PSXAttribute
    *    id CDATA #REQUIRED
    *    DbComponentId CDATA #REQUIRED
    *    componentState CDATA #REQUIRED
    *    name CDATA #REQUIRED
    * &gt;
    * </code></pre>
    *
    * @param doc The parent document.   May not be <code>null</code>.
    *
    * @return     the newly created PSXAttribute XML element node
    *
    * @throws IllegalArgumentException if doc is <code>null</code>
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      return toXml( doc, true );
   }


   /**
    * This method serializes all the properties of this object, optionally
    * including those properties that are PSDatabaseComponent objects. It
    * creates a node with a name unique to this object and adds attributes
    * and child elements to this node, which is returned when complete.
    *
    * @param doc The document to which the returned element will be added.
    *    Assumed not <code>null</code>.
    *
    * @param includeCompChildren A flag to indicate whether to include
    *    properties whose data type is PSDatabaseComponent. They are included
    *    if this is <code>true</code>, otherwise they aren't. It was designed
    *    to set this to <code>true</code> when calling from <code>toXml</code>
    *    and <code>false</code> when calling from <code>toDatabaseXml</code>.
    *    The db children are left out when saving to the db to make the
    *    update work correctly.
    *
    * @return An XML element containing some or all of the properties of this
    *    node, depending on the supplied flag, never <code>null</code>.
    */
   private Element toXml( Document doc, boolean includeCompChildren )
   {
      // create PSXSubject element and add type attribute
      Element root = doc.createElement(ms_NodeType);

      // add id attribute
      root.setAttribute(ID_ATTR, String.valueOf(m_id));

      // add name attribute
      root.setAttribute(NAME_ATTR, m_name);

      // add our db state
      addComponentState(root);

      if ( includeCompChildren )
      {
         // add our values
         addCollectionComponents(root, doc);
      }

      return root;
   }

   /**
    * This method is called to populate a PSAttribute Java object
    * from a PSXAttribute XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @throws   PSUnknownNodeTypeException if the XML element node is not
    *                                        of type PSXRole
    *
    * @see IPSComponent#fromXml(Element, IPSDocument, ArrayList) for the
    * interface description
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                       ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
      {
         throw new PSUnknownNodeTypeException(
         IPSObjectStoreErrors.XML_ELEMENT_NULL, ms_NodeType);
      }

      //make sure we got the Attribute type node
      if (false == ms_NodeType.equals(sourceNode.getNodeName()))
      {
         Object[] args = { ms_NodeType, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      PSXmlTreeWalker   tree = new PSXmlTreeWalker(sourceNode);

      String sTemp = tree.getElementData(ID_ATTR);
      try
      {
         m_id = Integer.parseInt(sTemp);
      } catch (Exception e)
      {
         Object[] args = { ms_NodeType, ID_ATTR ,((sTemp == null) ? "null" : sTemp) };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }

      // Restore our db state
      getComponentState(sourceNode);

      sTemp = tree.getElementData(NAME_ATTR);
      try
      {
         setName(sTemp);
      } catch (IllegalArgumentException e)
      {
         Object[] args = {ms_NodeType, NAME_ATTR, (sTemp == null) ? "null" :
            sTemp};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }

      // just delegate to our collection to get the values
      super.fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Gets the attribute values for this attribute, collected in a List of
    * Strings.
    *
    * @return the value list never <code>null<code>, may be empty.
    */
   public List getValues()
   {
      ArrayList values = new ArrayList();

      for (int i = 0; i < size(); i++)
      {
         PSAttributeValue val = (PSAttributeValue) get(i);
         values.add(val.getValueText());
      }

      return values;
   }

   /**
    * Set the attribute values for this attribute.
    *
    * @param list The list of <code>String</code> values for this attribute,
    * if <code>null</code>, the attribute values for this attribute will be
    * cleared.  If non-string members are included in the list, toString()
    * will be called to obtain a string value to store, though this is strongly
    * discouraged.  Any <code>null</code> members of the list will be ignored.
    */
   public void setValues(List list)
   {
      // Set any old values as deleted and move them to the delete list.
      for (int i = size() -1; i >= 0; i--)
      {
         removeElementAt(i);
      }

      if (list != null)
         for (int i = 0; i < list.size(); i++)
            if (list.get(i) != null)
               add(new PSAttributeValue(list.get(i).toString()));
   }

   public String toString()
   {
      StringBuffer buf = new StringBuffer(this.getName());
      buf.append("[");
      List valueList = this.getValues();
      Iterator iter = valueList.iterator();
      while (iter.hasNext())
      {
         String value = (String) iter.next();
         buf.append(value);
         if (iter.hasNext()) buf.append(",");
      }
      buf.append("]");

      return buf.toString();
   }

   // see IPSComponent interface
   public void validate(IPSValidationContext cxt)
   {
      //component is always valid
   }

   /**
    * Merge the supplied attribute's values into this attribute.
    * 
    * @param attr The attribute to merge, may not be <code>null</code>.
    */
   public void merge(PSAttribute attr)
   {
      if (attr == null)
         throw new IllegalArgumentException("attr may not be null");
      
      // build set of current values
      List curVals = getValues();
      for (int i = 0; i < attr.size(); i++)
      {
         PSAttributeValue val = (PSAttributeValue) attr.get(i);
         String text = val.getValueText();
         if (!curVals.contains(text))
            curVals.add(text);
      }
      setValues(curVals);
   }
   
   /**
    * The name of this attribute, never <code>null</code> or empty.
    */
   private String m_name = "attrib";

   /* package access on this so they may reference each other in fromXml */
   static final String   ms_NodeType            = "PSXAttribute";

   // XML elemenents
   private static final String NAME_ATTR = "name";

}

