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

(function($)
{
    
    function getRegionSelectorsFrom(cssString)
    {
    
	// Re%turn an array of region selectors present in the CSS string.
	// This currently blindly assumes that the IDs are the region
	// selectors. Make a note if this changes.

	// returns an array of the found selectors. It is the intention of this code
	// that all RegExp objects used are only used internally within the 
	// component functions.

	var foundSelectors = new Array();
	var re = new RegExp("\\#(.*?)\\{","g");
	var tmpMatch = cssString.match(re);
	
	for (var item in tmpMatch)
	    {
		var tmpSelector = tmpMatch[item].substring(1,tmpMatch[item].length-1);
		foundSelectors.push(tmpSelector);
	    }

	return foundSelectors;

    }

    /**
     * Gets a single CSS selector, with all delimiters, intermediate step.
     */
    function get_region_selector(cssString,selector)
    {
	var re = new RegExp("\\#"+selector+"\\{(.*?)\\}","g");
	return cssString.match(re);
    }

    function trim_it(inString)
    {
	inString = inString.replace(/^\ /,""); // trim spaces off beginning.
	inString = inString.replace(/\;/,""); // trim off any semicolons.
	return inString;
    }

    function get_properties_for(cssString,selector)
    {
	var cssFragment = get_region_selector(cssString,selector);
	var tmpre = new RegExp("\\{(.*?)\\}","g");   // match the selectors with delimiters.
	var tmpPropertiesre = new RegExp("([^:]*):([^;]*);","g");  // match the key/value pairs inside a selector.
	var cssProperties = {};
	
	/* Get the inside */
	var tmpMatch = tmpre.exec(cssFragment);
	tmpMatch = tmpMatch[1];  // Match the inside.
	// At this point we should just have the raw string of properties.
	// Now, we parse each property, one by one. and put it into the
	// property list to return.
	var propertiesArray = tmpMatch.match(tmpPropertiesre);
	// console.log(propertiesArray);

	// Traverse the properties, extract them, and cram them into cssProperties

	for (var propertyItem in propertiesArray)
	    {
		// console.log("Key: "+propertyItem+" Value: "+propertiesArray[propertyItem]);
		var tmpPropertyFragment = propertiesArray[propertyItem].match(/([^:]*):([^;]*);/g);
		var tmpSplitProperty = tmpPropertyFragment[0].split(":");
		var cssKey = trim_it(tmpSplitProperty[0]);
		var cssValue = trim_it(tmpSplitProperty[1]);
		// console.log("Key is "+cssKey+ " Value is"+cssValue);
		cssKey = cssKey.replace(" ","");
		cssValue = cssValue.replace(" ","");
		cssProperties[cssKey] = cssValue;
	    }

	// console.log(cssProperties);
	return cssProperties;

    }

    function parse_region_css(cssString)
    {
	// Given a string of CSS, break it up into the following data structure
	///////////////////////////////////////////////////////////////////////
	// 
	// returns:
	// regionCSS[css_selector_1][css_property_1][css_value]
	// regionCSS[css_selector_1][css_property_2][css_value]
	// regionCSS[css_selector_1][..............][css_value]
	// regionCSS[css_selector_2][css_property_1][css_value]
	// regionCSS[css_selector_2][css_property_2][css_value]
	// regionCSS[css_selector_2][..............][css_value]
	//
	// ... and so on.
	//////////////////////////////////////////////////////////////////////

	// This is a very domain specific CSS parser, in that it assumes that
	// all selectors are IDs (looking for #), and that the resulting properties
	// are properly terminated.

	cssString = cssString.replace("\"",""); // HACK!

	var cssSelectors = getRegionSelectorsFrom(cssString);
	var regionCSS = {};

	for (var item in cssSelectors)
	    {
		var tmp = {};
		var currentSelector = cssSelectors[item];
		tmp = get_properties_for(cssString,currentSelector);
		regionCSS[currentSelector] = tmp;
	    }

	// console.log("Region CSS Follows");
	// console.log(regionCSS);
	return regionCSS;

    }

    /**
     * Takes the region CSS data and outputs a ready made string to be put back into the Template object.
     */
    function region_css_out(regionCSS)
    {
	// See the above comments for a data structure format.

	var outputCSS = "";

	// This is peeled back in a nested set of for's
	for (var selector in regionCSS)
	    {
		outputCSS += "#" + selector + "{";
		for (var property in selector)
		    {
			outputCSS += " " + property + ": " + selector[property] + "; ";
		    }
		outputCSS += "} ";
	    }
	
	return outputCSS;

    }

    $.perc_css_utils = {
	getRegionSelectorsFrom : getRegionSelectorsFrom,
	get_region_selector : get_region_selector,
	get_properties_for : get_properties_for,
	parse_region_css : parse_region_css,
	region_css_out : region_css_out
    };

})(jQuery);
