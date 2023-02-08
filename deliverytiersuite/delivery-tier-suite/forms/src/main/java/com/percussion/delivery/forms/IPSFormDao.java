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

package com.percussion.delivery.forms;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.percussion.delivery.forms.data.IPSFormData;

public interface IPSFormDao {

	public void save(IPSFormData form);
	
	/**
     * Creates a new form data object for the underlying data implementation.
     * @param formname cannot be <code>null</code> or empty.
     * @param formdata cannot be <code>null</code>.
     * @return the new instance, never <code>null</code>.
     */
	public IPSFormData createFormData(String formname, Map<String, String[]> formdata);

	public void delete(IPSFormData form);

	public long getExportedFormCount(String name);

	public long getTotalFormCount(String name);

	public void markAsExported(Collection<IPSFormData> forms);

	public void deleteExportedForms(String formName);

	public List<IPSFormData> findFormsByName(String name);

	public List<IPSFormData> findAllForms();

	public List<String> findDistinctFormNames();

}
