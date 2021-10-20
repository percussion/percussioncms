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

import com.percussion.cms.objectstore.client.PSLightWeightField;
import com.percussion.util.PSCollection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Implements the PSChoiceFilter DTD in BasicObjects.dtd.
 */
public class PSChoiceFilter extends PSComponent
{
   /**
    * Construct a Java object from its XML representation.
    *
    * @param sourceNode   the XML element node to construct this object from,
    *    not <code>null</code>.
    * @param parentDoc the Java object which is the parent of this object,
    *    not <code>null</code>.
    * @param parentComponents   the parent objects of this object, may be
    *    <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node is not of
    *    the appropriate type
    */
   public PSChoiceFilter(Element sourceNode, IPSDocument parentDoc,
                         List parentComponents)
      throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Construct a choice filter from its member data.
    * @param dependentFields collection of DependentFields,
    * never <code>null</code>.
    * @param lookup the lookup request, never <code>null</code>.
    */
   public PSChoiceFilter(PSCollection dependentFields, PSUrlRequest lookup)
   {
      if (dependentFields == null || dependentFields.isEmpty())
         throw new IllegalArgumentException("dependentFields cannot be null or empty");

      if (lookup == null)
         throw new IllegalArgumentException("lookup cannot be null");

      m_dependentFields = dependentFields;
      m_lookup = lookup;
   }

   /**
    * @see IPSComponent
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                       List parentComponents)
      throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, XML_NODE_NAME);

      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      boolean hasDependentField = false;
      boolean hasPSXUrlRequest = false;
      NodeList nl = sourceNode.getChildNodes();

      int len =  nl.getLength();

      for (int i = 0; i < len; i++)
      {
         if (nl.item(i).getNodeType() != Node.ELEMENT_NODE)
            continue;

         Element elem = (Element)nl.item(i);

         //REQUIRED: DependentField+
         if (elem.getNodeName().equals(DependentField.XML_NODE_NAME))
         {
            m_dependentFields.add(new DependentField(elem));
            hasDependentField = true;
         }
         //REQUIRED: PSXUrlRequest
         else if (elem.getNodeName().equals(PSUrlRequest.XML_NODE_NAME))
         {
            m_lookup = new PSUrlRequest(elem, null, null);
            hasPSXUrlRequest = true;
         }
      }

      if (!hasDependentField)
      {
         Object[] args = { XML_NODE_NAME, DependentField.XML_NODE_NAME, "null"};

         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      if (!hasPSXUrlRequest)
      {
         Object[] args = { XML_NODE_NAME, PSUrlRequest.XML_NODE_NAME, "null"};

         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
    }

   /**
    * Validates that all of the required items are present.
    * See IPSComponent.
    */
   public void validate(IPSValidationContext context)
      throws PSSystemValidationException
   {
      if (!context.startValidation(this, null))
         return;

      if (m_dependentFields.size() <= 0)
      {
         Object[] args = { DependentField.XML_NODE_NAME };
         context.validationError(this,
            IPSObjectStoreErrors.CHOICE_FILTER_MISSING_REQUIRED_CHILD, args);
      }
      else
      {
         for (int i = 0; i < m_dependentFields.size(); i++)
         {
            DependentField df = (DependentField)m_dependentFields.elementAt(i);
            df.validate(context);
         }
      }

      if (m_lookup==null)
      {
         Object[] args = { PSUrlRequest.XML_NODE_NAME };
         context.validationError(this,
            IPSObjectStoreErrors.CHOICE_FILTER_MISSING_REQUIRED_CHILD, args);
      }
      else
      {
         m_lookup.validate(context);
      }

   }

   /**
    *
    * @see IPSComponent
   */
   public Element toXml(Document doc)
   {
      Element root = doc.createElement(XML_NODE_NAME);

      Iterator it = m_dependentFields.iterator();

      //add REQUIRED: DependentField+
      while(it.hasNext())
      {
         DependentField depF = (DependentField)it.next();

         Element el = depF.toXml(doc);

         root.appendChild(el);
      }

      //add REQUIRED: PSXUrlRequest
      Element el = m_lookup.toXml(doc);

      root.appendChild(el);

      return root;
   }

