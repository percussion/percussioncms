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
