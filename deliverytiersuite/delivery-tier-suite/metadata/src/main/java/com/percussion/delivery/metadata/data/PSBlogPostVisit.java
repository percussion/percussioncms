/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
