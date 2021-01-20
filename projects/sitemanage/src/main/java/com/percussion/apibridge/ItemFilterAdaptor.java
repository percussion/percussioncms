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

package com.percussion.apibridge;

import com.percussion.rest.Guid;
import com.percussion.rest.itemfilter.IItemFilterAdaptor;
import com.percussion.rest.itemfilter.ItemFilter;
import com.percussion.rest.itemfilter.ItemFilterRuleDefinition;
import com.percussion.rest.itemfilter.ItemFilterRuleDefinitionParam;
import com.percussion.services.filter.*;
import com.percussion.util.PSSiteManageBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@PSSiteManageBean
public class ItemFilterAdaptor implements IItemFilterAdaptor {

    private IPSFilterService filterService;
    private Log log = LogFactory.getLog(ItemFilterAdaptor.class);

    public ItemFilterAdaptor(){
        filterService = PSFilterServiceLocator.getFilterService();
    }


    /***
     * Get a list of the ItemFilters available on the system populated with rules and parameters.
     * @return A list of item filters
     */
    @Override
    public List<ItemFilter> getItemFilters() {
        List<ItemFilter> ret = new ArrayList<ItemFilter>();
        List<IPSItemFilter> filters = filterService.findAllFilters();

        for(IPSItemFilter i : filters){
            ret.add(copyFilter(i));
        }
        return ret;
    }

    private ItemFilter copyFilter(IPSItemFilter filter){
        ItemFilter ret  = new ItemFilter();

        ret.setFilter_id(ApiUtils.convertGuid(filter.getGUID()));
        ret.setDescription(filter.getDescription());
        ret.setName(filter.getName());
        ret.setLegacyAuthtype(filter.getLegacyAuthtypeId());

        if(filter.getParentFilter() != null){
            ret.setParentFilter(copyFilter(filter.getParentFilter()));
        }

        Set<IPSItemFilterRuleDef> ruleDefs = filter.getRuleDefs();
        Set<ItemFilterRuleDefinition> rules = new HashSet<ItemFilterRuleDefinition>();
        for(IPSItemFilterRuleDef def : ruleDefs){
            ItemFilterRuleDefinition r = copyItemFilterRuleDef(def);
            if(r != null)
                rules.add(r);
        }
        ret.setRules(rules);
        return ret;
    }

    private ItemFilterRuleDefinition copyItemFilterRuleDef(IPSItemFilterRuleDef def) {
        ItemFilterRuleDefinition ret = new ItemFilterRuleDefinition();

        try {
            ret.setName(def.getRuleName());
            ret.setRuleId(ApiUtils.convertGuid(def.getGUID()));

            Map<String,String> params = def.getParams();
            List<ItemFilterRuleDefinitionParam> retParams = new ArrayList<ItemFilterRuleDefinitionParam>();
            for(Map.Entry<String,String> pair : params.entrySet()){
                ItemFilterRuleDefinitionParam p = new ItemFilterRuleDefinitionParam();
                p.setName(pair.getKey());
                p.setValue(pair.getValue());
                retParams.add(p);
            }
           ret.setParams(retParams);
        } catch (PSFilterException e) {
            log.error("Error getting ItemFilter Rule Name.  Skipping Rule.", e);
            ret = null;
        }
        return ret;
    }


    /***
     * Update or create an ItemFilter
     * @param filter  The filter to update or create.
     * @return The updated ItemFilter.
     */
    @Override
    public ItemFilter updateOrCreateItemFilter(ItemFilter filter) {
        //TODO: Implement Me
        log.warn("updateOrCreateItemFilter not yet implemented");
        return null;
    }

    /***
     * Delete the specified item filter.
     * @param itemFilterId A valid ItemFilter id.  Filter must not be associated with any ContentLists or it won't be deleted.
     */
    @Override
    public void deleteItemFilter(Guid itemFilterId) {
        IPSItemFilter filter = filterService.loadFilter(ApiUtils.convertGuid(itemFilterId));

        filterService.deleteFilter(filter);
    }

    /***
     * Get a single ItemFilter by id.
     * @param itemFilterId  A Valid ItemFilter id
     * @return The ItemFilter
     */
    @Override
    public ItemFilter getItemFilter(Guid itemFilterId) {
        IPSItemFilter filter = filterService.loadFilter(ApiUtils.convertGuid(itemFilterId));
        return  copyFilter(filter);
    }
}
