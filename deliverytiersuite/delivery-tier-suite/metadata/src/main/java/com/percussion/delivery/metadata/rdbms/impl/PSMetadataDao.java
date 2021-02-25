/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

package com.percussion.delivery.metadata.rdbms.impl;

import com.google.common.collect.Lists;
import com.percussion.delivery.metadata.IPSMetadataDao;
import com.percussion.delivery.metadata.IPSMetadataEntry;
import com.percussion.delivery.metadata.IPSMetadataProperty;
import com.percussion.delivery.metadata.utils.PSHashCalculator;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressFBWarnings("UNSAFE_HASH_EQUALS")
@Repository
@Scope("singleton")
public class PSMetadataDao implements IPSMetadataDao
{

    private SessionFactory sessionFactory;

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Logger for this class.
     */
    private static final Log log = LogFactory.getLog(PSMetadataDao.class);


    private static PSHashCalculator hashCalculator = new PSHashCalculator();

    private final Pattern patternToGetDirectoryFromPagepath = Pattern.compile("(.+)/[^/]+");



    public void delete(Collection<String> pagepaths)
    {
        Validate.notNull(pagepaths, "pagepaths cannot be null.");

        Collection<String> pagepathHashes = getPagepathHashes(pagepaths);


        Transaction tx = null;
        try (Session session = getSession()){
            String hql = "delete from PSDbMetadataEntry  where pagepathHash in (:paths)";
            tx = session.beginTransaction();
            Query q = session.createQuery(hql);
            q.setParameterList("paths",pagepathHashes);
            q.executeUpdate();
            tx.commit();
        }catch(Exception e){
            if(tx != null && tx.isActive()) {
                tx.rollback();
            }
            log.error(e.getMessage(),e);
        }
    }

    public boolean delete(String pagepath)
    {
        Validate.notEmpty(pagepath, "pagepath cannot be null or empty.");

        PSDbMetadataEntry entry = (PSDbMetadataEntry)findEntry(pagepath);


        if (entry != null)
        {
            Transaction tx = null;
            try(Session session = getSession()) {
                tx = session.beginTransaction();
                session.delete(entry);
                tx.commit();
                return true;
            }catch(Exception e){
                if(tx !=null && tx.isActive()){
                    tx.rollback();
                }
                log.error(e.getMessage(),e);
            }
        }
        return false;

    }

    @Override
    public void deleteBySite(String prevSiteName, String newSiteName) {
        Validate.notEmpty(prevSiteName, "prevSiteName cannot be null or empty.");
        Validate.notEmpty(newSiteName, "newSiteName cannot be null or empty.");

        log.debug("Removing entries for site: " + prevSiteName);


        Transaction tx = null;
        try (Session session = getSession()) {
            tx = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaDelete<PSDbMetadataEntry> deleteQuery = builder.createCriteriaDelete(PSDbMetadataEntry.class);
            Root<PSDbMetadataEntry> root = deleteQuery.from(PSDbMetadataEntry.class);
            deleteQuery.where(builder.like(root.get("site"), prevSiteName));
            session.createQuery(deleteQuery).executeUpdate();
            tx.commit();
        }catch(Exception e){
            if(tx !=null && tx.isActive()){
                tx.rollback();
            }
            log.error(e.getMessage(),e);
        }
    }


    public void deleteAllMetadataEntries()
    {

        Transaction tx = null;
        try(Session session = getSession()) {

            tx=session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaDelete<PSDbMetadataProperty> deleteQuery = builder.createCriteriaDelete(PSDbMetadataProperty.class);
            deleteQuery.from(PSDbMetadataProperty.class);
            session.createQuery(deleteQuery).executeUpdate();

            CriteriaBuilder builder2 = session.getCriteriaBuilder();
            CriteriaDelete<PSDbMetadataEntry> deleteQuery2 = builder2.createCriteriaDelete(PSDbMetadataEntry.class);
            deleteQuery2.from(PSDbMetadataEntry.class);
            session.createQuery(deleteQuery2).executeUpdate();
            tx.commit();

        }catch(Exception e){
            if(tx !=null && tx.isActive()){
                tx.rollback();
            }
            log.error(e.getMessage(),e);
        }
    }

   public IPSMetadataEntry findEntry(String pagepath)
    {
        Validate.notEmpty(pagepath, "pagepath cannot be null nor empty");

        try (Session session = getSession()){
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<PSDbMetadataEntry> criteriaQuery = criteriaBuilder.createQuery(PSDbMetadataEntry.class);
            Root<PSDbMetadataEntry> root = criteriaQuery.from(PSDbMetadataEntry.class);
            criteriaQuery.select(root).where(criteriaBuilder.like(root.get("pagepath"), pagepath));
            List<PSDbMetadataEntry> resultList = session.createQuery(criteriaQuery).getResultList();
            if (!resultList.isEmpty())
                return (IPSMetadataEntry) resultList.get(0);
            else
                return null;
        }

    }

