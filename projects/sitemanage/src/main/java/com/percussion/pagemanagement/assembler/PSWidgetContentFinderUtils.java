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

package com.percussion.pagemanagement.assembler;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.pagemanagement.assembler.impl.finder.PSRelationshipWidgetContentFinder;
import com.percussion.pagemanagement.assembler.impl.finder.PSRelationshipWidgetContentFinder.WidgetCriteria;
import com.percussion.pagemanagement.data.PSWidgetDefinition;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.pagemanagement.service.IPSWidgetService;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.percussion.share.spring.PSSpringWebApplicationContextUtils.getWebApplicationContext;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

/**
 * Helper class used for retrieving page/template asset relationships. 
 * It uses the same API to retrieve and sort relationships as the content finder does.
 * The content finder is used during assemble page/template.
 * 
 * @author YuBingChen
 */
public class PSWidgetContentFinderUtils
{
    /**
     * Retrieves associated relationships where the specified page/template is the owner
     * and the dependents are local and shared assets. 
     * 
     * @param id the ID of the specified page or template, not <code>null</code> or empty.
     * 
     * @return the list of relationships, sorted by "sort-rank" property, never <code>null</code>, but may be empty.
     */
    public static List<PSRelationship> getLocalSharedAssetRelationships(String id)
    {
        return getAssetRelationships(id, null);
    }
    
    /**
     * This is the same as {@link #getLocalSharedAssetRelationships(String)}, 
     * except the dependents are shared assets only.
     * 
     * @param id the ID of the specified page or template, not <code>null</code> or empty.
     * 
     * @return the list of relationships, sorted by "sort-rank" property, never <code>null</code>, but may be empty.
     */
    public static List<PSRelationship> getSharedAssetRelationships(String id)
    {
        return getAssetRelationships(id, PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY);
    }
    
    /**
     * except the dependents are local assets only.
     * 
     * @param id the ID of the specified page or template, not <code>null</code> or empty.
     * 
     * @return the list of relationships, sorted by "sort-rank" property, never <code>null</code>, but may be empty.
     */
    public static List<PSRelationship> getLocalAssetRelationships(String id)
    {
        return getAssetRelationships(id, PSRelationshipConfig.TYPE_LOCAL_CONTENT);
    }
    
    /**
     * Retrieves associated relationships where the specified page/template is the owner
     * and the dependents are local and shared assets. In addition, the relationship category 
     * is active-assembly.
     * 
     * @param id the ID of the specified page or template, not <code>null</code>.
     * @param relationshipName the name of the returned relationship. It may be <code>null</code> 
     * if the dependent can be either share & local assets.
     * 
     * @return the list of relationships, sorted by "sort-rank" property, never <code>null</code>, but may be empty.
     */
    private static List<PSRelationship> getAssetRelationships(String id, String relationshipName)
    {
        notEmpty(id, "id may not be empty");
        IPSGuid guid = getIdMapper().getItemGuid(id);
        
        List<PSRelationship> rels = getFinder().findRelationshipByOwner(guid);
        if (relationshipName == null)
            return rels;
        
        Set<PSRelationship> sortSet = new HashSet<PSRelationship>();
        for (PSRelationship r : rels)
        {
            String name = r.getConfig().getName();
            if (name.equals(relationshipName) && (r.getOwner().getId()!=guid.getUUID()))
            {
                sortSet.add(r);
            }
        }
        
        return new ArrayList<PSRelationship>(sortSet);
    }
    
    /**
     * Gets a list of relationships (from the given source relationships) that matches one of the supplied widgets.
     * The matched relationships are the ones that will be used during (page) rendering.
     * 
     * @param srcRels the source relationships, never <code>null</code>.
     * @param widgets the widget instances (that may be used on a page/template to render the source relationships/assets). Not <code>null</code>.
     * 
     * @return the matching relationships, never <code>null</code>, but may be empty.
     */
    public static Collection<PSRelationship> getMatchRelationships(Collection<PSRelationship> srcRels, Collection<PSWidgetItem> widgets)
    {
        List<PSRelationship> result = new ArrayList<PSRelationship>();
        for (PSWidgetItem w : widgets)
        {
            PSRelationship r = getMatchRelationship(srcRels, w);
            if (r != null)
                result.add(r);
        }
        
        return result;
    }
    
