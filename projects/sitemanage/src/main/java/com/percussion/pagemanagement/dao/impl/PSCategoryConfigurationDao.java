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

package com.percussion.pagemanagement.dao.impl;

import com.percussion.pagemanagement.data.PSCategoryConfiguration;
import com.percussion.share.dao.PSFileDataRepository;
import com.percussion.share.dao.PSXmlFileDataRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component("categoryConfigurationDao")
@Lazy
public class PSCategoryConfigurationDao extends PSXmlFileDataRepository<PSCategoryConfiguration, PSCategoryConfiguration>
{
    public PSCategoryConfigurationDao()
    {
        super(PSCategoryConfiguration.class);
    }

    @Override
    protected PSCategoryConfiguration update(Set<PSFileDataRepository.PSFileEntry> files)
            throws IOException
    {
        if (files.isEmpty()) {
            return new PSCategoryConfiguration();
        }
        PSFileEntry fe = files.iterator().next();
        return fileToObject(fe);
    }

    @Override
    @Value("${rxdeploydir}/rxconfig/Categories")
    public void setRepositoryDirectory(String widgetsRepositoryDirectory)
    {
        // TODO Auto-generated method stub
        super.setRepositoryDirectory(widgetsRepositoryDirectory);
    }
    
    

}
