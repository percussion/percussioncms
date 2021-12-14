/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.delivery.email.data;

public class PSEmailRequest implements IPSEmailRequest {

	private String toList;
	private String ccList;
	private String bodycontent;
	private String subject;
	private String bccList;

	@Override
	public void setToList(String toList) {
		this.toList = toList;
	}

	@Override
	public void setCCList(String ccList) {
		this.ccList = ccList;
	}

	@Override
	public void setBody(String bodycontent) {
		this.bodycontent = bodycontent;
	}

	@Override
	public void setSubject(String subject) {
		this.subject = subject;
	}

	@Override
	public String getToList() {
		return this.toList;
	}

	@Override
	public String getCCList() {
		return this.ccList;
	}

	@Override
	public String getBody() {
		return this.bodycontent;
	}

	@Override
	public String getSubject() {
		return this.subject;
	}

	@Override
	public void setBCCList(String bccList) {
		this.bccList = bccList;
	}

	@Override
	public String getBCCList() {
		return this.bccList;
	}

}
