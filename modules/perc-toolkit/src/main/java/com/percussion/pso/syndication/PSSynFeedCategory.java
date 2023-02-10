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
