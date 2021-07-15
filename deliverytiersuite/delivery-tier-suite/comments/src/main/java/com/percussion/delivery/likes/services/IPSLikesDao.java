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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
