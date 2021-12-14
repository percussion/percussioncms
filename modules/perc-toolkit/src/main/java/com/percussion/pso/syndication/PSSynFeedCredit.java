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

import com.rometools.modules.mediarss.types.Credit;

/***
 * 
 * <media:credit>

Notable entity and the contribution to the creation of the media object. Current entities can include people, companies, locations, etc. Specific entities can have multiple roles, and several entities can have the same role. These should appear as distinct <media:credit> elements. It has 2 optional attributes.

        <media:credit role="producer" scheme="urn:ebu">entity name</media:credit>
 
role specifies the role the entity played. Must be lowercase. It is an optional attribute.

scheme is the URI that identifies the role scheme. It is an optional attribute. If this attribute is not included, the default scheme is 'urn:ebu'. See: European Broadcasting Union Role Codes.

Example roles:

        actor
        anchor person
        author
        choreographer
        composer
        conductor
        director
        editor
        graphic designer
        grip
        illustrator
        lyricist
        music arranger
        music group
        musician
        orchestra
        performer
        photographer
        producer
        reporter
        vocalist
        
 * @author natechadwick
 *
 */
public class PSSynFeedCredit {

	private Credit credit;
	
	public String getName(){
		return credit.getName();
	}
	
	public String getRole(){
		return credit.getRole();
	}
	
	public String getScheme(){
		return credit.getScheme();
	}
	
	public PSSynFeedCredit(Credit arg){
		credit = arg;
	}
	
	/***
	 * Returns in <Role>: <Name> format.
	 */
	@Override
	public String toString(){
		String ret = "";
		
		if(credit.getRole()!= null && !credit.getRole().equals("")){
			ret = credit.getRole();
		}
		
		if(credit.getName()!=null && !credit.getName().equals("")){
			if(!ret.equals("")){
				ret = ret.concat(": " + credit.getName());
			}else{
				ret = credit.getName();
			}
		}
			
		return ret;
	}
}
