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
package com.percussion.services.contentmgr.data;

import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSContentPropertyConstants;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.contentmgr.impl.PSContentUtils;
import com.percussion.services.contentmgr.impl.legacy.PSTypeConfiguration;
import com.percussion.services.contentmgr.impl.query.nodes.IPSQueryNode;
import com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeIdentifier;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.types.PSPair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.version.VersionException;

import org.apache.commons.lang.StringUtils;

/**
 * Represents a single JSR-170 query. This class contains processing code to
 * handle forming parts of the HQL statement for searching, namely the sort and
 * projection clauses.
 * 
 * @author dougrand
 */
public class PSQuery implements Query
{
   /**
    * Sort order enum for use in setting the order of returned rows by 
    * projection element
    */
   public enum SortOrder {
      /**
       * Sort the results in an ascending sense for the given property
       */
      ASC, 
      /**
       * Sort the results in an decending sense for the given property
       */
      DSC
   }

   /**
    * Source statement for query
    */
   private String m_statement;

   /**
    * The query language used
    */
   private String m_language;

   /**
    * Result fields to return from the query. The query may take the specified
    * field "*" which indicates that it should return nodes
    */
   private List<PSQueryNodeIdentifier> m_projection = new ArrayList<>();

   /**
    * The content types that are filtered for in the query
    */
   private List<PSQueryNodeIdentifier> m_typeConstraints = new ArrayList<>();

   /**
    * The fields that should be used to order the results, never <code>null</code>
    * after construction.
    */
   private List<PSPair<PSQueryNodeIdentifier, SortOrder>> m_sortFields = new ArrayList<>();

   /**
    * The initial tree of query nodes used to construct the actual database
    * query. If <code>null</code> then there is no filtering on the results.
    */
   private IPSQueryNode m_where = null;

   /**
    * Ctor, called by the parser
    * 
    * @param language the language that this query is derived from, never
    *           <code>null</code> or empty
    */
   public PSQuery(String language) {
      if (StringUtils.isBlank(language))
      {
         throw new IllegalArgumentException("language may not be null or empty"); //$NON-NLS-1$
      }
      m_language = language;
   }

   /** (non-Javadoc)
    * @see javax.jcr.query.Query#execute()
    */
   public QueryResult execute() throws RepositoryException
   {
      IPSContentMgr cm = PSContentMgrLocator.getContentMgr();
      return cm.executeQuery(this, -1, null);
   }

   /** (non-Javadoc)
    * @see javax.jcr.query.Query#getStatement()
    */
   public String getStatement()
   {
      return m_statement;
   }

   /** (non-Javadoc)
    * @see javax.jcr.query.Query#getLanguage()
    */
   public String getLanguage()
   {
      return m_language;
   }

   /**
    * @return Returns the projection.
    */
   public List<PSQueryNodeIdentifier> getProjection()
   {
      return m_projection;
   }

   /**
    * @param projection The projection to set.
    */
   public void setProjection(List<PSQueryNodeIdentifier> projection)
   {
      m_projection = projection;
   }

   /**
    * @return Returns the sortFields.
    */
   public List<PSPair<PSQueryNodeIdentifier, SortOrder>> getSortFields()
   {
      return m_sortFields;
   }

   /**
    * @return Returns the typeConstraints.
    */
   public List<PSQueryNodeIdentifier> getTypeConstraints()
   {
      return m_typeConstraints;
   }

   /**
    * @param typeConstraints The typeConstraints to set.
    */
   public void setTypeConstraints(List<PSQueryNodeIdentifier> typeConstraints)
   {
      m_typeConstraints = typeConstraints;
   }

   /**
    * @param language The language to set.
    */
   public void setLanguage(String language)
   {
      m_language = language;
   }

   /**
    * @param statement The statement to set.
    */
   public void setStatement(String statement)
   {
      m_statement = statement;
   }

   /**
    * @param sortFields The sortFields to set.
    */
   public void setSortFields(
         List<PSPair<PSQueryNodeIdentifier, SortOrder>> sortFields)
   {
      if (sortFields == null)
         m_sortFields = new ArrayList<>();
      else
         m_sortFields = sortFields;
   }

   /**
    * @return Returns the where.
    */
   public IPSQueryNode getWhere()
   {
      return m_where;
   }

   /**
    * @param where The where to set.
    */
   public void setWhere(IPSQueryNode where)
   {
      m_where = where;
   }

