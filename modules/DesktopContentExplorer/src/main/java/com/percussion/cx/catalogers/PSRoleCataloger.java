/**[ PSRoleCataloger.java ]*****************************************************
 *
 * COPYRIGHT (c) 2004 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

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
 * Catalogs all server roles by querying the
 * ../sys_components/getRole.xml app.
 */
public class PSRoleCataloger
{
   /**
    * Default constructor. Does nothing. Must be followed by call to fromXml()
    * method. This is useful only to build an object in the fly means the state
    * information might not come from the Rhythmyx server.
    */
   public PSRoleCataloger()
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
   public PSRoleCataloger(URL urlBase) throws PSCmsException
   {
      m_collRoles.clear();
      try
      {
         URL url = new URL(urlBase, "sys_components/getRole.xml");
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

   /**
    * Implementation of the clone.
    */
   public Object clone()
   {
      PSRoleCataloger clone = null;
      try
      {
         clone = (PSRoleCataloger)super.clone();

         Collection clonedRoles = new ArrayList();

         Iterator it = m_collRoles.iterator();
         while(it.hasNext())
            clonedRoles.add(((Role)it.next()).clone());

         clone.m_collRoles = clonedRoles;

      }
      catch(CloneNotSupportedException e)
      {
         //????
      }
      return clone;
   }

    public boolean equals(Object object) {
        if (this == object) return true;

        if (!(object instanceof PSRoleCataloger)) return false;

        PSRoleCataloger that = (PSRoleCataloger) object;

        return new org.apache.commons.lang.builder.EqualsBuilder()
                .appendSuper(super.equals(object))
                .append(m_collRoles, that.m_collRoles)
                .isEquals();
    }

    public int hashCode() {
        return new org.apache.commons.lang.builder.HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(m_collRoles)
                .toHashCode();
    }

    /**
    * Represents a single role.
    */
   public class Role
   {
      /**
       * Default constructor. Does nothing. Must be followed by call to fromXml()
       * method. This is useful only to build an object in the fly means the state
       * information might not come from the Rhythmyx server.
      */
      public Role()
      {

      }

      /**
       * Constructor that calls fromXml
       * @param elemRoot the element that contains data for a single Role
       * , never <code>null</code>
      */
      public Role(Element elemRoot) throws PSUnknownNodeTypeException
      {
         fromXml(elemRoot);
      }

      /*
       * Implementation of the interface method
       */
      public void fromXml(Element elemRoot) throws PSUnknownNodeTypeException
      {
         PSXMLDomUtil.checkNode(elemRoot, XML_ELEM_PSXROLE);
         Element el = PSXMLDomUtil.getFirstElementChild(elemRoot, XML_ELEM_NAME);
         m_name = PSXMLDomUtil.getElementData(el);

      }


      /**
       * @return role name, never <code>null</code>
       */
      public String getName() {
         return m_name;
      }

      /*
       * Implementation of the interface method
       */
      public boolean equals(Object obj)
      {
         if( !(obj instanceof Role) )
            return false;
         else
         {
            return ((Role)obj).getName().equals(getName());
         }

      }

      /*
       * Implementation of the interface method
       */
      public Object clone()
      {
         Role clone = null;
         try
         {
            clone = (Role)super.clone();

            clone.m_name = m_name;

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
         return m_name.hashCode();
      }

      /** */
      private String m_name;
   }

   /*
    * Implementation of the interface method
    */
   public void fromXml(Element elemRoot)
      throws PSUnknownNodeTypeException
   {
      m_collRoles.clear();

      PSXMLDomUtil.checkNode(elemRoot, XML_ELEM_ROOT);

      NodeList nl = elemRoot.getElementsByTagName(XML_ELEM_PSXROLE);

      for(int i = 0; i < nl.getLength(); i++)
      {
         Node n = nl.item(i);
         if (n.getNodeType() != Node.ELEMENT_NODE)
            continue;

        Role role = new Role((Element)n);

        m_collRoles.add(role);
      }
   }

   /*
    * Implementation of the interface method
    */
   public Element toXml(Document doc)
   {
      Element elem  = PSXmlDocumentBuilder.createRoot(doc, XML_ELEM_ROOT);

      return elem;
   }


   /**
    * @return unmodifiable collection of cataloged Role instances
    * , never <code>null</code>.
    */
   public Collection getRoles()
   {
      return Collections.unmodifiableCollection(m_collRoles);
   }

   /**
    * collection of cataloged Role instances
    */
   private Collection  m_collRoles = new ArrayList();

   public static final String XML_ELEM_ROOT = "getRole";
   public static final String XML_ELEM_PSXROLE = "PSXRole";
   public static final String XML_ELEM_NAME = "name";
}