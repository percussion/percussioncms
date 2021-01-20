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
package com.percussion.delivery.likes.service.rdbms;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.percussion.delivery.likes.data.IPSLikes;

/**
 * @author davidpardini
 * 
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSLikes1")
@Table(name = "PERC_PAGE_LIKES", uniqueConstraints = @UniqueConstraint(columnNames =
{"site", "likeId", "type"}))
public class PSLikes implements IPSLikes, Serializable
{

    @TableGenerator(name = "likesId", table = "PERC_ID_GEN", pkColumnName = "GEN_KEY", valueColumnName = "GEN_VALUE", pkColumnValue = "likesId", allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "likesId")
    private long id;

    @Basic
    private String site;

    @Basic
    private String likeId;

    @Basic
    private String type;

    @Basic
    private int total;

    public PSLikes()
    {

    }

    /**
     * Creates a new likes with the same values as the given one, except for the
     * id.
     * 
     * @param likes A Likes to create a copy from.
     */
    public PSLikes(IPSLikes likes)
    {
        this.type = likes.getType();
        this.site = likes.getSite();
        this.likeId = likes.getLikeId();
        this.total = likes.getTotal();
    }    

    public PSLikes(String site, String likeId, String type) {
		super();
		this.site = site;
		this.likeId = likeId;
		this.type = type;
	}

	/**
     * @param id the id to set
     */
    public void setId(String id)
    {        
    	this.id = id == null ? 0 : Long.valueOf(id);
    }

    /**
     * @return the likeId
     */
    public String getLikeId()
    {
        return likeId;
    }

    /**
     * @param url the likeId to set
     */
    public void setLikeId(String likeId)
    {
        this.likeId = likeId;
    }

    /**
     * @return the type
     */
    public String getType()
    {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * @return the site
     */
    public String getSite()
    {
        return site;
    }

    /**
     * @param site the site to set
     */
    public void setSite(String site)
    {
        this.site = site;
    }

    /**
     * @return the id
     */
    public String getId()
    {        
    	return String.valueOf(id);
    }

    /**
     * @return the total
     */
    public int getTotal()
    {
        return total;
    }

    /**
     * @param total the total to set
     */
    public void setTotal(int total)
    {
        this.total = total;
    }
}
