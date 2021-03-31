/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.finder;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.jcr.RepositoryException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.pso.utils.PSORequestContext;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSSlotContentFinder;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.impl.finder.PSBaseSlotContentFinder;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;

/**
 * This slot finder returns all of the Active Assembly parents of the current item.  The results 
 * can be limited by Slot name. 
 * The parameters may be supplied in the usual ways: via the slot finder parameters in the slot, or 
 * vai the template. 
 * <ul>
 * <li><code>max_results</code> limits the total number of items returned in the slot</li>
 * <li><code>order_by</code> sorts the results by a field (or property).  Use 
 * <code>ASC</code> and <code>DESC</code> to control direction of the sort. </li>
 * <li><code>limit_public</code> limits the results to items where the public revision has a 
 * relationship to this item.  Supply any non-blank value.  </li>
 * <li><code>source_slot</code> limits the results to those items where the current item is in the 
 * named slot</li> 
 * </ul>
 * 
 *
 * @author DavidBenua
 *
 */
public class PSOReverseSlotContentFinder extends PSBaseSlotContentFinder  implements IPSSlotContentFinder 
{
   /**
    * Logger for this class
    */
   private static final Log log = LogFactory.getLog(PSOReverseSlotContentFinder.class);
   
   /**
    * GUID Manager Service 
    */
   private static IPSGuidManager gmgr = null;
   /**
    * Assembly Service
    */
   private static IPSAssemblyService asm = null; 
   
   
   /**
    * Initializes the Percussion CMS services pointers. 
    * Used to prevent calls to these services during extension registration. 
    */
   private static void initServices()
   {
      if(gmgr == null)
      {
         asm = PSAssemblyServiceLocator.getAssemblyService(); 
         gmgr = PSGuidManagerLocator.getGuidMgr(); 
      }
   }
   
   /**
    * Default Constructor. 
    */
   public PSOReverseSlotContentFinder()
   {
      super();
   }
   /**
    * @see com.percussion.services.assembly.impl.finder.PSBaseSlotContentFinder#getSlotItems(com.percussion.services.assembly.IPSAssemblyItem, com.percussion.services.assembly.IPSTemplateSlot, java.util.Map)
    */
   @Override
   @SuppressWarnings("unchecked")
   protected Set<SlotItem> getSlotItems(IPSAssemblyItem sourceItem, IPSTemplateSlot slot, 
          Map<String, Object> selectors) 
      throws RepositoryException, PSFilterException, PSAssemblyException
   {
      Set<SlotItem> rval = new LinkedHashSet<SlotItem>();
      Map<String, ? extends Object> args = slot.getFinderArguments();
      String template = null;
      String sourceSlotName = null; 
      String orderBy = null; 
      String limitToPublic = null; 
      int maxResults = -1; 
      
      template = getValue(args, selectors, PARAM_TEMPLATE, null);
      sourceSlotName = getValue(args, selectors, PARAM_SOURCESLOT, null); 
      orderBy = getValue(args, selectors, PARAM_ORDERBY, null);
      limitToPublic = getValue(args, selectors, PARAM_LIMITPUBLIC, null );
      if(log.isDebugEnabled())
      {
         log.debug("Starting Reverse Slot Content Finder. Template="+ template + " source slot=" 
               + sourceSlotName + " order by " + orderBy + " public=" + limitToPublic); 
      }
      if (StringUtils.isBlank(template))
      {
         throw new IllegalArgumentException("template is a required argument");
      }
            
      initServices();
      IPSTemplateSlot sourceSlot = null; 
      IPSAssemblyTemplate slotTemplate = asm.findTemplateByName(template);
      if(StringUtils.isNotBlank(sourceSlotName))
      {
         sourceSlot = asm.findSlotByName(sourceSlotName);
      }
      PSRelationshipFilter filter = new PSRelationshipFilter();
      PSLocator sourceLoc = gmgr.makeLocator(sourceItem.getId()); 
      filter.setDependent(sourceLoc); 
      filter.setCategory(PSRelationshipFilter.FILTER_CATEGORY_ACTIVE_ASSEMBLY); 
      if(StringUtils.isNotBlank(limitToPublic))
      {
         log.debug("limiting to Public Revision"); 
         filter.limitToPublicOwnerRevision(true);
      }
      else
      {
         log.debug("limiting to Current Revision");
         filter.limitToEditOrCurrentOwnerRevision(true); 
      }
      
      //run Relationship API as system user. 
      IPSRequestContext req = new PSORequestContext(); 
      PSRelationshipSet relations;
      try
      {
         PSRelationshipProcessorProxy proxy = new PSRelationshipProcessorProxy(
               PSRelationshipProcessorProxy.PROCTYPE_SERVERLOCAL,req); 
         relations = proxy.getRelationships(filter);
      } catch (PSCmsException ex)
      {
         log.error("Unexpected CMS Exception " + ex.getMessage(), ex); 
         throw new PSAssemblyException(0, ex, ex.getMessage() );
      } 
      int sortrank = 0; //note: sortrank isn't really used here, but the SlotItem requires one 
      Iterator<PSRelationship> iter = relations.iterator(); 
      while(iter.hasNext())
      {
         PSRelationship rel =  iter.next();
         if(isRelationshipInSlot(rel, sourceSlot))
         {
            IPSGuid guid = gmgr.makeGuid(rel.getOwner()); 
            SlotItem si = new SlotItem(guid, slotTemplate.getGUID(), sortrank);
            sortrank++;
            rval.add(si);
         }
     }
     if(StringUtils.isNotBlank(orderBy) && rval.size() > 1)
     {
        rval = reorder(rval, orderBy);           
     }
   
     return rval;
   }

   /**
    * Determines if the relationship given references the slot. 
    * @param rel the relationship to check. 
    * @param slot the slot. If <code>null</code> all relationships return <code>true</code>
    * @return <code>true</code> if the relationship meets the slot criteria. 
    */
   private boolean isRelationshipInSlot(PSRelationship rel, IPSTemplateSlot slot)
   {
      if(slot == null)
      {
         return true; 
      }
      String relSlotId = rel.getProperty(IPSHtmlParameters.SYS_SLOTID);
      if(relSlotId == null)
      {
         return true;
      }
      String ourSlotId = String.valueOf(slot.getGUID().getUUID());
      return ourSlotId.equals(relSlotId); 
   }
   /**
    * Returns the slot type.  Reverse slots are Computed slots.  
    * @see com.percussion.services.assembly.IPSSlotContentFinder#getType()
    */
   public Type getType()
   {
      return com.percussion.services.assembly.IPSSlotContentFinder.Type.COMPUTED;
   }

   /**
    * Order By Parameter
    */
   public static final String PARAM_ORDERBY = "order_by";
   /**
    * Source Slot Parameter. 
    */
   public static final String PARAM_SOURCESLOT = "source_slot";
   /**
    * Limit to Public Revision.  Any non-empty value will cause the finder to return only items whose
    * public revision contains a relationship to the current item. 
    */
   public static final String PARAM_LIMITPUBLIC = "limit_public"; 


   /**
    * @param asm The asm to set. Only for use in Unit Tests
    */
   public static void setAsm(IPSAssemblyService asm)
   {
      PSOReverseSlotContentFinder.asm = asm;
   }

   /**
    * @param gmgr The gmgr to set. Only for use in Unit Tests 
    */
   public static void setGmgr(IPSGuidManager gmgr)
   {
      PSOReverseSlotContentFinder.gmgr = gmgr;
   }
   
}
