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
package com.percussion.services.contentmgr.impl;

import antlr.ANTLRException;
import antlr.CharScanner;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.IPSBackEndMapping;
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSField;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.services.contentmgr.PSContentMgrConfig;
import com.percussion.services.contentmgr.data.PSContentTemplateDesc;
import com.percussion.services.contentmgr.data.PSContentTypeWorkflow;
import com.percussion.services.contentmgr.data.PSNodeDefinition;
import com.percussion.services.contentmgr.data.PSQuery;
import com.percussion.services.contentmgr.impl.query.SqlLexer;
import com.percussion.services.contentmgr.impl.query.SqlParser;
import com.percussion.services.contentmgr.impl.query.XpathLexer;
import com.percussion.services.contentmgr.impl.query.XpathParser;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import java.io.Reader;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of the content manager
 * 
 * @author dougrand
 */
@Transactional
public class PSContentMgr  implements IPSContentMgr
{

   private SessionFactory sessionFactory;

   public SessionFactory getSessionFactory() {
      return sessionFactory;
   }

   public void setSessionFactory(SessionFactory sessionFactory) {
      this.sessionFactory = sessionFactory;
   }

   /**
    * The logger for the content manager
    */
    private static final Logger ms_log = LogManager.getLogger(PSContentMgr.class);
   
   /**
    * The region that caches information for the content manager. Used for 
    * hibernate's query cache.
    */
   static final String CACHE_REGION = "contentmanagerqueries";

   /**
    * The underlying content repository that implements the persistence layer
    */
   private IPSContentRepository m_repository = null;

   public NodeType getNodeType(@SuppressWarnings("unused") String arg0)
   {
      throw new UnsupportedOperationException("Not implemented");
   }

   public NodeTypeIterator getAllNodeTypes()
   {
      throw new UnsupportedOperationException("Not implemented");
   }

   public NodeTypeIterator getPrimaryNodeTypes()
   {
      throw new UnsupportedOperationException("Not implemented");
   }

   public NodeTypeIterator getMixinNodeTypes()
   {
      throw new UnsupportedOperationException("Not implemented");
   }

   /**
    * Set the repositories
    * 
    * @param rep the repository, never <code>null</code>
    */
   public void setRepository(IPSContentRepository rep)
   {
      if (rep == null)
      {
         throw new IllegalArgumentException("rep may not be null");
      }
      m_repository = rep;
   }

   public List<Node> findItemsByPath(Session sess, List<String> paths,
         PSContentMgrConfig config) throws PathNotFoundException,
         RepositoryException
   {
      return m_repository.loadByPath(paths, config);
   }

   public List<Node> findItemsByGUID(List<IPSGuid> guids,
         PSContentMgrConfig config) throws RepositoryException
   {
      return m_repository.loadByGUID(guids, config);
   }

   public IPSNodeDefinition createNodeDefinition()
   {
      PSNodeDefinition nodeDef = new PSNodeDefinition();
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      nodeDef.setGUID(gmgr.createGuid(PSTypeEnum.NODEDEF));
      return nodeDef;
   }

   @SuppressWarnings("unchecked")
   public List<IPSNodeDefinition> loadNodeDefinitions(List<IPSGuid> typeids)
         throws RepositoryException
   {

      try
      {
         org.hibernate.Session session = sessionFactory.getCurrentSession();
         //  Force initialize at the moment to prevent problems with callers not in session
         List defs =typeids.stream().map(tid -> session.get(PSNodeDefinition.class,tid.longValue())).filter(Objects::nonNull).map(nd -> {Hibernate.initialize(nd);return nd;})
                 .collect(Collectors.toList());

         if (defs.size() == 0)
         {
            throw new NoSuchNodeTypeException("Specified defs not found");
         }
         else
         {
            return defs;
         }
      }
      catch (Exception e)
      {
         throw new RepositoryException("Problem loading definitions", e);
      }
   }

