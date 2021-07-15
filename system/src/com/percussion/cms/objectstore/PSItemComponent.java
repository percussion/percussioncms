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

import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;

import java.util.Collection;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * All IPSDbComponents associated with an item must derive from this class.
 * It requires additional functionality that is unique to the way the item
 * is processed.
 *
 * @author Paul Howard
 * @version 1.0
 */
public abstract class PSItemComponent extends PSDbComponent
{
   //todo: remove and clean up derived classes
   protected PSItemComponent()
   {
      super(new PSLocator());
   }

   /**
    * See {@link PSDbComponent#PSDbComponent(PSKey) base class} for a
    * description.
    */
   protected PSItemComponent(PSLocator locator)
   {
      super(locator);
   }

   /**
    * See {@link PSDbComponent#getLookupName() base class} for a description
    */
   protected String getLookupName()
   {
      return "CONTENT";
   }

   /**
    * Generates an element that conforms to the schema specified in
    * sys_StandardItem.xsd. This is equivalent to calling {@link
    * #toXml(Document,PSAcceptElements) toXml(doc, null)}.
    * <p>See the {@link IPSDbComponent#toXml(Document) interface} for more
    * complete details.
    */
   public Element toXml(Document doc)
   {
      return toXml(doc, null);
   }

   /**
    * Similar to the 1 param form of this method, except it allows certain
    * parts of the component to be excluded from the generated element.
    *
    * @param doc
    *
    * @param acceptElements the rules to determine which elements will be
    *    included in the generated node.
    *
    * @return the newly created XML element, never <code>null</code>.
    */
   protected abstract Element toXml(
      Document doc,
      PSAcceptElements acceptElements);

   /**
    * Not implemented.
    */
   public void fromXml(Element src) throws PSUnknownNodeTypeException
   {
      throw new UnsupportedOperationException("Not supported for item components");
   }

   /**
    * This method is called to populate an object from its XML representation.
    * It assumes that the object may already have a complete data structure,
    * therefore method only overlays the data onto the existing object.
    * An element node may contain a hierarchical structure, including child
    * objects. The element node can also be a child of another element node.
    *
    * @param sourceNode   the XML element node from which to populate.  Must not
    *    be <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException if the XML element node does not
    *    represent a type supported by this class.
    */
   public abstract void loadXmlData(Element sourceNode)
      throws PSUnknownNodeTypeException;

   /**
    * Not implemented
    */
   public Element toDbXml(Document parm1)
   
   {
      throw new UnsupportedOperationException("Not supported by items");
   }

   /**
    * This toXml's all objects in the collection and appends the element
    * returned by the call to <code>el</code>. Collections are of type
    * <code>PSDbComponent</code>, this simply iterates through the collection
    * calling toXml.
    *
    * @param el - must not be <code>null</code>.
    * @param doc - must not be <code>null</code>.
    * @param col - must not be <code>null</code>, must contain objects of
    * <code>PSDbComponent</code>
    * @param accept - this applies to the elements for which there may be
    * special rules to determine inclusion in the returned element.  For
    * example a <code>PSItemField</code> may or may not include field value
    * depending on the context.  Binaries may not automatically loaded into the
    * field and may require an explicit load.  However, binaries may be saved
    * to the system with all other fields values.  Therefore there needs to be
    * a way to allow inclusion and exclusion of field values in XML and
    * this parameters value is what can determine if the value will be loaded.
    */
   protected static void toXmlCollection(
      Element el,
      Document doc,
      Collection col,
      PSAcceptElements accept)
   {
      if (el == null || doc == null || col == null)
         throw new IllegalArgumentException("arguments must not be null");

      Iterator it = col.iterator();
      PSItemComponent comp = null;
      while (it.hasNext())
      {
         Object o = it.next();
         if (!(o instanceof PSItemComponent))
            throw new IllegalArgumentException("Collection must contain only IPSItemComponent values");

         comp = (PSItemComponent)o;
         el.appendChild(comp.toXml(doc, accept));
      }
   }

   /**
    * Creates an element with the namespace that is defined in 
    * <code>sys_StandardItem.xsd</code>.
    * 
    * @param doc THe document used to create the element, must not be
    *    <code>null</code>. 
    * @param name The name of the created element, must not be 
    *    <code>null</code> or empty.
    * 
    * @return The created element, never <code>null</code>.
    */
   public static Element createStandardItemElement(
      Document doc,
      String name)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc must not be null");
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name must not be null or empty");
         
      return doc.createElementNS(
         NS_STANDARDITEM,
         NS_STANDARDITEM_PREFIX + ":" + name);
   }

   /**
    * Just like {@link #createStandardItemElement(Document, String)}, except
    * this is used to create a root (or top) element for the standard item. 
    */
   public static Element createStandardItemRootElement(
      Document doc,
      String name)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc must not be null");
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name must not be null or empty");
        
      // should not call PSXmlDocumentBuilder.createRoot(), since this 
      // may not be the element of the supplied document
      Element root = createStandardItemElement(doc, name);
      root.setAttribute("xmlns:" + NS_STANDARDITEM_PREFIX, NS_STANDARDITEM);
      return root; 
   }

   /**
    * All the elements created live in the standard item schema, 
    * sys_StandardItem.xsd, therefore we need to define the standard item 
    * namespace to be delivered when we create this element.
    */
   private static final String NS_STANDARDITEM =
      "urn:www.percussion.com/webservices/standarditem";

   private static final String NS_STANDARDITEM_PREFIX = "si";

}
