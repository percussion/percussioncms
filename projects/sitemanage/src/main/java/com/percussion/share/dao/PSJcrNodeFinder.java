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
/**
 * 
 */
package com.percussion.share.dao;

import static java.util.Arrays.asList;

import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A helper class to find our extended JCR 170 nodes.
 * 
 * @author adamgent
 *
 */
public class PSJcrNodeFinder {
    
    private IPSContentMgr contentMgr;
    private String contentType;
    private String uniqueIdFieldName;
    

    /**
     * 
     * @param contentMgr never <code>null</code>.
     * @param contentType never <code>null</code>.
     * @param uniqueIdFieldName never <code>null</code>.
     */
    public PSJcrNodeFinder(IPSContentMgr contentMgr, String contentType, String uniqueIdFieldName)
    {
        super();
        notNull(contentMgr);
        notNull(contentType);
        notNull(uniqueIdFieldName);
        this.contentMgr = contentMgr;
        this.contentType = contentType;
        this.uniqueIdFieldName = uniqueIdFieldName;
        
    }

    /**
     * 
     * @param folderPath Checks the folder path and descendents, never <code>null</code>.
     * @param id  User supplied unique id, never <code>null</code>.
     * @return the generated jcr query.
     */
    public String getQuery(String folderPath, String id) 
    {
        notNull(folderPath);
        notNull(id);
        String uniqCond = "rx:" + uniqueIdFieldName + " = " + "'" + id + "'";
        //Normalize path.
        String fp = StringUtils.removeEnd(folderPath, "/");
        
        String where = "jcr:path like '" + fp + "/%'" + " and " + uniqCond;
        return getQuery(where);
    }
        
    /**
     * 
     * @param folderPath Checks the folder path and descendents, never <code>null</code>.
     * @param id never <code>null</code>.
     * @return our extended jcr {@link Node}
     * @throws PSJcrNodeFinderException If more than one node is found or the query is bad.
     */
    public IPSNode find(String folderPath, String id) throws PSJcrNodeFinderException {
        notNull(folderPath);
        notNull(id);
        String query = getQuery(folderPath, id);
        List<IPSNode> nodes = executeQuery(query);
        if (nodes.isEmpty())
        {
            return null;
        }
        else if (nodes.size() > 1)
        {
            throw new PSJcrNodeFinderException("Returned multiple nodes: " + nodes.size() + " for query: " + query);
        }
        
        return nodes.get(0);
    }
    
    /**
     * Calls {@link #find(null, Map)}.
     */
    public List<IPSNode> find(Map<String, String> whereFields)
    {
        notNull(whereFields);
        
        return find(null, whereFields);
    }
    
    /**
     * Finds all nodes which match the specified jcr path and where criteria.
     * 
     * @param jcrPath set to <code>null</code> to include nodes from all paths.
     * @param whereFields map of fields which will make the where criteria.  The key is the field name and the value is
     * the field value.
     * 
     * @return list of {@link IPSNode} objects which match the criteria, never <code>null</code>, may be empty.
     */
    public List<IPSNode> find(String jcrPath, Map<String, String> whereFields)
    {
        notNull(whereFields);
        
        return executeQuery(getQuery(jcrPath, whereFields));
    }
    
    /**
     * Generates a basic jcr query using the specified jcr path and map of fields. 
     * 
     * @param jcrPath set to <code>null</code> to exclude the jcr path from the where clause.
     * @param whereFields map of fields which will make the where criteria.  The key is the field name and the value is
     * the field value.
     * 
     * @return never blank.
     */
    public String getQuery(String jcrPath, Map<String, String> whereFields)
    {
        notNull(whereFields);
        
        String where = "";
        if (jcrPath != null)
        {
            String fp = StringUtils.removeEnd(jcrPath, "/");
            where = "jcr:path like '" + fp + "/%'";
        }
        for (String field : whereFields.keySet())
        {
            if (!StringUtils.isEmpty(where))
            {
                where += " and ";
            }
            
            String value = whereFields.get(field);
            where += "rx:" + field + " = '" + value + "'";
        }
        
        return getQuery(where);
    }
    
