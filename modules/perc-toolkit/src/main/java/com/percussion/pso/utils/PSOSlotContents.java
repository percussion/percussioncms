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
 * com.percussion.pso.utils PSOSlotContents.java
 *  
 * @author DavidBenua
 *
 */
package com.percussion.pso.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;

/**
 * Tool for loading the contents of a slot as PSAaRelationship objects.
 * 
 * The standard load method in Web Services 
 * {@link IPSContentWs#loadContentRelations(PSRelationshipFilter, boolean)}
 * loads all content relationships, regardless of slot.  This tool uses this method
 * and returns a filtered and sorted list of <code>PSAaRelationship</code> 
 * objects.
 * <p>
 * Note that this tool is based on relationships and does not work on autoslots.  
 * If you are assembling a item, consider using {@link com.percussion.pso.jexl.PSOSlotTools#getSlotContents(com.percussion.services.assembly.IPSAssemblyItem, String, java.util.Map)}
 * instead of this method. 
 * <p>
 * The returned slot has not been filtered by any item filters, and the revisions
 * of the dependent items will not have been set. 
 * <p>
 * The implementation of this method is highly dependent on the sortrank property
 * of the relationships having been set correctly. The behavior when the sortrank
 * is missing or invalid (e.g. 0 or -1) may be inconsistent.   
 *  
 * 
 * 
 *
 * @see com.percussion.webservices.content.IPSContentWs#loadContentRelations(PSRelationshipFilter, boolean)
 * @author DavidBenua
 *
 */
public class PSOSlotContents
{
   private static Log log = LogFactory.getLog(PSOSlotContents.class);
   
   private static IPSContentWs cws = null; 
   private static IPSGuidManager gmgr = null; 
   private static IPSAssemblyService mAss;
   
   /**
    * Default constructor.  
    */
   public PSOSlotContents()
   {
   }
   
  
   /**
    * Gets the contents of a slot. 
    * @param parentItem the parent item
    * @param slot the slot 
    * @return all relationships in the given slot for this parent. 
    * Never <code>null</code>. May be <code>empty</code>.
    * @throws PSErrorException
    */
   public List<PSAaRelationship> getSlotContents(IPSGuid parentItem, IPSGuid slot) 
      throws PSErrorException
   {
      initServices();

      SortedSet<PSAaRelationship> slotRelations = 
         new TreeSet<PSAaRelationship>(new SlotItemComparator());
      
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setCategory(PSRelationshipFilter.FILTER_CATEGORY_ACTIVE_ASSEMBLY);
      PSLocator oloc = gmgr.makeLocator(parentItem); 
      log.debug("Owner locator is  " + oloc); 
      log.debug("Owner Slot GUID is " + slot);
      
      filter.setOwner(oloc); 
      
      // load ALL AA relations for the parent item
      // Note that Slot will be null unless we load the reference info. 
      List<PSAaRelationship> allRelations = cws.loadContentRelations(filter, true);  
      log.debug("this item has " + allRelations.size() + " active assembly children "); 
      
      for(PSAaRelationship rel : allRelations)
      {
         //log.debug("returned slot GUID is " + rel.getSlotId()); 
         if(slot.equals(rel.getSlotId()))
         { //this item is in our slot. Order will be determined by the comparator.
            //log.debug("found matching slot"); 
            slotRelations.add(rel); 
         }
//         else
//         {
//            log.debug("no match on slot"); 
//         }
      }
      
      //we just need our slot as a list.  
      List<PSAaRelationship> outputRelations = new ArrayList<PSAaRelationship>(slotRelations); 
      return outputRelations; 
   }
   
   
   private static void initServices()
   {
      if(cws == null)
      {
         gmgr = PSGuidManagerLocator.getGuidMgr(); 
         cws = PSContentWsLocator.getContentWebservice(); 
      }
   }
   /**
    * Compares two AA Relationships by sort rank. Since this comparator depends solely
    * on the sort rank, it may not be consistent with the contract of the Set interface. 
    * 
    * Note: this comparator imposes orderings that are inconsistent with equals.
    * 
    * @author DavidBenua
    *
    */
   protected class SlotItemComparator implements Comparator<PSAaRelationship>
   {
      public SlotItemComparator()
      {
         
      }

      /**
       * Compares PSAaRelationships by sort rank. 
       * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
       */
      public int compare(PSAaRelationship rel1, PSAaRelationship rel2)
      {
         if(rel1 == null || rel2 == null)
         {
            String emsg = "cannot compare null relationships"; 
            log.error(emsg);
            throw new IllegalArgumentException(emsg);
         }
         int sr1 = rel1.getSortRank(); 
         int sr2 = rel2.getSortRank(); 
         
         if(sr1 == sr2)
            return 0;
         if(sr1 < sr2)
            return -1;
         return 1; 
      }

      /**
       * All SlotItemComparators return the same order. 
       * @see java.lang.Object#equals(java.lang.Object)
       */
      @Override
      public boolean equals(Object obj)
      {
         if(obj instanceof SlotItemComparator)
         {
            return true;
         }
         return super.equals(obj);
      }
      
   }
   /**
    * @param cws the cws to set. Used for testing. 
    */
   public void setCws(IPSContentWs cws)
   {
      PSOSlotContents.cws = cws;
   }


   /**
    * @param gmgr the gmgr to set. Used for testing
    */
   public void setGmgr(IPSGuidManager gmgr)
   {
      PSOSlotContents.gmgr = gmgr;
   }
   
   /***
	 * Loads the specified slot. 
	 * @param name the name of the slot
	 * @return Null if the slot is not found, otherwise a valie IPSTemplateSlot instance for the specified slot.
	 */
	public static IPSTemplateSlot getSlot(String name){
		IPSTemplateSlot ret = null;
		
		try {
			ret =  getAssemblyService().findSlotByName(name);
			log.debug("Loaded slot " + name);
		} catch (PSAssemblyException e) {
			log.error("Unable to load slot " + name);
		}
		
		return ret;
	}
	
	protected static IPSAssemblyService getAssemblyService(){
		if(mAss == null){
			mAss = PSAssemblyServiceLocator.getAssemblyService();
		}
		return mAss;
	}
}
