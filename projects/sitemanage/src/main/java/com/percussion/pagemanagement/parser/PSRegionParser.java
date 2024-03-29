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
package com.percussion.pagemanagement.parser;

import static org.apache.commons.lang.Validate.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.Tag;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.percussion.pagemanagement.data.PSAbstractRegion;
import com.percussion.pagemanagement.data.PSRegionCode;
import com.percussion.pagemanagement.data.PSRegionNode;
import com.percussion.pagemanagement.parser.IPSRegionParser.IPSRegionParserRegionFactory;

/**
 * Walks the HTML Tree pulling out regions and code transforming
 * them into our Abstract Syntax Tree: {@link PSParsedRegionTree}.
 * <p> 
 * A region is an HTML element that utilize the {@link #REGION_CLASS}.
 * Everything else is html/velocity code.
 * <p>
 * The implementation is a simple stack based FSM parser.
 * 
 * @param <REGION> region type
 * @param <CODE> code type
 */
public class PSRegionParser<REGION extends PSAbstractRegion, CODE extends PSRegionCode>
{
    private IPSRegionParserRegionFactory<REGION, CODE> regionFactory;

    public PSRegionParser(IPSRegionParserRegionFactory<REGION, CODE> regionFactory)
    {
        super();
        this.regionFactory = regionFactory;
    }


    
    private class RegionToken {
        REGION region;
        Element element;
        public RegionToken(Element element, REGION region)
        {
            super();
            this.element = element;
            this.region = region;
        }
        
    }
    
    /**
     * Parses HTML to turn into an Abstract Syntax Tree.
     * @param text should be valid html never <code>null</code> or empty.
     * @return an AST of regions and code.
     */
    public PSParsedRegionTree<REGION, CODE> parse(String text) {
        Source src = new Source(text);
        Iterator<Segment> it = src.iterator();
        Stack<RegionToken> regionStack = new Stack<>();
        PSParsedRegionTree<REGION, CODE> tree = new PSParsedRegionTree<>(regionFactory);
        REGION root = tree.getRootNode();
        RegionToken rootToken = new RegionToken(null, root);
        regionStack.push(rootToken);
        boolean inCode = false;
        CODE code = null;
        /*
         * Loop through all the HTML elements.
         */
        while(it.hasNext()) {
            Element element = regionStack.peek().element;
            REGION current = regionStack.peek().region;
            Segment seg = it.next();
            if (current.getChildren() == null) {
                current.setChildren(new ArrayList<>());
            }
            /*
             * Start of a Region ?
             */
            if (isRegionStart(seg)) {
                inCode = false;
                StartTag st = (StartTag) seg;
                Element e = st.getElement();
                REGION r = createRegion(e);
                current.getChildren().add(r);
                tree.getRegions().put(r.getRegionId(), r);
                RegionToken rt = new RegionToken(e,r);
                if (e.getEndTag() != null) { // FINDBUGS: NC - 1-16-16 - Removed ;
                    regionStack.push(rt);
                }
            }
            /*
             * End of a region ?
             */
            else if( element != null && seg.equals(element.getEndTag()) ) {
                inCode = false;
                regionStack.pop();   
            }
            /*
             * In a code fragment ?
             */
            else {
                /*
                 * Already in code fragment ?
                 */
                if (inCode) {
                    String html = code.getTemplateCode();
                    html = html == null ? "" : html; 
                    code.setTemplateCode(html + seg.toString());
                }
                /*
                 * New code fragment.
                 */
                else {
                    code = regionFactory.createRegionCode();
                    code.setTemplateCode(seg.toString());
                    current.getChildren().add(code);
                    inCode = true;
                }
            }
            //ms_log.debug(seg.getClass() + seg.toString());
        }
        //ms_log.debug(root);
        return tree;
    }
    
    
    private boolean isRegionStart(Segment seg) {
        if (seg instanceof StartTag) {
            StartTag st = ((StartTag) seg);
            String divClass = st.getAttributeValue(CLASS_ATTR);
            if (divClass != null && divClass.contains(REGION_CLASS))
            {
                return true;
            }
        }
        return false;
    }
    
    
    private REGION createRegion(Element elem)
    {
        if (elem == null) {
            throw new IllegalArgumentException("elem may not be null");
        }
        String regionId = elem.getAttributeValue(REGION_ID_ATTR);
        notNull(regionId);
        REGION region = regionFactory.createRegion(regionId);
        region.setRegionId(regionId);
        region.setStartTag(elem.getStartTag().toString());
        Tag end = elem.getEndTag();
        if (end != null)
        {
            region.setEndTag(end.toString());
        }
        return region;
    }

    /**
     * String constant that represents the name of the region div class.
     */
    public static final String REGION_CLASS = "perc-region";


    /**
     * String constant that represents the name of the region id attribute.
     */
    public static final String REGION_ID_ATTR = "id";

    /**
     * String constant that represents the name of the class attribute.
     */
    private static final String CLASS_ATTR = "class";

    /**
     * Logger for this class.
     */

    private static final Logger ms_log = LogManager.getLogger(PSRegionParser.class);
}
