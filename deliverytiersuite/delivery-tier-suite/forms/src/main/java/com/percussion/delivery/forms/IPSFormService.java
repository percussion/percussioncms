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

package com.percussion.delivery.forms;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.mail.EmailException;

import com.percussion.delivery.forms.data.IPSFormData;
import com.percussion.delivery.forms.impl.PSRecaptchaService;
import com.percussion.delivery.utils.PSEmailServiceNotInitializedException;

public interface IPSFormService 
{
    public void save(IPSFormData formdata);
    
    public void delete(IPSFormData form);
    
    public PSRecaptchaService getRecaptchaService();
    
    public void setRecaptchaService(PSRecaptchaService recaptchaService);
    
    /**
     * Creates a new form data object for the underlying data implementation.
     * @param formname cannot be <code>null</code> or empty.
     * @param formdata cannot be <code>null</code>.
     * @return the new instance, never <code>null</code>.
     */
    public IPSFormData createFormData(String formname, Map<String, String[]> formdata);
    
    /**
     * @param formName if it is  <code>null</code>, or empty then permanently removes 
     * all forms that have been marked as
     * <code>exported</code> via the {@link #markAsExported(Collection)}
     * method. Otherwise only passed in form if it exists and marked as <code>exported</code>
     * then that will be deleted.  The form name comparison is case-insensitive.
     */
    public void deleteExportedForms(String formName);
   
    /**
     * @return Never <code>null</code>, may be empty. In the latter case, all forms are returned.
     * Ordered by created date ascending.  The form name comparison is case-insensitive.
     */
    public List<IPSFormData> findFormsByName(String name);

    /**
     * @return Never <code>null</code>, may be empty. Ordered by name first,
     * ascending, then created date ascending.
     */
    public List<IPSFormData> findAllForms();
    
    /**
     * @return list with distinct form names (names which differ only by case are not considered distinct).  Never
     * <code>null</code>, may be empty.  Ordered by created date ascending.
     */
    public List<String> findDistinctFormNames();
        
    /**
     * Sets a flag on each supplied form that is used by other methods in this
     * interface. The purpose of this is to allow the user to return form data,
     * then clear the forms at a later time.
     * 
     * @param forms Never <code>null</code>, may be empty. It is OK if a
     * supplied form has already been marked.
     */
    public void markAsExported(Collection<IPSFormData> forms);
    
    /**
     * @param name the form name, may be <code>null</code> or empty in which case all
     * forms will be counted.  The form name comparison is case-insensitive.
     * @return A count of forms that have been {@link #markAsExported(Collection)}.
     */
    public long getExportedFormCount(String name);

    /**
     * @param name the form name, may be <code>null</code> or empty in which case all
     * forms will be counted.  The form name comparison is case-insensitive.
     * @return A count of all forms currently in the system.
     */
    public long getTotalFormCount(String name);

    /**
     * Emails the supplied form data using the supplied info.
     * 
     * @param toList A comma-delimited list of recipient email addresses, not <code>null<code/> or empty.
     * @param subject The subject line to use, not <code>null<code/> or empty.
     * @param formData The form data to include in the body of the email, not <code>null</code>.
     * 
     * @throws PSEmailServiceNotInitializedException if the email service is not properly configured
     * @throws EmailException If there are any errors sending the email
     */
    public void emailFormData(String toList, String subject, IPSFormData formData)  throws PSEmailServiceNotInitializedException, EmailException;

    /**
     * Check if form name is a valid Form
     * @param form name
     * @return true if valid
     */
    public boolean isValidFormName(String formName);
}
