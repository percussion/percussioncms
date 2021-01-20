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

package com.percussion.delivery.forms.impl;

import com.percussion.delivery.email.data.IPSEmailRequest;
import com.percussion.delivery.email.data.PSEmailRequest;
import com.percussion.delivery.forms.IPSFormDao;
import com.percussion.delivery.forms.IPSFormService;
import com.percussion.delivery.forms.data.IPSFormData;
import com.percussion.delivery.utils.IPSEmailHelper;
import com.percussion.delivery.utils.PSEmailServiceNotInitializedException;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.apache.commons.mail.EmailException;


public class PSFormService implements IPSFormService
{
    
	private IPSFormDao dao;
    private IPSEmailHelper emailHelper;
    private PSRecaptchaService recaptchaService;
	
    public PSRecaptchaService getRecaptchaService() {
		return recaptchaService;
	}


	public void setRecaptchaService(PSRecaptchaService recaptchaService) {
		this.recaptchaService = recaptchaService;
	}


	public PSFormService(IPSFormDao dao)
	{
		this.dao = dao;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see com.percussion.delivery.forms.IPSFormService#createFormData(java.lang.String, java.util.Map)
	 */
	public IPSFormData createFormData(String formname, Map<String, String[]> formdata)
    {
        return dao.createFormData(formname, formdata);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.delivery.forms.IPSFormService#save(com.percussion.delivery
     * .forms.data.IPSFormData)
     */
    public void save(IPSFormData form)
    {
        if (form==null)
            throw new IllegalArgumentException("Form is null");
        if (form.getName() == null || !isValidFormName(form.getName()))
            throw new IllegalArgumentException("Invalid Form");
        dao.save(form);
    }

	/*
	 * (non-Javadoc)
	 * @see com.percussion.delivery.forms.IPSFormService#delete(com.percussion.delivery.forms.data.IPSFormData)
	 */
    public void delete(IPSFormData form)
    {
        dao.delete(form);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.delivery.forms.IPSFormService#getExportedFormCount(java
     * .lang.String)
     */
    public long getExportedFormCount(String name)
    {
        return isValidFormName(name) || name == null ? dao.getExportedFormCount(name) : 0;

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.delivery.forms.IPSFormService#getTotalFormCount(java.lang
     * .String)
     */
    public long getTotalFormCount(String name)
    {
        return isValidFormName(name) || name == null ? dao.getTotalFormCount(name) : 0;
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.delivery.forms.IPSFormService#markAsExported(java.util.Collection)
     */
    public void markAsExported(Collection<IPSFormData> forms)
    {
       dao.markAsExported(forms);
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.delivery.forms.IPSFormService#deleteExportedForms(java.lang.String)
     */
    public void deleteExportedForms(String formName)
    {
        dao.deleteExportedForms(formName);
    }    

    /*
     * (non-Javadoc)
     * @see com.percussion.delivery.forms.IPSFormService#findFormsByName(java.lang.String)
     */
    public List<IPSFormData> findFormsByName(String name)
    {
        return dao.findFormsByName(name);
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.delivery.forms.IPSFormService#findAllForms()
     */
    public List<IPSFormData> findAllForms()
    {
        List<IPSFormData> forms = dao.findAllForms();
        Iterator<IPSFormData> formIt = forms.iterator();
        while (formIt.hasNext())
        {
            if (!isValidFormName(formIt.next().getName()))
                formIt.remove();
        }
        return forms;
    }
    
    /*
     * (non-Javadoc)
     * @see com.percussion.delivery.forms.IPSFormService#findDistinctFormNames()
     */
    public List<String> findDistinctFormNames()
    {
        List<String> formNames = dao.findDistinctFormNames();
        Iterator<String> formIt = formNames.iterator();
        while (formIt.hasNext())
        {
            if (!isValidFormName(formIt.next()))
                formIt.remove();
        }
        return formNames;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.delivery.forms.IPSFormService#isValidFormName(java.lang
     * .String)
     */
    public boolean isValidFormName(String formName)
    {

        if (formName != null)
        {
            String pattern = "^[a-zA-Z0-9_\\-]*$";
            if (formName.matches(pattern))
            {
                return true;
            }
        }
        return false;
    }

    public void emailFormData(String toList, String subject, IPSFormData formData) throws PSEmailServiceNotInitializedException, EmailException
    {
        Validate.notEmpty(toList);
        Validate.notEmpty(subject);
        Validate.notNull(formData);
        
        boolean isEmailForm = false;
        
        if (emailHelper == null)
            throw new PSEmailServiceNotInitializedException();
        
        if (formData.getFields().containsKey("emailForm"))
        	isEmailForm = true;
        
        PSFormDataJoiner joiner = new PSFormDataJoiner();
        String body = joiner.generateEmailBody(formData);
        
        IPSEmailRequest emailRequest = new PSEmailRequest();
        emailRequest.setToList(toList);
        emailRequest.setSubject(subject);
        Map<String, String> temp = formData.getFields();
        if(temp.containsKey("email-cc") && isEmailForm){
            emailRequest.setCCList(temp.get("email-cc"));
        }
        if(temp.containsKey("email-bcc") && isEmailForm){
        	emailRequest.setBCCList(temp.get("email-bcc"));
        }
        if(temp.containsKey("email-from") && temp.containsKey("email-body") && isEmailForm){
        	emailRequest.setBody("From: " + temp.get("email-from") + "\r\nEmail Body: " + temp.get("email-body"));
        } else if(temp.containsKey("email-body") && isEmailForm){
        	emailRequest.setBody("Email Body: " + temp.get("email-body"));
        } else {
        	emailRequest.setBody(body);
        }
        emailHelper.sendMail(emailRequest);
    }


    public void setEmailHelper(IPSEmailHelper emailHelper)
    {
        this.emailHelper = emailHelper;
    }

}
