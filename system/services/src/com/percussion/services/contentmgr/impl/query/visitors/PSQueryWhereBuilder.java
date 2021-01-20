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

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.services.contentmgr.IPSContentPropertyConstants;
import com.percussion.services.contentmgr.data.PSQuery.SortOrder;
import com.percussion.services.contentmgr.impl.PSContentUtils;
import com.percussion.services.contentmgr.impl.legacy.PSTypeConfiguration;
import com.percussion.services.contentmgr.impl.query.nodes.IPSQueryNode;
import com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeComparison;
import com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeConjunction;
import com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeIdentifier;
import com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeLiteral;
import com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeValue;
import com.percussion.services.contentmgr.impl.query.nodes.IPSQueryNode.Op;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.jsr170.PSValueFactory;
import com.percussion.utils.string.PSStringUtils;
import com.percussion.utils.types.PSPair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.query.InvalidQueryException;

/**
 * The where builder actually serves two purposes. It builds the where clause
 * for the query engine by walking the node structure. This is the primary
 * purpose. Secondarily it also evaluates what classes are included in the from
 * clause and how they are mapped to identifiers for use in the where clause.
 * <p>
 * For the primary use, the where builder takes each identifier and maps it to
 * the right originating information. This may be a particular instance class
 * used to load the content node from storage, or it may be a virtual reference
 * such as the mapping from the property <em>jcr:path</em> to folder id
 * references.
 * <p>
 * Values are stripped from the where clause and substituted with parameters.
 * The original values are available as the {@link #getQueryParams()} call. This
 * is used by the query engine to get the parameters to bind. This is done to
 * allow the parameters to be correctly coerced into the right type for the
 * field. The coercion is done by {@link #visitValue(PSQueryNodeValue)} and
 * {@link #handleObject(Object, int)}.
 * <p>
 * The secondary use, where this class agregates information about the used
 * fields and therefore the used instance classes is a combination of the tree
 * walking, which finds where each field comes from, and processing the sorter
 * and projection information, which allows those references to be represented
 * in the from clause. This information is returned by {@link #getInuse()}. The
 * classes returned by this method are ordered and the references then
 * correspond to c0, c1, c2, etc. References to the component summary are on the
 * alias cs.
 * 
 * @author dougrand
 * 
 */
public class PSQueryWhereBuilder extends PSQueryNodeVisitor
{
   /**
    * The where clause, built as the IPSQueryNode is traversed.
    */
   StringBuilder m_where = new StringBuilder();

   /**
    * The type configuration, never <code>null</code> after construction
    */
   PSTypeConfiguration m_type = null;

   /**
    * The parameters, may be <code>null</code>
    */
   Map<String, ? extends Object> m_parameters = null;

   /**
    * The query parameters, built as we go.
    */
   Map<String, Object> m_queryParams = new HashMap<String, Object>();

   /**
    * The classes in use in the query, see {@link #getInuse()} for details.
    */
   List<Class> m_inuse = new ArrayList<Class>();

   /**
    * The parameter name counter
    */
   int m_pcounter = 0;

   /**
    * Value for type that communicates the need to force the value to an integer
    * instead of a long.
    */
   private final int FORCE_INTEGER = 100;

   /**
    * The properties listed in this set are integer values in
    * {@link PSComponentSummary} and should be referenced as integers and not as
    * longs. Used in the query handling to avoid binding the wrong type of
    * parameter.
    */
   private static Set<String> ms_integerProperties = new HashSet<String>();

   static
   {
      ms_integerProperties.add(IPSContentPropertyConstants.RX_SYS_CONTENTID);
      ms_integerProperties.add(IPSContentPropertyConstants.RX_SYS_REVISION);
      ms_integerProperties.add(IPSContentPropertyConstants.RX_SYS_COMMUNITYID);
      ms_integerProperties.add(IPSContentPropertyConstants.RX_SYS_OBJECTTYPE);
      ms_integerProperties
            .add(IPSContentPropertyConstants.RX_SYS_CONTENTSTATEID);
      ms_integerProperties.add(IPSContentPropertyConstants.RX_SYS_WORKFLOWID);

   }

   /**
    * Ctor
    * 
    * @param type the type configuration from the content repository for the
    *           specific content type being processed, never <code>null</code>
    * @param params the extra parameters that have been defined, may be
    *           <code>null</code>
    */
   public PSQueryWhereBuilder(PSTypeConfiguration type,
         Map<String, ? extends Object> params) {
      if (type == null)
      {
         throw new IllegalArgumentException("type may not be null");
      }
      m_type = type;
      if (params != null)
         m_parameters = params;
      else
         m_parameters = new HashMap<String, Object>();
      
      // Make sure that the main class is always "in use". This makes references
      // to c0 always valid
      m_inuse.add(m_type.getMainClass());
   }

