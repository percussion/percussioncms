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
package com.percussion.searchmanagement.service;

import com.percussion.share.service.exception.PSValidationException;

import java.util.Set;

/**
 * Provides search indexing support for pages.
 *  
 * @author peterfrontiero
 */
public interface IPSPageIndexService {
    
    /**
     * Indexes the specified pages and/or templates.  Templates are not indexed directly.  Instead, all pages which use
     * the templates are indexed.
     * 
     * @param set of content id's to index.  May not be <code>null</code>.
     */
    public void index(Set<Integer> ids) throws PSValidationException;
}
