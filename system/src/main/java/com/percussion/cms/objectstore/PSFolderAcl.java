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

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class represents a <code>PSObjectAcl</code> for a folder.  It is used
 * when restoring a folder's acl from the database without the context of the
 * enclosing folder object, so that it may provide any folder level information
 * required for calculating security permissions at runtime.
 */
public class PSFolderAcl extends PSObjectAcl
{
   /**
    * The default constructor to create an empty folder ACL.
    * 
    * @param contentId The id of the folder for which this ACL defines
    * security settings.
    * @param communityId The community id of the folder for which this ACL 
    * defines security settings.
    */
   public PSFolderAcl(int contentId, int communityId)
   {
      super();
      m_contentId = contentId;
      m_communityId = communityId;
   }

   /**
    * Just like {@link #PSFolderAcl(Element)}, in addition, there are couple
    * extra parameters. 
    *
    * @param contentId The id of the folder for which this ACL defines
    *    security settings.
    * @param communityId The community id of the folder for which this ACL 
    *    defines security settings.
    */
   public PSFolderAcl(Element element, int contentId, int communityId)
      throws PSUnknownNodeTypeException
   {
      super(element);
      m_contentId = contentId;
      m_communityId = communityId;
   }

   /**
    * Constructs this object from the supplied element. See 
    * <code>fromXml()</code> for the expected form of xml.
    *
    * @param element the element to load from, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if element is <code>null</code>
    * @throws PSUnknownNodeTypeException if element is not of expected format.
    */
   public PSFolderAcl(Element element)
      throws PSUnknownNodeTypeException
   {
      super(element);
      loadState(element);
   }

   /**
    * Constructs this object from the supplied element.
    * The xml format is that expected by <code>PSXObjectAcl.fromXml()</code>
    * with additional "contentid" and "communityId" attributes set on the root 
    * element.
    *
    * @param src the element to load from, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if <code>src</code> is
    * <code>null</code>
    * @throws PSUnknownNodeTypeException if element is not of expected format.
    */
   public void fromXml(Element src)
      throws PSUnknownNodeTypeException
   {
      super.fromXml(src);
      loadState(src);
   }
   
   /*
    *  (non-Javadoc)
    * @see com.percussion.cms.objectstore.IPSCmsComponent#toXml(org.w3c.dom.Document)
    */
   public Element toXml(Document doc)
   {
      Element root = super.toXml(doc);
      root.setAttribute(XML_ATTR_COMMUNITYID, String.valueOf(m_communityId));
      root.setAttribute(XML_ATTR_CONTENTID, String.valueOf(m_contentId));
      
      return root;
   }
   
   
   /**
    * Get the community id of the folder for which this ACL defines
    * security settings.
    * 
    * @return The community id, or <code>-1</code> if the folder is accessable
    * by all communities.
    */
   public int getCommunityId()
   {
      return m_communityId;
   }
   
   /**
    * Get the id of the folder for which this ACL defines security settings.
    * 
    * @return The id.
    */
   public int getContentId()
   {
      return m_contentId;
   }
   
   /**
    * Extract folder acl state from the provided element.
    * 
    * @param src The element containing the folder acl state, assumed not 
    * <code>null</code>.  See <code>fromXml()</code> for the expected form of 
    * the xml.
    * 
    * @throws PSUnknownNodeTypeException if the expected values cannot be
    * found.
    */
   private void loadState(Element src) throws PSUnknownNodeTypeException
   {
      m_communityId = getIntAttrVal(src, XML_ATTR_COMMUNITYID);
      m_contentId = getIntAttrVal(src, XML_ATTR_CONTENTID);
   }
   
   /**
    * Get the specified attribute value as an integer.
    * 
    * @param src The element containing the attribute, assumed not 
    * <code>null</code>.
    * @param attrName The name of the attribute, assumed not <code>null</code> 
    * or empty.
    * 
    * @return The attribute value.
    * 
    * @throws PSUnknownNodeTypeException if the expected attribute value cannot 
    * be found or does not represent an integer value.
    */
   private int getIntAttrVal(Element src, String attrName) 
      throws PSUnknownNodeTypeException
   {
      String temp = PSComponentUtils.getRequiredAttribute(src, 
         attrName);
      try 
      {
         return Integer.parseInt(temp);
      }
      catch (Exception ex) 
      {
         Object[] args = {src.getNodeName(), attrName, 
            temp};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
   }
   
   /**
    * The community id of this acl's folder, or <code>-1</code> if the folder
    * is accessable by all communities.  Set during the ctor, never modified
    * after that.
    */
   private int m_communityId;
   
   /**
    * The content id of this acl's folder, set during the ctor, never modified
    * after that.
    */
   private int m_contentId;
   
   private static final String XML_ATTR_COMMUNITYID = "communityId";
   private static final String XML_ATTR_CONTENTID = "contentId";
   
}
