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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.services.assembly.impl.nav;

import com.percussion.cms.PSCmsException;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSProxyNode;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.impl.nav.PSNavHelper.PSSectionTypeEnum;
import com.percussion.services.contentmgr.IPSNode;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.jsr170.PSNodeIterator;
import com.percussion.utils.jsr170.PSProperty;
import com.percussion.utils.jsr170.PSPropertyIterator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;

/**
 * This invocation handler wraps the behavior of the content node, augmenting it
 * with additional properties. The navon node appears to have children, which
 * are loaded using the relationship manager and the nav configuration.
 *
 * @author dougrand
 *
 */
public class PSNavonNodeInvocationHandler implements InvocationHandler
{
   private static final String NAV_PROXIED_NODE = "nav:proxiedNode";

   private static final String NAV_PREFIX = "nav:";

   private static final String NAV_LANDING_PAGE = "nav:landingPage";

   private static final String NAV_AXIS = "nav:axis";

   private static final String NAV_URL = "nav:url";

   private static final String NAV_LEAF = "nav:leaf";

   private static final String NAV_SELECTED_IMAGE = "nav:selectedImage";

   private static final String[] NAV_PROPERTIES =
   {
      NAV_URL, NAV_LANDING_PAGE, NAV_AXIS, NAV_LEAF, NAV_SELECTED_IMAGE,
      NAV_PROXIED_NODE
   };
   private static final String NAV_TYPE_FIELD = "rx:no_type";

   private static final String NAV_EXTERNAL_URL_FIELD = "rx:no_externalUrl";

   /**
    * This contains the node that is being proxied
    */
   private IPSNode m_containedNode = null;

   /**
    * The proxy node itself, which contains {@link #m_containedNode} node.
    */
   private IPSProxyNode m_proxyNode = null;

   /**
    * Contains the axis of the node. Set in the ctor, never modified after.
    */
   private PSNavAxisEnum m_axis = PSNavAxisEnum.NONE;

   /**
    * The landing page node. Initialized on first access, but may be <code>null</code>
    */
   private Property m_landingPage = null;

   /**
    * Holds the axis property object, calculated on first access.
    */
   private Property m_axisProp = null;

   /**
    * The URL for the landing page. Initialized on first access.
    */
   private Property m_landingPageUrl = null;

   /**
    * True if this is a leaf. Initialized on first access.
    */
   private Property m_isLeaf = null;

   /**
    * The specific selected nav image. Initialized on first access.
    */
   private Property m_selectedNavImage = null;

   /**
    * Related children for image and managed nav submenu. Initialized on first
    * access.
    */
   private MultiMap m_children = null;

   /**
    * Navigation configuration, never <code>null</code> after ctor
    */
   private PSNavHelper m_helper;

   /**
    * Set to <code>true</code> when the landing page information is initialized
    */
   private boolean m_landingPageInitialized = false;

   /**
    * The section type defaulted to the PSSectionTypeEnum.section
    */
   private PSSectionTypeEnum m_sectionType = PSSectionTypeEnum.section;

   /**
    * Construct the invocation handler
    *
    * @param helper the navigation helper, never <code>null</code>
    * @param current the current node, never <code>null</code>
    * @param axis the axis, never <code>null</code>
    * @param type the type of the section, if <code>null</code> set it to PSSectionTypeEnum.section
    */
   public PSNavonNodeInvocationHandler(PSNavHelper helper, IPSNode current,
         PSNavAxisEnum axis, PSSectionTypeEnum type) {
      if (helper == null)
      {
         throw new IllegalArgumentException("helper may not be null");
      }
      if (current == null)
      {
         throw new IllegalArgumentException("current may not be null");
      }
      if (axis == null)
      {
         throw new IllegalArgumentException("axis may not be null");
      }
      if(type == null)
      {
         type = PSSectionTypeEnum.section;
      }

      m_helper = helper;
      m_axis = axis;
      m_containedNode = current;
      m_sectionType = type;
   }


   /**
    * Sets the proxy node, which the current node itself.
    *
    * @param proxyNode the new proxy node, never <code>null</code>.
    */
   public void setProxyNode(IPSProxyNode proxyNode)
   {
      m_proxyNode = proxyNode;
   }

