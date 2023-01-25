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
package com.percussion.pagemanagement.assembler.impl;

import com.percussion.pagemanagement.data.PSResourceInstance;
import com.percussion.sitemanage.data.PSSiteSummary;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * A null site used for resource generation of assets during preview.
 * During preview of assets we do not have a site associated with them.
 * Instead of giving the assets a <code>null</code> value for sites
 * in the {@link PSResourceInstance resource instance} we use this object. 
 * <p>
 * This follows the Null Object pattern as we prefer to avoid null when we can.
 * 
 * @author adamgent
 *
 */
@XmlRootElement
public class PSNullSiteSummary extends PSSiteSummary
{

    private static final long serialVersionUID = 1L;
    private static PSNullSiteSummary siteSummary = new PSNullSiteSummary();
    static {
        siteSummary.setBaseUrl("http://localhost/");
        siteSummary.setFolderPath("//Sites/$NullSite$");
        siteSummary.setName("NullSite");
        siteSummary.setId(null);
    }
    
    private PSNullSiteSummary() {
    }
    
    public static PSNullSiteSummary getInstance() {
        return siteSummary;
    }

}