   public void saveNodeDefinitions(List<IPSNodeDefinition> defs)
         throws RepositoryException
   {
      try
      {
         org.hibernate.Session session = sessionFactory.getCurrentSession();
         defs.forEach(def -> session.saveOrUpdate(def));
      }
      catch (Exception e)
      {
         throw new RepositoryException("Problem saving definitions", e);
      }
   }

   public void deleteNodeDefinitions(List<IPSNodeDefinition> defs)
         throws RepositoryException
   {
      org.hibernate.Session s = sessionFactory.getCurrentSession();
      try
      {
         for (IPSNodeDefinition def : defs)
         {
            PSNodeDefinition realDef = (PSNodeDefinition) def;
            Set<PSContentTemplateDesc> descSet = realDef.getCvDescriptors();
            if (descSet != null)
            {
               for (PSContentTemplateDesc desc : descSet)
               {
                  s.delete(desc);
               }
            }
            // Remove the object
            s.delete(def);
         }
         s.flush();
      }
      catch (Exception e)
      {
         throw new RepositoryException("Problem deleting definitions", e);
      }

   }
   
   /* (non-Javadoc)
    * @see com.percussion.services.contentmgr.IPSContentTypeMgr#findNodeDefinitionByName(java.lang.String)
    */
   public IPSNodeDefinition findNodeDefinitionByName(String name) 
      throws RepositoryException
   {
      List<IPSNodeDefinition> defs = findNodeDefinitionsByName(name);
      
      if (defs.size() == 0)
      {
         throw new NoSuchNodeTypeException("Did not find " + name);
      }
      else if (defs.size() > 1)
      {
         throw new RepositoryException("Not a unique name - " + name);
      }
      else
      {
         return defs.get(0);
      }
   }

   @SuppressWarnings("unchecked")
   public List<IPSNodeDefinition> findNodeDefinitionsByName(String name)
         throws RepositoryException
   {
      org.hibernate.Session s = sessionFactory.getCurrentSession();
      try
      {
         name = PSContentUtils.internalizeName(name).toLowerCase();
         Criteria c = s.createCriteria(PSNodeDefinition.class);
         c.setCacheable(true);
         c.setCacheRegion(CACHE_REGION);
         c.add(Restrictions.ilike("m_name",name));
         List defs = c.list();
         if (defs.size() == 0)
         {
            // Try again with spaces in case the internal name had spaces
            name = name.replace('_', ' ');
            c = s.createCriteria(PSNodeDefinition.class);
            c.setCacheable(true);
            c.setCacheRegion(CACHE_REGION);
            c.add(Restrictions.ilike("m_name",name));
            defs = c.list();
         }
         
         // Make unique
         Set<IPSNodeDefinition> bdefs = new HashSet<>();
         bdefs.addAll(defs);
         
         // Convert back to a list
         defs = new ArrayList<IPSNodeDefinition>();
         defs.addAll(bdefs);

         return defs;
      }
      catch (Exception e)
      {
         throw new RepositoryException("Problem loading definitions", e);
      }

   }

   @SuppressWarnings("unchecked")
   public List<IPSNodeDefinition> findAllItemNodeDefinitions()
         throws RepositoryException
   {
      org.hibernate.Session s = sessionFactory.getCurrentSession();
      try
      {
         Criteria c = s.createCriteria(PSNodeDefinition.class);
         c.add(Restrictions.eq("m_objectType", 1));
         c.setCacheable(true);
         c.setCacheRegion(CACHE_REGION);
         
         List defs = c.list();
         //there may be an entry for every template association
         HashSet deduped = new HashSet<IPSNodeDefinition>(defs);
         return new ArrayList<>(deduped);
      }
      catch (Exception e)
      {
         throw new RepositoryException("Problem loading definitions", e);
      }

   }
   
