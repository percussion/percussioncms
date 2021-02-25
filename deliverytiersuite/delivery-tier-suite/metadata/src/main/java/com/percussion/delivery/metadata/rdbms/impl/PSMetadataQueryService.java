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

import com.percussion.delivery.metadata.IPSMetadataEntry;
import com.percussion.delivery.metadata.IPSMetadataQueryService;
import com.percussion.delivery.metadata.data.PSMetadataQuery;
import com.percussion.delivery.metadata.data.impl.PSCriteriaElement;
import com.percussion.delivery.metadata.error.PSMalformedMetadataQueryException;
import com.percussion.delivery.metadata.impl.PSMetadataQueryServiceHelper;
import com.percussion.delivery.metadata.impl.PSPropertyDatatypeMappings;
import com.percussion.delivery.metadata.impl.utils.PSPair;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.internal.SessionImpl;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import javax.xml.bind.DatatypeConverter;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * @author erikserating
 * 
 */

@Repository
@Scope("singleton")
public class PSMetadataQueryService implements IPSMetadataQueryService
{
    private SessionFactory sessionFactory;

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

   /**
    * Logger for this class.
    */
    private static final Logger log = LogManager.getLogger(PSMetadataQueryService.class);
    
    /**
     * Property datatype mappings, loaded by Spring.
     */
    protected PSPropertyDatatypeMappings datatypeMappings;
    private volatile Integer queryLimit=500;
    
    /**
     * ctor
     * 
     * @param datatypeMappings
     * @param queryLimit
     */
    public PSMetadataQueryService(PSPropertyDatatypeMappings datatypeMappings, Integer queryLimit)
    {
        this.datatypeMappings = datatypeMappings;
        this.queryLimit = queryLimit;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.metadata.IPSMetadataQueryService#executeQuery(com.percussion
     * .metadata.IPSMetadataQuery)
     */
    // I think this is leaking transactions
 //   @Transactional(propagation = Propagation.REQUIRES_NEW,isolation = Isolation.READ_COMMITTED)
    public PSPair<List<IPSMetadataEntry>, Integer> executeQuery(PSMetadataQuery query)
   {
       log.debug("Executing query for metadata entries");
       Transaction tx = null;

       PSPair<List<IPSMetadataEntry>, Integer>  searchResults = new PSPair<>();
       PSPair<Query, SORTTYPE>  queryInfo;

       try(Session session = getSession())
       {
           tx = session.beginTransaction();

         List<IPSMetadataEntry> results = new ArrayList<>();
         Integer totalResults = null; 
     
         if(!isPagingSupported(query))
         {
             throw new UnsupportedOperationException("Pagination is not supported for requested sort property");
         }
         else
         {
             //Get the Count of entries only when the StartIndex is 0, for later pages client should be having the 
             //total count already so no need to get the count again
             if(query.getStartIndex() == 0 || query.getReturnTotalEntries())
             {
                 queryInfo = buildHibernateQuery(session, query,true);
                 
                 Long count = (Long) queryInfo.getFirst().list().get(0);
                 totalResults = count.intValue();
             }

             // call the method for second time to get list of objects based on the query
             queryInfo = new PSPair<>();
             queryInfo = buildHibernateQuery(session, query,false);
             if(queryInfo.getSecond().equals(SORTTYPE.PROPERTY))
             {
                 List<Object[]> resultsTmpList = queryInfo.getFirst().list();
                 for (Object[] o : resultsTmpList)
                 {
                     results.add((PSDbMetadataEntry) o[0]);
                 }
                 searchResults.setFirst(results);
             }
             else if(queryInfo.getSecond().equals(SORTTYPE.METADATA) || queryInfo.getSecond().equals(SORTTYPE.NONE))
             {
                 results = queryInfo.getFirst().list();
                 searchResults.setFirst(results);
             }
             searchResults.setSecond(totalResults);
         }
         tx.commit();
      } catch (ParseException | PSMalformedMetadataQueryException e) {
               log.error(e.getMessage());
               log.debug(e.getMessage(),e);
               if(tx != null && tx.isActive()){
                   tx.rollback();
               }
       }

       return searchResults;
   }
 
