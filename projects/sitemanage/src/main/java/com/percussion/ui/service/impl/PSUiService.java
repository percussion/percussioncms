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
        
        List<PSDisplayFormatColumn> columns = new ArrayList<PSDisplayFormatColumn>();
        Iterator cols = df.getColumns();
        List<PSDisplayColumn> temp = new ArrayList<PSDisplayColumn>();
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
