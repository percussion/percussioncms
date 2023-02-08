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

package com.percussion.delivery.likes.services;

import java.util.Collection;
import java.util.List;

import com.percussion.delivery.likes.data.IPSLikes;

public interface IPSLikesDao 
{
    public List<IPSLikes> find(String site, String likeId, String type) throws Exception;

    public List<IPSLikes> findLikesForSite(String site) throws Exception;

    public void delete(Collection<String> ids) throws Exception;

    public void save(IPSLikes like) throws Exception;

    public void save(List<IPSLikes> likes) throws Exception;

    public IPSLikes create(String site, String likeId, String type) throws Exception;

    public int incrementTotal(String site, String likeId, String type) throws Exception;

    public int decrementTotal(String site, String likeId, String type) throws Exception;
}