    /**
     * Helper method to build the Hibernate query based on the raw query passed in.
     * There are four possible ways to create the query based on the passed in criteria
     * 1)Query for the count of total entries for the given criteria
     * Ex:select count(*) from PSDbMetadataEntry as me where me.type = :pagePropValue0 and me.site = :pagePropValue1 
     * and me.folder LIKE :pagePropValue2 and me.name != :pagePropValue3
     * 
     * 2)Query for Results set for passed in criteria and order by is a property from the properties table
     * Ex:select distinct me, lower(prop.stringvalue) as sort1  from PSDbMetadataEntry as me join me.properties as prop
     * where prop.id.name = 'dcterms:title' and me.site = :pagePropValue0 order by sort1 desc
     * 
     * 3)Query for Results set for passed in criteria and ordeby is a column name from the parent table
     * Ex:select distinct me from PSDbMetadataEntry as me where me.site=:pagePropValue0 order by lower(me.pagepath) asc
     * 
     * 4)Query for results set for passed in criteria but there no orderby
     * Ex:
     * @param sess
     * @param rawQuery
     * isCount if it is true then query will be created to get the count of entries otherwise
     * get all results.
     * @return PSPair with Query and the SORTTYPE
     * @throws PSMalformedMetadataQueryException
     * @throws ParseException 
     * @throws HibernateException 
     */
    private PSPair<Query, SORTTYPE> buildHibernateQuery(Session sess, PSMetadataQuery rawQuery, boolean isCount)
           throws PSMalformedMetadataQueryException, HibernateException, ParseException
    {
        List<PSCriteriaElement> entryCrit = new ArrayList<>();
        List<PSCriteriaElement> propsCrit = new ArrayList<>();
        Map<String, String> sortColumns = new HashMap<>();

        String orderBy = rawQuery.getOrderBy();
        orderBy= StringEscapeUtils.escapeSql(orderBy);
        String sortColumnName = "";
        SORTTYPE type = SORTTYPE.NONE;
        //is used for if the sort column is based on the property from the property table
        boolean isSortingOnProperty = false;
        
        //is used for if the sort column is based on the column name from the parent table 
        boolean isSortingOnMatadata = false;
        
        // Process criteria
        if (rawQuery.getCriteria() != null)
        {
            PSCriteriaElement el;
            for (String s : rawQuery.getCriteria()) {
                if(!s.isEmpty()){
                    el = new PSCriteriaElement(s);
                    if (PSMetadataQueryServiceHelper.ENTRY_PROPERTY_KEYS.contains(el.getName())) {
                        entryCrit.add(el);
                    } else {
                        propsCrit.add(el);
                    }
                }
            }
        }
 
        StringBuilder queryBuf = new StringBuilder();
        if(isCount)
        {
           queryBuf.append("select count(distinct me) from PSDbMetadataEntry as me");
        }
        else
        {
            if(!StringUtils.isBlank(orderBy))
            {
                isSortingOnMatadata = 
                    PSMetadataQueryServiceHelper.ENTRY_PROPERTY_KEYS.contains(
                                    PSMetadataQueryServiceHelper.getSortPropertyName(orderBy));
                if(isSortingOnMatadata)
                {
                    type = SORTTYPE.METADATA;
                }
                else
                {
                    sortColumnName = 
                        PSMetadataQueryServiceHelper.getValueColumnName(
                                        PSMetadataQueryServiceHelper.getSortPropertyName(orderBy), datatypeMappings);
                    isSortingOnProperty = true;
                    type = SORTTYPE.PROPERTY;
                }
            }
            
            // We need the distinct clause to avoid duplicate entries in the
            // query when multivalue properties are involved (e.g. categories, tags)
            // SQLSERVER have an issue with the distinct clause when some field
            // of the "order by" statement are not specified in the "select" statement.
            // This method was tested using different combinations of "order by"
            // under SQL server and all were ok.
            queryBuf.append("select distinct me");
            if(isSortingOnProperty)
            {
                if (PROP_STRINGVALUE_COLUMN_NAME.equals(sortColumnName))
                {
                    queryBuf.append(", lower(prop.");
                    queryBuf.append(sortColumnName);
                    queryBuf.append(") as sort1 ");
                }
                else
                {
                    queryBuf.append(", prop.");
                    queryBuf.append(sortColumnName);
                    queryBuf.append(" as sort1 ");
                }
            }
            queryBuf.append(" from PSDbMetadataEntry as me");
            if(isSortingOnProperty)
            {
                queryBuf.append(" join me.properties as prop");
            }
        }
        
        for (int i = 0; i < propsCrit.size(); i++)
             queryBuf.append(" join me.properties as p").append(i);
        
        if (!entryCrit.isEmpty() || ! propsCrit.isEmpty())
            queryBuf.append(" where");
        
        if((isSortingOnProperty))
        {
            queryBuf.append(" prop.id.name = ").append('\'')
                    .append(PSMetadataQueryServiceHelper.getSortPropertyName(orderBy))
                    .append('\'');
        }
        String clauseTemplate = " me.{0} {1} :{2}";
        String inClauseTemplate = " me.{0} {1} (:{2})";
        int paramIndex = 0;
        Map<String, Object> paramValues = new HashMap<>();
        Map<String, PSCriteriaElement.OPERATION_TYPE> paramOps = new HashMap<>();
        boolean needConjunction = false;
        if(isSortingOnProperty) 
        {
            needConjunction = true;
        }
        for (PSCriteriaElement ce : entryCrit)
        {
            if (needConjunction)
                queryBuf.append(" and");
            else
                needConjunction = true;
            String replParam = "pagePropValue" + paramIndex++;
            String useClause = clauseTemplate;
            if(ce.getOperationType() == PSCriteriaElement.OPERATION_TYPE.IN)
            {
               useClause = inClauseTemplate;
            }
            queryBuf.append(MessageFormat.format(useClause, ce.getName(), ce.getOperation(), replParam));
            paramValues.put(replParam, ce.getValue());
            paramOps.put(replParam, ce.getOperationType());
        }
            
        clauseTemplate = " lower(p{0}.id.name) = lower(:{4}) and p{0}.{1} {2} :{3}";
        inClauseTemplate = " lower(p{0}.id.name) = lower(:{4}) and p{0}.{1} {2} (:{3})";
        
        int i=0;
        for (PSCriteriaElement ce : propsCrit)
        {
            if (needConjunction)
                queryBuf.append(" and");
            else
                needConjunction = true;
            String nameParam = "propName" + paramIndex;
            String valueParam = "propValue" + paramIndex++;
            Object value = ce.getValue();
            String valueColumn = PSMetadataQueryServiceHelper.getValueColumnName(ce.getName(), datatypeMappings);
            
            if(PROP_DATEVALUE_COLUMN_NAME.equals(valueColumn))
            {
                Calendar date = DatatypeConverter.parseDate(value.toString().replace(' ', 'T'));
                value = new Date(date.getTimeInMillis());
            }
            
            String useClause = clauseTemplate;
            if(ce.getOperationType() == PSCriteriaElement.OPERATION_TYPE.IN)
            {
               useClause = inClauseTemplate;
            }
            
            queryBuf.append(MessageFormat.format(useClause, i++, valueColumn, ce.getOperation(), valueParam, 
                    nameParam));
          
            // Escape special characters that go into LIKE so that they don't
            // reach final DB query.
            if (ce.getOperationType().equals(PSCriteriaElement.OPERATION_TYPE.LIKE) && value instanceof String)
            {
                // Append HQL especial modifier to the end of LIKE
                queryBuf.append(" " + HQL_ESCAPE + " '" + ESCAPE_CHAR + "'");
                value = escapeSpecialCharacters((String) value);
            }

            paramValues.put(nameParam, ce.getName());
            paramValues.put(valueParam, value);
            paramOps.put(valueParam, ce.getOperationType());
        }
        //If the method is getting called only for the entry count, in that case order by doesn't need to be included
        if(isSortingOnProperty || isSortingOnMatadata)
        {
            if(isSortingOnProperty)
            {
                queryBuf.append(" order by sort1 ");
                //Add the extra order criteria like linktitle, to the current query.
                sortColumns = getAdditionalSortCriteria(orderBy);
                if(!sortColumns.isEmpty())
                {
                    String orderByFirstOrder = "asc";
                    if (orderBy.contains(","))
                    {
                        orderByFirstOrder = orderBy.substring(0, orderBy.indexOf(","));
                    }
                    queryBuf.append(PSMetadataQueryServiceHelper.getSortingOrder(orderByFirstOrder));
                    
                    for (Map.Entry<String,String> entry : sortColumns.entrySet())
                    {
                        queryBuf.append(", ").append( "me.").append( entry.getKey()).append(" ").append(entry.getValue());
                    }
                }
            }
            else
            {
                //Make it case insensitive
                queryBuf.append(" order by ").append( "me.").append(
                        PSMetadataQueryServiceHelper.getSortPropertyName(orderBy) ).append( " ");
            }

            if(sortColumns.isEmpty())
                queryBuf.append(PSMetadataQueryServiceHelper.getSortingOrder(orderBy));
        }

        log.debug("{}",queryBuf);
        
        Query q = sess.createQuery(queryBuf.toString());
        int useLimit=getQueryLimit();
        //All caller to set a query limit, but they can't allow higher than the server limit. 
        if(rawQuery.getTotalMaxResults() > 0 && rawQuery.getTotalMaxResults() < getQueryLimit()){
        	log.debug("Setting max query limit to client provided value : {}" , rawQuery.getTotalMaxResults());
        	useLimit=rawQuery.getTotalMaxResults();
        }
        
        q.setMaxResults(useLimit);
        q.setCacheable(true);
        
        //If it is not for count then only pagination properties need to be set on the query
        if(!isCount && rawQuery.getMaxResults() >0 && rawQuery.getMaxResults() <= useLimit)
        {
            if(rawQuery.getMaxResults() > 0)
                q.setMaxResults(rawQuery.getMaxResults());
            if(rawQuery.getStartIndex() >= 0)
                q.setFirstResult(rawQuery.getStartIndex());
        }
        for (String key : paramValues.keySet())
        {
            Object value = paramValues.get(key);
            PSCriteriaElement.OPERATION_TYPE opType = paramOps.get(key);
            if(opType == PSCriteriaElement.OPERATION_TYPE.IN)
            {
               q.setParameterList(key, 
                   PSMetadataQueryServiceHelper.parseToList(key, value.toString(), datatypeMappings));
            }
            else if (value instanceof Date)
            {
                q.setTimestamp(key, (Date) value);
            }
            else if (value instanceof String)
            {
               q.setString(key, value.toString());
            }
        }
        
        return  getBuildQueryInfo(q, type);
        
    }   
    