   @SuppressWarnings("unchecked")
   public PSContentTemplateDesc findContentTypeTemplateAssociation(
         IPSGuid tmpId, IPSGuid ctId) throws RepositoryException
   {
      if (tmpId == null)
         throw new IllegalArgumentException("tmpId may not be null");

      if (ctId == null)
         throw new IllegalArgumentException("Content Type id may not be null");
     
      org.hibernate.Session s = sessionFactory.getCurrentSession();

         Criteria c = s.createCriteria(PSContentTemplateDesc.class);
         c.setCacheable(true);
         c.setCacheRegion(CACHE_REGION);
         c.add(Restrictions.eq("m_templateid", tmpId.longValue()));
         c.add(Restrictions.eq("m_contenttypeid", ctId.longValue()));
         return (PSContentTemplateDesc) c.uniqueResult();



   }

   @SuppressWarnings("unchecked")
   public List<PSContentTypeWorkflow> findContentTypeWorkflowAssociations(
         IPSGuid ctId) throws RepositoryException
   {
      if (ctId == null)
         throw new IllegalArgumentException("Content Type id may not be null");
     
      org.hibernate.Session s = sessionFactory.getCurrentSession();

         Criteria c = s.createCriteria(PSContentTypeWorkflow.class);
         c.setCacheable(true);
         c.setCacheRegion(CACHE_REGION);
         c.add(Restrictions.eq("m_contenttypeid", ctId.longValue()));
         List<PSContentTypeWorkflow> ctwfs = c.list();
         return ctwfs;


   }
   
   @SuppressWarnings("unchecked")
   public List<IPSNodeDefinition> findNodeDefinitionsByTemplate(
         IPSGuid templateid) throws RepositoryException
   {
      if (templateid == null)
      {
         throw new IllegalArgumentException("templateid may not be null");
      }
      
      org.hibernate.Session s = sessionFactory.getCurrentSession();
      

         Criteria c = s.createCriteria(PSNodeDefinition.class);
         c.setCacheable(true);
         c.setCacheRegion(CACHE_REGION);
         Criteria d = c.createCriteria("m_cvDescriptors", "descriptor");
         d.setCacheable(true);
         d.setCacheRegion(CACHE_REGION);
         d.add(Restrictions.eq("m_templateid", templateid.longValue()));
         List<IPSNodeDefinition> defs = c.list();
         //there may be an entry for every template association
         Set deduped = new HashSet<IPSNodeDefinition>(defs);
         return new ArrayList<>(deduped);

   }

   @SuppressWarnings("unchecked")
   public List<IPSNodeDefinition> findNodeDefinitionsByWorkflow(
         IPSGuid workflowid) throws RepositoryException
   {
      if (workflowid == null)
      {
         throw new IllegalArgumentException("workflowid may not be null");
      }
      
      org.hibernate.Session s = sessionFactory.getCurrentSession();
      

         Criteria c = s.createCriteria(PSNodeDefinition.class);
         c.setCacheable(true);
         c.setCacheRegion(CACHE_REGION);
         Criteria d = c.createCriteria("m_ctWfRels", "ctwfrel");
         d.setCacheable(true);
         d.setCacheRegion(CACHE_REGION);
         d.add(Restrictions.eq("m_workflowid", new Integer(workflowid
               .longValue() + "")));
         List<IPSNodeDefinition> defs = c.list();
         //there may be an entry for every workflow association
         Set deduped = new HashSet<IPSNodeDefinition>(defs);
         return new ArrayList<>(deduped);

   }
   
   public Collection<IPSGuid> findItemIdsByNodeDefinition(NodeDefinition def)
   {
      PSNodeDefinition psdef = (PSNodeDefinition) def;
      
      Collection<IPSGuid> guids = new ArrayList<>();
      String query = "select c.m_contentId, c.m_currRevision"
            + " from PSComponentSummary c where c.m_contentTypeId = :ctid";

      @SuppressWarnings("unchecked")
      List<Object[]> results = sessionFactory.getCurrentSession().createQuery(query).setParameter(
            "ctid", psdef.getRawContentType()).list();

      for (Object[] result : results)
      {
         guids.add(new PSLegacyGuid((Integer) result[0], (Integer) result[1]));
      }

      return guids;
   }

