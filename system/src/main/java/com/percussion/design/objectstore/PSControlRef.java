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

import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Implementation for the PSXControlRef DTD in BasicObjects.dtd.
 */
public class PSControlRef extends PSComponent
{
   /**
    * Constructs a new <code>PSControlRef</code> as a (shallow) copy of
    * <code>source</code>.
    * @param source provides the initial state for this object, not <code>null
    * </code>.
    */
   protected PSControlRef(PSControlRef source)
   {
      if (null == source)
         throw new IllegalArgumentException("source may not be null");
      this.copyFrom( source ); // make sure we don't get overridden
   }

   /**
    * Create a new control reference for the provided name.
    *
    * @param name the control reference name, not <code>null</code> and not
    *    empty
    */
   public PSControlRef(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");

      m_name = name;
   }

   /**
    * Creates a new control reference with the provided parameters.  This
    * constructor is made available as a convenience to test classes.
    *
    * @param name the name of this control reference, never <code>null</code>
    *    or empty.
    * @param parameters a collection of PSParam objects, not <code>null</code>
    *    may be empty.
    */
   PSControlRef(String name, PSCollection parameters)
   {
      this( name );
      setParameters(parameters);
   }

   /**
    * Construct a Java object from its XML representation.
    *
    * @param sourceNode   the XML element node to construct this object from,
    *    not <code>null</code>.
    * @param parentDoc the Java object which is the parent of this object,
    *    not <code>null</code>.
    * @param parentComponents   the parent objects of this object, not
    *    <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node is not of
    *    the appropriate type
    */
   public PSControlRef(Element sourceNode, IPSDocument parentDoc,
                       List parentComponents)
      throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }


   // see interface for description
   @Override
   public Object clone()
   {
      PSControlRef copy = (PSControlRef) super.clone();
      // clone the PSCollection
      copy.m_parameters = new PSCollection( PSParam.class );
      for (int i = 0; i < m_parameters.size(); i++)
      {
         PSParam param = (PSParam) m_parameters.elementAt( i );
         copy.m_parameters.add( i , param.clone() );
      }
      return copy;
   }


   /**
    * Gets the name of this control.
    *
    * @return the control name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }


   /**
    * Get the list of parameters.
    *
    * @return the current collection (PSParam objects), never
    *    <code>null</code>, may be empty.
    */
   public Iterator getParameters()
   {
      return m_parameters.iterator();
   }

   /**
    * Set the a new collection of parameters.
    *
    * @param parameters a collection of PSParam objects, never
    *    <code>null</code>, may be empty.
    */
   public void setParameters(PSCollection parameters)
   {
      if (parameters == null)
         throw new IllegalArgumentException( "the parameters cannot be null" );

      if (!parameters.getMemberClassName().equals(
          m_parameters.getMemberClassName()))
         throw new IllegalArgumentException( "PSParam collection expected" );

      m_parameters.clear();
      m_parameters.addAll(parameters);
   }

   /**
    * Performs a shallow copy of the data from the supplied component to this
    * component.
    *
    * @param c <code>PSControlRef</code> to be copied, not <code>null</code>.
    */
   public void copyFrom(PSComponent c)
   {
      if (c instanceof PSControlRef)
      {
         PSControlRef source = (PSControlRef) c;
         try
         {
            super.copyFrom( source );
         } catch (IllegalArgumentException e) { } // cannot happen
         m_name = source.m_name;
         m_parameters = source.m_parameters;
      }
      else
         throw new IllegalArgumentException( "INVALID_OBJECT_FOR_COPY" );
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSControlRef)) return false;
      if (!super.equals(o)) return false;
      PSControlRef that = (PSControlRef) o;
      return Objects.equals(m_name, that.m_name) &&
              Objects.equals(m_parameters, that.m_parameters);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_name, m_parameters);
   }

   // see interface for description
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                       List parentComponents)
      throws PSUnknownNodeTypeException
   {
      validateElementName( sourceNode, XML_NODE_NAME );

      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;

      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      String data = null;
      Element node = null;
      try
      {
         PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

         // OPTIONAL: get the id attribute
         String sTemp = tree.getElementData( ID_ATTR );
         if (sTemp != null)
            try
            {
               m_id = Integer.parseInt( sTemp );
            } catch (NumberFormatException e)
            {
               Object[] args = { XML_NODE_NAME, sTemp };
               throw new PSUnknownNodeTypeException(
                     IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID, args );
            }

         // REQUIRED: get the name attribute
         m_name = tree.getElementData(NAME_ATTR);
         if (m_name == null || m_name.trim().length() == 0)
         {
            Object[] args =
            {
               XML_NODE_NAME,
               NAME_ATTR,
               "null"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }

         // OPTIONAL: get all parameters
         node = tree.getNextElement(PSParam.XML_NODE_NAME, firstFlags);
         while(node != null)
         {
            m_parameters.add(new PSParam(node, parentDoc, parentComponents));
            node = tree.getNextElement(PSParam.XML_NODE_NAME, nextFlags);
         }
      }
      finally
      {
         resetParentList(parentComponents, parentSize);
      }
   }

   // see IPSComponent
   public Element toXml(Document doc)
   {
      // create root and its attributes
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute( ID_ATTR, String.valueOf( m_id ) );
      root.setAttribute( NAME_ATTR, m_name );

      // OPTIONAL: create the parameters
      Iterator it = getParameters();
      while (it.hasNext())
         root.appendChild(((IPSComponent) it.next()).toXml(doc));

      return root;
   }

   // see IPSComponent
   public void validate(IPSValidationContext context)
      throws PSSystemValidationException
   {
      if (!context.startValidation(this, null))
         return;

      // the name is required
      if (m_name == null || m_name.trim().length() == 0)
         context.validationError(this,
            IPSObjectStoreErrors.INVALID_CONTROL_REF, null);

      // do children
      context.pushParent(this);
      try
      {
         Iterator it = getParameters();
         while (it.hasNext())
            ((IPSComponent) it.next()).validate(context);
      }
      finally
      {
         context.popParent();
      }
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXControlRef";

   /**
    * This name can be used to find the control in the referenced location.
    * Typically, this will be the mode of an XSL template.  Never empty or
    * <code>null</code>.  Cannot be mutated after construction.
    */
   private String m_name;

   /**
    * A collection of PSParam objects, never <code>null</code> after
    * construction, might be empty.
    */
   private PSCollection m_parameters = new PSCollection( PSParam.class );

   /*
    * The following strings define all elements/attributes used to create the
    * XML output for this object. No Java documentation will be added to this.
    */
   private static final String NAME_ATTR = "name";
}

