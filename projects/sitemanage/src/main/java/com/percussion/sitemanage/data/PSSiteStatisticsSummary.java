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
    
    private List<PSSiteIssueSummary> issues = new ArrayList<PSSiteIssueSummary>();

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