   public Query createQuery(String statement, String language)
         throws InvalidQueryException, RepositoryException
   {
      if (StringUtils.isBlank(statement))
      {
         throw new IllegalArgumentException(
               "statement may not be null or empty");
      }
      if (StringUtils.isBlank(language))
      {
         throw new IllegalArgumentException("language may not be null or empty");
      }
      Reader reader = new StringReader(statement);
      PSQuery q = null;
      CharScanner lexer = null;
      try
      {
         if (language.equals(Query.XPATH))
         {
            lexer = new XpathLexer(reader);
            XpathParser parser = new XpathParser(lexer);
            q = parser.start_rule();
         }
         else if (language.equals(Query.SQL))
         {
            lexer = new SqlLexer(reader);
            SqlParser parser = new SqlParser(lexer);
            q = parser.start_rule();
            
         }
         else
         {
            throw new InvalidQueryException("Language " + language
                  + " not recognized");
         }
         q.setStatement(statement);
      }
      catch (ANTLRException e)
      {
         String problem = "Encountered [" + e.getLocalizedMessage()
               + "] on line " + lexer.getLine() + " near character position "
               + lexer.getColumn() + " while parsing " + language + " query "
               + " original text: " + statement;
         throw new InvalidQueryException(problem);
      }

      return q;
   }

   /** (non-Javadoc)
    * @see javax.jcr.query.QueryManager#getQuery(javax.jcr.Node)
    */
   public Query getQuery(Node arg0) throws InvalidQueryException,
         RepositoryException
   {
      throw new UnsupportedRepositoryOperationException("Not yet implemented");
   }

   /** (non-Javadoc)
    * @see javax.jcr.query.QueryManager#getSupportedQueryLanguages()
    */
   public String[] getSupportedQueryLanguages() throws RepositoryException
   {
      return new String[]
      {Query.SQL, Query.XPATH};
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.contentmgr.IPSContentMgr#executeQuery(javax.jcr.query.Query,
    *      int, java.util.Map)
    */
   public QueryResult executeQuery(Query query, int maxresults,
         Map<String, ? extends Object> params) throws InvalidQueryException,
         RepositoryException
   {
      return executeQuery(query, maxresults, params, null);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.contentmgr.IPSContentMgr#executeQuery(javax.jcr.query.Query,
    *      int, java.util.Map, java.lang.String)
    */
   public QueryResult executeQuery(Query query, int maxresults,
         Map<String, ? extends Object> params, String locale)
         throws InvalidQueryException, RepositoryException
   {
      return m_repository.executeInternalQuery(query, maxresults, params,
            locale);
   }

