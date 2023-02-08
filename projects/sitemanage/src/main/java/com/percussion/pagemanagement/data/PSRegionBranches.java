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
package com.percussion.pagemanagement.data;

import net.sf.oval.constraint.AssertValid;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Contains the page regions that will override the templates regions.
 * It also contains the region-widgets assocations ({@link #getRegionWidgetAssociations()}).
 * 
 * @author adamgent
 *
 */
@XmlRootElement(name = "RegionBranches")
public class PSRegionBranches extends PSRegionWidgetAssociations
{
    @AssertValid()
    private List<PSRegion> regions = new ArrayList<>();

    @AssertValid()
    @XmlElementWrapper(name = "regions")
    @XmlElement(name = "region")
    public List<PSRegion> getRegions()
    {
        return regions;
    }
    
    public void setRegions(List<PSRegion> pageRegions)
    {
        this.regions = pageRegions;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PSRegionBranches)) return false;
        if (!super.equals(o)) return false;
        PSRegionBranches that = (PSRegionBranches) o;
        return Objects.equals(getRegions(), that.getRegions());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getRegions());
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("PSRegionBranches{");
        sb.append("regions=").append(regions);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public PSRegionBranches clone()
    {
        try
        {
            return (PSRegionBranches) BeanUtils.cloneBean(this);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Cannot clone", e);
        }
    }
    

}
