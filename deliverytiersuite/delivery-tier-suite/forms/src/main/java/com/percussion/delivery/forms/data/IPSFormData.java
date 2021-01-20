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

package com.percussion.delivery.forms.data;

import java.util.Date;
import java.util.Map;
import java.util.Set;

public interface IPSFormData {

	public static final String FIELD_VALUES_SEPARATOR = "|";
	public static final String FIELD_VALUES_SEPARATOR_ESCAPE = "\\";

	public String getName();

	public Date getCreateDate();

	public char isExported();

	public Date getCreated();

	/** 
	 * Retrieve all the field names in this form.
	 * @return An unmodifiable collection. Never <code>null</code>, may be empty. 
	 */
	public Set<String> getFieldNames();

	/**
	 * Retrieve the fields and their values.
	 * @return An unmodifiable map. Never <code>null</code>, may be empty.
	 */
	public Map<String, String> getFields();

	public String getId();

	public void setId(String id);

}