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

package com.percussion.sitemanage.service;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;

public abstract class Template {

    private List<String> siteIds = new ArrayList<>();

    public List<String> getSiteIds()
    {
        return siteIds;
    }

    public void setSiteIds(List<String> siteIds)
    {
        this.siteIds = siteIds;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Template{");
        sb.append("siteIds=").append(siteIds);
        sb.append('}');
        return sb.toString();
    }
}
