/**[ PSCommunityCataloger.java ]*****************************************************
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
 * Catalogs all server communities by querying the
 * ../sys_cmpCommunities/communities.xml app.
 */
public class PSCommunityCataloger
{
   /**
    * Default constructor. Does nothing. Must be followed by call to fromXml()
    * method. This is useful only to build an object in the fly means the state
    * information might not come from the Rhythmyx server.
    */
   public PSCommunityCataloger()
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
   public PSCommunityCataloger(URL urlBase)
      throws PSCmsException
   {
      m_collCommunities.clear();
      try
      {
         URL url = new URL(urlBase, "../sys_cmpCommunities/communities.xml");
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
      PSCommunityCataloger clone = null;
      try
      {
         clone = (PSCommunityCataloger)super.clone();

         Collection clonedComm = new ArrayList();

         Iterator it = m_collCommunities.iterator();
         while(it.hasNext())
            clonedComm.add(((Community)it.next()).clone());

         clone.m_collCommunities = clonedComm;

      }
      catch(CloneNotSupportedException e)
      {
         //????
      }
      return clone;
   }

    public boolean equals(Object object) {
        if (this == object) return true;

        if (!(object instanceof PSCommunityCataloger)) return false;

        PSCommunityCataloger that = (PSCommunityCataloger) object;

        return new org.apache.commons.lang.builder.EqualsBuilder()
                .appendSuper(super.equals(object))
                .append(m_collCommunities, that.m_collCommunities)
                .isEquals();
    }

    public int hashCode() {
        return new org.apache.commons.lang.builder.HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(m_collCommunities)
                .toHashCode();
    }

    /**
    * Represents a single community.
   */
   public static class Community
   {
      /**
       * Default constructor. Does nothing. Must be followed by call to fromXml()
       * method. This is useful only to build an object in the fly means the state
       * information might not come from the Rhythmyx server.
      */
      public Community()
      {

      }

      /**
       * Allows to create a community instance from passed params, main use
       * is for the Cx UI to add an entry for the -1 community id.
       * @param id community Id, -1 is reserved for "All"
       * @param name, never <code>null</code>
       * @param description, may be <code>null</code>
       */
      public Community(int id, String name, String description)
      {
         m_communityId = id;

         if (name==null)
            throw new IllegalArgumentException("community name may not be null");

         m_communityName = name;
         m_communityDesc = description;
      }

      /**
       * Constructor that calls fromXml.
       * @param elemRoot the element that contains data for a single community,
       * never <code>null</code>
       */
      public Community(Element elemRoot) throws PSUnknownNodeTypeException
      {
         fromXml(elemRoot);
      }

      /*
       * Implementation of the interface method
       */
      public void fromXml(Element elemRoot) throws PSUnknownNodeTypeException
      {
         PSXMLDomUtil.checkNode(elemRoot, XML_ELEM_LIST);
         Element el = PSXMLDomUtil.getFirstElementChild(elemRoot, XML_ELEM_COMMUNITYNAME);
         m_communityName = PSXMLDomUtil.getElementData(el);

         el = PSXMLDomUtil.getNextElementSibling(el, XML_ELEM_COMMUNITYID);
         String strComm = PSXMLDomUtil.getElementData(el);
         try
         {
            m_communityId = -1;
            m_communityId = Integer.parseInt(strComm);
         }catch(NumberFormatException e){}

         el = PSXMLDomUtil.getNextElementSibling(el, XML_ELEM_COMMUNITYDESC);
         m_communityDesc = PSXMLDomUtil.getElementData(el);
      }

     /**
       * @return community id
       */
      public int getId() {
         return m_communityId;
      }

      /**
       * @return community name, never <code>null</code>
       */
      public String getName() {
         return m_communityName;
      }


      /**
       * @return community description, never <code>null</code>
       */
      public String getDesc() {
         return m_communityDesc;
      }

      /*
       * Implementation of the interface method
       */
      public boolean equals(Object obj)
      {
         if( !(obj instanceof Community) )
            return false;
         else
         {
            return ((Community)obj).getName().equals(getName());
         }

      }

      /*
       * Implementation of the interface method
      */
      public Object clone()
      {
         Community clone = null;
         try
         {
            clone = (Community)super.clone();

            clone.m_communityName = m_communityName;
            clone.m_communityId = m_communityId;
            clone.m_communityDesc = m_communityDesc;

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
         return m_communityName;
      }

      /*
       * Implementation of the interface method
      */
      public int hashCode() {
         return m_communityName.hashCode();
      }

      /** */
      private String m_communityName;
      /** */
      private int m_communityId = -1;
      /** */
      private String m_communityDesc;
   }

   /*
    * Implementation of the interface method
    */
   public void fromXml(Element elemRoot)
      throws PSUnknownNodeTypeException
   {
      m_collCommunities.clear();

      PSXMLDomUtil.checkNode(elemRoot, XML_ELEM_ROOT);

      NodeList nl = elemRoot.getElementsByTagName(XML_ELEM_LIST);
      if (nl == null || nl.getLength() <= 0)
         throw new IllegalArgumentException("must have at least one community");

      for(int i = 0; i < nl.getLength(); i++)
      {
         Node n = nl.item(i);
         if (n.getNodeType() != Node.ELEMENT_NODE)
            continue;

        Community comm = new Community((Element)n);

        m_collCommunities.add(comm);
      }
   }

   /**
    * @return unmodifiable collection of cataloged Community instances
    * , never <code>null</code>.
    */
   public Collection getCommunities()
   {
      return Collections.unmodifiableCollection(m_collCommunities);
   }

   /**
    * Allows to create a community instance from passed params, main use
    * is for the Cx UI to create an new Community instance for the special
    * community id = -1. This instance is not cached or owned by the cataloger.
    * @param id community Id
    * @param name, never <code>null</code>
    * @param description, may be <code>null</code>
    *
    * @return newly created community instance, the itself cataloger doesn't own
    * this instance.
   */
   public static Community createCommunity(int id, String name, String description)
   {
      return new Community(id, name, description);
   }

   /**
    * collection of cataloged Community instances
    */
   private Collection  m_collCommunities = new ArrayList();

   public static final String XML_ELEM_ROOT = "communities";
   public static final String XML_ELEM_LIST = "list";
   public static final String XML_ELEM_COMMUNITYNAME = "communityname";
   public static final String XML_ELEM_COMMUNITYID = "communityid";
   public static final String XML_ELEM_COMMUNITYDESC = "communitydesc";
}