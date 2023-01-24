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
package com.percussion.cms.objectstore;

import com.percussion.cms.PSCmsException;
import com.percussion.util.PSXMLDomUtil;
import org.w3c.dom.Element;

/**
 * The value of an <code>PSItemField</code> that is treated as an XML element.
 */
public class PSXmlValue extends PSFieldValue
{
   /**
    * Creates a new instance with the element as the value.
    *
    * @param element the content of this value.  Must not be <code>null</code>.
    */
   public PSXmlValue(Element element)
   {
      if(element ==  null)
         throw new IllegalArgumentException("element must not be null");

      setXml(element);
   }

   /**
    * Sets the supplied element as the content of this value.  This object
    * takes ownership of the element. Any changes to the element after calling
    * this method will affect the value of this instance.
    *
    * @param element The element to use, may be <code>null</code> to clear the
    * value.
    */
   public void setXml(Element element)
   {
      m_data = element;
   }

   /**
    * Gets the XML content of this value.
    *
    * @return The content as <code>Element</code>, may be <code>null</code>.
    */
   public Object getValue()
   {
      return (Object)m_data;
   }

   /** @see IPSFieldValue */
   public Object clone()
   {
      PSXmlValue copy = null;

      copy = (PSXmlValue)super.clone();
      copy.m_data = (Element)m_data.cloneNode(true);

      return copy;
   }

   /** @see IPSFieldValue */
   public boolean equals(Object obj)
   {
      if(obj == null || !(getClass().isInstance(obj)))
         return false;

      PSXmlValue comp = (PSXmlValue) obj;
      if (!compare(m_data, comp.m_data))
         return false;

      return true;
   }

   /** @see IPSFieldValue */
   public int hashCode()
   {
      int hash = 0;

      // super is abtract, don't call
      hash += hashBuilder(m_data);

      return hash;
   }

   /**
    * Returns the <code>Element</code> as a <code>String</code>.  No indents.
    * This is called by <code>toXml</code>.  I use a <code>StringWriter</code>
    * to create the string.  Which means that an <code>IOException</code> may
    * occur.
    * @return The content as <code>String</code>, may be <code>null</code>.
    * @throws PSCmsException if conversion from <code>StringWriter</code> has a
    * problem.
    */
   public String getValueAsString() throws PSCmsException
   {
      return PSXMLDomUtil.toString(m_data);
   }

   /**
    * The data as an Element, may be <code>null</code> and may change often.
    */
   private Element m_data;

   }
