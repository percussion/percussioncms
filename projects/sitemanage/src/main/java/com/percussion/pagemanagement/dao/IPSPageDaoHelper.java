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
package com.percussion.pagemanagement.dao;

import com.percussion.pagemanagement.data.PSPage;
import com.percussion.searchmanagement.data.PSSearchCriteria;
import com.percussion.share.service.exception.PSValidationException;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author miltonpividori
 *
 */
public interface IPSPageDaoHelper
{
    /**
     * Takes a {@link PSPage} object and sets its workflowId property according
     * to the parent folder workflow association.
     * @param page
     */
    void setWorkflowAccordingToParentFolder(PSPage page) throws PSValidationException;
    
    /**
     * Get the workflow Id to use when creating pages in the specified folder path.
     * 
     * @param folderPath The path, cannot be <code>null<code/> or empty
     * 
     * @return The workflow Id
     */
    int getWorkflowIdForPath(String folderPath) throws PSValidationException;
    
    /**
     * Finds all page IDs which utilize the specified template.
     * 
     * @param templateId never blank.
     * 
     * @return list of page IDs, never <code>null</code>, may be empty.
     */
    Collection<Integer> findPageIdsByTemplate(String templateId);

    /**
     * After the removal of a template that was being used by an older revision
     * of a page, we need to update that revision to use the template that the
     * page is using in the current revision.
     * 
     * @param deletedTemplate {@link String} with the template id that is deleted. Must not
     *            be blank.
     */
    void replaceTemplateForPageInOlderRevisions(String deletedTemplate);

    /**
     * Gets the ids of those pages that are using the given template in an older
     * revision, that is not the current revision neither the tip revision. If a
     * page also uses that template in the mentioned revisions, it is not
     * returned.
     * 
     * @param deletedTemplate {@link String} with the template id that is deleted. Must not
     *            be blank.
     * @return {@link Collection}<{@link Integer}> with the ids of the pages.
     *         Never <code>null</code> but may be empty.
     */
    Collection<Integer> findPageIdsByTemplateInRecentRevision(String deletedTemplate);
    
    /**
     * Makes a query to find the template that is being use by the page in the
     * Current Revision.
     * 
     * @param pages {@link List}<{@link Integer}> with the pages ids we
     *            are going to update. Assumed not <code>null</code>.
     * @return {@link Map}<{@link String}, {@link String}> where the key is the
     *         id of the page, and the value is te template. Never
     *         <code>null</code> but may be empty.
     */
    Map<String, String> findTemplateUsedByCurrentRevisionOfPages(List<Integer> pages);
    
    /**
     * Finds all imported page IDs which utilize the specified template.
     * 
     * @param templateId {@link String} with the template id to find. Must not
     *            be blank.
     * @param pages {@link List}<{@link Integer}> with the pages ids to find.
     *            Assumed not <code>null</code>.
     * 
     * @return list of page IDs, never <code>null</code>, may be empty.
     */
    Collection<Integer> findImportedPageIdsByTemplate(String templateId, List<Integer> pages);

    public Collection<Integer> getContentIdsForFetchingByStatus(PSSearchCriteria criteria, List<Integer> contentIDs);
    
    /**
     * Makes a query to find the link text that is being used by the page in the
     * Current Revision.
     * 
     * @param pages {@link List}<{@link Integer}> with the pages ids we
     *            are going to update. Assumed not <code>null</code>.
     * @return {@link Map}<{@link String}, {@link String}> where the key is the content
     *         id of the page, and the value is the link text. Never
     *         <code>null</code> but may be empty.
     */
    Map<String, String> findLinkTextForCurrentRevisionOfPages(List<Integer> pages);    
 
}
