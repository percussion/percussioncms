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

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.PSXMLDomUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * The PSRelationshipInfoSet contains a list of PSRelationshipInfo objects
 */
public class PSRelationshipInfoSet implements IPSCmsComponent
{
   /**
    * Default constructor.
    */
   public PSRelationshipInfoSet()
   {
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
   public PSRelationshipInfoSet(Element source) throws PSUnknownNodeTypeException
   {
      fromXml(source);
   }

   /**
    * Adds a relationship info object to the list.
    *
    * @param info The to be added object, it may not be <code>null</code>.
    */
   public void add(PSRelationshipInfo info)
   {
      if (info == null)
         throw new IllegalArgumentException("info may not be null");

      m_compList.add(info);
   }

   /**
    * Get the relationship info objects
    *
    * @return An iterator over <code>0</code> or more
    *    <code>PSRelationshipInfo</code> objects.
    */
   public Iterator getComponents()
   {
      return m_compList.iterator();
   }

   /**
    * Get the number of component summaries in the object.
    *
    * @return the number of components.
    */
   public int size()
   {
      return m_compList.size();
   }


   /**
    * See {@link IPSCmsComponent#equals(Object)}
    */
   public boolean equals( Object o )
   {
      if ( !(o instanceof PSComponentSummaries ))
         return false;

      PSRelationshipInfoSet obj2 = (PSRelationshipInfoSet) o;

      return m_compList.equals(obj2.m_compList);
   }


   /**
    * See {@link IPSCmsComponent#hashCode()}
    */
   public int hashCode()
   {
      return m_compList.hashCode();
   }

   /**
    * Serializes this object into an xml element that can be attached to the
    * supplied document. It will conform to the following dtd:
    * <pre>
    * <!ELEMENT PSXRelationshipInfoSet (PSXRelationshipInfo*)>
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

      Element root = doc.createElement(getNodeName());
      Iterator infos = m_compList.iterator();

      while (infos.hasNext())
      {
         PSRelationshipInfo comp = (PSRelationshipInfo) infos.next();
         root.appendChild(comp.toXml(doc));
      }

      return root;
   }

   /**
    * See {@link IPSCmsComponent#fromXml(Element)}
    */
   public void fromXml(Element sourceNode)
      throws PSUnknownNodeTypeException
   {
      if (null == sourceNode)
         throw new IllegalArgumentException("sourceNode must be supplied");

      PSXMLDomUtil.checkNode(sourceNode, XML_NODE_NAME);

      m_compList.clear();

      Element compEl = PSXMLDomUtil.getFirstElementChild(sourceNode);
      while (compEl != null)
      {
         m_compList.add( new PSRelationshipInfo(compEl) );
         compEl = PSXMLDomUtil.getNextElementSibling(compEl);
      }
   }

   /**
    * See {@link IPSCmsComponent#clone()}
    */
   public Object clone()
   {
      PSRelationshipInfoSet infoSet = new PSRelationshipInfoSet();
      infoSet.m_compList.addAll(m_compList);

      return infoSet;
   }

   /**
    * See {@link IPSCmsComponent#getNodeName()}
    */
   public String getNodeName()
   {
      return XML_NODE_NAME;
   }

   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXRelationshipInfoSet";

   /**
    * It contains a list of <code>PSXRelationshipInfo<code> objects.
    * It never <code>null</code>, but may be empty.
    */
   private List m_compList = new ArrayList();

}
