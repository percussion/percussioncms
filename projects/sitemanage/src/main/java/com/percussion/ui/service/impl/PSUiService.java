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
package com.percussion.ui.service.impl;

import com.percussion.cms.objectstore.PSDisplayColumn;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.ui.data.PSDisplayFormatColumn;
import com.percussion.ui.data.PSSimpleDisplayFormat;
import com.percussion.ui.service.IPSUiService;
import com.percussion.util.PSSiteManageBean;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSWebserviceUtils;
import com.percussion.webservices.ui.IPSUiDesignWs;
import com.percussion.webservices.ui.PSUiWsLocator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * @author erikserating
 *
 */
@PSSiteManageBean("uiService")
public class PSUiService implements IPSUiService
{

    private IPSUiDesignWs designWs = PSUiWsLocator.getUiDesignWebservice();
    
    /* (non-Javadoc)
     * @see com.percussion.ui.service.IPSUiService#getDisplayFormat(long)
     */
    public PSSimpleDisplayFormat getDisplayFormat(int id)
    {
        if(id == -1) //Return default if -1
            return getDisplayFormatByName(null);
        
        IPSGuid guid = PSGuidUtils.makeGuid(id, PSTypeEnum.DISPLAY_FORMAT);
        PSDisplayFormat dispFormat = designWs.findDisplayFormat(guid);
        return convertToSimpleDisplayFormat(dispFormat);
    }

    /* (non-Javadoc)
     * @see com.percussion.ui.service.IPSUiService#getDisplayFormatByName(java.lang.String)
     */
    public PSSimpleDisplayFormat getDisplayFormatByName(String name)
    {
        if(StringUtils.isBlank(name))
            name = "CM1_Default";
        PSWebserviceUtils.setUserName("rxserver");
        PSDisplayFormat dispFormat = designWs.findDisplayFormat(name);
        return convertToSimpleDisplayFormat(dispFormat);
    }
    
    /**
     * Converts a <code>PSDisplayFormat</code> to a <code>PSSimpleDisplayFormat</code>.
     * @param df assumed not <code>null</code>.
     * @return a simple display format, never <code>null</code>.
     */
    @SuppressWarnings("unchecked")
    private PSSimpleDisplayFormat convertToSimpleDisplayFormat(PSDisplayFormat df)
    {
        PSSimpleDisplayFormat sdf = new PSSimpleDisplayFormat();
        sdf.setId(df.getDisplayId());
        sdf.setName(df.getInternalName());
        sdf.setDisplayName(df.getDisplayName());
        sdf.setDescription(df.getDescription());
        sdf.setSortby(df.getSortedColumnName());
        
        List<PSDisplayFormatColumn> columns = new ArrayList<>();
        Iterator cols = df.getColumns();
        List<PSDisplayColumn> temp = new ArrayList<>();
        while(cols.hasNext())
        {
            temp.add((PSDisplayColumn)cols.next());
        }
        Collections.sort(temp, new Comparator<PSDisplayColumn>()
        {
            public int compare(PSDisplayColumn d1, PSDisplayColumn d2)
            {                
                return d1.getPosition() - d2.getPosition();
            }

        }); 
        
        for(PSDisplayColumn c : temp)
        {
            PSDisplayFormatColumn current = new PSDisplayFormatColumn(c.getSource(), c.getDisplayName());
            current.setType(c.getRenderType());
            current.setWidth(String.valueOf(c.getWidth()));
            if(c.getSource().equals(df.getSortedColumnName()))
                sdf.setSortAscending(c.isAscendingSort());
            columns.add(current);
        }
        sdf.setColumns(columns);
        
        return sdf;
    }

}
