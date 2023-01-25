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

import com.rometools.rome.feed.synd.SyndImage;

/***
 * A Velocity friendly class for a syndication image.
 */
public class PSSynFeedImage {
	
	private SyndImage image;
	
	
/***
* Returns the image link.
* @return
*/
public String getLink() {
	return image.getLink();
}
    
/***
 * Returns the image title.
 * @return
 */
public String getTitle() {
	return image.getTitle();
}
    
/***
 *    Returns the image URL.
 * @return
 */
public String	getUrl() {
 return image.getUrl();
}
    /***
     * Returns the image description.
     * @return
     */
    public String getDescription(){
		return image.getDescription();
	}
	 
	public PSSynFeedImage(SyndImage arg){
		this.image = arg;
	}

}
