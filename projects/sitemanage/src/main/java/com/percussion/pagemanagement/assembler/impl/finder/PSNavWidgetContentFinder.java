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

package com.percussion.pagemanagement.assembler.impl.finder;

import com.percussion.pagemanagement.assembler.PSWidgetInstance;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.impl.finder.PSNavFinderUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.jcr.RepositoryException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The navigation widget content finder looks up a related navigation node 
 * (navon or navtree) for a specified item. The navigation node and the given 
 * item are under the same folder. The navigation node can be accessed from  
 * "$nav.self" binding of the returned assembly item. The navigation node 
 * implements IPSProxyNode. In addition, the binding of "$nav.root" is 
 * the root of the navigation.
 * <p>
 * All navigation nodes, from the related node to the root of the navigation
 * are filtered by the item filter, which is specified in the given item.
 * 
 * @author YuBingChen
 */
@Transactional(readOnly = true, noRollbackFor = Exception.class)
public class PSNavWidgetContentFinder extends PSWidgetContentFinder
{
    @Override
    public List<IPSAssemblyItem> find(IPSAssemblyItem sourceItem,
            PSWidgetInstance widget, Map<String, Object> params)
            throws RepositoryException, PSAssemblyException
    {
        IPSAssemblyItem item = PSNavFinderUtils.findItem(sourceItem, null);
        if (item == null)
            return Collections.emptyList();
        return Collections.singletonList(item);
    }       

    protected Set<ContentItem> getContentItems(
            IPSAssemblyItem item, PSWidgetInstance widget, Map<String, Object> params)
    {
        // this is not used, do nothing
        return null;
    }
}
