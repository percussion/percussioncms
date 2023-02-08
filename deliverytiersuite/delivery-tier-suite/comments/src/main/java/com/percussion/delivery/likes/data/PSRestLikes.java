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
package com.percussion.delivery.likes.data;

import com.percussion.delivery.likes.data.IPSLikes;

/**
 * @author davidpardini
 * 
 */
public class PSRestLikes implements IPSLikes
{

    private String id;

    private String likeId;

    private String type;

    private String site;

    private int total;

    /**
     * @return the id
     */
    public String getId()
    {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * @return the likeId
     */
    public String getLikeId()
    {
        return likeId;
    }

    /**
     * @param likesId the likeId to set
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
