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

import com.percussion.utils.service.impl.PSJsoupUtils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Base class for migration rules, handles basic validation of parameters and provides some common functionality
 * likely to be needed by most matching rules.
 * 
 * @author JaySeletz
 *
 */
public abstract class PSBaseMatchingMigrationRule implements IPSContentMigrationRule
{
    // todo: move to interface
    public static final String[] IGNORE_CLASS_PREFIXES = {"perc-", "vspan_", "hspan_", "ui-"};
    public static final List<String> IGNORE_CLASS_NAMES = Arrays.asList("ui-helper-clearfix");
    
    @Override
    public String findMatchingContent(String widgetId, Document sourceDoc, Document targetDoc)
    {
        Validate.notEmpty(widgetId);
        Validate.notNull(sourceDoc);
        Validate.notNull(targetDoc);
        
        return matchOnRule(widgetId, sourceDoc, targetDoc);
    }

    /**
     * Derived class implementation delegated from {@link #findMatchingContent(String, Document, Document)},
     * that method handles basic contract validation of parameters.
     */
    protected abstract String matchOnRule(String widgetId, Document sourceDoc, Document targetDoc);

    /**
     * Given a widget id, finds the region element that is the immediate parent wrapping the widget, which
     * is the element rules should use for matching in the reference/source document.
     * 
     * @param widgetId The widget id, not <code>null</code>.
     * @param sourceDoc The doc to use, not <code>null</code>.
     * 
     * @return The element, <code>null</code> if no match is found.
     */
    protected static Element findEnclosingRegionElement(String widgetId, Document sourceDoc)
    {
        Validate.notNull(widgetId);
        Validate.notNull(sourceDoc);
        
        Element regionElem = PSJsoupUtils.closestParentByClass(sourceDoc,
                PSJsoupUtils.generateAttributeSelector(IPSContentMigrationRule.ATTR_WIDGET_ID, widgetId),
                IPSContentMigrationRule.CLASS_PERC_REGION);
        
        return regionElem;
    }
    
    /**
     * Given a set of class names, filters out the names used by the system as we don't want to match on those.
     * 
     * @param classNames The names, not <code>null</code>.
     */
    public static void filterClassName(Set<String> classNames)
    {
        Validate.notNull(classNames);
        
        classNames.removeAll(IGNORE_CLASS_NAMES);
        for (Iterator<String> iter = classNames.iterator(); iter.hasNext();)
        {
            String className = iter.next();

            for (String ignore : IGNORE_CLASS_PREFIXES)
            {
                if (StringUtils.isBlank(className) || className.startsWith(ignore))
                {
                    iter.remove();
                    break;
                }
            }
        }
    }
}