   public List<IPSMetadataEntry> getAllEntries()
    {
        try( Session session = getSession()) {
            session.setHibernateFlushMode(FlushMode.MANUAL);
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<PSDbMetadataEntry> criteriaQuery = criteriaBuilder.createQuery(PSDbMetadataEntry.class);
            Root<PSDbMetadataEntry> root = criteriaQuery.from(PSDbMetadataEntry.class);
            List<PSDbMetadataEntry> result = session.createQuery(criteriaQuery).getResultList();
            List<IPSMetadataEntry> entries = new ArrayList<>();
            if (result != null) {
                for (PSDbMetadataEntry e : result)
                    entries.add(e);
            }

            return entries;
        }

    }

   public void save(Collection<IPSMetadataEntry> entries)
    {
        Validate.notNull(entries, "entries cannot be null");

        if (entries.isEmpty())
            return;


        Transaction tx = null;
        try(Session session = getSession()) {
            Collection<PSDbMetadataEntry> dbEntries = convertRestEntriesToDb(entries);
            session.setHibernateFlushMode(FlushMode.ALWAYS);
            // Save entries
            int i = 0;
            for (PSDbMetadataEntry entry : dbEntries) {
                tx  = session.beginTransaction();
                try {
                    session.saveOrUpdate(entry);
                    tx.commit();
                } catch (org.hibernate.NonUniqueObjectException e) {
                    session.merge(entry);
                    tx.commit();
                }
            }
        } catch(Exception e){
            if(tx !=null && tx.isActive()){
                tx.rollback();
            }
            log.error(e.getMessage(),e);
        }

    }

    public void save(IPSMetadataEntry entry)
    {
        Validate.notNull(entry, "entry cannot be null");

        save(Lists.newArrayList(entry));

    }


    public boolean hasDirtyEntries(Collection<IPSMetadataEntry> entries)
    {
        PSDbMetadataEntry existing;

        for (PSDbMetadataEntry entry : this.convertRestEntriesToDb(entries))
        {
            existing = (PSDbMetadataEntry)findEntry(entry.getPagepath());
            if (existing != null)
            {
                if (isEntryDirty(existing, entry))
                    return true;
            }
        }

        return false;
    }


    public Set<String> getAllIndexedDirectories()
    {


        try ( Session session = getSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<String> criteriaQuery = criteriaBuilder.createQuery(String.class);
            Root<PSDbMetadataEntry> root = criteriaQuery.from(PSDbMetadataEntry.class);
            criteriaQuery.select(root.get("pagepath"));
            List<String> resultList = session.createQuery(criteriaQuery).getResultList();

            // TODO Instead of using a Pattern and regular expressions here, we may
            // use
            // the Derby built-in regular expression functionality if this is too
            // slow.
            Matcher matcher;
            Set<String> indexedDirectories = new HashSet<>();

            for (String dataEntry : resultList) {
                matcher = patternToGetDirectoryFromPagepath.matcher(dataEntry);
                matcher.find();
                indexedDirectories.add(matcher.group(1));
            }

            return indexedDirectories;
        }
    }

   // @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_UNCOMMITTED)
    public List<String> getAllSites()
    {
        List<String> msgFromList = null;
        try(Session session=getSession()) {

            Criteria criteria = session.createCriteria(PSDbMetadataEntry.class);
            criteria.setProjection(Projections.distinct(Projections.property("site")));
            msgFromList = criteria.list();
        }
        return msgFromList;
    }

    private Collection<String> getPagepathHashes(Collection<String> pagepaths)
    {
        List<String> pagepathHashes = new ArrayList<>();

        for (String pp : pagepaths)
        {
            pagepathHashes.add(hashCalculator.calculateHash(pp));
        }

        return pagepathHashes;
    }



