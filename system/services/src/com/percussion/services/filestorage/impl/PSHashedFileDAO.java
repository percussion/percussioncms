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
package com.percussion.services.filestorage.impl;

import com.percussion.cms.IPSConstants;
import com.percussion.services.filestorage.IPSHashedFileDAO;
import com.percussion.services.filestorage.data.PSBinary;
import com.percussion.services.filestorage.data.PSBinaryMetaEntry;
import com.percussion.services.filestorage.data.PSBinaryMetaKey;
import com.percussion.services.filestorage.data.PSHashedColumn;
import com.percussion.utils.jdbc.PSConnectionHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.query.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;


/**
 * @author stephenbolton
 *
 */
@Repository
@Scope("singleton")
@Transactional
public class PSHashedFileDAO implements IPSHashedFileDAO
{

   /**
    * Logger for this class
    */
   private static final Logger log = LogManager.getLogger(IPSConstants.CONTENTREPOSITORY_LOG);

   @PersistenceContext
   private EntityManager entityManager;

   private Session getSession(){
      return entityManager.unwrap(Session.class);
   }



   /**
    * 
    */
   public PSHashedFileDAO()
   {
   }

   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSHashedFileDAO#save(com.percussion.services.filestorage.data.PSBinary)
    */
   @Override

   public void save(PSBinary binary)
   {
      getSession().saveOrUpdate(binary);
      getSession().saveOrUpdate(binary.getData());
   }
  
   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSHashedFileDAO#findOrCreateMetaKey(java.lang.String, boolean)
    */
   public PSBinaryMetaKey findOrCreateMetaKey(String name, boolean enabled)
   {

      Query query = getSession().createQuery("select o " + "from PSBinaryMetaKey o " + "where o.name = :name")
            .setParameter("name", name);

      PSBinaryMetaKey obj = (PSBinaryMetaKey) query.uniqueResult();

      if (obj == null)
      {
         obj = new PSBinaryMetaKey(name, enabled);
         getSession().persist(obj);
      }
      return obj;

   }

   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSHashedFileDAO#exists(java.lang.String)
    */
   @Override
   public boolean exists(String hash)
   {
      if (hash != null && StringUtils.isNotBlank(hash))
      {

         Query query = getSession().createQuery(
               "select count(bf.hash) " + "from PSBinary bf " + "where bf.hash = :hash").setParameter("hash", hash);
         long count = (Long)query.uniqueResult();
         return (count>0);
      }
      return false;
   }


   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSHashedFileDAO#getBinary(java.lang.String)
    */
   @Override
   public PSBinary getBinary(String hash)
   {

      if (hash != null && StringUtils.isNotBlank(hash))
      {

         CriteriaBuilder builder = getSession().getCriteriaBuilder();
         CriteriaQuery<PSBinary> criteria = builder.createQuery(PSBinary.class);
         Root<PSBinary> critRoot = criteria.from(PSBinary.class);
         criteria.where(builder.equal(critRoot.get("hash"),hash));
         return entityManager.createQuery(criteria).getSingleResult();

      }
      else
         return null;

   }

   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSHashedFileDAO#countOlderThan(int)
    */
   @Override
   public long countOlderThan(int days)
   {
      long result = 0;
      Date testDate = DateUtils.addDays(DateUtils.truncate(new Date(), Calendar.DATE), -days);

      Query query = getSession().createQuery(
            "select count(*) " + "from PSBinary " + "where lastAccessedDate < :testDate").setDate("testDate", testDate);

      result = (Long) query.uniqueResult();

      return result;
   }
   
   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSHashedFileDAO#deleteOlderThan(int)
    */
   public long deleteOlderThan(int days)
   { 

      
      Date testDate = DateUtils.addDays(DateUtils.truncate(new Date(), Calendar.DATE), -days);

      int deleteddata =  getSession().createQuery(
            "delete from PSBinaryData data " + "where data.id in (select b.id from PSBinary b where b.lastAccessedDate < :testDate )")
            .setDate("testDate", testDate)
            .executeUpdate();
      getSession().flush();
      int deletedmetadata =  getSession().createQuery(
            "delete from PSBinaryMetaEntry meta " + "where meta.binary in (select b from PSBinary b where b.lastAccessedDate < :testDate )")
            .setDate("testDate", testDate)
            .executeUpdate();
      getSession().flush();
      int deletedEntities =  getSession().createQuery(
            "delete from PSBinary b where (b.lastAccessedDate < :testDate )")
            .setDate("testDate", testDate)
            .executeUpdate();
      
      log.debug("Delete updated {} entities",deletedEntities);

      return deletedEntities;
   }
   
   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSHashedFileDAO#delete(java.lang.String)
    */
   public void delete(String hash)
   {
      delete(getBinary(hash));
   }

   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSHashedFileDAO#delete(com.percussion.services.filestorage.data.PSBinary)
    */
   public void delete(PSBinary file)
   {
      if (file != null)
      {
         getSession().delete(file.getData());
         getSession().delete(file);
      }
   }

   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSHashedFileDAO#deleteMeta(com.percussion.services.filestorage.data.PSBinary)
    */
   @Override
   public void deleteMeta(PSBinary binary)
   {
      Session session = getSession();
      Criteria crit = session.createCriteria(PSBinaryMetaEntry.class);
      crit.add(Restrictions.eq("binary", binary));
      List<PSBinary> entries = (List<PSBinary>) crit.list();
      binary.getMetaEntries().clear();
      for (PSBinary entry : entries)
      {
         session.delete(entries);
      }
      session.flush();
   }

   
   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSHashedFileDAO#getAllHashes(java.util.Set)
    */
   @Override
   public List<String> getAllHashes(Set<PSHashedColumn> columns)
   {
      String sql = "";
      int count = 0;
      List<String> results = new ArrayList<>();
      // union will automatically remove duplicates.
      for (PSHashedColumn column : columns)
      {
         if (count++ > 0)
            sql += " union ";
         sql += "select " + column.getColumnName() + " from " + column.getTablename() + " where "
               + column.getColumnName() + " is not null";
      }
      if (columns.size()>0) {
         SQLQuery query = getSession().createSQLQuery(sql);
         results = (List<String>)query.list();
      }
      return results;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSHashedFileDAO#touch(java.util.List)
    */
   public void touch(List<String> hashes)
   {
      Date today = DateUtils.truncate(new Date(), Calendar.DATE);
      if (hashes != null && hashes.size() > 0)
      {

         Query query = getSession().createQuery("update PSBinary set lastAccessedDate = :date where hash in (:hashes)")
               .setParameterList("hashes", hashes).setParameter("date", today);
         int rowCount = query.executeUpdate();
         log.debug("Updated batch with " + rowCount + " updates");
         getSession().flush();
      }

   }

   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSHashedFileDAO#getMetaKeys()
    */
   @Override
   public List<PSBinaryMetaKey> getMetaKeys()
   {


      CriteriaBuilder builder = getSession().getCriteriaBuilder();
      CriteriaQuery<PSBinaryMetaKey> criteria = builder.createQuery(PSBinaryMetaKey.class);
      Root<PSBinaryMetaKey> critRoot = criteria.from(PSBinaryMetaKey.class);
      criteria.orderBy(builder.asc(critRoot.get("name")));
      return entityManager.createQuery(criteria).getResultList();

   }

   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSHashedFileDAO#getMetaKey(java.lang.String)
    */
   @Override
   public PSBinaryMetaKey getMetaKey(String keyname)
   {


      CriteriaBuilder builder = getSession().getCriteriaBuilder();
      CriteriaQuery<PSBinaryMetaKey> criteria = builder.createQuery(PSBinaryMetaKey.class);
      Root<PSBinaryMetaKey> critRoot = criteria.from(PSBinaryMetaKey.class);
      criteria.where(builder.equal(critRoot.get("name"),keyname));
      return entityManager.createQuery(criteria).getSingleResult();
   }

   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSHashedFileDAO#setReparseAllMeta()
    */
   @Override
   public void setReparseAllMeta()
   {

      Query query = getSession().createQuery("update PSBinary set reparseMeta=1");
      int rowCount = query.executeUpdate();
      log.debug("Marked " + rowCount + " items for reparse");

   }

   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSHashedFileDAO#findAllBinary(int, int)
    */
   public List<PSBinary> findAllBinary(int pageNum, int pageSize)
   {

      Criteria crit = getSession().createCriteria(PSBinary.class);
      crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
      int start = (pageSize * pageNum);
      crit.setFirstResult(start);
      crit.setMaxResults(pageSize);
      return (List<PSBinary>) crit.list();
   }

   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSHashedFileDAO#getReparseBatch(int)
    */
   @Override
   public List<PSBinary> getReparseBatch(int batchSize) {
      Criteria crit = getSession().createCriteria(PSBinary.class);
      crit.add(Restrictions.eq("reparseMeta", true));
      crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
      crit.setFirstResult(1);
      crit.setMaxResults(batchSize);
      return (List<PSBinary>) crit.list();
   }

   
   /**
    * @return if PSX_BINARYSTORE table exists
    */
   @Override
   public boolean hasLegacyTable()
   {
      Connection conn = null;
      ResultSet rs = null;
      try
      {
         conn = PSConnectionHelper.getDbConnection();
         String schema = PSConnectionHelper.getConnectionDetail().getOrigin();
         DatabaseMetaData md = conn.getMetaData();
         rs = md.getTables(null, schema, "PSX_BINARYSTORE", new String[]
         {"TABLE"});
         if (rs.next())
         {
            return true;
         }
      }
      catch (NamingException e)
      {
         log.error("Error checking for legacy table PSX_BINARYSTORE", e);
      }
      catch (SQLException e)
      {
         log.error("Error checking for legacy table PSX_BINARYSTORE", e);
      }
      finally
      {
         try
         {
            if (rs != null)
               rs.close();
         }
         catch (SQLException e)
         {
            // Ignore
         }
         try
         {
            if (conn != null)
               conn.close();
         }
         catch (SQLException e)
         {
            // Ignore
         }
      }
      return false;
   }

   public Blob createBlob(InputStream is, long l)
   {
      return Hibernate.getLobCreator(getSession()).createBlob(is, l);
   }

}
