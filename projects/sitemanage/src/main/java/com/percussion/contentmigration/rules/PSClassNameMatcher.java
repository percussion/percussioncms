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
package com.percussion.contentmigration.rules;

import static com.percussion.contentmigration.rules.PSBaseMatchingMigrationRule.filterClassName;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.Validate;
import org.jsoup.nodes.Element;

/**
 * Handles matching an element based on a set of class names, walking up the parent element chain.  Each time
 * a method is called that walks up the parent chain, the current element is set to the last parent
 * referenced (see {@link #getCurrentElement()}).
 * 
 * @author JaySeletz
 */
public class PSClassNameMatcher
{
    Element element;
    
    Element cur;
    
    List<Element> parents = new ArrayList<Element>();

    /**
     * Construct the matcher from the Element to start matching on, walking up it's parent chain.
     * 
     * @param element The element, not <code>null</code>.
     */
    public PSClassNameMatcher(Element element)
    {
        Validate.notNull(element);
        this.element = element;
        cur = element;
        parents.addAll(cur.parents());
    }
    
    /**
     * Get the current elements comparable class names.
     * 
     * @return The names, never <code>null</code>, may be empty.
     */
    public Set<String> getCurrentElementClasses()
    {
        return getFilteredClassNames(cur);
    }
    
    /**
     * Get the element this matcher is currently referencing, may be the element supplied during construction
     * or one of its parent elements.
     * 
     * @return The element, not <code>null</code>.
     */
    public Element getCurrentElement()
    {
        return cur;
    }
    
    /**
     * Walk the parent elements of the current element and for the first element with comparable class names, return them, 
     * setting the current element reference to the matched element (see {@link #getCurrentElement()})
     * 
     * @return The set of class names, may be empty if none found in any element in the list, not <code>null</code>.
     */
    public Set<String> getNextParentElementClasses()
    {
        Set<String> classNames = new HashSet<String>();

        while (classNames.isEmpty() && !parents.isEmpty())
        {
            cur = parents.remove(0);
            classNames = getFilteredClassNames(cur);
        }
        
        return classNames;
    }
    

    private Set<String> getFilteredClassNames(Element elem)
    {
        Set<String> classNames = elem.classNames();
        filterClassName(classNames);
        return classNames;
    }

    /**
     * Determine if the current element has any parent elements.
     * 
     * @return <code>true</code> if it does, <code>false</code> otherwise.
     */
    public boolean hasMoreParents()
    {
        return !parents.isEmpty();
    }

    /**
     * Get the element supplied during construction.
     * 
     * @return The element, not <code>null</code>.
     */
    public Element getSrcElement()
    {
        return element;
    }

    /**
     * Resets the current element reference to the first parent element of the current element, and checks it
     * for any class name matches with the supplied set. 
     * 
     * @param classNames The set to match, may not be <code>null</code>.
     * 
     * @return <code>true</code> if the parent has a match, <code>false</code> if not, or if the current element
     * has no parent elements.  
     */
    public boolean matchParentClasses(Set<String> classNames)
    {
        Validate.notNull(classNames);
        
        if (parents.isEmpty())
            return false;
        
        cur = parents.remove(0);
        return (CollectionUtils.containsAny(cur.classNames(), classNames));
    }
}
