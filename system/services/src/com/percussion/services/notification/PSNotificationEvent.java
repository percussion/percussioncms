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
package com.percussion.services.notification;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A single notification event. This object is immutable after construction.
 * 
 * @author dougrand
 * 
 */
public class PSNotificationEvent implements Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   
   /**
    * Message id to identify the message being sent
    */
   private static AtomicLong ms_mid = new AtomicLong();

   /**
    * An enumeration of event types that may occur. 
    */
   public enum EventType 
   {
      /**
       * A file has been modified. The target object of the notification will be
       * of type {@link java.io.File}
       */
      FILE(true),
      
      /**
       * An cached in-memory object has been invalidated, either because it has
       * been deleted or because it has been modified. This event's target is
       * the modified object's key for the object cache. This uses a queue as
       * the actual invalidation is propagated by ehcache.
       */
      OBJECT_INVALIDATION(false),

      /**
       * An event that is signaled when a content item is changed. The target
       * will be the guid of the changed item.
       */
      CONTENT_CHANGED(false),

      /**
       * An event that is signaled when a site is deleted. The target
       * will be the GUID of the deleted site.
       */
      SITE_DELETED(false),

       /**
        * An event that is signaled when a site is renamed. The target
        * will be the IPSSite object.
        *
        * @see IPSSite
        */
       SITE_RENAMED(false),

      /**
       * The CMS (Core) Server has completed its initialization.
       * No Solutions have been initialized yet, and this is a signal that
       * its is OK to do so.
       */
      CORE_SERVER_INITIALIZED(false),
      
      /**
       * This is the final even in server initialization, called after all notifications for {@link #CORE_SERVER_INITIALIZED}
       * have been processed.
       */
      CORE_SERVER_POST_INIT(false),
      
      /**
       * Signaling the CMS (Core) Server is in the shutdown process.
       */
      CORE_SERVER_SHUTDOWN(false),

      /**
       * A set of relationships have been modified. The target object of the
       * notification is {@link com.percussion.cms.PSRelationshipChangeEvent}.
       */
      RELATIONSHIP_CHANGED(false),
      
      /**
       * An event that is signaled when an asset is deleted. The target
       * will be the GUID of the deleted asset as a String.
       */
      ASSET_DELETED(false),
      
      /**
       * An Event that is signaled when a template is saved.  The target
       * will be the ID of the saved template
       */
      TEMPLATE_SAVED(false),
      
      /**
       * An event triggered when a template is loaded
       */
      TEMPLATE_LOAD(false),
      
      /**
       * An event triggered when a page is deleted
       */
      TEMPLATE_DELETE(false),
      
      /**
       * An event triggered when a page is saved
       */
      PAGE_SAVED(false),
      
      /**
       * An event triggered when a page is loaded
       */
      PAGE_LOAD(false), 
      
      /**
       * An event triggered when a page is deleted
       */
      PAGE_DELETE(false),
      
      /**
       * Trigger when the startup package installer has completed
       */
      STARTUP_PKG_INSTALL_COMPLETE(false),
      
      /**
       * Trigger when the Save Assets Process has completed
       */
      SAVE_ASSETS_PROCESS_COMPLETE(false),
      
      /**
       * An event triggered when a user is deleted
       */
      USER_DELETE(false),
      
      SEARCH_INDEX_ITEM_QUEUED(false),
      
      SEARCH_INDEX_ITEM_PROCESSED(false),
      
      SEARCH_INDEX_STATUS_CHANGE(false),
      
      
      /**
       * Processing folders to queue items for workflow assignment. The target object is an integer of the number of folders,
       * positive to add to count, negative when folders are processed.
       */
      WORKFLOW_FOLDER_ASSIGNMENT_QUEUEING(false),
      
      /**
       * Items are found or updated for workflow folder assignment.  The target object is an integer of the number of
       * items found or processed (positive to add to found count, negative when items are processed).
       */
      WORKFLOW_FOLDER_ASSIGNMENT_PROCESSING(false);

      
      /**
       * If <code>true</code> then this event should use a topic for delivery
       * instead of a queue
       */
      private boolean m_isTopic;

      /**
       * Constructor
       * 
       * @param isTopic set to <code>true</code> to use a topic and
       *           <code>false</code> to use a queue
       */
      private EventType(boolean isTopic) {
         m_isTopic = isTopic;
      }

      /**
       * Should is event type use a topic for delivery
       * 
       * @return <code>true</code> if this should use a topic
       */
      public boolean useTopic()
      {
         return m_isTopic;
      }
   }

   /**
    * The type of the event, initialized in the constructor and never 
    * <code>null</code>
    */
   private final EventType m_type;

   /**
    * The target of the event, may be <code>null</code> after initialization
    * in the constructor.
    */
   private final Object m_target;
   
   /**
    * server type
    */
   private  String serverType;


   public String getServerType() {
      return serverType;
   }

   public void setServerType(String serverType) {
      this.serverType = serverType;
   }

   /**
    * The message id, assigned during construction
    */
   private final long m_id;


   /**
    * Constructor
    * 
    * @param type the event type, never <code>null</code>
    * @param target the target, may be <code>null</code>
    */
   public PSNotificationEvent(EventType type, Object target)
   {
      if (type == null)
      {
         throw new IllegalArgumentException("type may not be null");
      }
      m_type = type;
      m_target = target;
      m_id = ms_mid.getAndIncrement();
   }

   /**
    * Get the event target, which the listener can interrogate but should not
    * alter.
    * 
    * @return the event target, may be <code>null</code>
    */
   public Object getTarget()
   {
      return m_target;
   }

   /**
    * Get the event type, which dictates what kind of an event triggered the
    * notification.
    * 
    * @return the event type, which is never <code>null</code>
    */
   public EventType getType()
   {
      return m_type;
   }
   
   /**
    * Get the message identifier, use only for correlation purposes
    * 
    * @return the id, only repeated over a very, very long period of time as
    * these IDs are allocated incrementally from a long
    */
   public long getId()
   {
      return m_id;
   }

   @Override
   public String toString() {
      return "PSNotificationEvent{" +
              "m_type=" + m_type +
              ", m_target=" + m_target +
              ", m_id=" + m_id +
              '}';
   }
}
