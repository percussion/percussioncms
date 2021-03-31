/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.relationships;

import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.error.PSException;
import com.percussion.pso.utils.PSORequestContext;
import com.percussion.pso.utils.UniqueIdLocatorSet;
import com.percussion.pso.workflow.IPSOWorkflowInfoFinder;
import com.percussion.pso.workflow.PSOWorkflowInfoFinder;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.util.IPSHtmlParameters;

/**
 * Finds parent items for a given content item.
 *
 * @author DavidBenua
 *
 */
public class PSOParentFinder implements IPSOParentFinder
{
   
   private IPSRequestContext requestContext = null; 
   private PSRelationshipProcessorProxy proxy = null;
   private IPSGuidManager gmgr = null; 
   private IPSAssemblyService asm = null;
   private IPSOWorkflowInfoFinder workflow;   

   private static Log log = LogFactory.getLog(PSOParentFinder.class); 

   /**
    * Default constructor.
    */
   public PSOParentFinder()
   {
      workflow = new PSOWorkflowInfoFinder();
   }
   
   /**
    * @see com.percussion.pso.relationships.IPSOParentFinder#findAllParents(java.lang.String, java.lang.String)
    */
   public Set<PSLocator> findAllParents(String contentid, String slotName) 
      throws PSAssemblyException, PSCmsException
   {
      PSLocator dependent = new PSLocator(contentid); 
      return findAllParents(dependent, slotName); 
   }
   
   /**
    * @see com.percussion.pso.relationships.IPSOParentFinder#findAllParents(com.percussion.design.objectstore.PSLocator, java.lang.String)
    */
   public Set<PSLocator> findAllParents(PSLocator dependent, String slotName) 
      throws PSAssemblyException, PSCmsException
   {
      Set<PSLocator> parents = new UniqueIdLocatorSet();
      //add the parents for the current revision
      parents.addAll(findParents(dependent,slotName, false));
      //add the parents for the last public revision
      parents.addAll(findParents(dependent, slotName, true));
      return parents; 
   }
   
   /**
    * @see com.percussion.pso.relationships.IPSOParentFinder#findParents(java.lang.String, java.lang.String, boolean)
    */
   public Set<PSLocator> findParents(String contentid, String slotName, boolean usePublic ) 
   throws PSAssemblyException, PSCmsException
   {
      PSLocator dependent = new PSLocator(contentid);
      return findParents(dependent, slotName, usePublic);
   }
   
   /**
    * @see com.percussion.pso.relationships.IPSOParentFinder#findParents(com.percussion.design.objectstore.PSLocator, java.lang.String, boolean)
    */
   public Set<PSLocator> findParents(PSLocator dependent, String slotName, boolean usePublic ) 
      throws PSAssemblyException, PSCmsException
   {       
      initServices();
      String slotid = getSlotId(slotName); 
      log.debug("Slot name " + slotName +  " id is " + slotid); 
      PSRelationshipFilter filter = new PSRelationshipFilter(); 
      filter.setDependent(dependent); 
      filter.setCategory(PSRelationshipFilter.FILTER_CATEGORY_ACTIVE_ASSEMBLY);
      if(usePublic)
      {
         filter.limitToPublicOwnerRevision(true); 
      }
      else
      {
         filter.limitToEditOrCurrentOwnerRevision(true); 
      }
      filter.setProperty(IPSHtmlParameters.SYS_SLOTID, slotid); 
      PSRelationshipSet rels = proxy.getRelationships(filter); 
      log.debug("there were " + rels.size() + " parents found"); 
      Set<PSLocator> parents = new UniqueIdLocatorSet();
      for(Object relobj : rels)
      {
         PSRelationship rel = (PSRelationship) relobj;
         PSLocator parent = rel.getOwner(); 
         parents.add(parent); 
      }
      
      return parents; 
       
   }
   
   /**
    * @see com.percussion.pso.relationships.IPSOParentFinder#hasOnlyPublicAncestors(java.lang.String, java.lang.String, java.util.List)
    */
   public boolean hasOnlyPublicAncestors(String contentId, String slotName, List<String> validFlags) 
      throws PSAssemblyException, PSException
   {      
      Set<PSLocator> parents = this.findAllParents(contentId, slotName);  
      for(PSLocator p : parents)
      {
         String id = p.getPart(PSLocator.KEY_ID); 
         if(!workflow.IsWorkflowValid(id , validFlags)) 
         {
            return false; 
         }
         if(!hasOnlyPublicAncestors(id, slotName, validFlags))
         {
            return false; 
         }
      }
      return true;
   }
   
   /**
    * Gets the slotid from the slot name
    * @param slotName the slot name. 
    * @return the slot id. 
    * @throws PSAssemblyException if the named slot does not exist.
    */
   protected String getSlotId(String slotName) throws PSAssemblyException
   {
      initServices();
      IPSTemplateSlot slot = asm.findSlotByName(slotName);
      int slotid = slot.getGUID().getUUID();
      return String.valueOf(slotid);
   }
   
   
   private void initServices()
   {
      if(gmgr == null)
      {
         gmgr = PSGuidManagerLocator.getGuidMgr(); 
      }
      if(asm == null)
      {
         asm = PSAssemblyServiceLocator.getAssemblyService(); 
      }
      if(proxy == null)
      {
         try
         {
            requestContext = new PSORequestContext();
            proxy = new PSRelationshipProcessorProxy(
                  PSRelationshipProcessorProxy.PROCTYPE_SERVERLOCAL,requestContext);
         } catch (PSCmsException ex)
         {
            log.error("Unexpected Exception initializing proxy " + ex,ex);
         }
      }
   }

   /**
    * @param proxy the proxy to set
    */
   public void setProxy(PSRelationshipProcessorProxy proxy)
   {
      this.proxy = proxy;
   }

   /**
    * @param gmgr the gmgr to set
    */
   public void setGmgr(IPSGuidManager gmgr)
   {
      this.gmgr = gmgr;
   }

   /**
    * @param asm the asm to set
    */
   public void setAsm(IPSAssemblyService asm)
   {
      this.asm = asm;
   }
}
