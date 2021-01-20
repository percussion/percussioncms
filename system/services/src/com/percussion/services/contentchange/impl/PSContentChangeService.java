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
package com.percussion.services.contentchange.impl;

import com.percussion.cms.IPSEditorChangeListener;
import com.percussion.cms.PSEditorChangeEvent;
import com.percussion.cms.PSRelationshipChangeEvent;
import com.percussion.cms.handlers.PSContentEditorHandler;
import com.percussion.server.IPSHandlerInitListener;
import com.percussion.server.IPSRequestHandler;
import com.percussion.server.PSServer;
import com.percussion.services.contentchange.IPSContentChangeHandler;
import com.percussion.services.contentchange.IPSContentChangeService;
import com.percussion.services.contentchange.PSContentChangeServiceLocator;
import com.percussion.services.contentchange.data.PSContentChangeEvent;
import com.percussion.services.contentchange.data.PSContentChangePK;
import com.percussion.services.contentchange.data.PSContentChangeType;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.notification.IPSNotificationListener;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.share.dao.IPSGenericDao;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author JaySeletz
 *
 */
public class PSContentChangeService implements IPSContentChangeService, IPSEditorChangeListener, IPSHandlerInitListener, IPSNotificationListener
{
   private static final Log log = LogFactory.getLog(PSContentChangeService.class);
   
  

   /**
    * Constant for the key used to generate link id's.
    */
   private static final String GUID_MGR_KEY = "PSX_CONTENTCHANGEEVENT";
   
   private IPSGuidManager m_guidMgr;
   
   private List<IPSContentChangeHandler> changeHandlers = new ArrayList<IPSContentChangeHandler>();
   
   
   public PSContentChangeService()
   {
      PSServer.addInitListener(this);
       
   }
   
   @Transactional
   public void contentChanged(PSContentChangeEvent changeEvent)
   {
      Validate.notNull(changeEvent);
      
      Session session = sessionFactory.getCurrentSession();
      try
      {
         PSContentChangeEvent ce = (PSContentChangeEvent)session.get(PSContentChangeEvent.class, new PSContentChangePK(changeEvent.getContentId(), changeEvent.getSiteId(), changeEvent.getChangeType().name()));
         
         if (ce == null)
         {
            session.saveOrUpdate(changeEvent);
         }
      }
      catch (HibernateException e)
      {
          String msg = "database error " + e.getMessage();
          log.error(msg);
          throw new IPSGenericDao.SaveException(msg, e);
      }      
   }

   @Transactional
   public List<Integer> getChangedContent(long siteId, PSContentChangeType changeType)
   {
      Session session = sessionFactory.getCurrentSession();
      
    
      Query query = session.createQuery("from PSContentChangeEvent where changeType = :changeType and siteId = :siteId");
      query.setParameter("changeType", changeType.name());
      query.setParameter("siteId", siteId);
      

      List<PSContentChangeEvent> results = query.list();
      List<Integer> changedContentIds = new ArrayList<Integer>();
      for (PSContentChangeEvent result : results)
      {
         changedContentIds.add(result.getContentId());
      }

      return changedContentIds;
  
   }
   
   public void setGuidManager(IPSGuidManager guidMgr)
   {
       m_guidMgr = guidMgr;
   }
   
   public void setNotificationService(IPSNotificationService notificationSvc)
   {
      notificationSvc.addListener(EventType.RELATIONSHIP_CHANGED, this);
   }
   @Transactional
   public void deleteChangeEvents(long siteId, int contentId, PSContentChangeType changeType)
   {
      Session session = sessionFactory.getCurrentSession();
      
     
      String queryStr = "delete from PSContentChangeEvent where contentId = :contentId and changeType = :changeType";
      if (siteId != -1)
         queryStr += " and siteId = :siteId"; 
      
      Query query = session.createQuery(queryStr);
      query.setParameter("contentId", contentId);
      query.setParameter("changeType", changeType.name());
      if (siteId != -1)
         query.setParameter("siteId", siteId);

      query.executeUpdate();
     
   }
   
   @Transactional
   public void deleteChangeEventsForSite(long siteId)
   {
      Session session = sessionFactory.getCurrentSession();
      

      String queryStr = "delete from PSContentChangeEvent where siteId = :siteId"; 
      
      Query query = session.createQuery(queryStr);
      query.setParameter("siteId", siteId);

      query.executeUpdate();
    
   }
   
   @Transactional
   public void deleteChangeEventsForSite(long siteId, PSContentChangeType changeType)
   {
      Session session = sessionFactory.getCurrentSession();
      
      String queryStr = "delete from PSContentChangeEvent where siteId = :siteId and changeType = :changeType"; 
      
      Query query = session.createQuery(queryStr);
      query.setParameter("siteId", siteId);
      query.setParameter("changeType", changeType.toString());
      query.executeUpdate();
     
   }

   public void addContentChangeHander(IPSContentChangeHandler handler)
   {
      Validate.notNull(handler);
      changeHandlers.add(handler);
   }

   /* (non-Javadoc)
    * @see com.percussion.cms.IPSEditorChangeListener#editorChanged(com.percussion.cms.PSEditorChangeEvent)
    */
   
   @Transactional
   public void editorChanged(PSEditorChangeEvent e)
   {
	  //This didn't work, it ate up resources and fried the server - consider yourself warned - MJE
	  try{
		for (IPSContentChangeHandler handler : changeHandlers)
	      {
	         handler.handleEvent(e);
	      }
		} catch(Exception ex){
			//We don't want to fail the entire transaction on account of this... log and go
	    	log.error("Failed to handle editor change event: " + ExceptionUtils.getStackTrace(ex));  
	    }
   }
   

   /* (non-Javadoc)
    * @see com.percussion.services.notification.IPSNotificationListener#notifyEvent(com.percussion.services.notification.PSNotificationEvent)
    */
   public void notifyEvent(PSNotificationEvent notification)
   {
      Object target = notification.getTarget();
      if (target instanceof PSRelationshipChangeEvent)
      {
         for (IPSContentChangeHandler handler : changeHandlers)
         {
            handler.handleEvent((PSRelationshipChangeEvent) target);
         }
      }

   }

   /* (non-Javadoc)
    * @see com.percussion.server.IPSHandlerInitListener#initHandler(com.percussion.server.IPSRequestHandler)
    */
   public void initHandler(IPSRequestHandler requestHandler)
   {
      if (requestHandler instanceof PSContentEditorHandler)
      {
         PSContentEditorHandler ceh = (PSContentEditorHandler)requestHandler;
         //  Need to get spring proxy of this item to handle transaction annotations
         ceh.addEditorChangeListener((IPSEditorChangeListener) PSContentChangeServiceLocator.getContentChangeService());
      }
   }

   /* (non-Javadoc)
    * @see com.percussion.server.IPSHandlerInitListener#shutdownHandler(com.percussion.server.IPSRequestHandler)
    */
   public void shutdownHandler(IPSRequestHandler requestHandler)
   {
      // noop
   }
   
   
   private SessionFactory sessionFactory;
   
   public SessionFactory getSessionFactory()
   {
      return sessionFactory;
   }

   public void setSessionFactory(SessionFactory sessionFactory)
   {
      this.sessionFactory = sessionFactory;
   }

   


}