    private String escapeSpecialCharacters(String value)
    {
        List<String> specialChars = getCharactersToEscape();

        String escapedString = value;

        // If the value starts or ends with a wildcard, leave those unescaped.
        boolean startsWithWildcard = escapedString.startsWith("%");
        boolean endsWithWildcard = escapedString.endsWith("%");

        if (startsWithWildcard && escapedString.length() > 1)
            escapedString = escapedString.substring(1);

        if (endsWithWildcard)
            escapedString = escapedString.substring(0, escapedString.length() - 1);

        // Escape all special characters
        for (String specialChar : specialChars)
        {
            escapedString = escapedString.replaceAll(specialChar, ESCAPE_CHAR + specialChar);
        }

        if (startsWithWildcard)
            escapedString = "%" + escapedString;

        if (endsWithWildcard)
            escapedString = escapedString + "%";

        return escapedString;
    }
    
    private List<String> getCharactersToEscape()
    {
        List<String> specialChars = new ArrayList<>();

        // Escape the char that is used to escape too, in case it appears in the
        // string. MUST be escaped first.
        specialChars.add(String.valueOf(ESCAPE_CHAR));

        // These are common wildcards for all supported DBs
        specialChars.add("_");
        specialChars.add("%");

        String jdbcProvider = getJdbcProvider();
        if (StringUtils.isNotBlank(jdbcProvider) && jdbcProvider.contains(JDBC_SQLSERVER_DRIVER))
        {
                // Characters that are relevant to regex need to be escaped, to
                // work properly with replaceAll.
                specialChars.add("\\[");
                specialChars.add("\\]");
                specialChars.add("\\^");
                specialChars.add("'");
            // Derby, ORACLE and MySQL only support escaping "%" and "_"
            // characters.
        }
        return specialChars;
    }