   /**
    * Get the url request used to lookup the filtered choices.
    *
    * @return The url request, never <code>null</code>.
    */
   public PSUrlRequest getLookup()
   {
      return m_lookup;
   }

   /**
    * @return collection of PSChoiceFilter.DependentField(s),
    * never <code>null</code>, never <code>empty</code>.
    */
   public PSCollection getDependentFields()
   {
      return m_dependentFields;
   }

   // see interface for description
   public Object clone()
   {
      PSChoiceFilter copy = (PSChoiceFilter) super.clone();

      copy.m_dependentFields = new PSCollection( DependentField.class );

      Iterator it = m_dependentFields.iterator();
      while (it.hasNext())
      {
         DependentField dependentField = (DependentField) it.next();
         copy.m_dependentFields.add(dependentField.clone());
      }

      if (m_lookup != null)
         copy.m_lookup = (PSUrlRequest) m_lookup.clone();

      return copy;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSChoiceFilter)) return false;
      if (!super.equals(o)) return false;
      PSChoiceFilter that = (PSChoiceFilter) o;
      return Objects.equals(m_dependentFields, that.m_dependentFields) &&
              Objects.equals(m_lookup, that.m_lookup);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_dependentFields, m_lookup);
   }

   /**
    * Implementation of the DependentField, see DTD in BasicObjects.dtd.
    */
   public static class DependentField extends PSComponent
   {

      /**
       * Construct from Xml.
       * @param el, assuming never <code>null</code>.
       * @throws PSUnknownNodeTypeException
       */
      public DependentField(Element el)
         throws PSUnknownNodeTypeException
      {
         fromXml(el, null, null);
      }

      /**
       * Construct from params.
       * @param fieldRef, never <code>null</code> or <code>empty</code>.
       * @param dependencyType, never <code>null</code> or <code>empty</code>.
       */
      public DependentField(String fieldRef, String dependencyType)
      {
         if (fieldRef==null || fieldRef.trim().length()<=0)
            throw new IllegalArgumentException("fieldRef must not be null");

         if (dependencyType==null || dependencyType.trim().length()<=0)
            throw new IllegalArgumentException("dependencyType must not be null");

         m_fieldRef = fieldRef;
         m_dependencyType = dependencyType;
      }

      /**
       * @see IPSComponent
       */
      public Element toXml(Document doc)
      {
         // create root and its attributes
         Element root = doc.createElement(XML_NODE_NAME);

         if (m_fieldRef != null && m_fieldRef.trim().length() > 0)
            root.setAttribute(XML_ATTR_fieldRef, m_fieldRef);

         if (m_dependencyType != null && m_dependencyType.trim().length() > 0)
            root.setAttribute(XML_ATTR_dependencyType, m_dependencyType);

         return root;
      }

      /**
       * @see IPSComponent
       */
      public void fromXml(Element sourceNode, IPSDocument parentDoc,
         List parentComponents)
         throws PSUnknownNodeTypeException
      {
         if (sourceNode == null)
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_NULL, XML_NODE_NAME);

         if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
         {
            Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
         }

         m_fieldRef = sourceNode.getAttribute(XML_ATTR_fieldRef);
         if (m_fieldRef==null || m_fieldRef.trim().length()<=0)
         {
            Object[] args = { XML_NODE_NAME, XML_ATTR_fieldRef, "null"};

            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }

         m_dependencyType = sourceNode.getAttribute(XML_ATTR_dependencyType);
         if (m_dependencyType==null || m_dependencyType.trim().length()<=0)
         {
            Object[] args = { XML_NODE_NAME, XML_ATTR_dependencyType, "null"};
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }

      }

      /**
       * Validates that all of the required items are present.
       * See IPSComponent.
       */
      @Override
      public void validate(IPSValidationContext context)
         throws PSSystemValidationException
      {
         if (!context.startValidation(this, null))
            return;

         // REQUIRED: fieldRef attr
         if (m_fieldRef==null || m_fieldRef.trim().length()<=0)
         {
            Object[] args = { XML_ATTR_fieldRef };
            context.validationError(this,
               IPSObjectStoreErrors.CHOICE_FILTER_DEPENDENT_FIELD_MISSING_ATTR,
               args);
         }

         // REQUIRED: dependencyType attr
         if (m_dependencyType==null || m_dependencyType.trim().length()<=0)
         {
            Object[] args = { XML_ATTR_dependencyType };
            context.validationError(this,
               IPSObjectStoreErrors.CHOICE_FILTER_DEPENDENT_FIELD_MISSING_ATTR,
               args);
         }
      }

      /**
       * Returns FieldRef name.
       * @return FieldRef, never <code>null</code>.
       */
      public String getFieldRef()
      {
         return m_fieldRef;
      }

      /**
       * Returns DependencyType.
       * @return DependencyType, never <code>null</code>.
       */
      public String getDependencyType()
      {
         return m_dependencyType;
      }

      /**
       * Attaches a reference to a cataloged and corresponding
       * LightWeightField instance.
       * @param lwf a corresponding LightWeightField instance,
       * may be <code>null</code>.
       */
      public void attachField(PSLightWeightField lwf)
      {
         m_lightWeightField = lwf;
      }

      /**
       * Returns and Detaches a reference to a cataloged and
       * previously attached corresponding LightWeightField instance.
       *
       * @return previously attached instance of the corresponsing
       * LightWeightField instance, may be <code>null</code>.
       */
      public PSLightWeightField detachField()
      {
         PSLightWeightField lwf = m_lightWeightField;

         m_lightWeightField = null;

         return lwf;
      }

      /**
       * Returns a reference to a cataloged and previously attached
       * corresponding LightWeightField instance.
       *
       * @return previously attached instance of the corresponsing
       * LightWeightField instance, may be <code>null</code>.
       */
      public PSLightWeightField getField()
      {
         return m_lightWeightField;
      }

      /**
       * Test if the provided object and this are equal.
       *
       * @param o the object to compare to.
       * @return <code>true</code> if this and o are equal,
       *    <code>false</code> otherwise.
       */
      public boolean equals(Object o)
      {
         if (!(o instanceof DependentField))
            return false;

         DependentField t = (DependentField) o;

         boolean equal = true;
         if (!compare(m_fieldRef, t.m_fieldRef))
            equal = false;
         else if (!compare(m_dependencyType, t.m_dependencyType))
            equal = false;

         return equal;
      }

      /**
       * Generates code of the object. Overrides {@link Object#hashCode().
       */
      @Override
      public int hashCode()
      {
         return m_fieldRef.hashCode() + m_dependencyType.hashCode();
      }


      /**
       * optional dependency.
       */
      public static final String TYPE_OPTIONAL = "optional";
      /**
       * required dependency.
       */
      public static final String TYPE_REQUIRED = "required";

      /**
       * Represents field reference name, initialized in ctor,
       * never <code>null</code> after that.
       */
      private String m_fieldRef;

      /**
       * Represents dependency type name, initialized in ctor,
       * never <code>null</code> after that.
       */
      private String m_dependencyType;

      /**
       * Represents a reference to the corresponsing instance of the
       * cataloged LightWeightField, may be <code>null</code>.
       */
      private PSLightWeightField m_lightWeightField;

      private static final String XML_NODE_NAME = "DependentField";
      private static final String XML_ATTR_fieldRef = "fieldRef";
      private static final String XML_ATTR_dependencyType = "dependencyType";
   }


   /**
    * Collection of REQUIRED DependentField+ . Initialized here, loaded by ctor,
    * never <code>empty</code> after that.
    */
   private PSCollection m_dependentFields = new PSCollection(DependentField.class);

   /**
    * REQUIRED PSXUrlRequest specifies the URL request used to generate
    * the filter choice list. Initialized by ctor, never <code>null</code> or
    * <code>empty</code> after that.
    */
   private PSUrlRequest m_lookup = null;

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXChoiceFilter";
}
