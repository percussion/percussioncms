/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.services.assembly.impl.finder;

import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSSlotContentFinder;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.jexl.PSAssemblerUtils;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerException;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static com.percussion.services.assembly.impl.finder.PSContentFinderUtils.reorderItems;

/**
 * The base slot content finder provides the common functionality needed by each
 * slot content finder implementation. The general pattern (not followed for
 * managed nav) is to implement the abstract
 * {@link #getContentItems(IPSAssemblyItem, IPSTemplateSlot, Map)} method. This
 * method provides the information to a general implementation of the
 * {@link #find(IPSAssemblyItem, IPSTemplateSlot, Map)} method.
 * <p>
 * The base class' find method filters and organizes the returned slot items
 * into a set of assembly items to be assembled.
 * 
 * @deprecated use {@link PSSlotContentFinderBase} instead.
 * 
 * @author dougrand
 * 
 */
public abstract class PSBaseSlotContentFinder extends PSContentFinderBase<IPSTemplateSlot>
   implements IPSSlotContentFinder
{
   
   /**
    * Represents a single slot item to be filtered, sorted and output as an
    * assembly item
    */
   public static class SlotItem extends ContentItem
   {      
      /**
       * Ctor
       * 
       * @param itemId content item guid, never <code>null</code>
       * @param templateId template guid, may be <code>null</code>
       * @param sortrank sort order
       */
      public SlotItem(IPSGuid itemId, IPSGuid templateId, int sortrank) 
      {
         super(itemId, templateId, sortrank);
      }
      
      /**
       * Create an instance from {@link ContentItem}.
       * @param item the source of the item, never <code>null</code>.
       */
      public SlotItem(ContentItem item)
      {
         super(item.getItemId(), item.getTemplate(), item.getSortrank());
      }
   }

   /**
    * Comparitor to order slot relationships
    */
   protected static class SlotItemOrder implements Comparator<SlotItem>
   {
      /**
       * Compare slot items for ordering
       * 
       * @param s1 slot item one, never <code>null</code>
       * @param s2 slot item two, never <code>null</code>
       * @return positive number for increasing order, negative for decreasing
       *         order, zero for no change
       */
      public int compare(SlotItem s1, SlotItem s2)
      {
         if (s1 == null)
         {
            throw new IllegalArgumentException("s1 may not be null");
         }
         if (s2 == null)
         {
            throw new IllegalArgumentException("s2 may not be null");
         }
         if (s1.getSortrank() == s2.getSortrank())
         {
            /*
             * If this comparator returns zero, a set based on this comparator
             * will treat the two slot items as equal (and only store one of
             * them).
             * 
             * Therefore, if by some chance the sort ranks are the same, compare
             * the items using their relationship ids (if set) or their item
             * ids.
             */
            IPSGuid id1 = s1.getRelationshipId();
            IPSGuid id2 = s2.getRelationshipId();
            if (id1 == null || id2 == null)
            {
               id1 = s1.getItemId();
               id2 = s2.getItemId();
            }
            return (id1.longValue() < id2.longValue() ? -1 : 1);
         }
         else
            return s1.getSortrank() - s2.getSortrank();
      }
   }
  
   /**
    * Logger
    */
   private static final Logger ms_log = LogManager.getLogger(PSBaseSlotContentFinder.class);
   
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.assembly.IPSSlotContentFinder#find(com.percussion.services.assembly.IPSAssemblyItem,
    *      com.percussion.services.assembly.IPSTemplateSlot, java.util.Map)
    */
   @Override
   @SuppressWarnings("unchecked")
   public List<IPSAssemblyItem> find(IPSAssemblyItem sourceItem,
         IPSTemplateSlot slot, Map<String, Object> selectors)
           throws RepositoryException, PSFilterException, PSAssemblyException, PSNotFoundException {
      Map<String, ? extends Object> args = slot.getFinderArguments();
      Map<String, Object> params = new HashMap<>();
      params.putAll(args);
      params.putAll(selectors);

      return super.find(sourceItem, slot, params);
   }

   /**
    * Get the slot items to be operated on. The items do not need to be ordered
    * or filtered. This method is supplied by an implementer and is normally
    * called by the {@link #find(IPSAssemblyItem, IPSTemplateSlot, Map)} method
    * to get initial content. 
    * <p>
    * It is important to note that while this mechanism works well for most
    * cases, there may be cases where an implementer must simply write their
    * own find method.
    * 
    * @param sourceItem the source assembly item, guaranteed never
    *           <code>null</code> because this is called from
    *           {@link #find(IPSAssemblyItem, IPSTemplateSlot, Map)}
    * @param slot the slot, guaranteed never <code>null</code> because this is
    *           called from {@link #find(IPSAssemblyItem, IPSTemplateSlot, Map)}
    * @param selectors the selectors, may be <code>null</code>
    * @return a set of slot items, never <code>null</code>, that can be
    *         filtered, ordered and turned into assembly items. The set must be
    *         ordered if the slots are to be ordered. Use {@link SlotItemOrder}
    *         to order slot items in the set.
    * 
    * @throws RepositoryException
    * @throws PSFilterException
    * @throws PSAssemblyException
    */
   protected abstract Set<SlotItem> getSlotItems(IPSAssemblyItem sourceItem,
         IPSTemplateSlot slot, Map<String, Object> selectors)
         throws RepositoryException, PSFilterException, PSAssemblyException;
       
   @Override
   protected Set<ContentItem> getContentItems(IPSAssemblyItem sourceItem,
         IPSTemplateSlot slot, Map<String, Object> selectors)
   {
      Set<ContentItem> rval = new TreeSet<>(new ContentItemOrder());
      
      IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
      
      try
      {
         Set<SlotItem> items = getSlotItems(sourceItem, slot, selectors);
         for (SlotItem item : items)
         {
            ContentItem ci = new ContentItem(item.getItemId(), item.getTemplate(),
                  item.getSortrank());
            rval.add(ci);
         }
         return rval;
      }
      catch (Exception e)
      {
         String errMsg = "Problem retrieving items for slot id=" + slot.getGUID();
         ms_log.error(errMsg, e);
         throw new RuntimeException(errMsg, e);      }
   }
   
   @Override
   protected IPSAssemblyItem createAssemblyItem(ContentItem slotitem, 
         IPSAssemblyItem sourceItem,
         String templatename, IPSAssemblyService asm,
         IPSTemplateSlot slot) throws PSAssemblyException
   {
      IPSAssemblyItem clone = super.createAssemblyItem(slotitem, sourceItem, 
            templatename, asm, slot);

      boolean isAaSlot = new PSAssemblerUtils().isAASlot(slot);

      // If it is not AA slot and sys_command=editrc
      clone.getParameters().put(IPSHtmlParameters.SYS_FORAASLOT, new String[]
      {Boolean.toString(isAaSlot)});

      return clone;
   }

   /**
    * Get the specified argument from the passed selectors. If no argument is
    * present then the default is returned.
    * 
    * @param args the arguments, never <code>null</code>
    * @param selectors the selectors, never <code>null</code>
    * @param key the key, never <code>null</code> or empty
    * @param defaultvalue the default value to use
    * @return the value, or <code>null</code> if not defined
    */
   protected String getValue(Map<String, ? extends Object> args,
         Map<String, Object> selectors, String key, String defaultvalue)
   {
      if (args == null)
      {
         throw new IllegalArgumentException("args may not be null");
      }
      if (StringUtils.isBlank(key))
      {
         throw new IllegalArgumentException("key may not be null or empty");
      }
      if (selectors == null)
      {
         throw new IllegalArgumentException("selectors may not be null");
      }
      
      Map<String, Object> params = new HashMap<>();
      params.putAll(args);
      params.putAll(selectors);

      return PSContentFinderUtils.getValue(params, key, defaultvalue);
   }

   /**
    * Reorder the return set by a different algorithm. This first extracts the
    * content ids from the set. Then it does a JSR-170 query with the order
    * by clause on 'nt:base' and this returns the content ids. That is then
    * turned into a map that yields ascending sort order, which is used with
    * a different comparator.
    * 
    * Calls {@link #reorder(Set, String, String)} with no locale.
    * 
    * @param rval the original results, may be empty but not <code>null</code>
    * @param orderby the orderby string, never <code>null</code> or empty
    * @return the reordered set
    * @deprecated use {@link #reorder(Set, String, String)} instead.
    */
   protected Set<SlotItem> reorder(Set<SlotItem> rval, String orderby)
   {
      return reorder(rval, orderby, null);
   }
   
   /**
    * Reorder the return set by a different algorithm. This first extracts the
    * content ids from the set. Then it does a JSR-170 query with the order
    * by clause on 'nt:base' and this returns the content ids. That is then
    * turned into a map that yields ascending sort order, which is used with
    * a different comparator.
    * 
    * @param orderby the orderby string, never <code>null</code> or empty
    * @param locale the locale to use in the search to ensure the correct
    * collating sequence. If <code>null</code> or empty the JVM locale is used.
    * @return the reordered set
    * @deprecated use  instead.
    */
   protected Set<SlotItem> reorder(Set<SlotItem> srcItems, String orderby, 
         String locale)
   {
      Set<ContentItem> items = new TreeSet<>(new ContentItemOrder());
      for (SlotItem item : srcItems)
      {
         items.add(new ContentItem(item.getItemId(), item.getTemplate(), item.getSortrank()));
      }
      Set<ContentItem> tgtItems = reorderItems(items, orderby, locale);
      Set<SlotItem> reItems = new TreeSet<>(new SlotItemOrder());
      for (ContentItem item : tgtItems)
      {
         reItems.add(new SlotItem(item));
      }
      return reItems;
   }
   
   
   /**
    * Get the locale from the available information. The default value is taken
    * from the source node using the sys_lang field.
    * 
    * @param source the source assembly item, never <code>null</code>.
    * @param args the arguments, never <code>null</code>
    * @param selectors the selectors, never <code>null</code>
    * @return the locale, or <code>null</code> if it cannot be determined. The
    * returned locale will be in normal form with an underscore for use with
    * java, i.e. en_us not en-us.
    */
   protected String getLocale(IPSAssemblyItem source, Map<String, ? extends Object> args, Map<String, Object> selectors)
   {
      Node n = source.getNode();
      try
      {
         if (n.hasProperty(IPSHtmlParameters.SYS_LANG))
         {
            String locale = getValue(args, selectors, IPSHtmlParameters.SYS_LANG,
                  n.getProperty(IPSHtmlParameters.SYS_LANG).getString());
            return locale.replace("-", "_");
         }
         else
         {
            return null;
         }
      }
      catch (Exception e)
      {
         ms_log.error(e);
         return null;
      }
   }

   /**
    * Finds the site and folder of the item id in the slot item and sets it on
    * the supplied slot item. If the item exists in more than one site, then
    * sets it to the first site returned by
    * {@link IPSSiteManager#getItemSites(IPSGuid)} method.
    * 
    * @param slotItem slot item on which the site id needs to be set, if
    *           <code>null</code> does nothing.
    * @param skipFolderID if true skips setting the folder id.
    * @throws PSSiteManagerException
    */
   protected void setSiteFolderId(SlotItem slotItem, boolean skipFolderID)
           throws PSSiteManagerException, PSNotFoundException {
      if (slotItem == null)
         return;
      IPSGuid guid = slotItem.getItemId();
      IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
      List<IPSSite> itemSites = smgr.getItemSites(guid);
      if (!itemSites.isEmpty())
      {
         IPSGuid siteId = itemSites.get(0).getGUID();
         slotItem.setSiteId(siteId);
         if(skipFolderID)
         {
            IPSGuid folderId = smgr.getSiteFolderId(siteId, guid);
            slotItem.setFolderId(new PSLegacyGuid(folderId.getUUID(), 0));
         }
      }
   }
   
   
}
