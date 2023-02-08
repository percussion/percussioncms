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
