/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.design.objectstore;

import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Objects;

/**
 * Container class used to define directory or role provider catalogers.
 */
public class PSProvider extends PSComponent
{
   /**
    * Construct a Java object from its XML representation.
    *
    * @param sourceNode   the XML element node to construct this object from,
    *    not <code>null</code>.
    * @param parentDoc the Java object which is the parent of this object,
    *    may be <code>null</code>.
    * @param parentComponents   the parent objects of this object, may be
    *    <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node is not of
    *    the appropriate type
    */
   public PSProvider(Element sourceNode, IPSDocument parentDoc,
      List parentComponents) throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Construct a new provider for the supplied parameters.
    *
    * @param className the fully qualified class name of the provider,
    *    not <code>null</code> or empty.
    * @param type the provider type to create, one of
    *    <code>TYPE_ENUM</code>.
    * @param reference the provider reference, may be <code>null</code>.
    */
   public PSProvider(String className, String type, PSReference reference)
   {
      setClassName(className);
      setType(type);
      setReference(reference);
   }

   /**
    * @return the fully qualified provider class name, never
    *    <code>null</code> or empty.
    */
   public String getClassName()
   {
      return m_class;
   }

   /**
    * Set the fully qualified provider class name.
    *
    * @param className the new class name to set, not <code>null</code>
    *    or empty.
    */
   public void setClassName(String className)
   {
      if (className == null)
         throw new IllegalArgumentException("className cannot be null");

      className = className.trim();
      if (className.length() == 0)
         throw new IllegalArgumentException("className cannot be null");

      m_class = className;
   }
   
   /**
    * Test if this provider is a provider for the supplied class name.
    * 
    * @param className the class name to test for, may be <code>null</Code>.
    * @return <code>true</code> if this provider is for the speccified class 
    *    name, <code>false</code> otherwise.
    */
   public boolean isProviderFor(String className)
   {
      if (className == null)
         return false;
         
      return getClassName().equals(className);
   }

   /**
    * Is this a directory provider?
    *
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isDirectoryProvider()
   {
      return m_type.equals(TYPE_DIRECTORY);
   }

   /**
    * Set the provider type.
    *
    * @param type the provider type, must be one of <code>TYPE_ENUM</code>.
    */
   private void setType(String type)
   {
      boolean valid = false;
      for (int i=0; i<TYPE_ENUM.length; i++)
      {
         if (TYPE_ENUM[i].equals(type))
         {
            valid = true;
            break;
         }
      }

      if (!valid)
         throw new IllegalArgumentException("type must be one of TYPE_ENUM");

      m_type = type;
   }

   /**
    * @return the provider reference, may be <code>null</code>.
    */
   public PSReference getReference()
   {
      return m_reference;
   }

   /**
    * Set the supplied reference.
    *
    * @param reference the new reference, may be <code>null</code>.
    */
   public void setReference(PSReference reference)
   {
      m_reference = reference;
   }

   /** @see IPSComponent */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
      List parentComponents) throws PSUnknownNodeTypeException
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

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

      setClassName(getRequiredElement(tree, PROVIDER_CLASS_ATTR, true));
      setType(getEnumeratedAttribute(tree, PROVIDER_TYPE_ATTR,
         TYPE_ENUM));

      Element reference = tree.getNextElement(PSReference.XML_NODE_NAME);
      if (reference != null)
         m_reference = new PSReference(reference, parentDoc, parentComponents);
   }

   /** @see IPSComponent */
   public Element toXml(Document doc)
   {
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(PROVIDER_CLASS_ATTR, m_class);
      root.setAttribute(PROVIDER_TYPE_ATTR, m_type);

      if (m_reference != null)
         root.appendChild(m_reference.toXml(doc));

      return root;
   }

   /** @see IPSComponent */
   public Object clone()
   {
      return super.clone();
   }

   /** @see PSComponent */
   public void copyFrom(PSComponent c)
   {
      super.copyFrom(c);

      if (!(c instanceof PSProvider))
         throw new IllegalArgumentException("c must be a PSProvider object");

      PSProvider o = (PSProvider) c;

      setClassName(o.getClassName());
      setType(o.m_type);
      setReference(o.getReference());
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSProvider)) return false;
      if (!super.equals(o)) return false;
      PSProvider that = (PSProvider) o;
      return Objects.equals(m_type, that.m_type) &&
              Objects.equals(m_class, that.m_class) &&
              Objects.equals(m_reference, that.m_reference);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_type, m_class, m_reference);
   }

   /** The XML node name */
   public static final String XML_NODE_NAME = "PSXProvider";

   /**
    * A constant for directory providers.
    */
   public final static String TYPE_DIRECTORY = "directory";

   /**
    * A constant for role providers.
    */
   public final static String TYPE_ROLE = "role";

   /**
    * An enumeration with all valid provider types.
    */
   public final static String[] TYPE_ENUM =
   {
      TYPE_DIRECTORY,
      TYPE_ROLE
   };

   /**
    * The provider type, initialized while constructed, never changed after
    * that. One of <code>Provider.TYPE_ENUM</code>.
    */
   private String m_type = null;

   /**
    * The fully qualified provider class name. Initialized during
    * construction, never <code>null</code>, empty or changed after that.
    */
   private String m_class = null;

   /**
    * The provider definition reference, might be <code>null</code>.
    */
   private PSReference m_reference = null;

   // XML element and attribute constants.
   private static final String PROVIDER_CLASS_ATTR = "class";
   private static final String PROVIDER_TYPE_ATTR = "type";
}
