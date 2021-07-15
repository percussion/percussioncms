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