   /* (non-Javadoc)
    * @see com.percussion.services.contentmgr.IPSContentMgr#filterItemsByNodeDefinitions(java.util.Set, java.util.Collection)
    */
   @SuppressWarnings("unchecked")
   public Collection<IPSGuid> filterItemsByNodeDefinitions(Set<IPSGuid> types,
         Collection<IPSGuid> ids)
   {
      if (types == null)
      {
         throw new IllegalArgumentException("types may not be null");
      }
      if (ids == null)
      {
         throw new IllegalArgumentException("ids may not be null");
      }
      // Build new collection
      Collection<IPSGuid> rval = new ArrayList<>();
      
      if (types.size() > 0 && ids.size() > 0)
      {
         String query = "select c.m_contentId, c.m_contentTypeId"
               + " from PSComponentSummary c where c.m_contentId in (:ids)";
   
         List<Integer> cids = new ArrayList<>();
         for (IPSGuid i : ids)
         {
            PSLegacyGuid lg = (PSLegacyGuid) i;
            cids.add(lg.getContentId());
         }
         List<Object[]> results = sessionFactory.getCurrentSession().createQuery(query).setParameter(
               "ids", cids).list();
         // Build a map
         Map<Integer,IPSGuid> idToType = new HashMap<>();
         for(Object[] result : results)
         {
            Integer cid = (Integer) result[0];
            idToType.put(cid, new PSGuid(PSTypeEnum.NODEDEF, (Long) result[1]));
         }
        
         for (IPSGuid i : ids)
         {
            PSLegacyGuid lg = (PSLegacyGuid) i;
            IPSGuid ctype = idToType.get(lg.getContentId());
            if (types.contains(ctype))
            {
               rval.add(i);
            }
         }
      }
      
      return rval;
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.contentmgr.IPSContentMgr#findNodesByTitle(
    *      java.long.Long, java.lang.String)
    */
   @SuppressWarnings("unchecked")
   public List<String> findNodesByTitle(Long contentTypeId, String title)
   throws RepositoryException
   {
      org.hibernate.Session s = sessionFactory.getCurrentSession();
      List<String> contentIds = new ArrayList<>();
      try
      {
         Criteria c = s.createCriteria(PSComponentSummary.class);
         c.add(Restrictions.eq("m_contentTypeId",  contentTypeId));
         c.add(Restrictions.ilike("m_name",title));
         c.setProjection(Projections.property("m_contentId"));
         List defs = c.list();
         for(Object def : defs)
         {
            contentIds.add(def + "");
         }
         return contentIds;
      }
      catch (Exception e)
      {
         throw new RepositoryException("Problem loading definitions", e);
      }

   }
   

   public Node copyItem(Node existing)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public Node createItem(NodeDefinition def)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public Node createItemRevision(Node existing)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public void deleteItems(List<IPSGuid> items) throws RepositoryException
   {
      // TODO Auto-generated method stub
      
   }

   public void saveItems(List<Node> items, PSContentMgrConfig config) throws RepositoryException
   {
      // TODO Auto-generated method stub
      
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.contentmgr.IPSContentMgr#findItemsByLocalFieldValue(
    *      java.long.Long, java.lang.String, java.lang.String)
    */
   @SuppressWarnings("unchecked")
   public List<Integer> findItemsByLocalFieldValue(long contentTypeId,
         String fieldName, String fieldValue)
   {
      List<Integer> contentIds = new ArrayList<>();
      PSItemDefManager itemDefMgr = PSItemDefManager.getInstance();
      long[] ctypeIds = new long[]{contentTypeId};
      Collection<PSField> fields = itemDefMgr.getFieldsByName(ctypeIds, fieldName);
      if(fields.isEmpty())
      {
         throw new RuntimeException("Invalid field");
      }
      PSField field = fields.iterator().next();
      IPSBackEndMapping loc = field.getLocator();
      if(!(loc instanceof PSBackEndColumn))
      {
         throw new RuntimeException("Invalid column");
      }
      PSBackEndColumn beColumn = (PSBackEndColumn)loc;
      String tableName = beColumn.getTable().getTable();
      String columnName = beColumn.getColumn();
      org.hibernate.Session sess = sessionFactory.getCurrentSession();


      String sql = null;
      try {
         sql = "SELECT DISTINCT c.CONTENTID FROM " +
                        PSSqlHelper.qualifyTableName("CONTENTSTATUS") + " c, " +
                        PSSqlHelper.qualifyTableName(tableName) +
                        " t WHERE c.CONTENTID=t.CONTENTID AND c.CURRENTREVISION=t.REVISIONID AND t." +
                        columnName + " = '" + fieldValue + "'";
      } catch (SQLException e) {
         throw new RuntimeException(e);
      }
      List<Object> result = sess.createSQLQuery(sql).list();
         
         for (Object row : result)
         {
            contentIds.add((Integer)row);
         }

      
      return contentIds;
   }

}
