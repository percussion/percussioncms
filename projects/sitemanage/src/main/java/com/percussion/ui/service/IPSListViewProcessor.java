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
package com.percussion.ui.service;

import com.percussion.ui.data.PSDisplayPropertiesCriteria;

/**
 * Provides additional processing for the {@link IPSListViewHelper}
 * 
 * @author JaySeletz
 *
 */
public interface IPSListViewProcessor
{
    public static final String TEMPLATE_NAME = "templateName";
    
    public static final String LINK_TEXT = "linkText";
    
    /**
     * Process the items in the supplied criteria
     * 
     * @param criteria The criteria to process, not <code>null</code>.
     */
    void process(PSDisplayPropertiesCriteria criteria);
}
