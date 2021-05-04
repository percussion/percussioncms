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

import com.rometools.rome.feed.synd.SyndEnclosure;

/***
 * Provides a Velocity friendly class for handling enclosures.
 * 
 * @author natechadwick
 *
 */
public class PSSynFeedEnclosure {
	
	private SyndEnclosure enc;

	/***
	 * Returns the enclosure length.
	 * @return
	 */
	 public long	getLength() {
		 return enc.getLength();
	 }
     
	 /***
	  * Returns the enclosure type.
	  * @return
	  */
	 public String getType() {
		 return enc.getType();
	 }

	 /***
	  * Returns the enclosure URL.
	  * @return
	  */
	 public String	getUrl() {
		 return enc.getUrl();
	 }
	public PSSynFeedEnclosure(SyndEnclosure arg){
		this.enc = arg;
	}
	
	
}