    /**
     * Generates a basic jcr query using the specified where clause.  The select fields are determined by
     * {@link #getSelectFields()}.
     * 
     * @param where assumed not blank.
     * 
     * @return never blank.
     */
    private String getQuery(String where) 
    {
        return "select " + getSelectFields() + " from " + contentType + " where " + where;
    }
    
    /**
     * Generates the select fields string used by {@link #getQuery(String)}.  Current fields include content id,
     * folder id, and jcr path.
     * 
     * @return never blank.
     */
    private String getSelectFields()
    {
        List<String> selectFields = asList("rx:sys_contentid", "rx:sys_folderid", "jcr:path");
        return StringUtils.join(selectFields, ", ");
    }
    
    /**
     * Executes the given jcr query.
     * 
     * @param query assumed not blank.
     * 
     * @return list of {@link IPSNode} object results, never <code>null</code>, may be empty.
     * @throws PSJcrNodeFinderException if the query is not valid or if an error occurred executing the query.
     */
    private List<IPSNode> executeQuery(String query)
    {
        List<IPSNode> nodes = new ArrayList<>();
        
        try
        {
            log.debug("Executing query: " + query);
            Query q = contentMgr.createQuery(query , Query.SQL);
            QueryResult results = contentMgr.executeQuery(q, -1, null, null);
            NodeIterator it =  results.getNodes();
            while (it.hasNext())
            {
                IPSNode node = (IPSNode) it.nextNode();
                nodes.add(node);
            }
           
            return nodes;
        }
        catch (InvalidQueryException e)
        {
            throw new PSJcrNodeFinderException("Invalid query: " + query, e);
        }
        catch (RepositoryException e)
        {
            throw new PSJcrNodeFinderException("Repository error for query: " + query, e);
        }
    }
    
    public static class PSJcrNodeFinderException extends RuntimeException
    {

        private static final long serialVersionUID = 1L;

        public PSJcrNodeFinderException(String message)
        {
            super(message);
        }

        public PSJcrNodeFinderException(String message, Throwable cause)
        {
            super(message, cause);
        }

        public PSJcrNodeFinderException(Throwable cause)
        {
            super(cause);
        }

    }
    
    public Map<String, String> find(List<String> selectFields, String uniqueId)
    {
        notEmpty(uniqueId);
        notEmpty(selectFields);
        
        Map<String, String> result = new HashMap<>();
        
        // parse fields
        
        StringBuilder queryBldr = new StringBuilder("select rx:displaytitle");
        for (String fieldName : selectFields)
        {
            queryBldr.append(", rx:");
            queryBldr.append(fieldName);
        }
        queryBldr.append(" from rx:");
        queryBldr.append(contentType);
        queryBldr.append(" where rx:");
        queryBldr.append(uniqueIdFieldName);
        queryBldr.append("=");
        queryBldr.append(uniqueId);
        
        
        try
        {
            Query query = contentMgr.createQuery(queryBldr.toString(), Query.SQL);
            QueryResult qresults = contentMgr.executeQuery(query, 1, null, null);
            
            RowIterator rowIter = qresults.getRows();
            if (rowIter.hasNext())
            {
                Row row = rowIter.nextRow();
                
                for (String fieldName : selectFields)
                {
                    String fieldVal = "";
                    Value val = row.getValue("rx:" + fieldName);
                    if (val != null)
                    {
                        fieldVal = val.getString();
                    }
                    result.put(fieldName, fieldVal);
                }
            }
            
            return result;
        }
        catch (Exception e)
        {
            throw new PSJcrNodeFinderException(e);
        }
    }
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(PSJcrNodeFinder.class);
    
}
