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

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Iterator;

/**
 * The class represents a collection of ACL entries. Each ACL entries represents
 * a level of access for a particular user, role or virtual entry
 * (Folder Community, Everyone) on a specified securable object such as
 * a "folder".
 * This class acts as a Set even though it does not implement the
 * <code>Set</code> interface.
 * A <code>PSObjectAclEntry</code> object is considered a duplicate if it
 * represents permissions for the same user, role, or virtual entry as another
 * existing object in this collection.
 * Adding a duplicate <code>PSObjectAclEntry</code> returns <code>false</code>
 * from the <code>add()</code> method and the collection does not change.
 * Adding a collection of <code>PSObjectAclEntry</code> objects using
 * <code>addAll()</code> method returns <code>true</code> even if the
 * collection being added contains objects already existing in this collection.
 * However the resulting collection does not contain any duplicate entries.
 * <p>
 * This class needs to extend <code>PSDbComponentSet</code> to use the
 * functionality of storing state (new, modified, unmodified, markedfordelete)
 * of ACL entries. The state of ACL entries is used while serializing it to the
 * database (in the <code>PSServerFolderProcessor</code> class). Each ACL entry
 * in this collection should be in one of the DBSTATE_xxx state.
 */
public class PSObjectAcl extends PSDbComponentSet
{
   /**
    * The default constructor to create an empty ACL.
    */
   public PSObjectAcl()
   {
      super(PSObjectAclEntry.class);
   }

   /**
    * Constructs this object from the supplied element. See {@link
    * #toXml(Document) } for the expected form of xml.
    *
    * @param element the element to load from, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if element is <code>null</code>
    * @throws PSUnknownNodeTypeException if element is not of expected format.
    */
   public PSObjectAcl(Element element)
      throws PSUnknownNodeTypeException
   {
      super(element);
   }

   /**
    * Constructs this object from the supplied element.
    * See {@link #toXml(Document)} for the expected form of xml.
    *
    * @param src the element to load from, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if <code>src</code> is
    * <code>null</code>
    * @throws PSUnknownNodeTypeException if element is not of expected format.
    */
   public void fromXml(Element src)
      throws PSUnknownNodeTypeException
   {
      super.fromXml(src);
   }

   /**
    * Gets the specified ACL entry with the specified name and type.
    * 
    * @param name the name of the specified ACL entry. It may not be 
    *    <code>null</code> or emtpy. The comparison is case insensitive.
    * @param type the specified ACL type. 
    * 
    * @return the specified ACL entry. It may be <code>null</code> if the
    *    specified ACL entry does not exist.
    */
   public PSObjectAclEntry getAclEntry(String name, int type)
   {
      Iterator entries = iterator();
      PSObjectAclEntry entry;
      while (entries.hasNext())
      {
         entry = (PSObjectAclEntry) entries.next();
         if (entry.getName().equalsIgnoreCase(name) &&
               entry.getType() == type)
         {
            return entry;
         }
      }
      
      return null;
   }
   
   /**
    * Produce XML representation of this object.
    * The xml format is:
    * <p>
    *
    * &lt;!ELEMENT PSXObjectAcl (PSXObjectAclEntry+)><br/>
    *
    * <p>
    * See {@link com.percussion.cms.objectstore.PSObjectAclEntry#toXml(Document)}
    * for the DTD of PSXObjectAclEntry element.
    *
    * @param doc the Xml document to use for creating elements, may not be
    * <code>null</code>
    *
    * @return the element containing the Xml representation of this object,
    * never <code>null</code>.
    *
    * @throws IllegalArgumentException if doc is <code>null</code>
    */
   public Element toXml(Document doc)
   {
      return super.toXml(doc);
   }

   /**
    * The name of the element returned by the <code>toXml</code> and
    * expected by the <code>fromXml</code> methods.
    *
    * @return A name valid for an xml element name. Never empty or
    * <code>null</code>.
    */
   public String getNodeName()
   {
      return XML_NODE_NAME;
   }

   /**
    * Compares the specified object with this object.
    *
    * @param obj the object which should be compared with this object, may
    * not be <code>null</code>
    *
    * @return <code>true</code> if the specified object is an instance of this
    * class and contains same number of entries and for the same users, role
    * and virtual entries.
    *
    * @throws IllegalArgumentException if <code>obj</code> is
    * <code>null</code>
    */
   public boolean equals(Object obj)
   {
      boolean equals = super.equals(obj);
      if (equals)
      {
         if (!(obj instanceof PSObjectAcl))
            equals = false;
      }
      return equals;
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return super.hashCode();
   }

   /**
    * Returns an iterator over a collection of deleted ACL entries.
    *
    * @return An iterator with zero or more <code>PSObjectAclEntry</code>
    * objects. Never <code>null</code>, but may be empty.
    */
   public Iterator getDeletedAclEntries()
   {
      return getDeleteCollection().iterator();
   }

   /**
    * The constant to indicate root node name.
    */
   public static final String XML_NODE_NAME = "PSXObjectAcl";

}



