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

package com.percussion.contentmigration.rules;

import org.jsoup.nodes.Document;

public interface IPSContentMigrationRule
{
    /**
     * Finds the matching content based on the rule implementation and returns it, if the content is not found returns <code>null</code>, so that
     * other rules can be applied to match the content.
     * @param widgetId must not be <code>null</code>.
     * @param sourceDoc must not be <code>null</code>, either a rendered page document or rendered template document if the page doesn't exist.
     * @param targetDoc must not be <code>null</code>, the target page document.
     * @return String matched content or <code>null</code> if not found.
     */
    String findMatchingContent(String widgetId, Document sourceDoc, Document targetDoc);
    
    static String ATTR_WIDGET_ID = "widgetid";
    static String CLASS_PERC_REGION = "perc-region";
    static String PERC_CLASS_PREFIX = "perc-";
}