   /** (non-Javadoc)
    * @see javax.jcr.query.Query#getStoredQueryPath()
    */
   @SuppressWarnings("unused") //$NON-NLS-1$
   public String getStoredQueryPath() throws ItemNotFoundException,
         RepositoryException
   {
      throw new UnsupportedRepositoryOperationException("Not yet implemented"); //$NON-NLS-1$
   }

   /** (non-Javadoc)
    * @see javax.jcr.query.Query#storeAsNode(java.lang.String)
    */
   @SuppressWarnings("unused") //$NON-NLS-1$
   public Node storeAsNode(String arg0) throws ItemExistsException,
         PathNotFoundException, VersionException, ConstraintViolationException,
         LockException, UnsupportedRepositoryOperationException,
         RepositoryException
   {
      throw new UnsupportedRepositoryOperationException("Not yet implemented"); //$NON-NLS-1$
   }

   /**
    * Calculate final projection, filtering with type information and adding
    * necessary required fields. The projection string uses a map function to
    * package all the fields so they are mapped to the JSR-170 property names
    * from the internal names. In addition, the fields "rx:sys_contentid" and
    * "rx:sys_revision" are always mapped to allow the extraction of Nodes
    * <p>
    * Note that if the projection is "*" then all the fields for the given type
    * will be mapped and returned.
    * 
    * @param type the configuration type, never <code>null</code>
    * @param classes the classes in use by the query, never <code>null</code> 
    * @return the final projection string
    */
   public String getProjection(PSTypeConfiguration type, List<Class> classes)
   {
      String columns[];
      if (hasStarProjection())
      {
         Set<String> props = type.getAllJSR170Properties();
         columns = new String[props.size()];
         props.toArray(columns);
      }
      else
      {
         columns = getColumns();
      }

      StringBuilder rval = new StringBuilder();

      boolean first = true;

      rval.append("new map("); //$NON-NLS-1$
      Set<String> seen = new HashSet<>();
      for (String col : columns)
      {
         // Skip jcr:path, synthetic value
         if (col.equals(IPSContentPropertyConstants.JCR_PATH))
            continue;
         first = mapProjectionParam(rval, first, col, type, classes);
         seen.add(col);
      }
      
      // Add missing fields needed for sorting
      Iterator<PSPair<PSQueryNodeIdentifier,SortOrder>>
         sfiter = m_sortFields.iterator();
      while(sfiter.hasNext())
      {
         String name = sfiter.next().getFirst().getName();
         if (!seen.contains(name))
         {
            seen.add(name);
            first = mapProjectionParam(rval, first, name, type, classes);
         }
      }

      if (!seen.contains(IPSContentPropertyConstants.RX_SYS_CONTENTID))
      {
         first = mapProjectionParam(rval, first, IPSContentPropertyConstants.RX_SYS_CONTENTID, type, classes);
      }
      if (!seen.contains(IPSContentPropertyConstants.RX_SYS_REVISION))
      {
         first = mapProjectionParam(rval, first, IPSContentPropertyConstants.RX_SYS_REVISION, type, classes);
      }
      if (! first)
      {
         rval.append(", "); //$NON-NLS-1$
      }
      // we have to use "sys_folderid", but not "rx:sys_folderid" because this
      // will be literally passed to HQL, but the ':' is not a valid character.
      rval.append("f.owner_id as " + IPSHtmlParameters.SYS_FOLDERID); //$NON-NLS-1$
      rval.append(")"); //$NON-NLS-1$

      return rval.toString();
   }

   /**
    * Map a single query projection parameter
    * 
    * @param rval the string being built
    * @param first pass <code>true</code> for the first param
    * @param col the name of the property
    * @param type the type being processed
    * @param classes the classes in use in the where clause of the query
    * @returns <code>true</code> if first was <code>true</code> or if this
    *          property was processed
    */
   private boolean mapProjectionParam(StringBuilder rval, boolean first,
         String col, PSTypeConfiguration type, List<Class> classes)
   {
      PSPair<String,Class> resolvedref 
         = PSContentUtils.resolveFieldReference(col, type);
      if (resolvedref != null)
      {
         String queryref = PSContentUtils.makeQueryRef(resolvedref,classes);
         if (!first)
            rval.append(", "); //$NON-NLS-1$
         rval.append(queryref);
         rval.append(" as "); //$NON-NLS-1$
         rval.append(col.replace(':', '\u00A8'));
         return false;
      }
      else
      {
         return first;
      }
   }

