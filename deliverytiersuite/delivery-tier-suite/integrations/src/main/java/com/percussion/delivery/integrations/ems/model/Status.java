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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.delivery.integrations.ems.model;

/*
 * Example Data
 * <Statuses>
  <Data>
    <Description>Confirmed</Description>
    <ID>36</ID>
    <StatusTypeID>-14</StatusTypeID>
    <DisplayOnWeb>true</DisplayOnWeb>
  </Data>
  </Statuses>
 */
public class Status {
	private int id;
	private String description;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getStatusTypeId() {
		return statusTypeId;
	}
	public void setStatusTypeId(int statusTypeId) {
		this.statusTypeId = statusTypeId;
	}
	public boolean isDisplayOnWeb() {
		return displayOnWeb;
	}
	public void setDisplayOnWeb(boolean displayOnWeb) {
		this.displayOnWeb = displayOnWeb;
	}
	private int statusTypeId;
	private boolean displayOnWeb;
}