   @SuppressWarnings("unchecked")
   public Object invoke(Object proxy, Method method, Object[] args)
         throws Throwable
   {
      String mname = method.getName();
      try {
         if (mname.equals("getProperty"))
         {
            return getProperty((Node) proxy, method, args);
         }
         else if (mname.startsWith("getNode"))
         {
            return getNodes(method, args);
         }
         else if (mname.startsWith("getAncestors"))
         {
            return getAncestors();
         }
         else if (mname.startsWith("getProperties"))
         {
            PSPropertyIterator iter =
               (PSPropertyIterator) method.invoke(m_containedNode, args);

            // Add nav properties
            Map<String,Property> map = new HashMap<>(iter.getMap());
            for(String prop : NAV_PROPERTIES)
            {
               Property p = getNavProperty((Node) proxy, prop);
               map.put(prop, p);
            }
            iter.setMap(map);

            return iter;
         }
         else if (mname.equals("getAncestors"))
         {
            return getAncestors();
         }
         else if (mname.equals("getRoot"))
         {
            return m_helper.getRoot(m_proxyNode);
         }
         else
         {
            return method.invoke(m_containedNode, args);
         }
      } catch (InvocationTargetException e) {
         throw e.getCause();
      }
   }

   /**
    * Figure out whether we're calling the proxied object's getNodes method or
    * intercepting
    *
    * @param method the method, never <code>null</code>
    * @param args the arguments, never <code>null</code>
    * @return the result
    * @throws IllegalArgumentException
    * @throws IllegalAccessException
    * @throws InvocationTargetException
    * @throws PSCmsException
    * @throws RepositoryException
    * @throws PSFilterException
    */
   private Object getNodes(@SuppressWarnings("unused")
   Method method, Object[] args) throws IllegalArgumentException,
         IllegalAccessException, InvocationTargetException, PSCmsException,
         RepositoryException, PSFilterException
   {
      if (args == null || args.length == 0)
      {
         return method.invoke(m_containedNode, args); // getNodes()
      }
      else
      {
         if(m_sectionType == PSSectionTypeEnum.section)
            loadNavChildren();
         else
            m_children = new MultiHashMap();

         // We treat getNode(String name) and getNodes(String pathpattern)
         // the same here.
         return new PSNodeIterator(m_children, (String) args[0]);
      }
   }

   /**
    * Dispatch to interceptor for some property calls
    *
    * @param proxy the proxy node
    * @param method the method, never <code>null</code>
    * @param args the arguments, never <code>null</code>
    *
    * @return the result
    * @throws IllegalArgumentException
    * @throws IllegalAccessException
    * @throws InvocationTargetException
    * @throws RepositoryException
    * @throws PSCmsException
    * @throws PSFilterException
    * @throws PSAssemblyException
    */
   private Object getProperty(Node proxy, Method method, Object[] args)
         throws IllegalArgumentException, IllegalAccessException,
         InvocationTargetException, RepositoryException, PSCmsException,
         PSAssemblyException, PSFilterException
   {
      String property = null;

      if (args.length == 1 && args[0].getClass().equals(String.class))
      {
         property = (String) args[0];
      }

      if (property.startsWith(NAV_PREFIX))
      {
         return getNavProperty(proxy, property);
      }
      else
      {
         return method.invoke(m_containedNode, args);
      }
   }

