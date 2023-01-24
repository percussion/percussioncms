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
