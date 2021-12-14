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

import com.percussion.design.objectstore.server.PSDatabaseComponentLoader;
import com.percussion.util.PSCollection;
import com.percussion.util.PSIteratorUtils;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Iterator;
import java.util.List;

/**
 * The PSDatabaseComponentCollection class is responsible for encapsulating
 * collections of PSDatabaseComponents and maintaining state information on
 * additions, deletions, or modifications to the collection so that the back end
 * database can be updated appropriately.  Note: The interface to this
 * "collection" most closely resembles a subset of "List".
 */
public class PSDatabaseComponentCollection extends PSDatabaseComponent
{
   /**
    * Construct a collection of database components.
    * 
    * @param c The class name of the database component to be stored in this
    * collection.  This class must implement an empty constructor that this
    * class can access the constructor to.  May not be <code>null</code>.
    *
    * @param datasetName The dataset name to be used to retrieve this
    *    collection of components.  May not be <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if any argument is invalid.
    */
   PSDatabaseComponentCollection(Class c, String datasetName)
   {
      if (c == null || datasetName == null || datasetName.length() < 1)
         throw new IllegalArgumentException(
            "Class and resource name must be specified.");

      m_components = new PSCollection(c);
      m_deletes = new PSCollection(c);
      
      m_resourceName = datasetName;
   }
   
   /**
    * Get an iterator over the database components available in this 
    * object.  The iterator will be non-destructive (remove() is not allowed).
    *
    * @return The iterator, never <code>null</code>.
    */
   public Iterator iterator()
   {
      return PSIteratorUtils.protectedIterator(m_components.iterator());
   }
   
   /**
    * The method is used to retrieve deletes from a collection. ph: can you be a little clearer, what are 'deletes'?
    * It is package level so that only the objectstore can access this
    * when needed.
    *
    * @return The list (ph: not a list) of deletes.  May be empty.  Never <code>null</code>.
    */
   Iterator deletes()
   {
      return PSIteratorUtils.protectedIterator(m_deletes.iterator());
   }
   
   /**
    * Add all the components in this component collection to the specified
    * element.  This is accomplished by calling <code>toXml()</code> on all
    * contained components.  This method is a convenience for classes which
    * extend this class, but do not desire to have the collection component's
    * XML element, defining their own instead (by overriding 
    * {@link #toXml(Document)} ).
    *
    * @param collectionRoot The element to append the components' xml to.
    * May not be <code>null</code>.
    *
    * @param doc The document that is being built.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException If any parameter is invalid.
    */
   void addCollectionComponents(Element collectionRoot, Document doc)
   {
      if (collectionRoot == null)
         throw new IllegalArgumentException("Root element must be specified.");
         
      if (doc == null)
         throw new IllegalArgumentException("Document must be specified.");

      // First do the collection proper
      Iterator i = m_components.iterator();
      while (i.hasNext())
      {
         IPSDatabaseComponent component = (PSDatabaseComponent) i.next();
         collectionRoot.appendChild(component.toXml(doc));
      }
      i = m_deletes.iterator();
      while (i.hasNext())
      {
         IPSDatabaseComponent component = (PSDatabaseComponent) i.next();
         collectionRoot.appendChild(component.toXml(doc));
      }
   }
   
   /**
    * This method is called to create an XML element node with the
    * appropriate format for the given object. An element node may contain a
    * hierarchical structure, including child objects. The element node can
    * also be a child of another element node.
    *
    * @param doc The document being built.  May not be <code>null</code>.
    *
    * @return    the newly created XML element node
    *
    * @throws IllegalArgumentException If doc is <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("Document must be specified.");

      Element   root = doc.createElement(ms_NodeType);

      //root.setAttribute(RESOURCE_NAME_ATTRIBUTE_NAME, m_resourceName);
      // we don't save m_resourceName because it is always provided in the ctor

      addCollectionComponents(root, doc);

      return root;
   }

   /**
    * Set this collection to an unchanged state.  Call this method after
    * creating a collection from the database and using 
    * {@link #add(PSDatabaseComponent)} to insert components to reset each
    * individual component's state.
    */
   void setUnchanged()
   {
      super.setUnchanged();
      
      m_deletes.clear();
      Iterator i = iterator();
      while (i.hasNext())
      {
         PSDatabaseComponent c = (PSDatabaseComponent) i.next();
            c.setUnchanged();
      }
   }
   
