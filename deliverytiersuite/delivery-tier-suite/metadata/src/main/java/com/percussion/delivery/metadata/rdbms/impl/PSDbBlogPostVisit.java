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
package com.percussion.delivery.metadata.rdbms.impl;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.percussion.delivery.metadata.IPSBlogPostVisit;

/**
 * Page visit object
 * 
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSBlogPostVisit")
@Table(name = "BLOG_POST_VISIT")
public class PSDbBlogPostVisit implements IPSBlogPostVisit, Serializable
{

    @Id
	@GeneratedValue
	@Column(name = "VISIT_ID")
    private long visitId;
    
    @Column(length = 2000)
    private String pagepath;

    @Basic
    @Temporal(TemporalType.DATE)
    private Date hitDate;


    @Basic
    private BigInteger hitCount;

    public PSDbBlogPostVisit()
    {

    }

    /**
     * 
     * @param pagepath
     * @param hitDate
     * @param hitCount
     */
    public PSDbBlogPostVisit(String pagepath, Date hitDate, BigInteger hitCount)
    {
        if (pagepath == null || pagepath.length() == 0)
            throw new IllegalArgumentException("pagepath cannot be null or empty");
        if (hitDate == null)
            throw new IllegalArgumentException("hitDate cannot be null");
        if (hitCount == null)
            throw new IllegalArgumentException("hitCount cannot be null");

        setHitCount(hitCount);
        setHitDate(hitDate);
        setPagepath(pagepath);
    }

    /**
     * @return the page path
     */
    public String getPagepath()
    {
        return pagepath;
    }

    /**
     * @param path the pagepath to set
     */
    public void setPagepath(String path)
    {
        this.pagepath = path;
    }

    public Date getHitDate() {
		return hitDate;
	}

	public void setHitDate(Date hitDate) {
		this.hitDate = hitDate;
	}

	public BigInteger getHitCount() {
		return hitCount;
	}

	public void setHitCount(BigInteger hitCount) {
		this.hitCount = hitCount;
	}

	/*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !getClass().getName().equals(obj.getClass().getName()))
            return false;
        PSDbBlogPostVisit visits = (PSDbBlogPostVisit) obj;
        return new EqualsBuilder()
            .append(hitDate, visits.hitDate)
            .append(hitCount, visits.hitCount)
            .append(pagepath, visits.pagepath)
            .isEquals();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return new HashCodeBuilder()
            .append(hitDate)
            .append(hitCount)
            .append(pagepath)
            .toHashCode();
    }

}
