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
package com.percussion.rx.publisher.jsf.nodes;

import com.percussion.rx.jsf.PSEditableNodeContainer;
import com.percussion.rx.jsf.PSNodeBase;
import com.percussion.rx.publisher.PSPublisherUtils;
import com.percussion.rx.publisher.jsf.beans.PSDesignNavigation;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.notification.IPSNotificationListener;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationServiceLocator;
import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.services.publisher.IPSContentList;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherException;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This node displays a collection of content lists that are not associated with
 * an edition in some site. This allows such content lists to be found and
 * removed if desired.
 * 
 * @author dougrand
 * 
 */
public class PSContentListViewNode extends PSEditableNodeContainer 
   implements IPSNotificationListener
{
   /**
    * The logger for the site container node.
    */
   private static final Logger ms_log =
         LogManager.getLogger(PSContentListViewNode.class);

   /**
    * The type of view node.
    */
   public enum Type {
      /**
       * Show content lists that are associated with the given site for this
       * node.
       */
      SITE, 
      /**
       * Show content lists that are not currently in use.
       */
      UNUSED;
   }

   /**
    * The type of this node, never <code>null</code>.
    */
   private Type m_type;

   /**
    * The site, may be <code>null</code>.
    */
   private IPSSite m_site;

   /**
    * Ctor.
    * 
    * @param title the title for the node, never <code>null</code> or empty.
    * @param type the type of the node, never <code>null</code>.
    * @param site site, must be non-null if the type is <code>SITE</code>
    * @param key the key for this node in the data model, never
    *            <code>null</code> or empty.
    */
   public PSContentListViewNode(String title, Type type, IPSSite site,
         String key) {
      super(title, "pub-design-content-list-views");
      IPSNotificationService nsvc = 
         PSNotificationServiceLocator.getNotificationService();
      nsvc.addListener(EventType.OBJECT_INVALIDATION, this);
      if (type == null)
      {
         throw new IllegalArgumentException("type may not be null");
      }
      if (type.equals(Type.SITE) && site == null)
      {
         throw new IllegalArgumentException(
               "site must be non-null if type is SITE");
      }
      if (StringUtils.isBlank(key))
      {
         throw new IllegalArgumentException("key may not be null or empty");
      }
      m_type = type;
      m_site = site;
      m_key = key;
   }

   @Override
   public List<? extends PSNodeBase> getChildren() throws PSNotFoundException {
      if (m_children == null)
      {
         m_children = new ArrayList<PSNodeBase>();
         List<IPSContentList> clists;
         if (m_type.equals(Type.SITE))
            clists = getPublisherService().findAllContentListsBySite(
                  m_site.getGUID());
         else
            clists = getPublisherService().findAllUnusedContentLists();
         Collections.sort(clists, new Comparator<IPSContentList>() {
            public int compare(IPSContentList o1, IPSContentList o2)
            {
               return o1.getName().compareToIgnoreCase(o2.getName());
            }});
         Set<IPSGuid> clistshown = new HashSet<>();
         for (IPSContentList c : clists)
         {
            if (clistshown.contains(c.getGUID()))
               continue;
            clistshown.add(c.getGUID());
            PSContentListNode cnode = new PSContentListNode(c);
            cnode.update();
            addNode(cnode);
         }
      }
      return super.getChildren();
   }

   /**
    * Create a new style content list.
    * 
    * @return the outcome, <code>null</code> in this case.
    * @throws PSPublisherException
    */
   public String createContentList() throws PSPublisherException, PSNotFoundException {
      PSContentListNode newcln = create();
      IPSContentList cl = newcln.getContentList();
      cl.setUrl(PSPublisherUtils.SERVLET_URL_PATH + "?sys_deliverytype=filesystem"
            + "&sys_contentlist=" + cl.getName());
      newcln.update();
      return PSContentListNode.handleNewContentList(
            getUnusedContentListNode(), newcln);
   }

   /**
    * Gets the "Unused Content List" node, which is a child node under the
    * root node.
    * @return the node, never <code>null</code>s.
    */
   public PSContentListViewNode getUnusedContentListNode()
   {
      if (m_type.ordinal() == Type.UNUSED.ordinal())
         return this;
      
      PSDesignNavigation nav = (PSDesignNavigation) getModel().getNavigator();
      return nav.getUnsedContentList();
   }
   
   /**
    * Create a legacy content list.
    * 
    * @return the outcome, <code>null</code> in this case.
    * @throws PSPublisherException
    */
   public String createLegacyContentList() throws PSPublisherException, PSNotFoundException {
      PSContentListNode newcln = create();
      IPSContentList cl = newcln.getContentList();
      cl.setUrl("/Rhythmyx/<yourapplication>/<yourresource>?"
            + "sys_deliverytype=filesystem&sys_publish=publish");
      newcln.update();
      return PSContentListNode.handleNewContentList(
            getUnusedContentListNode(), newcln);
   }

   /**
    * Create a new content list and persist it.
    * 
    * @return the content list node, never <code>null</code>.
    */
   protected PSContentListNode create() throws PSNotFoundException {
      IPSContentList newcl = getPublisherService().createContentList("new");
      newcl.setName(getUniqueName("ContentList", false));
      return new PSContentListNode(newcl);
   }

   // see base
   @Override
   protected boolean findObjectByName(String name) throws PSNotFoundException {
      IPSPublisherService pub = PSPublisherServiceLocator.getPublisherService();
      return pub.findContentListByName(name) != null;
   }

   public void notifyEvent(PSNotificationEvent notification)
   {
      if (notification.getType().equals(EventType.OBJECT_INVALIDATION))
      {
         IPSGuid invalidated = (IPSGuid) notification.getTarget();
         if (invalidated.getType() == PSTypeEnum.EDITION.getOrdinal())
         {
            // If an edition changes then the content list ownership can change
            // and content lists might be added or removed from the in use list.
            // Invalidate the current nodes to sync this up.
            m_children = null; 
         }
      }
   }
   
   /**
    * @return <code>true</code> if this node should show the remove menu item,
    * which is only true for unused content lists.
    */
   public boolean getShowRemove()
   {
      return m_type.equals(Type.UNUSED);
   }

   @Override
   public Set<Object> getAllNames()
   {
      final Set<Object> names = new HashSet<>();
      try
      {
         for (final IPSContentList contentList :
               getPublisherService().findAllContentLists(""))
         {
            names.add(contentList.getName());
         }
      }
      catch (Exception e)
      {
         ms_log.error("Problem obtaining content list names", e);
      }
      return names;
   }

   /**
    * Convenience method to access publisher service.
    * @return the publisher service object. Not <code>null</code>.
    */
   private IPSPublisherService getPublisherService()
   {
      return PSPublisherServiceLocator.getPublisherService();
   }

   @Override
   public String returnToListView()
   {
      return "return-to-contentlists";
   }
   
   @Override
   public String getHelpTopic()
   {
      return "ContentlistView";
   }


}
