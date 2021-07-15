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
package com.percussion.services.assembly.data;

import com.percussion.services.assembly.IPSTemplateBinding;
import com.percussion.utils.jexl.IPSScript;
import com.percussion.utils.jexl.PSJexlEvaluator;
import com.percussion.utils.xml.IPSXmlSerialization;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a single binding that is part of an assembly template. A binding
 * matches the declaraction of a variable with an expression to calculate its
 * value.
 * 
 * @author dougrand
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSTemplateBinding")
@Table(name = "PSX_TEMPLATE_BINDING")
public class PSTemplateBinding implements IPSTemplateBinding, Cloneable,
   Serializable
{
   /**
    * Serial id identifies versions of serialized data
    */
   private static final long serialVersionUID = 1L;

   @Id
   @GenericGenerator(name = "id", strategy = "com.percussion.data.utils.PSGuidHibernateGenerator")
   @GeneratedValue(generator = "id")
   @Column(name = "BINDING_ID", nullable = false)
   private long m_bindingId;

   @Version
   @Column(name = "VERSION")
   private Integer m_version;


   //@Basic
   //@Column(name = "EXECUTION_ORDER")
   @Transient
   private int m_executionOrder;

   @Basic
   @Column(name = "VARIABLE")
   private String m_variable;

   @Lob
   @Basic(fetch = FetchType.EAGER)
   @Column(name = "EXPRESSION")
   @Fetch(FetchMode. SUBSELECT)
   private String m_expression;

   @Transient
   private transient IPSScript m_jexl = null;

   /**
    * Default ctor
    */
   public PSTemplateBinding() {

   }

   /**
    * Create a new template binding
    * 
    * @param order bindings are executed in order, low to high. Minimum value is
    *           1.
    * @param var the variable to bind to, never <code>null</code> or empty
    * @param exp the expression, never <code>null</code> or empty
    */

   public PSTemplateBinding(int order, String var, String exp) {
      m_executionOrder = order;
      setVariable(var);
      setExpression(exp);
   }

   /**
    * Create a new template binding
    *
    * @param order bindings are executed in order, low to high. Minimum value is
    *           1.
    * @param var the variable to bind to, never <code>null</code> or empty
    * @param exp the expression, never <code>null</code> or empty
    */
   public PSTemplateBinding(String var, String exp) {
      setVariable(var);
      setExpression(exp);
   }


   /**
    * @return Returns the executionOrder.
    */
   public Integer getExecutionOrder()
   {
      return m_executionOrder;
   }

   /**
    * @param executionOrder The executionOrder to set.
    */

   public void setExecutionOrder(Integer executionOrder)
   {
      m_executionOrder = executionOrder;
   }

   /**
    * @return Returns the expression.
    */
   public String getExpression()
   {
      return m_expression;
   }

   /**
    * @param expression The expression to set, should never be <code>null</code>
    *           or empty, but allows these for the purpose of editing
    */
   public void setExpression(String expression)
   {
      m_expression = expression;
   }

   /**
    * @return Returns the variable.
    */
   public String getVariable()
   {
      return m_variable;
   }

   /**
    * @param variable The variable to set, Can be <code>null</code> or empty
    *           if the result returned by the expression should be discarded.
    */
   public void setVariable(String variable)
   {
      m_variable = variable;
   }

   /**
    * @return Returns the bindingId.
    */
   @IPSXmlSerialization(suppress = true)
   public long getBindingId()
   {
      return m_bindingId;
   }

   /**
    * @return Returns the version.
    */
   @IPSXmlSerialization(suppress = true)
   public Integer getVersion()
   {
      return m_version;
   }

   /**
    * Set the object version. The version can only be set once in the life cycle
    * of this object.
    * 
    * @param version the version of the object, must be >= 0.
    */
   public void setVersion(Integer version)
   {
      if (this.m_version != null && version != null)
         throw new IllegalStateException("version can only be initialized once");

      if (version != null && version < 0)
         throw new IllegalArgumentException("version must be >= 0");

      this.m_version = version;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSTemplateBinding)) return false;
      PSTemplateBinding that = (PSTemplateBinding) o;
      return Objects.equals(m_variable, that.m_variable);
   }

   @Override
   public int hashCode() {
      return Objects.hash(m_variable);
   }

   // see base
   @Override
   public Object clone()
   {
      final PSTemplateBinding binding = new PSTemplateBinding(getVariable(), getExpression());
      return binding;
   }

   /**
    * Non-interface method used for xml serialization
    * 
    * @return the id of this object
    */
   public long getId()
   {
      return m_bindingId;
   }

   /**
    * Non-interface method used for xml serialization
    * 
    * @param newid the new id for this object
    */
   public void setId(long newid)
   {
      if (this.m_bindingId >0)
      {
         if (this.m_bindingId!=newid)
            throw new IllegalArgumentException("Cannot change id to a new value");
      }
      else
         if (newid >0)this.m_bindingId = newid;
   }

   public synchronized IPSScript getJexlScript() throws Exception
   {
      if (m_jexl == null)
      {
         m_jexl = PSJexlEvaluator.createScript(m_expression);
      }
      return m_jexl;
   }


    @Override
    public String toString() {
        return "PSTemplateBinding{" +
                "m_bindingId=" + m_bindingId +
                ", m_version=" + m_version +
                ", m_executionOrder=" + m_executionOrder +
                ", m_variable='" + m_variable + '\'' +
                ", m_expression='" + m_expression + '\'' +
                '}';
    }
}