   /**
    * This method is called to populate an object from an XML
    * element node. An element node may contain a hierarchical structure,
    * including child objects. The element node can also be a child of
    * another element node.
    *
    * @exception PSUnknownNodeTypeException  if the XML element node does not
    *                                           represent a type supported
    *                                           by the class.
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc, 
                        List parentComponents)
      throws PSUnknownNodeTypeException
   {
      m_components.clear();
      m_deletes.clear();
      
      if (sourceNode == null)
      {
         throw new PSUnknownNodeTypeException(
         IPSObjectStoreErrors.XML_ELEMENT_NULL, ms_NodeType);
      }

      PSXmlTreeWalker   tree = new PSXmlTreeWalker(sourceNode);

      try
      {
         // Get our class (for instantiation)
         Class c = m_components.getMemberClassType();

         int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
         firstFlags |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

         int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;
         nextFlags |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

         for (Element e = tree.getNextElement(firstFlags); e != null;
               e = tree.getNextElement(nextFlags))
         {
            IPSDatabaseComponent component = (IPSDatabaseComponent) c.newInstance();
            component.fromXml(e, parentDoc, parentComponents);
            if (component.isDelete())
            {
               m_deletes.add(component);
            }
            else
            {
               m_components.add(component);
            }
         }
      } catch (InstantiationException instE)
      {
         // Should never happen here, fail fast.
         throw new UnknownError("Can't inst collection component!  Reason: "+
            instE.toString());
      } catch (IllegalAccessException accE)
      {
         // Should never happen here, fail fast.
         throw new UnknownError("Can't access constructor!  Reason: "+
            accE.toString());
      }
   }

   /**
    * Remove the specified component from this component collection.
    *
    * @param component The component to remove, can't be <code>null</code>.
    *
    * @throws IllegalArgumentException If component is <code>null</code>.
    */
   public void remove(PSDatabaseComponent component)
   {
      if (component == null)
         throw new IllegalArgumentException("Component must be specified!");
         
      for (int i = 0; i < size(); i++)
      {
         PSDatabaseComponent comp = get(i);
         if (comp == component)
         {
            removeElementAt(i);
            break;
         }
      }
   }
   
   /**
    * Override {@link PSDatabaseComponent#setDelete()} to 
    * inform our members that they are deletes as well.
    */
   protected void setDelete()
   {
      super.setDelete();
      for (int i = size() - 1; i >= 0 ; i--)
      {
         removeElementAt(i);
      }
   }


   /**
    * Loads this object from the supplied element using {@link #fromXml}
    * then recursively loads every one of the newly created objects
    * using the supplied loader.  See {@link
    * PSDatabaseComponent#fromDatabaseXml} for more information.
    */
   public void fromDatabaseXml(Element source, PSDatabaseComponentLoader cl,
      PSRelation relationContext)
      throws PSUnknownNodeTypeException, PSDatabaseComponentException
   {
      if (source == null || cl == null)
         throw new IllegalArgumentException("one or more params is null");

      PSXmlTreeWalker   tree = new PSXmlTreeWalker(source);

      try
      {
         // Get our class (for instantiation)
         Class c = m_components.getMemberClassType();

         int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
         firstFlags |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

         int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;
         nextFlags |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

         for (Element e = tree.getNextElement(firstFlags); e != null;
               e = tree.getNextElement(nextFlags))
         {
            PSRelation myCtx = null;
            if ( null != relationContext )
               myCtx = (PSRelation)relationContext.clone();
            IPSDatabaseComponent component = (IPSDatabaseComponent)
                  c.newInstance();
            component.fromDatabaseXml(e, cl, myCtx );
            m_components.add(component);
         }
      }
      catch (InstantiationException instE)
      {
         // Should never happen here, fail fast.
         throw new UnknownError("Can't inst collection component!  Reason: "+
            instE.toString());
      }
      catch (IllegalAccessException accE)
      {
         // Should never happen here, fail fast.
         throw new UnknownError("Can't access constructor!  Reason: "+
            accE.toString());
      }
   }


   /**
    * Adds the supplied component to this collection without tracking inserts
    * and deletes. This is only for use while rebuilding objects from the
    * database.
    *
    * @param c A valid component with a type that matches the type this
    *    collection was made with. Never <code>null</code>.
    *
    * @throws IllegalArgumentException if c is <code>null</code>.
    */
   void addFromDb( PSDatabaseComponent c )
   {
      if (c == null)
         throw new IllegalArgumentException("Component must be supplied.");

      m_components.add(c);
   }

