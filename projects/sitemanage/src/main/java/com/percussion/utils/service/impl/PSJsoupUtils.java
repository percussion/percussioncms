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

package com.percussion.utils.service.impl;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class PSJsoupUtils
{
    public static Element closestParentByClass(Element document, String cuurentElemSelector, String parentClass)
    {
        Validate.notNull(document);
        Validate.notNull(cuurentElemSelector);
        Validate.notNull(parentClass);
        Elements elems = document.select(cuurentElemSelector);
        if(elems.size()!=1)
            return null;
        Element elem = elems.get(0);
        Element parent = null;
        Elements parents = elem.parents();
        for (Element pelem : parents)
        {
            if(getClassNames(pelem).contains((parentClass)))
            {
                parent = pelem;
                break;
            }
        }
        return parent;
    }
    
    public static String generateAttributeSelector(String attrName, String attrValue)
    {
        Validate.notNull(attrName);
        String selector = "[" + attrName + "]"; 
        if(StringUtils.isNotBlank(attrValue))
        {
            selector = "[" + attrName + "=" + attrValue + "]";
        }
        return selector;
    }
    
    /**
     * Helper method to get the classnames for a given Jsoup element.
     * Jsoup element's classNames method returns a set of class names, but looks like
     * there is a bug in Jsoup classNames method where it doesn't split the class name string
     * properly if it has a non-breaking space. Here is a workaround method to get the
     * class names of a given element.
     * @param elem assumed not <code>null</code>
     * @return Set of class names never <code>null</code> may be empty.
     */
    private static Set<String> getClassNames(Element elem)
    {
        String classNames = elem.attr("class");
        classNames = classNames.replaceAll("\u00A0", " ");
        String[] names = classNames.split("\\s+");
        Set<String> result = new LinkedHashSet<>(Arrays.asList(names));
        return result;        
    }
}