    /**
     * Method to obtain the URL string of the JDBC connection. Used to check the
     * DB provider for delivery server.
     * 
     * @return URL string of the JDBC connection. Can be empty if there was a
     *         problem retrieving the information from the session.
     */
    private String getJdbcProvider()
    {
        // If JDBC provider has already been obtained, don't retrieve it again
        // from session.
        if (isNotBlank(jdbcConnectionUrl))
        {
            return jdbcConnectionUrl;
        }

        Connection connection = null;
        try(Session session = getSession())
        {
            connection = ((SessionImpl) session).connection();
            jdbcConnectionUrl = connection.getMetaData().getURL();
        }
        catch (SQLException | RuntimeException e)
        {
            log.error("There was an error getting jdbc driver name. ERROR: {}", e.getMessage());
            log.debug( e.getMessage(),e);
        }

        return jdbcConnectionUrl;
    }
    
    /**
     * Based on the order By option, get the additional fields to add to the order by
     * sentence in the query  
     * * Ex: orderBy = "dcterms:created desc, linktitle desc" and the method returns linktitle desc
     * @param orderBy cannot be <code>null</code> or empty
     * @return sortColums the map of additional fields to be added, may be empty never<code>null</code>
     */
    private Map<String, String> getAdditionalSortCriteria(String orderBy)
    {
        Map<String, String> hMapColumns = new HashMap<>();
        if (orderBy.contains(","))
        {    
            String orderByColumns = orderBy.substring(orderBy.indexOf(",")+1);
            String[] arrayOrderBy = orderByColumns.split(",");
        
            for(String orderColumn : arrayOrderBy)
            {
                String sortField = PSMetadataQueryServiceHelper.getSortPropertyName(orderColumn.trim());
                String sortingOrder = PSMetadataQueryServiceHelper.getSortingOrder(orderColumn.trim());
                hMapColumns.put(sortField, sortingOrder);   
            }
        }
        return hMapColumns;
    }
    