   // See base class
   public void toDatabaseXml(Document doc, 
      Element actionRoot, 
      PSRelation relationContext) throws PSDatabaseComponentException
   {
      Iterator i = m_components.iterator();
      while (i.hasNext())
      {
         IPSDatabaseComponent component = (IPSDatabaseComponent) i.next();
         component.toDatabaseXml(doc, actionRoot, 
            (PSRelation) relationContext.clone());
      }
      i = m_deletes.iterator();
      while (i.hasNext())
      {
         IPSDatabaseComponent component = (IPSDatabaseComponent) i.next();
         component.toDatabaseXml(doc, actionRoot, 
            (PSRelation) relationContext.clone());
      }
   }

   // See base class
   public String getDatabaseAppQueryDatasetName()
   {
      return m_resourceName;
   }
   
   /**
    * Override method to return <code>null</code> since top level collections
    * (example global subjects) do not have keys.  This can be overridden by
    * classes extending this object in the future.
    *
    * @return <code>null</code> always.
    */
   public String getDatabaseComponentIdKeyName()
   {
      return null;
   }

   /**
    * Get the size of this component collection.
    *
    * @return the number of objects.
    */
   public int size()
   {
      return m_components.size();
   }
   
   /**
    * Get the component at the specified index.
    *
    * @return the component
    */
   public PSDatabaseComponent get(int index)
   {
      return (PSDatabaseComponent) m_components.get(index);
   }
   
   /**
    * Add the specified component to this collection.
    *
    * @param component the component to add, may not be <code>null</code>,
    * must be a newly constructed component.
    *
    * @throws IllegalArgumentException If a component not newly constructed
    * is supplied.
    */
   public void add(PSDatabaseComponent component)
   {
      if (component == null)
         throw new IllegalArgumentException("Component must be supplied.");

      if (!component.isInsert())
         throw new IllegalArgumentException(
            "Added components must be newly created!");
         
      m_components.add(component);
   }
   
   /**
    * Validates this object within the given validation context. The method
    * signature declares that it throws PSSystemValidationException, but the
    * implementation must not directly throw any exceptions. Instead, it
    * should register any errors with the validation context, which will
    * decide whether to throw the exception (in which case the implementation
    * of <CODE>validate</CODE> should not catch it unless it is to be
    * rethrown).
    * 
    * @param   cxt The validation context.
    * 
    * @throws PSSystemValidationException According to the implementation of the
    * validation context (on warnings and/or errors).
    */
   public void validate(IPSValidationContext cxt)
      throws PSSystemValidationException
   {
      if (cxt == null)
         return;
         
      for (int i = 0; i < size(); i++)
      {
         Object o = get(i);
         if (o instanceof IPSComponent)
         {
            IPSComponent comp = (IPSComponent)o;
            comp.validate(cxt);
         }
      }
   }

   /**
    * Remove the element at the specified index.
    *
    * @param index the index of the element to remove.
    *
    * @see PSCollection#remove(int)
    */
   public void removeElementAt(int index)
   {
      PSDatabaseComponent component = 
         (PSDatabaseComponent)m_components.remove(index);

      component.setDelete();
      m_deletes.add(component);
   }

   @Override
   public boolean equals(Object obj)
   {
      if (!(obj instanceof PSDatabaseComponentCollection))
         return false;
      
      PSDatabaseComponentCollection t = (PSDatabaseComponentCollection) obj;

      return new EqualsBuilder()
         .append(m_components, t.m_components)
         .append(m_deletes, t.m_deletes)
         .append(m_resourceName, t.m_resourceName).isEquals();
   }

   @Override
   public int hashCode()
   {
      return new HashCodeBuilder()
         .append(m_components)
         .append(m_deletes)
         .append(m_resourceName).toHashCode();
   }
   
   /**
    * The internal collection of components.  Initialized at construction
    * time, never <code>null</code> after that.  May be empty.
    */
   private PSCollection m_components = null;

   /**
    * The internal collection for storing deleted components.  
    * Initialized at construction
    * time, never <code>null</code> after that.  May be empty.
    */
   private PSCollection m_deletes = null;
   
   /**
    * The resource name to use to retrieve this collection of components.
    * Initialized at construction time, never <code>null</code> or empty
    * after that.
    */
   private String m_resourceName;

   /**
    * The name of the attribute the resource name is stored in.
    */
   private static final String RESOURCE_NAME_ATTRIBUTE_NAME = "resourceName";

   /* package access on this so they may reference each other in fromXml */
   static final String      ms_NodeType = "PSXDatabaseComponentCollection";
}
