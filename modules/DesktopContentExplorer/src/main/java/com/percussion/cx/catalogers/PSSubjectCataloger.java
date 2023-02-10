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

package com.percussion.cx.catalogers;

import com.percussion.cms.PSCmsException;
import com.percussion.cx.error.IPSContentExplorerErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * Catalogs all server users by querying the
 * ../sys_components/getSubject.xml app.
 */
public class PSSubjectCataloger
{
   /**
    * Default constructor. Does nothing. Must be followed by call to fromXml()
    * method. This is useful only to build an object in the fly means the state
    * information might not come from the Rhythmyx server.
    */
   public PSSubjectCataloger()
   {

   }

   /**
    * Constructor meant to be used in the context of an applet. This may not work
    * in other contexts since there is no way of supplying credentials for logging
    * in.
    * @param urlBase the document or code base for the applet.
    * @throws PSCmsException if request to server to get the data fails for
    * any reason.
    */
   public PSSubjectCataloger(URL urlBase)
      throws PSCmsException
   {
      m_collSubjects.clear();
      try
      {
         URL url = new URL(urlBase, "sys_components/getSubject.xml");
         Document doc = PSXmlDocumentBuilder.createXmlDocument(
            url.openStream(), false);
         fromXml(doc.getDocumentElement());
      }
      catch(Exception e)
      {
         throw new PSCmsException(
            IPSContentExplorerErrors.CATALOG_ERROR,
            e.getMessage());
      }
   }

   /*
    * Implementation of the interface method.
    */
   public Object clone()
   {
      PSSubjectCataloger clone = null;
      try
      {
         clone = (PSSubjectCataloger)super.clone();

         Collection clonedSubjects = new ArrayList();

         Iterator it = m_collSubjects.iterator();
         while(it.hasNext())
            clonedSubjects.add(((Subject)it.next()).clone());

         clone.m_collSubjects = clonedSubjects;

      }
      catch(CloneNotSupportedException e)
      {
         //TODO:  Fix ME ????
      }
      return clone;
   }

    public boolean equals(Object object) {
        if (this == object) return true;

        if (!(object instanceof PSSubjectCataloger)) return false;

        PSSubjectCataloger that = (PSSubjectCataloger) object;

        return new org.apache.commons.lang.builder.EqualsBuilder()
                .appendSuper(super.equals(object))
                .append(m_collSubjects, that.m_collSubjects)
                .isEquals();
    }

    public int hashCode() {
        return new org.apache.commons.lang.builder.HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(m_collSubjects)
                .toHashCode();
    }

    /**
    * Represents a single Subject
    */
   public class Subject
   {
      /**
       * Default constructor. Does nothing. Must be followed by call to fromXml()
       * method. This is useful only to build an object in the fly means the state
       * information might not come from the Rhythmyx server.
       */
      public Subject()
      {

      }

      /**
       * Constructor that calls fromXml
       * @param elemRoot the element that contains data for a single Subject,
       * never <code>null</code>
       */
      public Subject(Element elemRoot) throws PSUnknownNodeTypeException
      {
         fromXml(elemRoot);
      }


      /*
       * Implementation of the interface method.
       */
      public void fromXml(Element elemRoot) throws PSUnknownNodeTypeException
      {
         PSXMLDomUtil.checkNode(elemRoot, XML_ELEM_PSXSUBJECT);
         Element el = PSXMLDomUtil.getFirstElementChild(elemRoot, XML_ELEM_NAME);
         m_name = PSXMLDomUtil.getElementData(el);

         el = PSXMLDomUtil.getNextElementSibling(el, XML_ELEM_SP_TYPE);
         String spType = PSXMLDomUtil.getElementData(el);
         try
         {
            m_securityProviderType = -1;
            m_securityProviderType = Integer.parseInt(spType);
         }catch(NumberFormatException e){}

         el = PSXMLDomUtil.getNextElementSibling(el, XML_ELEM_SP_INSTANCE);
         m_securityProviderInstance = PSXMLDomUtil.getElementData(el);
      }

      /**
       * @return subject name, never <code>null</code>, never <code>empty</code>
       */
      public String getName() {
         return m_name;
      }

      /**
       * @return SecurityProvider type as.
       */
      public int getSecurityProviderTypeId() {
         return m_securityProviderType;
      }

      /**
       * @return SecurityProviderInstance name, may be <code>empty</code>,
       * never <code>null</code>
       */
      public String getSecurityProviderInstance() {
         return m_securityProviderInstance;
      }

      /*
       * Implementation of the interface method.
       */
      public boolean equals(Object obj)
      {
         if( !(obj instanceof Subject) )
            return false;
         else
         {
            if (!((Subject)obj).getName().equals(getName()))
               return false;

            if (((Subject)obj).getSecurityProviderTypeId() !=
               getSecurityProviderTypeId())
               return false;

            if (!((Subject)obj).getSecurityProviderInstance()
               .equals(getSecurityProviderInstance()))
               return false;

            return true;
         }

      }

      /*
       * Implementation of the interface method.
       */
      public Object clone()
      {
         Subject clone = null;
         try
         {
            clone = (Subject)super.clone();

            clone.m_name = m_name;

            clone.m_securityProviderType = m_securityProviderType;
            clone.m_securityProviderInstance = m_securityProviderInstance;
         }
         catch(CloneNotSupportedException e)
         {
            //????
         }

         return clone;
      }


      /*
       * Implementation of the interface method
       */
      public String toString() {
         return m_name;
      }

      /*
       * Implementation of the interface method
       */
      public int hashCode() {
         return m_name.hashCode() + m_securityProviderType +
            m_securityProviderInstance.hashCode();
      }

      /** */
      private String m_name;
      /**  */
      private int m_securityProviderType;
      /**  */
      private String m_securityProviderInstance;

      public static final String XML_ELEM_NAME = "name";
      public static final String XML_ELEM_SP_TYPE = "securityProviderType";
      public static final String XML_ELEM_SP_INSTANCE = "securityProviderInstance";
   }

   /*
    * Implementation of the interface method.
    */
   public void fromXml(Element elemRoot)
      throws PSUnknownNodeTypeException
   {
      m_collSubjects.clear();

      PSXMLDomUtil.checkNode(elemRoot, XML_ELEM_ROOT);

      NodeList nl = elemRoot.getElementsByTagName(XML_ELEM_PSXSUBJECT);
      if (nl == null || nl.getLength() <= 0)
         throw new IllegalArgumentException("must have at least one Subject");

      for(int i = 0; i < nl.getLength(); i++)
      {
         Node n = nl.item(i);
         if (n.getNodeType() != Node.ELEMENT_NODE)
            continue;

        Subject subject = new Subject((Element)n);

        m_collSubjects.add(subject);
      }
   }


   /**
    * @return unmodifiable collection of cataloged Subject instances,
    * never <code>null</code>.
    */
   public Collection getSubjects()
   {
      return Collections.unmodifiableCollection(m_collSubjects);
   }

   /**
    * collection of cataloged Subject instances.import com.percussion.cms.objectstore.PSRoleCataloger;
    */
   private Collection  m_collSubjects = new ArrayList();

   public static final String XML_ELEM_ROOT = "getSubject";
   public static final String XML_ELEM_PSXSUBJECT = "PSXSubject";
}