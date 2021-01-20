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
package com.percussion.services.contentmgr.impl.query.nodes;

import com.percussion.services.contentmgr.impl.query.visitors.PSQueryNodeVisitor;

import javax.jcr.query.InvalidQueryException;

import org.apache.commons.lang.StringUtils;

/**
 * Represents a query for the JSR-170 query module. Each implementation of this
 * class is either a single operator or a conjunction of other queries. These
 * values are walked when building the HQL query to actually get instances from
 * the database.
 * <p>
 * The structure is built during the parsing of the JSR170 query. This structure
 * is reused for each query string to be built.
 * 
 * @author dougrand
 */
public interface IPSQueryNode
{
   /**
    * The operator for this query node, each operator has the usual meaning,
    * which depends on the data type being compared. It is an error if the
    * operator is <code>AND</code> or <code>OR</code> and either node is a
    * literal non-boolean value.
    */
   public enum Op {
      /**
       */
      GT(">"),
      /**
       */
      GE(">="),
      /**
       */
      EQ("="),
      /**
       */
      NE("!="),
      /**
       */
      LE("<="),
      /**
       */
      LT("<"),
      /**
       */
      LIKE("like", "upper"),
      /**
       */
      AND("AND"),
      /**
       */
      OR("OR"),
      /**
       */
      NOT("NOT"),
      /**
       */
      FUNC("function"),
      /**
       */
      IN("in");

      /**
       * The operator for HQL, never <code>null</code> or empty
       */
      private String mi_hqlop;

      /**
       * The inverse operator, never <code>null</code> or empty
       */
      private Op mi_revop;
      
      /**
       * If set (currently only for like) then this function is applied
       * to all values being compared with the operator.
       */
      private String mi_scalarPreprocessingFunction = null;

      /**
       * 
       */
      static private volatile Boolean mi_inverseInitialized = false;
      
      private Object mi_lock = new Object();

      /**
       * Ctor for enumeration
       * 
       * @param hqlop the hql operator, never <code>null</code> or empty
       */
      Op(String hqlop) {
         if (StringUtils.isBlank(hqlop))
         {
            throw new IllegalArgumentException("hqlop may not be null or empty");
         }

         mi_hqlop = hqlop;
         mi_revop = this;
      }
 
      /**
       * Ctor for enumeration
       * 
       * @param hqlop the hql operator, never <code>null</code> or empty
       * @param scalarFunc a scalar function to apply to all arguments,
       *    never <code>null</code> or empty for this ctor
       */
      Op(String hqlop, String scalarFunc) {
         this(hqlop);
         if (StringUtils.isBlank(scalarFunc))
         {
            throw new IllegalArgumentException(
                  "scalarFunc may not be null or empty");
         }
         mi_scalarPreprocessingFunction = scalarFunc;
      }

      /**
       * Get the HQL operator
       * 
       * @return the HQL operator, never <code>null</code> or empty
       */
      public String getHqlOperator()
      {
         return mi_hqlop;
      }

      /**
       * Get the HQL operator
       * 
       * @return the HQL operator, never <code>null</code> or empty
       */
      public Op getReverseOp()
      {
         //FB: DL_SYNCHRONIZATION_ON_BOOLEAN NC 1-17-16
         synchronized (mi_lock)
         {
            if (!mi_inverseInitialized)
            {
               mi_inverseInitialized = true;
               GT.setReverseOp(LT);
               GE.setReverseOp(LE);
               LT.setReverseOp(GT);
               LE.setReverseOp(GE);
            }
         }

         return mi_revop;
      }

      /**
       * For some operator (right now only like) there needs to be additional
       * functions applied to the parameters. 
       * @return the scalarPreprocessingFunction, may be <code>null</code> if
       * none defined.
       */
      public String getScalarPreprocessingFunction()
      {
         return mi_scalarPreprocessingFunction;
      }

      /**
       * Set the reverse operator for some operators
       * @param rev the operator
       */
      private void setReverseOp(Op rev)
      {
         mi_revop = rev;
      }
   }

   /**
    * The operator. Each kind of query node supports some operators.
    * 
    * @return the operator, <code>null</code> if the node doesn't represent an
    *         operation
    */
   Op getOp();

   /**
    * Accept a visitor and dispatch to the correct visitor method
    * 
    * @param visitor the visitor, never <code>null</code>
    * @return either the original node or a transformed node, never
    *         <code>null</code>
    * @throws InvalidQueryException
    */
   IPSQueryNode accept(PSQueryNodeVisitor visitor) throws InvalidQueryException;
}