    /**
     * Determine if the entries are "dirty", in other words the field
     * values differ.
     * @param existing assumed not <code>null</code>.
     * @param entry assumed not <code>null</code>.
     * @return <code>true</code> if dirty.
     */
    private boolean isEntryDirty(PSDbMetadataEntry existing, PSDbMetadataEntry entry)
    {
        if(!existing.equals(entry))
            return true;
        int count1 = existing.getPropertyCount();
        int count2 = entry.getPropertyCount();
        if((count1 != count2) || (count1 + count2 == 0))
            return true;
        if(existing.getPropertyCount() + entry.getPropertyCount() == 0)
            return false;
        Set<IPSMetadataProperty> props1 = existing.getProperties();
        Set<IPSMetadataProperty> props2 = entry.getProperties();

        Map<Object, IPSMetadataProperty> propMap = new HashMap<>();
        for(IPSMetadataProperty p : props2)
            propMap.put(((PSDbMetadataProperty)p).getId(), p);
        for(IPSMetadataProperty prop : props1)
        {

            String hash1 = ((PSDbMetadataProperty)prop).getHash();
            IPSMetadataProperty p2 = propMap.get(((PSDbMetadataProperty)prop).getId());
            if(p2 == null)
                return true;
            String hash2 = ((PSDbMetadataProperty)p2).getHash();
            if(!hash1.equals(hash2))
                return true;

        }
        return false;
    }


    /**
     * Converts a database-agnostic collection of metadata entries to Hibernate
     * specific ones.
     *
     * @param entries A list of database-agnostic metadata entry objects. Cannot
     * be <code>null</code>, may be empty.
     * @return A list of database specific metadata entry objects. Never <code>null</code>,
     * may be empty.
     */
    private Collection<PSDbMetadataEntry> convertRestEntriesToDb(Collection<IPSMetadataEntry> entries)
    {
        Validate.notNull(entries, "list of metadata entries cannot be null");
        Collection<PSDbMetadataEntry> result = new ArrayList<>();

        for (IPSMetadataEntry metadataEntry : entries)
        {
            IPSMetadataEntry dbMetadataEntry = null;
            if(metadataEntry.getPagepath() == null){
                dbMetadataEntry = new PSDbMetadataEntry();
            }else {
                dbMetadataEntry = findEntry(metadataEntry.getPagepath());
            }
            if(dbMetadataEntry == null){
                dbMetadataEntry = new PSDbMetadataEntry();
            }else {
                dbMetadataEntry.clearProperties();
            }

            if (!(metadataEntry instanceof PSDbMetadataEntry))
            {

                dbMetadataEntry.setFolder(metadataEntry.getFolder());
                dbMetadataEntry.setLinktext(metadataEntry.getLinktext());
                dbMetadataEntry.setName(metadataEntry.getName());
                dbMetadataEntry.setPagepath(metadataEntry.getPagepath());
                dbMetadataEntry.setSite(metadataEntry.getSite());
                dbMetadataEntry.setType(metadataEntry.getType());
                PSDbMetadataProperty prop = null;
                for (IPSMetadataProperty metadataProperty : metadataEntry.getProperties())
                {
                    if (metadataProperty instanceof PSDbMetadataProperty)
                    {
                        prop = (PSDbMetadataProperty) metadataProperty;
                    }
                    else
                    {
                        prop = new PSDbMetadataProperty(metadataProperty.getName(), metadataProperty.getValuetype(),
                                metadataProperty.getValue());
                    }
                    dbMetadataEntry.addProperty(prop);
                }
            }
            else
            {
                dbMetadataEntry = (PSDbMetadataEntry)metadataEntry;
            }

          result.add((PSDbMetadataEntry)dbMetadataEntry);
        }

        return result;
    }

    @Override
    public int updateByCategoryProperty(String oldCategoryName, String newCategoryName) {

        int updatedRows = 0;

        if(oldCategoryName == null || newCategoryName == null)
            throw new IllegalArgumentException("Old and New Category Names are required");

        ;
        Transaction tx = null;
        try (Session session = getSession()) {
            tx = session.beginTransaction();
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();

            CriteriaUpdate<PSDbMetadataProperty> criteriaUpdate = criteriaBuilder.createCriteriaUpdate(PSDbMetadataProperty.class);
            Root<PSDbMetadataProperty> employeeRoot = criteriaUpdate.from(PSDbMetadataProperty.class);
            criteriaUpdate.set(employeeRoot.get("stringvalue"), newCategoryName).where(
                    criteriaBuilder.and(criteriaBuilder.equal(employeeRoot.get("stringvalue"), oldCategoryName),
                            criteriaBuilder.equal(employeeRoot.get("name"), "perc:category")));
            updatedRows = session.createQuery(criteriaUpdate).executeUpdate();
            tx.commit();
        }catch(Exception e){
            if(tx !=null && tx.isActive()){
                tx.rollback();
            }
            log.error(e.getMessage(),e);
        }
        return updatedRows;
    }

    private Session getSession(){

        return sessionFactory.openSession();

    }
}