   /**
    * Get the results, never <code>null</code>
    * 
    * @return the query string, never <code>null</code>
    */
   @Override
   public String toString()
   {
      return m_where.toString();
   }

   /**
    * (non-Javadoc)
    * 
    * @see com.percussion.services.contentmgr.impl.query.visitors.PSQueryNodeVisitor#visitValue(com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeValue)
    */
   @SuppressWarnings("unchecked")
   @Override
   public IPSQueryNode visitValue(PSQueryNodeValue value)
         throws InvalidQueryException
   {
      try
      {
         handleObject(getNodeValue(value, m_parameters), value.getType());
      }
      catch (InvalidQueryException iq)
      {
         throw iq;
      }
      catch (Exception e)
      {
         throw new InvalidQueryException("Problem formatting value", e);
      }
      return value;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.contentmgr.impl.query.visitors.PSQueryNodeVisitor#visitLiteral(com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeLiteral)
    */
   @Override
   public IPSQueryNode visitLiteral(PSQueryNodeLiteral literal)
   {
      m_where.append(' ');
      m_where.append(literal.getLiteral());
      m_where.append(' ');
      return literal;
   }

   /**
    * The key is a generated parameter name. The value is the value for the
    * named parameter. This is returned after the where builder has run. Each
    * value is assigned to a parameter and returned.
    * 
    * @return Returns the queryParams.
    */
   public Map<String, Object> getQueryParams()
   {
      return m_queryParams;
   }

   /**
    * This list records the classes in use. Property references must resolve to
    * one of these classes. The user of this will request these after the where
    * builder has run and use the results to build a join.
    * 
    * @return the inuse classes from the type definition, never
    *         <code>null</code> but may be empty in some cases
    */
   public List<Class> getInuse()
   {
      return m_inuse;
   }

   /**
    * Format a single value
    * 
    * @param object the value, may be <code>null</code>
    * @param type the type of the value
    * @throws RepositoryException
    * @throws IllegalStateException
    */
   @SuppressWarnings("unchecked")
   private void handleObject(Object object, int type)
         throws IllegalStateException, RepositoryException
   {
      // Handle null
      if (object == null)
      {
         m_where.append("null");
         return;
      }

      // Preprocess strings a little bit - trim and strip quotes.
      if (object instanceof String)
      {
         String trimmed = ((String) object).trim();
         object = PSStringUtils.stripQuotes(trimmed);
      }
      else if (object instanceof Collection)
      {
         // Special case for collection
         m_where.append('(');
         boolean first = true;
         for (Object o : (Collection<Object>) object)
         {
            if (!first)
            {
               m_where.append(',');
            }
            handleObject(o, type);
            first = false;
         }
         m_where.append(')');
         return;
      }
      else if (object instanceof IPSGuid)
      {
         if (object instanceof PSLegacyGuid)
         {
            PSLegacyGuid lg = (PSLegacyGuid) object;
            // Content items and folders must be referenced by just
            // the content id
            m_where.append(lg.getContentId());
            return;
         }
         else if (object instanceof PSGuid)
         {
            PSGuid g = (PSGuid) object;
            m_where.append(g.longValue());
            return;
         }
         else
         {
            throw new InvalidQueryException("Unknown type of guid found: "
                  + object.getClass().getCanonicalName());
         }
      }

      // First convert to a Value
      Value v = PSValueFactory.createValue(object);
      String pname = "p" + m_pcounter++;
      m_where.append(":" + pname);

      switch (type)
      {
         case PropertyType.BOOLEAN :
            m_queryParams.put(pname, v.getBoolean());
            break;
         case PropertyType.DATE :
            m_queryParams.put(pname, v.getDate().getTime());
            break;
         case PropertyType.DOUBLE :
            m_queryParams.put(pname, v.getDouble());
            break;
         case PropertyType.LONG :
            m_queryParams.put(pname, (long) v.getLong());
            break;
         case PropertyType.STRING :
            m_queryParams.put(pname, v.getString());
            break;
         case FORCE_INTEGER :
            m_queryParams.put(pname, (int) v.getLong());
            break;
         default :
            throw new InvalidQueryException("Unsupported property type found "
                  + PropertyType.nameFromValue(type));
      }

   }

   /**
    * (non-Javadoc)
    * 
    * @see com.percussion.services.contentmgr.impl.query.visitors.PSQueryNodeVisitor#visitIdentifier(com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeIdentifier)
    */
   @Override
   public IPSQueryNode visitIdentifier(PSQueryNodeIdentifier identifier)
   {
      String name = identifier.getName();
      PSPair<String, Class> fieldref = PSContentUtils.resolveFieldReference(
            identifier.getName(), m_type);
      if (fieldref == null)
      {
         throw new IllegalStateException("Could not resolve field " + name);
      }
      checkAndRegisterInUse(fieldref.getSecond());
      String queryref = PSContentUtils.makeQueryRef(fieldref, m_inuse);
      if (m_type.getSimpleChildProperties().contains(fieldref.getFirst()))
      {
         m_where.append("some elements(");
         m_where.append(queryref);
         m_where.append(")");
      }
      else
      {
         if (PSContentUtils.isNonPropertyRef(name))
         {
            m_where.append(fieldref.getFirst());
         }
         else
         {
            m_where.append(queryref);
         }
      }
      return identifier;
   }

   /**
    * Check a class reference to see if it is currently in the in use
    * collection.
    * 
    * @param classref the reference to the instance class, if <code>null</code>
    * then do nothing.
    */
   private void checkAndRegisterInUse(Class classref)
   {
      if (classref == null) return;
      
      if (!m_inuse.contains(classref))
      {
         m_inuse.add(classref);
      }
   }

   /**
    * (non-Javadoc)
    * 
    * @see com.percussion.services.contentmgr.impl.query.visitors.PSQueryNodeVisitor#visitConjunction(com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeConjunction)
    */
   @Override
   public IPSQueryNode visitConjunction(PSQueryNodeConjunction conjunctionNode)
         throws InvalidQueryException
   {
      // Handle some short circuit cases first
      IPSQueryNode left = conjunctionNode.getLeft();
      IPSQueryNode right = conjunctionNode.getRight();
      Op op = conjunctionNode.getOp();
      IPSQueryNode v = findValue(left, right);

      // The only valid value for use with OR, AND and NOT is a boolean value
      // so non booleans will throw an invalid query exception
      if (v instanceof PSQueryNodeValue)
      {
         PSQueryNodeValue truenode = new PSQueryNodeValue(Boolean.TRUE);
         truenode.setType(PropertyType.BOOLEAN);
         PSQueryNodeValue falsenode = new PSQueryNodeValue(Boolean.FALSE);
         falsenode.setType(PropertyType.BOOLEAN);

         PSQueryNodeValue nv = (PSQueryNodeValue) v;
         if (nv.getType() != PropertyType.BOOLEAN)
         {
            throw new InvalidQueryException(
                  "Cannot compare non-boolean value in conjunction");
         }
         if ((Boolean) nv.getValue())
         {
            // If true we take the remaining node and return that expression
            // for the right for AND and TRUE for OR, return true for the left
            if (left == nv)
            {
               if (op.equals(Op.NOT))
                  return falsenode;
               else
                  return truenode;
            }
            else
            {
               if (op.equals(Op.OR))
                  return truenode;
               else
               {
                  left.accept(this);
                  return left;
               }
            }
         }
         else
         {
            // If false we do different things for and and for or
            if (op.equals(Op.NOT))
            {
               return truenode;
            }
            else if (op.equals(Op.AND))
            {
               return falsenode;
            }
            else if (left == nv)
            {
               if (op.equals(Op.OR))
               {
                  right.accept(this);
                  return right;
               }
               else
               {
                  throw new InvalidQueryException("Invalid op in conjunction "
                        + op);
               }
            }
            else
            {
               if (op.equals(Op.OR))
               {
                  left.accept(this);
                  return left;
               }
               else
               {
                  throw new InvalidQueryException("Invalid op in conjunction "
                        + op);
               }
            }
         }
      }
      else
      {
         m_where.append('(');
         if (op.equals(Op.NOT))
         {
            m_where.append("NOT ");
         }
         else
         {
            left.accept(this);
         }
         if (op.equals(Op.AND))
         {
            m_where.append(" AND ");
         }
         else if (op.equals(Op.OR))
         {
            m_where.append(" OR ");
         }
         right.accept(this);
         m_where.append(')');
         return conjunctionNode;
      }
   }

   /**
    * This method has special handling for simple children. A simple child must
    * appear on the "right" side of the operator, so if the left is a simple
    * child the two sides are reversed, along with an inverse of the operator
    * (for those operators like LT that are not symmetric).
    * <p>
    * If the
    * <q>value</q>
    * is actually an identifier, then the type won't be found in this code; but
    * that doesn't matter since it will be an id on the other side of the
    * comparison.
    * 
    * @param comparisonNode the current comparison node, never <code>null</code>
    * @return for this implementation, the original node is simply returned
    * @throws InvalidQueryException never thrown in this method
    */
   @Override
   public IPSQueryNode visitComparison(PSQueryNodeComparison comparisonNode)
         throws InvalidQueryException
   {
      IPSQueryNode left = comparisonNode.getLeft();
      IPSQueryNode right = comparisonNode.getRight();
      IPSQueryNode.Op op = comparisonNode.getOp();

      if (left instanceof PSQueryNodeIdentifier)
      {
         PSQueryNodeIdentifier id = (PSQueryNodeIdentifier) left;
         PSPair<String, Class> fieldref = PSContentUtils.resolveFieldReference(
               id.getName(), m_type);
         if (m_type.getSimpleChildProperties().contains(fieldref.getFirst()))
         {
            IPSQueryNode t = left;
            left = right;
            right = t;
            op = op.getReverseOp();
         }
      }

      IPSQueryNode id = findProperty(left, right);
      IPSQueryNode value = findValue(left, right);
      if (id != null)
      {
         PSQueryNodeIdentifier idnode = (PSQueryNodeIdentifier) id;
         // Get and inject the type of the property into the value
         if (value != null && value instanceof PSQueryNodeValue)
         {
            PSQueryNodeValue val = (PSQueryNodeValue) value;
            int type = idnode.getType();
            String name = PSContentUtils.externalizeName(idnode.getName());
            if (ms_integerProperties.contains(name))
            {
               type = FORCE_INTEGER;
            }
            val.setType(type);
         }
      }
      else if (value instanceof PSQueryNodeValue)
      {
         // Guess the type from the value
         PSQueryNodeValue val = (PSQueryNodeValue) value;
         try
         {
            Value v = PSValueFactory.createValue(val.getValue());
            val.setType(v.getType());
         }
         catch (ValueFormatException e)
         {
            throw new InvalidQueryException("Could not determine value type", e);
         }
      }

      wrapAndAccept(left, op);
      m_where.append(' ');
      m_where.append(op.getHqlOperator());
      m_where.append(' ');
      wrapAndAccept(right, op);

      return comparisonNode;
   }

   /**
    * Take the passed node, and wrap if appropriate
    * 
    * @param node the node, assumed never <code>null</code>
    * @param op the operator, assumed never <code>null</code>
    * @throws InvalidQueryException if there's a problem with calling accept on
    *            the node
    */
   private void wrapAndAccept(IPSQueryNode node, IPSQueryNode.Op op)
         throws InvalidQueryException
   {
      if (op.getScalarPreprocessingFunction() != null)
      {
         m_where.append(op.getScalarPreprocessingFunction());
         m_where.append('(');
         node.accept(this);
         m_where.append(')');
      }
      else
      {
         node.accept(this);
      }
   }

   /**
    * Check each id in the projection to see that the containing class is
    * included in the inuse list.
    * 
    * @param projection the projection, never <code>null</code>
    */
   public void processProjection(List<PSQueryNodeIdentifier> projection)
   {
      if (projection == null)
      {
         throw new IllegalArgumentException("projection may not be null");
      }
      for (PSQueryNodeIdentifier id : projection)
      {
         String name = id.getName();
         if (name.equals("*"))
         {
            // This means all properties, add all available classes to inuse
            // and return
            for(PSTypeConfiguration.ImplementingClass clazz 
                  : m_type.getImplementingClasses())
            {
               checkAndRegisterInUse(clazz.getImplementingClass());
            }
            return;
         }
         PSPair<String, Class> fieldref = PSContentUtils.resolveFieldReference(
               name, m_type);
         if (fieldref == null)
         {
            continue; // There are valid cases for this
         }
         checkAndRegisterInUse(fieldref.getSecond());
      }
   }

   /**
    * Check each id in the sorter list to see that the containing class is
    * included in the inuse list.
    * 
    * @param sortFields the sortFields, never <code>null</code>
    */
   public void processSortfields(
         List<PSPair<PSQueryNodeIdentifier, SortOrder>> sortFields)
   {
      if (sortFields == null)
      {
         throw new IllegalArgumentException("sortFields may not be null");
      }
      for (PSPair<PSQueryNodeIdentifier, SortOrder> sorter : sortFields)
      {
         PSPair<String, Class> fieldref = PSContentUtils.resolveFieldReference(
               sorter.getFirst().getName(), m_type);
         if (fieldref == null)
         {
            continue; // There are valid cases for this
         }
         checkAndRegisterInUse(fieldref.getSecond());
      }
   }

}
