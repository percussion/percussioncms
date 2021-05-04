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

import com.rometools.modules.mediarss.types.Rating;

/***
 * <media:rating>

This allows the permissible audience to be declared. If this element is not included, it assumes that no restrictions are necessary. It has one optional attribute.

               <media:rating scheme="urn:simple">adult</media:rating>
               <media:rating scheme="urn:icra">r (cz 1 lz 1 nz 1 oz 1 vz 1)</media:rating>
               <media:rating scheme="urn:mpaa">pg</media:rating>

               <media:rating scheme="urn:v-chip">tv-y7-fv</media:rating>
scheme is the URI that identifies the rating scheme. It is an optional attribute. If this attribute is not included, the default scheme is urn:simple (adult | nonadult).

For compatibility, a medai:adult tag will appear in the ratings as a urn:simple equiv.
 * 
 * @author natechadwick
 *
 */
public class PSSynFeedRating {
	
	private Rating rating;
	
	
	public String getScheme(){
		return rating.getScheme();
	}
	
	public String getValue(){
		return rating.getValue();
	}
	
	
	public PSSynFeedRating(Rating arg){
		rating = arg;
	}

	
	@Override
	public String toString(){
		return rating.getValue();
		
	}
}
