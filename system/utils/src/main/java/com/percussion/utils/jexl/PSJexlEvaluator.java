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
package com.percussion.utils.jexl;

import com.percussion.utils.timing.PSStopwatchStack;
import org.apache.commons.jexl3.JexlException;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The evaluator creates an environment where a number of expressions can be
 * evaluated. Before evaluation the {@link #bind(String, Object)} may be called
 * as needed to bind initial values into the context for use in expressions.
 * After a number of expressions are evaluated using
 * {@link #evaluate(String, IPSScript)} , the results can be extracted by
 * calling {@link #getVars()}.
 * 
 * @author dougrand
 */
@SuppressWarnings(value = "unchecked")
public class PSJexlEvaluator
{
   /**
    * Commons logger for evaluator
    */
   private static final Logger log = LogManager.getLogger(PSJexlEvaluator.class);

   
   /**
    * Regex pattern
    */
   private static final String SQUARE_BRACKET_PATTERN = "[\\x5B\\x5D]";

   /**
    * Regex pattern
    */
   private static final String PERIOD_PATTERN = "\\x2e";

   /**
    * Internal context which is modified after each call to
    * {@link #evaluate(String, JexlExpression)}
    */
   private Map<String,Object> m_vars =  new HashMap<>();

   /**
    * Create an evaluator with no prebound values
    */
   public PSJexlEvaluator()
   {
   }

   /**
    * Ctor for an evaluator with prebound values
    * 
    * @param bindings values to bind, never <code>null</code>
    */
   public PSJexlEvaluator(Map<String, Object> bindings)
   {
      setValues(bindings);
   }

   /**
    * Set initial bound values for the evaluator
    * 
    * @param bindings the bindings, never <code>null</code>
    */
   public void setValues(Map<String, Object> bindings)
   {
      Map<String, Object> m = m_vars;
      for (Map.Entry<String, Object> e : bindings.entrySet())
      {
         m.put(e.getKey(), e.getValue());
      }
   }

   /**
    * Get the context, callers should understand that the returned context is
    * not a copy and that any modifications will be reflected in the evaluator's
    * context
    * 
    * @return get the context, never <code>null</code>
    */
   public Map<String, Object> getVars()
   {
      return m_vars;
   }

   /**
    * Evaluate a jexl expression within the evaluator's context. See
    * {@link #bind(String, Object)} for a description of the binding process.
    * 
    * @param var the variable to bind the result of the expression to, may be
    *           <code>null</code> or empty if no binding is to be done.
    * @param jexlexpression the expression, never <code>null</code>
    * @throws Exception if an error occurs while evaluating the expression
    */
   public void evaluate(String var, IPSScript jexlexpression) throws Exception
   {
      if (StringUtils.isBlank(var))
      {
         evaluate(jexlexpression);
      }
      else
      {
         bind(var, evaluate(jexlexpression));
      }
   }


   /**
    * Get a string value from the evaluator
    * 
    * @param var an expression, never <code>null</code>
    * @param defaultval a default value, may be <code>null</code>
    * @param required if <code>true</code> then a missing value will cause an
    *           exception
    * @return the value, could be <code>null</code> or empty
    * @throws Exception
    */
   public String getStringValue(IPSScript var, String defaultval, boolean required) throws Exception
   {
      if (var == null)
      {
         throw new IllegalArgumentException("var may not be null or empty");
      }
      Object val = evaluate(var);

      if (val == null)
      {
         if (required)
            throw new IllegalStateException("No value for required expression " + var.toString());
    
         return defaultval;
      } 
      
     if (!(val instanceof String))
        throw new IllegalStateException("Value not a string for expression " + var.toString());
   
     return (String) val;
 
   }

   /**
    * Evaluate the given expression within the current context, and return the
    * result.
    * 
    * @param jexlexpression the expression to evaluate
    * @return the value of the evaluated expression
    * @throws Exception
    */
   public Object evaluate(IPSScript jexlexpression) throws Exception
   {
      if (null == jexlexpression)
      {
         throw new IllegalArgumentException("jexlexpression may not be null");
      }
      PSStopwatchStack sws = PSStopwatchStack.getStack();
      sws.start("jexleval");
      try
      {
         return jexlexpression.eval(m_vars);
      }
      catch (JexlException.Variable e)
      {
         if (!e.isUndefined() || !jexlexpression.getSourceText().trim().startsWith("$"))
            throw e;
      }
      finally
      {
         sws.stop();
      }
      return null;
   }

   /**
    * Bind a variable of arbitrary complexity, creating the variable and
    * subcomponents as necessary. If a non-matching component is found, throw an
    * <code>IllegalStateException</code> noting where in the variable we were.
    * 
    * @param var a path that may have one or more components. Each component can
    *           be a name, or a subscripted name such as id[3]. Components are
    *           separated by dots. Each dot denotes a <code>Map</code>.
    *           Rebinding at any level is possible, but it is an error if the
    *           path finds the wrong stored data, i.e. finds a Map, but expects
    *           a List, in traversing the context. Never <code>null</code> and
    *           must not be empty
    * @param value the new value to bind, if <code>null</code> then the
    *           particular variable will be set to <code>null</code>
    * @throws IllegalStateException if the var path is wrong or doesn't match
    *            the actual var
    */
   public void bind(String var, Object value) throws IllegalStateException
   {
      if (StringUtils.isBlank(var))
      {
         throw new IllegalArgumentException("var may not be null or empty");
      }
      if (!var.startsWith("$"))
      {
         throw new IllegalArgumentException("var must start with a dollar sign");
      }
      String components[] = var.split(PERIOD_PATTERN);
      if (components.length == 0)
      {
         components = new String[]
         {var};
      }
      
         Object current = m_vars;
         int index = -1;
         String component = null;

         for (int i = 0; i < components.length; i++)
         {
            boolean last = (components.length - i) == 1;
            Object next = null;
            component = components[i];
            int square = component.indexOf("[");
            index = -1;
            if (square > 0)
            {
               String parts[] = component.split(SQUARE_BRACKET_PATTERN);
               if (parts.length < 2)
                  throw new IllegalStateException("Insufficient parts for array deref");
               component = parts[0];
               try
               {
                  index = Integer.parseInt(parts[1]);
               }
               catch (NumberFormatException nfe)
               {
                  throw new IllegalStateException("Found bad index expression: " + parts[1] + " for component "
                        + parts[0]);
               }
            }
            // If we're at the end, then we let index and component
            // fall through for the assignment code, otherwise we must
            // dereference here
            if (!last)
            {
               next = dereferenceMap(current, component);
               if (index >= 0)
               {
                  if (next != null && !(next instanceof List))
                  {
                     throw new IllegalStateException("While trying to bind variable " + var
                           + " instead of finding a java.util.List, an object of class "
                           + next.getClass().getCanonicalName() + " was found");
                  }
                  List nlist = (List) next;
                  if (nlist == null)
                  {
                     nlist = new ArrayList();
                     ((Map) current).put(component, nlist);
                  }
                  matchLength((ArrayList) nlist, index);
                  next = nlist.get(index);
                  if (next == null)
                  {
                     next = new HashMap();
                     nlist.set(index, next);
                  }
                  current = next;
               }
               else
               {
                  if (next == null)
                  {
                     next = new HashMap();
                     ((Map) current).put(component, next);
                  }
                  current = next;
               }
            }
         }
         // At this point we have either a map, or a map and an index
         if (index >= 0)
         {
            Object val = dereferenceMap(current, component);
            if (val != null && !(val instanceof List))
            {
               throw new IllegalStateException("While trying to bind variable " + var
                     + " instead of finding a java.util.List, an object of class " + val.getClass().getCanonicalName()
                     + " was found");
            }
            ArrayList setval = (ArrayList) val;
            if (setval == null)
            {
               setval = new ArrayList();
               ((Map) current).put(component, setval);
            }
            matchLength(setval, index);
            setval.set(index, value);
         }
         else
         {
            if (!(current instanceof Map))
            {
               throw new IllegalStateException("Did not find a Map when setting " + "component " + component);
            }
            ((Map) current).put(component, value);
         }
      
   }

   /**
    * Method that folds the additional bindings into the map referenced by the
    * variable.
    * 
    * @param var the variable to bind the resulting map to, never
    *           <code>null</code> or empty
    * 
    * @param varexp the expression, must evaluate to a map, never
    *           <code>null</code>
    * @param bindings a set of additional bindings to add in, never
    *           <code>null</code>
    * @throws Exception
    */
   public void add(String var, IPSScript varexp, Map<String, Object> bindings) throws Exception {
      if (StringUtils.isBlank(var)) {
         throw new IllegalArgumentException("var may not be null or empty");
      }
      if (varexp == null) {
         throw new IllegalArgumentException("varexp may not be null");
      }
      if (bindings == null) {
         throw new IllegalArgumentException("bindings may not be null");
      }
      Object value = null;
      try {
         value = evaluate(varexp);
      } catch (JexlException.Variable e)
      {
         // If variable expression does not exist e.g. $rx then we will create a new map;
         if (!e.isUndefined())
            throw e;

      }
      if (value == null)
      {
         value = new HashMap<String, Object>();
      }
      else if (!(value instanceof Map))
      {
         throw new IllegalStateException("var " + var + " did not evaluate to a Map");
      }
      Map varmap = (Map) value;
      for (Map.Entry<String, Object> binding : bindings.entrySet())
      {
         varmap.put(binding.getKey(), binding.getValue());
      }
      bind(var, varmap);
   }

   /**
    * Make sure that the array list is of the right size
    * 
    * @param list list, assumed never <code>null</code>
    * @param index index, assumed zero or positive
    */
   private static void matchLength(ArrayList list, int index)
   {
      if (list.size() > index)
         return;

      list.ensureCapacity(index + 1);
      for (int i = list.size(); i <= index; i++)
      {
         list.add(null);
      }
   }

   /**
    * Dereference map
    * 
    * @param obj object to be dereferences, assumed not <code>null</code>
    * @param component the named component, assumed not <code>null</code> or
    *           empty
    * @return the dereferenced value
    */
   private static Object dereferenceMap(Object obj, String component)
   {
      if (obj instanceof Map)
      {
         obj = ((Map) obj).get(component);
      }
      else
      {
         throw new IllegalStateException("Expected map but found " + obj.getClass() + " at component " + component);
      }
      return obj;
   }

   @Override
   public String toString()
   {
      StringBuilder b = new StringBuilder();

      b.append("#<JexlBindings ");
      b.append(m_vars.toString());
      b.append(">");

      return b.toString();
   }

   /**
    * Output variable bindings for aid in debugging
    * 
    * @return a string representation of the bindings, never <code>null</code>
    *         or empty
    */
   public String bindingsToString()
   {
      return bindingsToString(m_vars, "");
   }

   /**
    * Output variable bindings for aid in debugging
    * 
    * @param data the map data
    * @param prefix the prefix to use when outputting the key, never
    *           <code>null</code> or empty
    * @return a string representation, one variable per line, never
    *         <code>null</code> or empty
    */
   private String bindingsToString(Map<String, Object> data, String prefix)
   {
      if (data == null)
         return "";
      StringBuilder rval = new StringBuilder();

      for (String key : data.keySet())
      {
         if (key.equals("$rx") || key.equals("$tools"))
            continue;
         Object value = data.get(key);
         if (value instanceof Map)
         {
            rval.append(bindingsToString((Map) value, prefix.length() > 0 ? prefix + "." + key : key));
         }
         else
         {
            if (prefix.length() > 0)
            {
               rval.append(prefix);
               rval.append('.');
            }
            rval.append(key);
            rval.append(": ");
            if (value != null)
            {
               rval.append(StringUtils.abbreviate(value.toString(), 60));
            }
            else
            {
               rval.append("[[null]]");
            }
            rval.append('\n');
         }
      }

      return rval.toString();
   }

   /**
    * Create an expression using the expression factory. If this expression has
    * already been parsed, and is in the cache, then the original expression is
    * returned.
    * 
    * Added support for a cacher. We cannot add Ehcache here as this utils
    * package does not have access. Cache manager will be injected on startup
    * 
    * @param expression the expression, never <code>null</code> or empty
    * 
    * @return the expression object, never <code>null</code>, may be shared with
    *         other threads, care must be used if any methods are called that
    *         modify this object
    * @throws Exception
    */
   public static IPSScript createExpression(String expression) throws Exception
   {
       return new PSScript(expression);
   }

  

   /**
    * Create a script using the script factory. If this script has already been
    * parsed, and is in the cache, then the original script is returned.
    * 
    * @param script the script, never <code>null</code> or empty
    * 
    * @return the script object, never <code>null</code>, may be shared with
    *         other threads, care must be used if any methods are called that
    *         modify this object
    * @throws Exception
    */
   public static IPSScript createScript(String script) throws Exception
   {
       return new PSScript(script);
   }

  
   /**
    * Create an expression for use in a class static, does not throw exceptions
    * 
    * @param expression the expression, never <code>null</code> or empty
    * @return the expression, <code>null</code> if there is an error
    */
   public static IPSScript createStaticExpression(String expression)
   {

       
      try
      {
          return new PSScript(expression);
      }
      catch (Exception e)
      {
         log.error(e);
         return null;
      }
   }

   /**
    * Create a script for use in a class static, does not throw exceptions
    * 
    * @param script the script, never <code>null</code> or empty
    * @return the script, <code>null</code> if there is an error
    */
   public static IPSScript createStaticScript(String script)
   {

       try
       {
           return new PSScript(script);
       }
      catch (Exception e)
      {
         log.error(e);
         return null;
      }
   }

}
