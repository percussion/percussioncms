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
import com.percussion.design.objectstore.PSEntry;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Catalogs the community and content types mapping by querying the
 * ../sys_commSupport/CommunityContentTypeMapper.xml app.
 *
 * Note, the communities in the mapper may not contain all communities that
 * are defined in the server. It only contains the commmunities which contain
 * a list of non empty content types.
 */
public class PSCommunityContentTypeMapperCataloger
{
   /**
    * Constructor meant to be used in the context of an applet. This may not work
    * in other contexts since there is no way of supplying credentials for logging
    * in.
    * @param urlBase the document or code base for the applet.
    * @throws PSCmsException if request to server to get the data fails for
    * any reason.
    */
   public PSCommunityContentTypeMapperCataloger(URL urlBase)
      throws PSCmsException
   {
      try
      {
         URL url = new URL(urlBase,
               "sys_commSupport/CommunityContentTypeMapper.xml");
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
    * Gets a list of communities which are compatible with the supplied
    * source community. A compatible community contains the same set or more
    * content types as the source does.
    *
    * @param srcCommunityId the id of the source community. Never
    *    <code>null</code>.
    *
    * @return a list of zero or more <code>PSCommunityCataloger.Community</code>
    *    objects. It may be <code>null</code> if the source community is not in
    *    the mapper.
    */
   public Collection getCompatibleCommunities(Integer srcCommunityId)
   {
      if (srcCommunityId == null)
         throw new IllegalArgumentException("srcCommunityId cannot be null");

      Set retCommunities = new HashSet();

      PSCommunityCataloger.Community srcCommunity = getCommunity(srcCommunityId);
      if (srcCommunity == null)
         return null;

      Collection srcContentTypes = (Collection) m_commCtMapper.get(srcCommunity);

      Collection tgtContentTypes;
      Iterator communities = m_commCtMapper.keySet().iterator();
      PSCommunityCataloger.Community community;
      while (communities.hasNext())
      {
         community = (PSCommunityCataloger.Community) communities.next();
         tgtContentTypes = (Collection) m_commCtMapper.get(community);
         if (tgtContentTypes.containsAll(srcContentTypes))
            retCommunities.add(community);
      }

      return retCommunities;
   }

   /**
    * Create an instance of the class from its XML representation.
    *
    * The DTD of its XML represenation is:
    * <pre><code>
    * &lt;!ELEMENT ContentType EMPTY>
    * &lt;!ATTLIST  ContentType name CDATA #REQUIRED>
    * &lt;!ATTLIST  ContentType id CDATA #REQUIRED>
    * &lt;!ELEMENT CommunityContentTypeMapping (ContentType* )>
    * &lt;!ATTLIST  CommunityContentTypeMapping communityName CDATA #REQUIRED>
    * &lt;!ATTLIST  CommunityContentTypeMapping communityId CDATA #REQUIRED>
    * &lt;!ELEMENT CommunityContentTypeMapper (CommunityContentTypeMapping* )>
    * </code></pre>
    *
    * @param elemRoot the XML representation, assme not <code>null</code>.
    *
    * @exception PSUnknownNodeTypeException if the XML does not conform
    *    its DTD.
    */
   private void fromXml(Element elemRoot)
      throws PSUnknownNodeTypeException
   {
      m_commCtMapper.clear();

      PSXMLDomUtil.checkNode(elemRoot, XML_ELEM_ROOT);

      NodeList nl = elemRoot.getElementsByTagName(XML_ELEM_COMMUNITYMAPPING);
      if (nl == null || nl.getLength() <= 0)
         throw new IllegalArgumentException("must have at least one community");

      int length = nl.getLength();
      for(int i = 0; i < length; i++)
      {
         Node mapping = nl.item(i);
         if (mapping.getNodeType() != Node.ELEMENT_NODE)
            continue;

         addMapping((Element)mapping);
      }
   }

   /**
    * Adds the community and content type mapping from the supplied XML element.
    *
    * The DTD of its XML represenation is:
    * <pre><code>
    * &lt;!ELEMENT ContentType EMPTY>
    * &lt;!ATTLIST  ContentType name CDATA #REQUIRED>
    * &lt;!ATTLIST  ContentType id CDATA #REQUIRED>
    * &lt;!ELEMENT CommunityContentTypeMapping (ContentType* )>
    * &lt;!ATTLIST  CommunityContentTypeMapping communityName CDATA #REQUIRED>
    * &lt;!ATTLIST  CommunityContentTypeMapping communityId CDATA #REQUIRED>
    * </code></pre>
    *
    * @param mapping the XML representation of the mapping. Assume not
    *    <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException if the element does not conform
    *    the DTD.
    */
   @SuppressWarnings("unchecked")
   private void addMapping(Element mapping) throws PSUnknownNodeTypeException
   {
      // get the community
      String name = PSXMLDomUtil.checkAttribute(mapping,
            XML_ATTR_COMMUNITYNAME, true);
      int id = PSXMLDomUtil.checkAttributeInt(mapping,
            XML_ATTR_COMMUNITYID, true);

      PSCommunityCataloger.Community community = PSCommunityCataloger
            .createCommunity(id, name, null);

      // get a set of content types for this community
      Collection<PSEntry> ctSet = new HashSet<PSEntry>();
      Element ctElem = PSXMLDomUtil.getFirstElementChild(mapping);
      while (ctElem != null)
      {
         PSXMLDomUtil.checkNode(ctElem, XML_ELEM_CONTENTTYPE);
         String ctName = PSXMLDomUtil.checkAttribute(ctElem,
               XML_ATTR_CONTENTTYPE_NAME, true);
         String ctId = PSXMLDomUtil.checkAttribute(ctElem,
               XML_ATTR_CONTENTTYPE_ID, true);
         PSEntry ct = new PSEntry(ctId, ctName);

         ctSet.add(ct);

         ctElem = PSXMLDomUtil.getNextElementSibling(ctElem);
      }

      if (m_commCtMapper.containsKey(community))
      {
          ((Collection<PSEntry>) m_commCtMapper.get(community)).addAll(ctSet);
      }
      else
      {
          m_commCtMapper.put(community, ctSet);
      }
   }


   /**
    * Gets a community for the supplied community id.
    *
    * @param id the community id, assume not <code>null</code>.
    *
    * @return the community object, which may be <code>null</code> if cannot
    *    find the community with the supplied id.
    */
   private PSCommunityCataloger.Community getCommunity(Integer id)
   {
      Collection communitySet = m_commCtMapper.keySet();
      Iterator communities = communitySet.iterator();
      PSCommunityCataloger.Community community;
      while (communities.hasNext())
      {
         community = (PSCommunityCataloger.Community) communities.next();
         if (community.getId() == id.intValue())
            return community;
      }

      return null;
   }

   /**
    * It maps a community to a list of content types which are specified for
    * the community. The map key is
    * <code>PSCommunityCataloger.PSCommunity</code> object. The map value is
    * a <code>Collection</code> of zero or more <code>PSEntry</code> object,
    * which contain the id and name of the content type.
    */
   private Map m_commCtMapper = new HashMap();

   /**
    * Constants for XML elements and attributes
    */
   private static final String XML_ELEM_ROOT = "CommunityContentTypeMapper";
   private static final String XML_ELEM_COMMUNITYMAPPING = "CommunityContentTypeMapping";
   private static final String XML_ATTR_COMMUNITYNAME = "communityName";
   private static final String XML_ATTR_COMMUNITYID = "communityId";
   private static final String XML_ELEM_CONTENTTYPE = "ContentType";
   private static final String XML_ATTR_CONTENTTYPE_ID = "id";
   private static final String XML_ATTR_CONTENTTYPE_NAME = "name";
}