   /**
    * Get the names of the defined projection elements. If the projection is
    * just "*" then no columns are returned and the query result object will not
    * limit itself to returning only a specific set of fields
    * 
    * @return an empty array if the projection is just "*", or the specific list
    *         if they are defined
    */
   public String[] getColumns()
   {
      if (hasStarProjection())
         return null;
      else
      {
         String columns[] = new String[m_projection.size()];
         int i = 0;
         for (PSQueryNodeIdentifier id : m_projection)
         {
            columns[i++] = id.getName();
         }
         return columns;
      }
   }

   /**
    * Does this query have a projection to return all fields?
    * 
    * @return <code>true</code> if the projection is missing or has the single
    *         value "*"
    */
   private boolean hasStarProjection()
   {
      return m_projection.size() == 0
            || (m_projection.size() == 1 && m_projection.get(0).getName()
                  .equals("*")); //$NON-NLS-1$
   }

   /**
    * Calculate and return the sort clause with respect to a specific content
    * type, removing any properties that don't exist in the target type.
    * 
    * @param type the target content type configuration, never <code>null</code>
    * @param classes the inuse classes, used to calculate the field reference
    * @return the order by string, never <code>null</code>, but can be empty
    */
   public String getSortClause(PSTypeConfiguration type, List<Class> classes)
   {
      if (type == null)
      {
         throw new IllegalArgumentException("type may not be null"); //$NON-NLS-1$
      }
      if (m_sortFields.size() == 0)
      {
         return ""; //$NON-NLS-1$
      }

      StringBuilder rval = new StringBuilder();
      boolean first = true;
      Set<String> fields = type.getAllJSR170Properties();

      for (PSPair<PSQueryNodeIdentifier, SortOrder> sf : m_sortFields)
      {
         String prop = sf.getFirst().getName();
         if (fields.contains(prop))
         {
            PSPair<String,Class> resolvedref = 
               PSContentUtils.resolveFieldReference(prop, type);
            String queryref = PSContentUtils.makeQueryRef(resolvedref,classes);
            if (resolvedref != null)
            {
               if (!first)
                  rval.append(',');
               rval.append(" "); //$NON-NLS-1$
               rval.append(queryref);
               if (sf.getSecond().equals(SortOrder.ASC))
                  rval.append(" asc"); //$NON-NLS-1$
               else
                  rval.append(" desc"); //$NON-NLS-1$

               first = false;
            }
         }
      }

      if (rval.length() > 0)
         return "order by " + rval.toString(); //$NON-NLS-1$
      else
         return ""; //$NON-NLS-1$
   }

   /**
    * Get a sorter for the result set
    * 
    * @return the sorter, which may have nothing to sort
    */
   public PSRowComparator getSorter()
   {
      List<PSPair<String, Boolean>> comparisons = new ArrayList<>();
      for (PSPair<PSQueryNodeIdentifier, PSQuery.SortOrder> sf : m_sortFields)
      {
         comparisons.add(new PSPair<>(sf.getFirst().getName(),
               sf.getSecond().equals(PSQuery.SortOrder.ASC)));
      }

      return new PSRowComparator(comparisons);
   }

   /**
    * Add a single additional projection field
    * 
    * @param fieldname the field to add, never <code>null</code> or empty
    */
   public void addProjectionField(String fieldname)
   {
      if (StringUtils.isBlank(fieldname))
      {
         throw new IllegalArgumentException("fieldname may not be null or empty"); //$NON-NLS-1$
      }
      // Check to see if present before adding
      for(PSQueryNodeIdentifier id : m_projection)
      {
         if (id.getName().equals(fieldname)) return;
      }
      m_projection.add(new PSQueryNodeIdentifier(fieldname));
   }
   
   /**
    * Checks whether a field is in the projection or not.
    * 
    * @param fldName name of the projection field to be checked if
    *           <code>null</code> or empty returns false.
    * @return <code>true</code> if the field exists in the query otherwise
    *         <code>false</code>.
    */
   public boolean doesQueryHasField(String fldName)
   {
      if(StringUtils.isBlank(fldName))
         return false;
      for (PSQueryNodeIdentifier identifier : m_projection)
      {
         if (identifier.getName().equalsIgnoreCase(fldName))
            return true;
      }
      return false;
   }
}
