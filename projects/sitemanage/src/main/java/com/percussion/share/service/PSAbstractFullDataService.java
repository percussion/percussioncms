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
package com.percussion.share.service;

import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.data.PSItemSummaryUtils;
import com.percussion.share.service.exception.PSDataServiceException;

public abstract class PSAbstractFullDataService<FULL,SUM extends IPSItemSummary> 
    extends PSAbstractDataService<FULL, SUM, String> implements IPSDataService<FULL, SUM, String>
{

    protected IPSDataItemSummaryService itemSummaryService;
    
    public PSAbstractFullDataService(IPSDataItemSummaryService itemSummaryService, IPSGenericDao<FULL, String> dao)
    {
        super(dao);
        this.itemSummaryService = itemSummaryService;
    }

    public SUM find(String id) throws PSDataServiceException {

        validateIdParameter("find", id);
        IPSItemSummary itemSummary = itemSummaryService.find(id);
        SUM sum = createSummary(id);
        PSItemSummaryUtils.copyProperties(itemSummary, sum);
        return sum;

    }
    
    protected abstract SUM createSummary(String id);

}
