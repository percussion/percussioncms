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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Find a content match based on the class name of an element. If multiple
 * classes are present then tries to find elements based on all classes first.
 * If multiple matches are found, attempts to find unique match by comparing
 * class names on parent elements, walking up the parent chain until a unique
 * match is found. If no unique match is found (no match or multiple elements
 * that match), then <code>null</code> is returned.
 * 
 * @author JaySeletz
 */
public class PSClassNameMatchingMigrationRule extends PSBaseMatchingMigrationRule
{
    @Override
    protected String matchOnRule(String widgetId, Document sourceDoc, Document targetDoc)
    {
        // find region
        Element regionElem = findEnclosingRegionElement(widgetId, sourceDoc);
        if(regionElem == null)
            return null;
        
        // filter non-perc classnames
        PSClassNameMatcher srcMatch = new PSClassNameMatcher(regionElem);
        Set<String> classNames = srcMatch.getCurrentElementClasses();
        if (classNames.isEmpty())
        {
            // nothing to match on
            return null;
        }            
        
        // Find matches in the target doc
        Elements elems = findMatches(targetDoc, classNames);
        if (elems.size() == 0)
        {
            // nothing matches
            return null;  
        }
        
        if (elems.size() == 1)
        {
            // exactly 1 match, we are done!
            return elems.get(0).html();
        }   
        
        if (!srcMatch.hasMoreParents())
        {
            // no match, no parents, nothing left to match on
            return null; 
        }
        
        List<PSClassNameMatcher> matches = new ArrayList<>();
        for (Element elem : elems)
        {
            matches.add(new PSClassNameMatcher(elem));
        }
        
        Element match = findParentMatch(srcMatch, matches);
        if (match == null)
            return null;
        
        return match.html();
    }

    /**
     * Find a match by class name for parent elements of the supplied elements
     * 
     * @param srcMatch The element whose parents to match
     * @param targetElems The elements whose parents to check
     * 
     * @return The match if found, otherwise <code>null</code>.
     */
    private Element findParentMatch(PSClassNameMatcher srcMatch, List<PSClassNameMatcher> matches)
    {
        Set<String> classNames = srcMatch.getNextParentElementClasses();
        if (classNames.isEmpty())
            return null;  // nothing left to match
        
        filterParentMatches(matches, classNames);
        if (matches.isEmpty())
            return null;
        
        if (matches.size() == 1)
            return matches.get(0).getSrcElement();
        
        // recurse up the parent stack and try again
        return findParentMatch(srcMatch, matches);
    }

    /**
     * Remove any matches from the supplied set whose immediate parents do not match on any of the 
     * supplied classnames
     * 
     * @param matches The matches to check, assumed not <code>null</code>, the set is modified.
     * @param classNames The set of classnames to check, assumed not <code>null<code/> or empty. 
     */
    private void filterParentMatches(List<PSClassNameMatcher> matches, Set<String> classNames)
    {
        Iterator<PSClassNameMatcher> iter = matches.iterator();
        while (iter.hasNext())
        {
            PSClassNameMatcher match = iter.next();
            if (!match.matchParentClasses(classNames))
                iter.remove();
        }
    }

    private Elements findMatches(Document targetDoc, Set<String> classNames)
    {
        Elements found = new Elements();
        //FB: SBSC_USE_STRINGBUFFER_CONCATENATION NC 1-17-16
        StringBuffer buffer = new StringBuffer();
        for (String className : classNames)
        {
            buffer.append(".").append(className);
        }
        String clsSelector = buffer.toString();
        
        Elements elems = targetDoc.select(clsSelector);
        if(elems != null && elems.size()==1)
        {
            found.addAll(elems);
        }
        else
        {
            for (String className : classNames)
            {
                elems = targetDoc.select("." + className);
                for (Element element : elems)
                {
                    if(!found.contains(element))
                        found.add(element);
                }
            }
        }
        return found;
    }
}
