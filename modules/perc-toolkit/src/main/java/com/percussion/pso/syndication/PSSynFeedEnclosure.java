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
