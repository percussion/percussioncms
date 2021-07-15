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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
/**
 * 
 */
package com.percussion.share.dao.impl;

import static org.apache.commons.lang.Validate.notNull;

import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.IPSNameGenerator;

import com.percussion.util.PSSiteManageBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.ext.Provider;


@Provider
@PSSiteManageBean("nameGenerator")
public class PSNameGenerator implements IPSNameGenerator
{
    private IPSIdMapper idMapper;

    /**
     * Constructs a new local content name generator.
     * 
     * @param idMapper used for id generation, never <code>null</code>.
     */
    @Autowired
    public PSNameGenerator(IPSIdMapper idMapper)
    {
        notNull(idMapper);
        
        this.idMapper = idMapper;
    }
    
    public String generateLocalContentName()
    {
        return LOCAL_CONTENT_PREFIX + idMapper.getLocalContentId();
    }
    
    /**
     * Constant for the prefix used when generating names for local content items.
     */
    private static final String LOCAL_CONTENT_PREFIX = "LocalContent-";
}
