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
