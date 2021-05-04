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
