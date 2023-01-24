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

/**
 * @author Administrator
 * 
 */
public interface IPSLikes
{

    /**
     * @return the page path, the relative path to the page that this likes is
     *         on, not including the site. Never <code>null</code> or empty.
     */
    public String getSite();

    public String getType();

    public String getLikeId();
    
    public int getTotal();
    
    public String getId();
    
    public void setId(String id);
    
    public void setSite(String site);

    public void setType(String type);

    public void setLikeId(String id);
    
    public void setTotal(int total);

    /**
     * Comment approval states.
     */
    public enum TYPE {
        PAGE, COMMENT, IMAGE
    }
}