    /**
     * Gets the relationship (from the source relationships) that matches the given widgets.
     * 
     * @param srcRels the source relationships, assumed not <code>null</code>.
     * @param widget the widget instances, assumed not <code>null</code>.
     * 
     * @return the matching relationship. It may be <code>null</code> if cannot find one.
     */
    public static PSRelationship getMatchRelationship(Collection<PSRelationship> srcRels, PSWidgetItem widget)
    {
        PSWidgetInstance wi = new PSWidgetInstance();
        wi.setItem(widget);
        String widgetDefId = widget.getDefinitionId();
        PSWidgetDefinition widgetDef = getWidgetService().load(widgetDefId);
        wi.setDefinition(widgetDef);
        WidgetCriteria criteria = new WidgetCriteria(wi);
        
        TreeSet<PSRelationship> rels = new TreeSet<PSRelationship>(new RelationshipOrder(criteria));
        for (PSRelationship r : srcRels)
        {
            if (getFinder().isMatchRelationship(r, criteria, null)) {
                if (StringUtils.isNotBlank(widget.getId()) 
                        && StringUtils.isNotBlank(r.getProperty("sys_slotid"))
                        && !r.getProperty("sys_slotid").equals(widget.getId())) {
                    PSRelationshipSet relationships = new PSRelationshipSet();
                    r.setProperty("sys_slotid", widget.getId());
                    relationships.add(r);
                    try {
                        PSRelationshipProcessor.getInstance().save(relationships);
                    } catch (PSCmsException e) {
                        log.error("Error saving relationship when matching content relationships.", e);
                    }
                }
                rels.add(r);
            }
        }
        if (rels.size() == 0)
            return null;
        
        return rels.first();
    }
    
    private static IPSIdMapper getIdMapper()
    {
        if (idMapper == null)
            idMapper = (IPSIdMapper) getWebApplicationContext().getBean("sys_idMapper");
        return idMapper;
    }
    
    private static IPSIdMapper idMapper = null;
    
    private static IPSWidgetService getWidgetService()
    {
        if (widgetService == null)
            widgetService = (IPSWidgetService) getWebApplicationContext().getBean("widgetService");
        return widgetService;
    }
    private static IPSWidgetService widgetService = null;    
    
    /**
     * Comparator to order widget/slot relationships.
     * Note, this comparator must be compatible or behaves the same as the 
     * comparator defined in {@link PSRelationshipWidgetContentFinder}.
     */
    private static class RelationshipOrder implements Comparator<PSRelationship>
    {
       private WidgetCriteria m_criteria;
       
       public RelationshipOrder(WidgetCriteria widget)
       {
           m_criteria = widget;    
       }
       
       /**
        * Compare widget/slot items for ordering
        * 
        * @param r1 page/asset relationship one, never <code>null</code>
        * @param r2 page/asset relationship two, never <code>null</code>
        * @return positive number for increasing order, negative for decreasing
        *         order, zero for no change
        */
       public int compare(PSRelationship r1, PSRelationship r2)
       {
          notNull(r1);
          notNull(r2);

          if (isBlank(m_criteria.getWidgetName()))
              return compareUnnamed(r1, r2);

          String wname1 = r1.getProperty(PSRelationshipConfig.PDU_WIDGET_NAME);
          String wname2 = r2.getProperty(PSRelationshipConfig.PDU_WIDGET_NAME);
          if (isBlank(wname1) && isNotBlank(wname2))
              return 1;

          if (isNotBlank(wname1) && isBlank(wname2))
              return -1;
          
          return compareUnnamed(r1, r2);
       }
       
       /**
        * Compare items as unnamed items.
        * @param r1 widget/slot item one, never <code>null</code>
        * @param r2 widget/slot item two, never <code>null</code>
        * @return positive number for increasing order, negative for decreasing
        *         order, zero for no change
        */
       private int compareUnnamed(PSRelationship r1, PSRelationship r2)
       {
           int sortRank1 = getSortRank(r1);
           int sortRank2 = getSortRank(r2);
           
           if (sortRank1 != sortRank2)
               return sortRank1 - sortRank2;
               
          /*
           * If this comparator returns zero, a set based on this comparator
           * will treat the two slot items as equal (and only store one of
           * them).
           * 
           * Therefore, if by some chance the sort ranks are the same, compare
           * the items using their relationship ids (if set) or their item
           * ids.
           */
          IPSGuid id1 = r1.getGuid();
          IPSGuid id2 = r2.getGuid();
          return (id1.longValue() < id2.longValue() ? -1 : 1);
       }
       
       private int getSortRank(PSRelationship rel)
       {
           String sort = rel.getProperty(PSRelationshipConfig.PDU_SORTRANK);
           if (isBlank(sort))
               return 0;
           try
           {
               return Integer.parseInt(sort);
           }
           catch (NumberFormatException e)
           {
               return Integer.MAX_VALUE;
           }
       }

    }
    
    private static PSRelationshipWidgetContentFinder getFinder()
    {
        if (ms_finder == null)
            ms_finder = (PSRelationshipWidgetContentFinder) PSPageUtils.getWidgetContentFinder(null);

        return ms_finder;
    }
    private static PSRelationshipWidgetContentFinder ms_finder = null;
    

    /**
     * Logger for this class
     */
    private static Log log = LogFactory.getLog(PSWidgetContentFinderUtils.class);

}
