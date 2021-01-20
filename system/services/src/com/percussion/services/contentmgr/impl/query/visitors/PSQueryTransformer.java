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
package com.percussion.services.contentmgr.impl.query.visitors;

import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSContentPropertyConstants;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.contentmgr.impl.query.IPSFolderExpander;
import com.percussion.services.contentmgr.impl.query.nodes.IPSQueryNode;
import com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeComparison;
import com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeFunction;
import com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeIdentifier;
import com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeLiteral;
import com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeValue;
import com.percussion.services.contentmgr.impl.query.nodes.IPSQueryNode.Op;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.utils.orm.PSDataCollectionHelper;
import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;

import org.hibernate.Session;

/**
 * The query transformer traverses the nodes and modifies them according to
 * transformation rules. At this point the rules include:
 * <ul>
 * <li>Replace jcr:path with an in clause on folder id or a set of clauses
 * on the temp id collection
 * <li>Replace jcr:primaryType with an equivalent clause based on content type
 * id
 * <li>Replace jcr:mixinType with false
 * </ul>
 * This class implements the transformation using a kind of visitor pattern
 * based on the specific node. The visitor methods return either the original
 * node or a transformed node. The caller splices the new node into place.
 * 
 * @author dougrand
 * 
 */
public class PSQueryTransformer extends PSQueryNodeVisitor
{
   /**
    * The folder expander to use, never <code>null</code> after ctor
    */
   private IPSFolderExpander m_folderExpander = null;

   /**
    * The content manager, never <code>null</code> after construction
    */
   private IPSContentMgr m_cm = null;

   /**
    * The parameters, may be <code>null</code>
    */
   private Map<String, ? extends Object> m_parameters = null;
   
   /**
    * See {@link #getIdCollections()} for information.
    */
   private List<Long> m_idCollections = new ArrayList<Long>();
   
   /**
    * This counts the number of ids used for paths. This is used to decide when
    * to switch from the in clause to the join to the temporary table. See
    * {@link #m_idCollections}.
    */
   private int m_idsUsed = 0;

   /**
    * Session that is used for data collection handling
    */
   private Session m_session;

   /**
    * Construct a new transformer
    * 
    * @param folderExpander the expander is used to go from a jcr:path to a list
    *           of folder ids, never <code>null</code>
    * @param parameters parameters used in the query, may be <code>null</code>
    *           or empty
    * @param session the hibernate session that is used, may be
    *           <code>null</code> for testing only
    */
   public PSQueryTransformer(IPSFolderExpander folderExpander,
         Map<String, ? extends Object> parameters, Session session) {
      if (folderExpander == null)
      {
         throw new IllegalArgumentException("folderExpander may not be null");
      }
      if (parameters == null)
      {
         m_parameters = new HashMap<String, Object>();
      }
      m_folderExpander = folderExpander;
      m_cm = PSContentMgrLocator.getContentMgr();
      m_parameters = parameters;
      m_session = session;
   }

   /**
    * If one or more jcr:path expressions involves more than
    * {@link PSDataCollectionHelper#MAX_IDS} values, then a collection will be
    * allocated and the ids stored in that collection. This list stores those
    * ids, which are retrieved by the content manager and used to add the extra
    * objects to the resulting query.
    * 
    * @return the idCollections
    */
   public List<Long> getIdCollections()
   {
      return m_idCollections;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.contentmgr.impl.query.visitors.PSQueryNodeVisitor#visitComparison(com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeComparison)
    */
   @Override
   public IPSQueryNode visitComparison(PSQueryNodeComparison comparisonNode)
         throws InvalidQueryException
   {
      IPSQueryNode left = comparisonNode.getLeft();
      IPSQueryNode right = comparisonNode.getRight();
      Op op = comparisonNode.getOp();

      PSQueryNodeIdentifier property = findProperty(left, right);
      PSQueryNodeValue value = (PSQueryNodeValue) findValue(left, right);

      Object valuesValue = getNodeValue(value, m_parameters);

      if (property != null)
      {
         String name = property.getName();
         if (name.startsWith("jcr:"))
         {
            if (name.equals(IPSContentPropertyConstants.JCR_PATH))
            {
               if (!(valuesValue instanceof String))
               {
                  throw new InvalidQueryException(
                        "Value for jcr:path must be a string");
               }
               left = new PSQueryNodeIdentifier("f.owner_id", PropertyType.LONG);
               
               List<IPSGuid> folders = m_folderExpander
                     .expandPath((String) valuesValue);
               m_idsUsed += folders.size();
               if (folders.size() == 0)
               {
                  PSQueryNodeValue v = new PSQueryNodeValue(Boolean.FALSE);
                  v.setType(PropertyType.BOOLEAN);
                  return v;
               }
               else if (m_idsUsed > PSDataCollectionHelper.MAX_IDS)
               {
                  IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
                  long collectionId = 
                     PSDataCollectionHelper.createIdSet(m_session, gmgr
                        .extractContentIds(folders));
                  m_idCollections.add(collectionId);
                  return new PSQueryNodeLiteral(
                        "f.owner_id in (select t.pk.itemId from "
                              + "PSTempId t where t.pk.id = " + collectionId
                              + ")");
               }
               else
               {
                  return new PSQueryNodeComparison(left, new PSQueryNodeValue(
                        folders), Op.IN);
               }
            }
            else if (name.equals("jcr:primaryType"))
            {
               if (!op.equals(Op.EQ))
               {
                  throw new InvalidQueryException(
                        "primary type restriction is only valid with the "
                              + "equals operator");
               }
               if (!(valuesValue instanceof String))
               {
                  throw new InvalidQueryException(
                        "Value for jcr:primaryType must be a string");
               }
               IPSNodeDefinition nd = null;
               try
               {
                  String type = (String) valuesValue;
                  nd = m_cm.findNodeDefinitionByName(type);
               }
               catch (RepositoryException e)
               {
                  throw new InvalidQueryException("Could not find primaryType "
                        + valuesValue);
               }
               left = new PSQueryNodeIdentifier(
                     "sys_componentsummary.m_contentTypeId", PropertyType.LONG);

               return new PSQueryNodeComparison(left, new PSQueryNodeValue(nd
                     .getGUID().longValue()), op);
            }
            else if (name.equals("jcr:mixinType"))
            {
               PSQueryNodeValue v = new PSQueryNodeValue(Boolean.FALSE);
               v.setType(PropertyType.BOOLEAN);
               return v;
            }
         }
      }

      return new PSQueryNodeComparison(left, right, op);
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.contentmgr.impl.query.visitors.PSQueryNodeVisitor#visitFunction(com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeFunction)
    */
   @Override
   public IPSQueryNode visitFunction(PSQueryNodeFunction functionNode)
         throws InvalidQueryException
   {
      if (functionNode.getName().equals("jcr:like"))
      {
         if (functionNode.getParameterCount() != 2)
         {
            throw new InvalidQueryException("jcr:like takes two parameters");
         }
         // Should be two params. These get turned into a comparison node
         IPSQueryNode left, right;

         left = functionNode.getParameter(0);
         right = functionNode.getParameter(1);
         PSQueryNodeComparison c = new PSQueryNodeComparison(left, right,
               IPSQueryNode.Op.LIKE);
         return c;
      }
      else
      {
         throw new InvalidQueryException("Function " + functionNode.getName()
               + " unsupported");
      }
   }

}
