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
