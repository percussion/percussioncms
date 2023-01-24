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
