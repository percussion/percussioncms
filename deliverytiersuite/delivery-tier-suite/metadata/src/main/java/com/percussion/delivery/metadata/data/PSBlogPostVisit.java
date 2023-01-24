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

package com.percussion.delivery.metadata.data;

import com.percussion.delivery.metadata.IPSBlogPostVisit;

import java.math.BigInteger;
import java.util.Date;
import java.util.Optional;

public class PSBlogPostVisit implements IPSBlogPostVisit {

	private String pagePath;
	private Date date;
	private BigInteger hitCount;

    public PSBlogPostVisit(){

    }
	public PSBlogPostVisit(String pagePath, Date date, BigInteger hitCount) {
		this.pagePath = pagePath;
		this.date = Optional
				.ofNullable(date)
				.map(Date::getTime)
				.map(Date::new)
				.orElse(null);
		this.hitCount = hitCount;
	}
	
	@Override
	public BigInteger getHitCount() {
		return hitCount;
	}

	@Override
	public void setHitCount(BigInteger count) {
		hitCount = count;
	}

	@Override
	public Date getHitDate() {
		return Optional
				.ofNullable(date)
				.map(Date::getTime)
				.map(Date::new)
				.orElse(null);
	}

	@Override
	public void setHitDate(Date date) {
		this.date = Optional
				.ofNullable(date)
				.map(Date::getTime)
				.map(Date::new)
				.orElse(null);
	}

	@Override
	public String getPagepath() {
		return this.pagePath;
	}

	@Override
	public void setPagepath(String pagePath) {
		this.pagePath = pagePath;
	}

}
