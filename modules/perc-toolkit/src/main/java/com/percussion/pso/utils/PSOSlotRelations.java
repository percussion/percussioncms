/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
/*
 * com.percussion.pso.utils PSOSlotRelations.java
 *  
 * @author DavidBenua
 *
 */
package com.percussion.pso.utils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.cms.objectstore.PSAaRelationshipList;
import com.percussion.cms.objectstore.PSActiveAssemblyProcessorProxy;
import com.percussion.cms.objectstore.PSProcessorProxy;
import com.percussion.cms.objectstore.PSSlotType;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;

/**
 * This class provides an API for reading and writing the contents of a slot
 * as a List of PSAaRelationships.  
 * The content Web services API provides methods for reading and writing individual relationships,
 * or all of the relationships for a given parent, but it does not provide a way to 
 * view and modify the contents of a specific slot.  
 * This is accomplished by wrapping the legacy Active Assembly Processor Proxy using objects 
 * that are consistent with the Java service API.  
 * These methods should only be used when the Java Service API cannot be used for 
 * some reason. 
 *
 * @author DavidBenua
 *
 */
public class PSOSlotRelations
{
   
   private static IPSGuidManager gmgr = null; 
   
   /**
    * Logger for this class
    */
   private static final Log log = LogFactory.getLog(PSOSlotRelations.class);

   /**
    * Static methods only, never constructed.  
    */
   private PSOSlotRelations()
   {     
   }
   
   /**
    * Gets the contents of a slot. 
    * @param owner the owning items GUID
    * @param slot the Slot template
    * @param session the Percussion CMS session id. 
    * @return a List of PSAaRelations.  Never <code>null</code> but may be empty. 
    * @throws PSCmsException
    */
   @SuppressWarnings("unchecked")
   public static List<PSAaRelationship> getSlotRelations(IPSGuid owner, IPSTemplateSlot slot, String session) 
      throws PSCmsException
   {
      initServices();
      PSActiveAssemblyProcessorProxy proxy = getProxy(session);
      PSSlotType legacySlot = new PSSlotType(slot);
      PSLocator ownLoc = gmgr.makeLocator(owner); 
      PSAaRelationshipList relList = proxy.getSlotRelationships(ownLoc, legacySlot, 0); 
      List<PSAaRelationship> relations = fromRelList(relList); 
      return relations;
   }
   
   
   /**
    * Saves a list of relationships. These relationships have already been persisted
    * at least once, and are being updated. 
    * @param relations the list of relationships; 
    * @param session the Percussion CMS session id. Never <code>null</code>
    * @throws PSCmsException
    */
   public static void saveSlotRelations(List<PSAaRelationship> relations, String session) 
      throws PSCmsException
   {
      PSActiveAssemblyProcessorProxy proxy = getProxy(session); 
      PSAaRelationshipList relList = toRelList(relations);  
      log.debug("Saving " + relList.size() + " relationships"); 
      proxy.save(relList);
   }
   
   
   /**
    * Adds new relations to a slot
    * @param relations a list of new relationships to add. 
    * @param session the Percussion CMS session id; 
    * @throws PSCmsException
    */
   public static void addSlotRelations(List<PSAaRelationship> relations, String session) 
      throws PSCmsException
   {
      PSActiveAssemblyProcessorProxy proxy = getProxy(session); 
      PSAaRelationshipList relList = toRelList(relations); 
      proxy.addSlotRelationships(relList, -1);
   }
   
   
   /**
    * Removes slot relations
    * @param relations the relations to remove
    * @param session the Percussion CMS session id.
    * @throws PSCmsException
    */
   public static void removeSlotRelations(List<PSAaRelationship> relations, String session)
      throws PSCmsException
   {
      PSActiveAssemblyProcessorProxy proxy = getProxy(session); 
      PSAaRelationshipList relList = toRelList(relations); 
      proxy.removeSlotRelations(relList); 
   }
   
   /**
    * reorders a list of slot relations. 
    * @param relations the relationships to reorder
    * @param top if <code>true</code> move the relationships to the top of the slot. 
    * Otherwise, they will be placed at the end of the slot. 
    * @param session the Percussion CMS session id. 
    * @throws PSCmsException
    */
   public static void reorderSlotRelations(List<PSAaRelationship> relations, boolean top, String session)
      throws PSCmsException 
   {
      PSActiveAssemblyProcessorProxy proxy = getProxy(session); 
      PSAaRelationshipList relList = toRelList(relations);
      int index = (top)? 0 : -1; 
      proxy.reArrangeSlotRelationships(relList, index); 
   }
   
   /**
    * Gets the ActiveAssembly Proxy from the session id
    * @param session the Percussion CMS session id. Never <code>null</code>
    * @return the proxy to use.  
    * @throws PSCmsException
    */
   private static PSActiveAssemblyProcessorProxy getProxy(String session) 
      throws PSCmsException
   {      
      PSActiveAssemblyProcessorProxy proxy = null;
      log.debug("Session Id is " + session);
      PSSecurityToken token = new PSSecurityToken(session); 
      PSRequest req = new PSRequest(token);
      PSRequestContext reqCtx = new PSRequestContext(req); 
      proxy = new PSActiveAssemblyProcessorProxy(PSProcessorProxy.PROCTYPE_SERVERLOCAL,reqCtx);
      return proxy;
   }

   /**
    * Convert a List of relations to a relationship list. 
    * @param relations the relations.
    * @return the relationship list
    */
   private static PSAaRelationshipList toRelList(List<PSAaRelationship> relations)  
   {
      PSAaRelationshipList relList = new PSAaRelationshipList(); 
      for(PSAaRelationship rel : relations)
      {
         relList.add(rel); 
      }
      return relList; 
   }

   /**
    * Convert a relationship list to a List of relations. 
    * @param relList the relationship list.
    * @return the List of relations, never <code>null</code> but may be <code>empty</code>
    * @throws PSCmsException
    */
   @SuppressWarnings("unchecked")
   private static List<PSAaRelationship> fromRelList(PSAaRelationshipList relList)
      throws PSCmsException
   {
      List<PSAaRelationship> relations = new ArrayList<PSAaRelationship>(); 
      Iterator<PSAaRelationship> itr = relList.iterator(); 
      while(itr.hasNext())
      {
         relations.add(itr.next()); 
      }
      return relations;
   }
   /**
    * Initialize static Java service pointers. 
    */
   private static void initServices()
   {
      if(gmgr == null)
      {
         gmgr = PSGuidManagerLocator.getGuidMgr(); 
      }
   }
    
}
