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
    
    List<Element> parents = new ArrayList<>();

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
        Set<String> classNames = new HashSet<>();

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
