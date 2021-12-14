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

import com.rometools.modules.mediarss.types.Thumbnail;


/***
 * <media:thumbnail>

Allows particular images to be used as representative images for the media object. If multiple thumbnails are included, and time coding is not at play, it is assumed that the images are in order of importance. It has 1 required attribute and 3 optional attributes.

        <media:thumbnail url="http://www.foo.com/keyframe.jpg" width="75" height="50" time="12:05:01.123" />
url specifies the url of the thumbnail. It is a required attribute.

height specifies the height of the thumbnail. It is an optional attribute.

width specifies the width of the thumbnail. It is an optional attribute.

time specifies the time offset in relation to the media object. Typically this is used when creating multiple keyframes within a single video. The format for this attribute should be in the DSM-CC's Normal Play Time (NTP) as used in RTSP [RFC 2326 3.6 Normal Play Time]. It is an optional attribute.
 * @author natechadwick
 *
 */
public class PSSynFeedThumbnail {

private Thumbnail thumb;


/***
 * Returns the thumbHeight.
 * @return
 */
public Integer	getHeight() {
	return thumb.getHeight();
}

/***
 * returns the time that the thumbnail was captured from its source
 * @return
 */
public String getTime(){
	return thumb.getTime().toString();
}

/***
 * Return the URL
 * @return
 */
public String getUrl() {
	return thumb.getUrl().toString();
}

public Integer	getWidth(){
	return thumb.getWidth();
}


public PSSynFeedThumbnail(Thumbnail arg){
	this.thumb = arg;
}
	
}
