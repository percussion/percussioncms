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

package com.percussion.delivery.forms.impl.rdbms;

import com.percussion.delivery.forms.IPSFormDao;
import com.percussion.delivery.forms.data.IPSFormData;
import com.percussion.delivery.forms.data.PSFormData;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
public class PSFormDao extends HibernateDaoSupport implements IPSFormDao
{


    /*
     * (non-Javadoc)
     * @see com.percussion.delivery.forms.IPSFormDao#createFormData(java.lang.String, java.util.Map)
     */
    public IPSFormData createFormData(String formname, Map<String, String[]> formdata)
    {
        if(StringUtils.isBlank(formname))
            throw new IllegalArgumentException("formname cannnot be blank.");
        if(formdata == null)
            throw new IllegalArgumentException("formdata cannot be null");
        return new PSFormData(formname, formdata);
    }

    /* (non-Javadoc)
     * @see com.percussion.delivery.forms.impl.rdbms.IPSFormDao#save(com.percussion.delivery.forms.data.IPSFormData)
     */
    public void save(IPSFormData form)
    {
        if (form == null)
        {
            throw new IllegalArgumentException("clist may not be null");
        }

        getHibernateTemplate().saveOrUpdate(form);
    }

    /* (non-Javadoc)
     * @see com.percussion.delivery.forms.impl.rdbms.IPSFormDao#delete(com.percussion.delivery.forms.data.IPSFormData)
     */
    public void delete(IPSFormData form)
    {
        getHibernateTemplate().delete(form);
    }

    /* (non-Javadoc)
     * @see com.percussion.delivery.forms.impl.rdbms.IPSFormDao#getExportedFormCount(java.lang.String)
     */
    public long getExportedFormCount(String name)
    {
        String query = "select count(*) from PSFormData formData where formData.isExported = 'y'";
        if (name != null && name.trim().length() > 0)
            query += " and lower(formData.name) = lower('" + name + "')";
        return ((Long) getHibernateTemplate().find(query).iterator().next())
                .intValue();
    }

    /* (non-Javadoc)
     * @see com.percussion.delivery.forms.impl.rdbms.IPSFormDao#getTotalFormCount(java.lang.String)
     */
    public long getTotalFormCount(String name)
    {
        String query = "select count(*) from PSFormData formData";
        if (name != null && name.trim().length() > 0)
            query += " where lower(formData.name) = lower('" + name + "')";
        return ((Long) getHibernateTemplate().find(query).iterator().next())
                .intValue();
    }

    // rather than saving all the forms, we just change the exported property
    /* (non-Javadoc)
     * @see com.percussion.delivery.forms.impl.rdbms.IPSFormDao#markAsExported(java.util.Collection)
     */
    public void markAsExported(Collection<IPSFormData> forms)
    {
        Session session = getSession();
        try
        {
            // because of limitations in JDBC/hibernate, we have to keep IN
            // clauses less than 1k elements
            String query = "update PSFormData set isExported = 'y' where id in (:ids)";
            Collection<Long> values = new ArrayList<Long>();
            for (IPSFormData form : forms)
            {
                values.add(Long.valueOf(form.getId()));
                if (values.size() > 950 || values.size() == forms.size())
                {
                    session.createQuery(query).setParameterList("ids", values)
                            .executeUpdate();
                    session.flush();
                    values.clear();
                }
            }
        }
        finally
        {
           // releaseSession(session);
        }
    }

    /* (non-Javadoc)
     * @see com.percussion.delivery.forms.impl.rdbms.IPSFormDao#deleteExportedForms(java.lang.String)
     */
    public void deleteExportedForms(String formName)
    {
        List<IPSFormData> forms = findExportedForms(formName);
        if (!forms.isEmpty())
            getHibernateTemplate().deleteAll(forms);

    }

    @SuppressWarnings("unchecked")
    private List<IPSFormData> findExportedForms(String formName)
    {
        String sqlString = "";
        if (StringUtils.isEmpty(formName))
        {
            sqlString = "from PSFormData where isExported = 'y' order by name asc, created asc";
        }
        else
        {
            sqlString = "from PSFormData formData where formData.isExported = 'y' and lower(formData.name) = "
                    + "lower('" + formName + "')";
        }
        return (List<IPSFormData>) getHibernateTemplate().find(sqlString);
    }

    /* (non-Javadoc)
     * @see com.percussion.delivery.forms.impl.rdbms.IPSFormDao#findFormsByName(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public List<IPSFormData> findFormsByName(String name)
    {
        Validate.notNull(name);

        return (List<IPSFormData>) getHibernateTemplate().findByNamedParam(
                "from PSFormData formData " +
                        "where lower(formData.name) = lower(:name) " +
                        "order by created asc ",
                "name", name);
    }

    /* (non-Javadoc)
     * @see com.percussion.delivery.forms.impl.rdbms.IPSFormDao#findAllForms()
     */
    @SuppressWarnings("unchecked")
    public List<IPSFormData> findAllForms()
    {
        return (List<IPSFormData>) getHibernateTemplate().find(
                "from PSFormData order by name asc, created asc");
    }

    /* (non-Javadoc)
     * @see com.percussion.delivery.forms.impl.rdbms.IPSFormDao#findDistinctFormNames()
     */
    @SuppressWarnings("unchecked")
    public List<String> findDistinctFormNames()
    {
        List<String> lowerNames = new ArrayList<String>();
        List<String> distinctNames = (List<String>) getHibernateTemplate().find(
                "select distinct name from PSFormData order by name asc");
        Iterator<String> iter = distinctNames.iterator();
        while (iter.hasNext())
        {
            String name = iter.next().toLowerCase();
            if (!lowerNames.contains(name))
            {
                lowerNames.add(name);
            }
            else
            {
                iter.remove();
            }
        }

        return distinctNames;
    }

    private Session getSession(){

        return getSessionFactory().getCurrentSession();

    }


}
