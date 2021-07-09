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
package com.percussion.delivery.forms.impl;

import com.percussion.delivery.email.data.IPSEmailRequest;
import com.percussion.delivery.forms.IPSFormDao;
import com.percussion.delivery.forms.IPSFormService;
import com.percussion.delivery.forms.data.IPSFormData;
import com.percussion.delivery.forms.data.PSFormData;
import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author JaySeletz
 *
 */

public class PSBaseFormServiceTest
{

    public static final String FIELD_VALUES_SEPARATOR = "|";
    public static final String FIELD_VALUES_SEPARATOR_ESCAPE = "\\\\";

    public static final String INVALID_FORM_NAME_PREFIX = "invalidFormName_";
    public static final String INVALID_TEST_CHARS = "<>&*' ";

    @Autowired
    protected SessionFactory sessionFactory;

    @Autowired
    protected IPSFormService formService;

    @Autowired
    protected IPSFormDao dao;

    Lock sequential = new ReentrantLock();

    @Before
    public void setUp() throws Exception {
        sequential.lock();
    }


    protected List<IPSFormData> getAllForms()
    {
        return formService.findAllForms();
    }


    protected void addInvalidFormToDb()
    {
        // add an invalid form should be filtered out
        Map<String, String[]> data = new HashMap<String, String[]>();
        data.put("field1", new String[]
                {"value1"});
        for (int i = 0; i < INVALID_TEST_CHARS.length(); i++)
        {
            IPSFormData formdata = dao.createFormData(INVALID_FORM_NAME_PREFIX + INVALID_TEST_CHARS.charAt(i), data);
            dao.save(formdata);
        }
    }

    protected void removeInvalidFormsFromDb()
    {
        List<IPSFormData> allForms = dao.findAllForms();
        for (IPSFormData form : allForms)
        {
            if (form.getName().startsWith(INVALID_FORM_NAME_PREFIX))
            {
                dao.delete(form);
            }
        }
    }


    @After
   // @Transactional
    public void tearDown()
    {
//        Session session = getSession();
//        session.createSQLQuery("delete from PERC_FORM_FIELDS").executeUpdate();
//        CriteriaBuilder builder = session.getCriteriaBuilder();
//        CriteriaDelete<PSFormData> deleteQuery = builder.createCriteriaDelete(PSFormData.class);
//        Root<PSFormData> root = deleteQuery.from(PSFormData.class);
//        session.createQuery(deleteQuery).executeUpdate();



       //session.createQuery("delete from PERC_FORMS").executeUpdate();

        //session.close();

    }

    private Session getSession(){

        return sessionFactory.getCurrentSession();

    }

    protected PSFormData generateFormData(String formName, String... formProperties)
    {
        Map<String, String[]> props = new HashMap<String, String[]>();

        for (int i=0; i<formProperties.length; i += 2)
        {
            props.put(formProperties[i], formProperties[i+1].split(FIELD_VALUES_SEPARATOR_ESCAPE + FIELD_VALUES_SEPARATOR));
        }

        return new PSFormData(formName, props);
    }

    protected void validateEmailSent(String[] fieldValue1, String[] fieldValue2, String toList, String subject)
    {
        List<IPSEmailRequest> emails = PSMockEmailHelper.getEmailRequests();
        assertNotNull(emails);
        assertEquals(1, emails.size());
        IPSEmailRequest req = emails.get(0);
        assertNotNull(req);
        assertEquals(toList, req.getToList());
        assertEquals(subject, req.getSubject());
        String body = req.getBody();
        assertTrue(StringUtils.contains(body, fieldValue1[0] + ": " + fieldValue1[1] + "\r\n" + fieldValue2[0] + ": " + fieldValue2[1] + "\r\n"));
    }

}
