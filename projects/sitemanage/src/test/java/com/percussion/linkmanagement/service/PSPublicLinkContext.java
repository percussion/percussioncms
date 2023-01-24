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
package com.percussion.linkmanagement.service;

import com.percussion.pagemanagement.data.PSRenderLinkContext;
import com.percussion.pagemanagement.data.PSRenderLinkContext.Mode;
import com.percussion.sitemanage.data.PSSiteSummary;

/**
 * @author JaySeletz
 *
 */
public final class PSPublicLinkContext extends PSRenderLinkContext
{
    private final Mode mode = Mode.PUBLISH;
    
    private PSSiteSummary site;

    /**
     * @param mode
     */
    public PSPublicLinkContext(PSSiteSummary site)
    {
        this.site = site;
        super.setDeliveryContext(true);
    }

    @Override
    public Mode getMode()
    {
        return mode;
    }

    @Override
    public PSSiteSummary getSite()
    {
        return site;
    }
}