   /**
    * Find and return the requisite navigation property
    *
    * @param proxy the proxy node
    * @param property the property name, assumed not <code>null</code> or
    *           empty
    *
    * @return the property, never <code>null</code>
    * @throws RepositoryException
    * @throws PSCmsException
    * @throws PSFilterException
    * @throws PSAssemblyException
    */
   private Property getNavProperty(Node proxy, String property)
         throws RepositoryException, PSCmsException, PSAssemblyException,
         PSFilterException
   {
      Property rval = null;

      if (property.equals(NAV_AXIS))
      {
         if (m_axisProp == null)
         {
            m_axisProp = new PSProperty(NAV_AXIS, m_containedNode, m_axis
                  .name());
         }
         rval = m_axisProp;
      }
      else if (property.equals(NAV_LANDING_PAGE))
      {
         if (m_landingPage == null)
         {
            m_landingPage = loadLandingPage();
         }
         rval = m_landingPage;
      }
      else if (property.equals(NAV_URL))
      {
         if (m_landingPageUrl == null)
         {
            m_landingPageUrl = loadLandingPageUrl();
         }
         rval = m_landingPageUrl;
      }
      else if (property.equals(NAV_LEAF))
      {
         if (m_isLeaf == null)
         {
            loadNavChildren();
            m_isLeaf = new PSProperty(NAV_LEAF, m_containedNode, m_children
                  .size() == 0 ? Boolean.TRUE : Boolean.FALSE);
         }
         rval = m_isLeaf;
      }
      else if (property.equals(NAV_SELECTED_IMAGE))
      {
         if (m_selectedNavImage == null)
         {
            loadNavChildren();
            m_selectedNavImage = new PSProperty(NAV_SELECTED_IMAGE,
                  m_containedNode, m_helper.findSelectedImage(proxy));
         }
         rval = m_selectedNavImage;
      }
      else if (property.equals(NAV_PROXIED_NODE))
      {
         return new PSProperty(NAV_PROXIED_NODE, m_containedNode,
               m_containedNode);
      }
      return rval;
   }

   /**
    * Gets all ancestor nodes of the current node.
    *
    * @return the ancestor nodes, where the 1st element is the root node, 2nd
    * element is the direct child node of the root, ... etc. The last element
    * is the direct parent of the current node. It never be <code>null</code>,
    * but may be empty.
    *
    * @throws ItemNotFoundException
    * @throws AccessDeniedException
    * @throws RepositoryException
    */
   private List<Node> getAncestors() throws ItemNotFoundException,
         AccessDeniedException, RepositoryException
   {
      List<Node> ancesters = new ArrayList<>();
      Node node = m_containedNode;
      while (node.getParent() != null)
      {
         ancesters.add(node.getParent());
         node = node.getParent();
      }
      Collections.reverse( ancesters );

      return ancesters;
   }

   private void loadNavChildren() throws PSCmsException, RepositoryException,
         PSFilterException
   {
      if (m_children != null)
         return;

      m_children = m_helper.findNavChildren(m_axis, m_containedNode);
   }

   private Property loadLandingPageUrl() throws RepositoryException,
         PSCmsException, PSAssemblyException
   {
      //Default the landing page url to # first.
      m_landingPageUrl = new PSProperty(NAV_URL, m_containedNode,"#");
      PSSectionTypeEnum type = null;
      try
      {
         type = PSSectionTypeEnum.valueOf(m_containedNode.getProperty(NAV_TYPE_FIELD).getString());
      }
      catch(Exception e)
      {
         type = PSSectionTypeEnum.section;
      }

      if(type ==  PSSectionTypeEnum.section || type == PSSectionTypeEnum.sectionlink)
      {
         //Load the landing page if needed.
         if(m_landingPage == null)
         {
            m_landingPage = loadLandingPage();
         }
         //If landing page is not null even after loading
         if(m_landingPage != null)
         {
            IPSNode node = (IPSNode) m_landingPage.getNode();
            PSLegacyGuid cid = (PSLegacyGuid) node.getGuid();
            IPSGuid tid = m_helper.getTemplateForContent(cid);
            if (tid != null)
            {
               IPSAssemblyService asm = PSAssemblyServiceLocator
               .getAssemblyService();
               IPSAssemblyResult item = (IPSAssemblyResult) m_helper
               .getAssemblyItem();
               String url = asm.getLandingPageLink(item, node, tid);
               if (url != null)
               {
                  m_landingPageUrl = new PSProperty(NAV_URL, node, url);
               }
            }
         }
      }
      else if(type == PSSectionTypeEnum.externallink)
      {
         String url = m_containedNode.getProperty(NAV_EXTERNAL_URL_FIELD).getString();
         m_landingPageUrl = new PSProperty(NAV_URL, m_containedNode,url);
      }
      return m_landingPageUrl;
   }

   /**
    * Finds the landing page and returns it as Property object.
    * @return landing page property may be <code>null</code>.
    * @throws PSCmsException
    * @throws RepositoryException
    */
   private Property loadLandingPage() throws PSCmsException,
         RepositoryException
   {
      if (!m_landingPageInitialized )
      {
         m_landingPageInitialized = true;
         m_landingPage = m_helper.findLandingPage(m_containedNode);
      }
      return m_landingPage;
   }

}
