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
package com.percussion.services.filestorage.impl;

import com.percussion.services.filestorage.IPSHashedFileDAO;
import com.percussion.services.filestorage.data.*;
import com.percussion.utils.jdbc.PSConnectionHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.*;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.NamingException;
import java.io.InputStream;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * @author stephenbolton
 *
 */
@Transactional
public class PSHashedFileDAO implements IPSHashedFileDAO
{

   /**
    * Logger for this class
    */
   private static final Log log = LogFactory.getLog(PSHashedFileDAO.class);

   /**
    * The hibernate session factory injected by spring
    */
   private SessionFactory sessionFactory;

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
         Criteria crit = getSession().createCriteria(PSBinary.class);
         crit.add(Restrictions.eq("hash", hash));
         return (PSBinary) crit.uniqueResult();
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
            "delete from PSBinary b where b.lastAccessedDate < :testDate )")
            .setDate("testDate", testDate)
            .executeUpdate();
      
      log.debug("Delete updated " + deletedEntities + " entities");

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
      List<String> results = new ArrayList<String>();
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
      Criteria crit = getSession().createCriteria(PSBinaryMetaKey.class);
      crit.addOrder(Order.asc("name"));
      return (List<PSBinaryMetaKey>) crit.list();
   }

   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSHashedFileDAO#getMetaKey(java.lang.String)
    */
   @Override
   public PSBinaryMetaKey getMetaKey(String keyname)
   {
      Criteria crit = getSession().createCriteria(PSBinaryMetaKey.class);
      crit.add(Restrictions.eq("name", keyname));
      return (PSBinaryMetaKey) crit.uniqueResult();
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
   /**
    * The hibernate session factory injected by spring
    * @param sessionFactory
    */
   public void setSessionFactory(SessionFactory sessionFactory)
   {
      this.sessionFactory = sessionFactory;
   }

   /**
    * @return The hibernate session factory injected by spring
    */
   public Session getSession()
   {
      return sessionFactory.getCurrentSession();
   }
}
