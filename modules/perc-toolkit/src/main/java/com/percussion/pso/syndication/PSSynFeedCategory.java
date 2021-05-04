/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.syndication;

import com.rometools.modules.mediarss.types.Category;

/***
 * For Media RSS: 
 * <media:category>

Allows a taxonomy to be set that gives an indication of the type of media content, and its particular contents. It has 2 optional attributes.

        <media:category scheme="http://search.yahoo.com/mrss/category_
        schema">music/artist/album/song</media:category>

        <media:category scheme="http://dmoz.org" label="Ace Ventura - Pet
        Detective">Arts/Movies/Titles/A/Ace_Ventura_Series/Ace_Ventura_
        -_Pet_Detective</media:category>

        <media:category scheme="urn:flickr:tags">ycantpark
        mobile</media:category>
scheme is the URI that identifies the categorization scheme. It is an optional attribute. If this attribute is not included, the default scheme is 'http://search.yahoo.com/mrss/category_schema'.

label is the human readable label that can be displayed in end user applications. It is an optional attribute.

 * @author natechadwick
 *
 */
public class PSSynFeedCategory {

	public static final String SCEHEME_FLICKR_TAGS = Category.SCHEME_FLICKR_TAGS;
	
	private Category category;

	public PSSynFeedCategory(Category arg) {
		this.category = arg;
	}

	/**
	 * 
	 * @return
	 */
	public String getScheme(){
		return category.getScheme();
	}
	
	public String getValue(){
		return category.getValue();
	}
	
	/***
	 * label is the human readable label that can be displayed in end user applications. It is an optional attribute.
	 * @return
	 */
	public String getLabel(){
		return category.getLabel();
	}

	@Override
	public String toString(){
		return category.getLabel();	
	}
}
