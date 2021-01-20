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
package com.percussion.delivery.comments.services;

import com.percussion.delivery.comments.data.IPSComment;
import com.percussion.delivery.comments.data.PSCommentCriteria;
import com.percussion.delivery.comments.data.PSPageInfo;
import com.percussion.delivery.comments.data.IPSComment.APPROVAL_STATE;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author erikserating
 *
 */
public interface IPSCommentsDao
{

    public List<IPSComment> find(PSCommentCriteria criteria) throws Exception;
    
    public List<PSPageInfo> findPagesWithComments(String site) throws Exception;
    
    public Set<String> findSitesForCommentIds(Collection<String> ids) throws Exception;
    
    public APPROVAL_STATE findDefaultModerationState(String site) throws Exception;

    public void save(IPSComment comment) throws Exception;
    
    public void saveDefaultModerationState(String sitename, APPROVAL_STATE state) throws Exception;

    public void delete(Collection<String> commentIds) throws Exception;

    public void moderate(Collection<String> commentIds, APPROVAL_STATE newApprovalState) throws Exception;

}