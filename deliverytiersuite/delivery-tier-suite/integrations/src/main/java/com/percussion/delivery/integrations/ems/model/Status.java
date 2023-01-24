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