    /**
     * Based on the sort type which would be by count/by property/by column on the parent table 
     * @param query
     * @param type sort type
     * @return
     */
    private PSPair<Query, SORTTYPE> getBuildQueryInfo(Query query, SORTTYPE type)
    {
        PSPair<Query, SORTTYPE>  queryInfo = new PSPair<>();
        queryInfo.setFirst(query);
        queryInfo.setSecond(type);
        return queryInfo;
    }  
   
   /**
    * if orderby on the query is a clob type then 
    * pagination is not allowed
    * @param query
    * @return
    */
   private boolean isPagingSupported(PSMetadataQuery query)
   {
       //TODO:  It is unclear that this code actually works. How can the column name ever equal the data type?
       
       boolean isSupported= true;
       String orderBy =  query.getOrderBy();
       if(StringUtils.isNotBlank(orderBy))
       {
           isSupported = !PSMetadataQueryServiceHelper.getDatatype(
                           PSMetadataQueryServiceHelper.getSortPropertyName(orderBy), datatypeMappings).toString().equals(PROP_TEXTVALUE_COLUMN_NAME);
       }
       return isSupported;
   }
   
    /**
     * Constant for "jtds:sqlserver". Part of the JDBC URL required to connect
     * to the MS SQL Server database using JTds driver
     */
    public static final String JDBC_SQLSERVER_DRIVER = "jtds:sqlserver";

    /**
     * constant for the Oracle thin driver type.
     */
    public static final String JDBC_ORACLE_DRIVER = "oracle:thin";

    /**
     * constant for the MySQL driver type.
     */
    public static final String JDBC_MYSQL_DRIVER = "mysql";

    /**
     * constant for the Apache Derby driver type.
     */
    public static final String JDBC_DERBY_DRIVER = "derby";
    
    public static final String HQL_ESCAPE = "escape";
    
    public static final char ESCAPE_CHAR = '=';
    
    private static String jdbcConnectionUrl = null;

	@Override
	public synchronized Integer getQueryLimit() {
		return this.queryLimit;
	}

	@Override
	public synchronized void setQueryLimit(Integer limit) {
		this.queryLimit = limit;
	}

    private Session getSession(){

        return sessionFactory.openSession();

    }
}
