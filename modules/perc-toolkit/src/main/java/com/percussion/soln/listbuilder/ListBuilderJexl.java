/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.soln.listbuilder;

import com.percussion.extension.*;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.soln.jcr.NodeUtils;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;


public class ListBuilderJexl implements IPSJexlExpression {


    private static final String PROPERTY_PREFIX = "rx:soln_list_";
    private static final String MANUAL_SLOT_NAME = "soln_list_ManualSlot";
    private FolderTools folderTools;

    @IPSJexlMethod(description = "Creates a Query Builder from a Node", params =
    {
          @IPSJexlParam(name = "node", description = "$sys.assemblyItem")
    }, 
    returns = "List builder item")  
    public ListBuilderItem getListBuilderItem(IPSAssemblyItem assemblyItem) throws Exception {
        ListBuilderItem item = new ListBuilderItem();
        NodeUtils.copyFromNode(assemblyItem.getNode(), item, PROPERTY_PREFIX, null);
        item.setFolderPath(getFolderPath(assemblyItem));
        notEmpty(item.getSlot(), "Slot must be defined");
        return item;
    }

    protected String getFolderPath(IPSAssemblyItem assemblyItem) throws Exception {
        return folderTools.getParentFolderPath(assemblyItem);
    }

    private boolean hasFolderPaths(ListBuilderItem item) {
        Collection<String> fp = item.getFolderPaths();
        return  fp == null || 
        fp.isEmpty() || 
        (fp.size() == 1 && isBlank(fp.iterator().next()));
    }
    
    
    
    @IPSJexlMethod(description = "Creates a Query Builder from a List builder", params =
    {
          @IPSJexlParam(name = "lbn", description = "getListBuilderItem($sys.item)")
    }, 
    returns = "JCRQueryBuilder")  
    public JCRQueryBuilder getQueryBuilder(ListBuilderItem lbn) {
        notNull(lbn);
        JCRQueryBuilder b = new JCRQueryBuilder();
        b.setEndDate(lbn.getDateRangeEnd());
        b.setStartDate(lbn.getDateRangeStart());
        b.setTitleContains(lbn.getTitleContains());
        b.setQuery(lbn.getJcrQuery());
        
        if ( ! hasFolderPaths(lbn) && isNotBlank(lbn.getFolderPath())) {
            b.setFolderPaths(asList(lbn.getFolderPath()));
        }
        else {
            b.setFolderPaths(lbn.getFolderPaths());
        }
        Collection<String> contentTypes = lbn.getContentTypes();
        String contentType = lbn.getContentType();
        if (contentTypes == null || contentTypes.isEmpty() 
                && isNotBlank(contentType)) {
            contentTypes = asList(contentType);
        }
        if ( ! contentTypes.isEmpty() ) {
            b.setFromTypes(contentTypes);
        }
        return b;
    }
    
    public void resolveQuery(ListBuilderItem lbn) {
        JCRQueryBuilder b = getQueryBuilder(lbn);
        resolveQuery(lbn, b);
    }
    
    public void resolveQuery(ListBuilderItem lbn, JCRQueryBuilder b) {
        notNull(lbn);
        notNull(b);
        lbn.setJcrQuery(b.getQuery());
    }
    
    @IPSJexlMethod(description = "Creates slot parameters from a list builder", params =
    {
          @IPSJexlParam(name = "lbn", description = "getListBuilderItem($sys.item)")
    }, 
    returns = "Map of slot parameters") 
    public Map<String, String> getSlotParameters(ListBuilderItem lbn) {
        notNull(lbn);
        Map<String, String> params = new HashMap<>();
        if ( ! MANUAL_SLOT_NAME.equals(lbn.getSlot()) )
            params.put("template", lbn.getChildSnippet());
        if (lbn.getCount() != null)
            params.put("max_results", ""+ lbn.getCount());
        params.put("query", lbn.getJcrQuery());
        removeBlanks(params);
        return params;
        
    }
    
    private <K> void removeBlanks(Map<K,String> map) {
        map.entrySet().removeIf(e -> isBlank(e.getValue()));
        
    }

    
    
    public void setFolderTools(FolderTools folderTools) {
        this.folderTools = folderTools;
    }



    public void init(IPSExtensionDef def, File file) throws PSExtensionException {
        FolderTools ft = new FolderTools();
        ft.init(def, file);
        setFolderTools(ft);
    }
}
