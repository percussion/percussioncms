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
package com.percussion.sitemanage.data;

import com.percussion.share.data.PSAbstractDataObject;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotNull;

@XmlRootElement(name = "SiteSummaryData")
public class PSSiteStatisticsSummary extends PSAbstractDataObject
{

    /**
     * Safe to serialize
     */
    private static final long serialVersionUID = 1L;
    
    @NotBlank
    @NotNull
    private String name;
    
    private long id;
    
    private PSSiteStatistics statistics;
    
    private List<PSSiteIssueSummary> issues = new ArrayList<>();

    private String abridgedErrorMessage;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    
    public long getSiteId() {
        return this.id;
    }

    public void setSiteId(long id) {
        this.id = id;
    }

    public PSSiteStatistics getStatistics()
    {
        return statistics;
    }

    public void setStatistics(PSSiteStatistics statistics)
    {
        this.statistics = statistics;
    }

    public List<PSSiteIssueSummary> getIssues()
    {
        return issues;
    }

    public void setIssues(List<PSSiteIssueSummary> issues)
    {
        this.issues = issues;
    }

    public void setAbridgedErrorMessage(String message)
    {
        this.abridgedErrorMessage = message;
    }

    public String getAbridgedErrorMessage()
    {
        return abridgedErrorMessage;
    }

}
