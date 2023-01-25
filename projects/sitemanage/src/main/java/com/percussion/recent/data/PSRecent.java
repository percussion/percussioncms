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

package com.percussion.recent.data;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.percussion.share.data.PSAbstractDataObject;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;

/**
 * Entity for PSRecentService service.
 * 
 * @author Stephen Bolton
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSRecent")
@Table(name = "PSX_RECENT")
@XmlRootElement(name = "recent")
@JsonRootName("recent")
public class PSRecent extends PSAbstractDataObject
{
    private static final long serialVersionUID = 1L;

    public static enum RecentType {
        ITEM(20), TEMPLATE(6), SITE_FOLDER(10), ASSET_FOLDER(10), ASSET_TYPE(6);
        private final int maxSize;

        RecentType(int maxSize)
        {
            this.maxSize = maxSize;
        }

        public int MaxSize()
        {
            return this.maxSize;
        }
    }

    @Id
    @GenericGenerator(name = "id", strategy = "com.percussion.data.utils.PSNextNumberHibernateGenerator")
    @GeneratedValue(generator = "id")
    @Column(name = "ID", nullable = false)
    private int id;

    @Column(name = "USER_NAME")
    private String user;

    /**
     * siteName can be null for recent entries that 
     * do not need to be filtered by site.
     */
    @Basic
    @Column(name = "SITE_NAME")
    private String siteName;

    @Column(name = "TYPE")
    @Enumerated(EnumType.STRING)
    private RecentType type;

    @Column(name = "SORTORDER")
    private int order;

    /**
     * Representation of value based upon the type and
     * controlled by the service
     */
    @Column(name = "VALUE")
    private String value;

    /**
     *
     * @param user
     * @param siteName
     * @param type
     * @param order
     * @param value
     */
    public PSRecent(String user, String siteName, RecentType type, int order, String value)
    {
        this.user = user;
        this.siteName = siteName;
        this.type = type;
        this.order = order;
        this.value = value;
    }

    /**
    * 
    */
    public PSRecent()
    {

    }

    public String getUser()
    {
        return user;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    public String getSiteName()
    {
        return siteName;
    }

    public void setSiteName(String siteName)
    {
        this.siteName = siteName;
    }

    public RecentType getType()
    {
        return type;
    }

    public void setType(RecentType type)
    {
        this.type = type;
    }

    public int getOrder()
    {
        return order;
    }

    public void setOrder(int order)
    {
        this.order = order;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

}
