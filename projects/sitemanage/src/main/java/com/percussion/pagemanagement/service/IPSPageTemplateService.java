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
package com.percussion.pagemanagement.service;

import com.percussion.share.service.exception.PSDataServiceException;

import java.util.List;

import javax.jcr.RepositoryException;

/**
 * Service to encapsulate operations acting on both pages and templates
 * 
 * @author JaySeletz
 *
 */
public interface IPSPageTemplateService
{
    
    /**
     * Name of the field on page content type to store the template id.
     */
    public static final String FIELD_NAME_TEMPLATE_ID = "templateid";
    
    //fixme: doc and remove from page servcie
    public void changeTemplate(String pageId, String templateId) throws PSDataServiceException;

    /**
     * Find all the pages that use a certain template and return their ids
     * 
     * @param templateId The id of the template, not <code>null</code>.
     * 
     * @return The list of ids, not <code>null</code>, may be empty.
     */
    List<Integer> findPageIdsByTemplate(String templateId) throws IPSPageService.PSPageException;
    
}
