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
