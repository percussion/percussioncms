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
package com.percussion.design.objectstore;

import java.util.Iterator;

/**
 * This class is responsible for resolving macros in content editor dependency
 * parameters.  Macros take the form "$(macroName)".  The following macros are
 * currently supported:
 * <br>
 * <table border=1>
 * <tr><th>macroName</th><th>Resolution</th></tr>
 * <tr><td>fieldName</td><td>replaced by the html field name that this control
 * is associated with</td></tr>
 * </table>
 */
public class PSContentEditorDependencyMacroResolver
{
   /**
    * Walks the values of <code>parameters</code>, mutating any macro names to
    * to the macro's value.
    * <p>
    * A warning will be generated if a immutable <code>IPSReplacementValue
    * </code> contains a macro.
    * 
    * @param mapping the mapping that contains the dependency whose parameters
    * we are modifing; may not be <code>null</code>.
    * @param parameters the parameters from the dependency as <code>
    * IPSParameter</code> objects; may not be <code>null</code>.
    * @throws IllegalArgumentException if either argument is <code>null</code>,
    * or if <code>parameters</code> contains objects other than 
    * <code>IPSParameter</code>.
    * @throws IllegalStateException if a macro is contained in an immutable
    * replacement value
    */ 
   public static void replaceMacroWithValue(PSDisplayMapping mapping,
      Iterator<Object> parameters)
   {
      if (null == mapping)
         throw new IllegalArgumentException("mapping may not be null");
      if (null == parameters)
         throw new IllegalArgumentException("parameters may not be null");
      
      while (parameters.hasNext())
      {
         Object o = parameters.next();
         if (o instanceof IPSParameter)
         {
            IPSParameter parameter = (IPSParameter) o;
            IPSReplacementValue value = parameter.getValue();
            
            String macroName = extractMacroName( value );           
            if (macroName != null)
            {
               // can only resolve macros if the replacement value is mutable
               if (value instanceof IPSMutatableReplacementValue)
               {
                  String macroValue = resolveMacro( mapping, macroName );
                  if (macroValue != null)
                     ((IPSMutatableReplacementValue)value).setValueText( 
                        macroValue );            
               }
               else
               {
                  throw new IllegalStateException(
                     "macro supplied on immutable replacement value" );
               }
            }
         }
         else
            throw new IllegalArgumentException( 
               "Only IPSParameters may be supplied as parameters" );         
      }
   }
   
   /**
    * Walks the <code>templateParameters</code> and <code>instanceParameters
    * </code> in synch, and when the template's parameter's value is found to
    * have a macro, the instance's parameter's value is checked to see if it is
    * equal to the value of that macro, and if so, the instance's parameter's 
    * value is mutated to the macro name.
    * <p>
    * The loop will terminate when either <code>Iterator</code> is exhausted.
    * If the template and the instance differ in the number of parameters, the
    * extra parameters will not be processed.
    * <p>
    * This method assumes that the template's ordering of parameters does not
    * differ from the instance's ordering of parameters.  If this assumption
    * is violated, the macro values will not be restored to macro names.
    * 
    * @param context the content editor component that contains the control
    * with the dependency whose parameters we are modifing; may not be <code>
    * null</code>.
    * @param templateParameters the parameters from the dependency template
    * (obtained from the control metadata) as <code>IPSParameter</code> 
    * objects; may not be <code>null</code>.
    * @param instanceParameters the parameters from the dependency being loaded
    * as <code>IPSParameter</code> objects; may not be <code>null</code>.
    * @throws IllegalArgumentException if any argument is <code>null</code>,
    * or if either <code>Iterator</code> contains objects other than 
    * <code>IPSParameter</code>.
    */ 
   public static void replaceValueWithMacro(PSDisplayMapping context,
      Iterator<Object> templateParameters, Iterator<Object> instanceParameters)
   {
      if (null == context)
         throw new IllegalArgumentException("context may not be null");
      if (null == templateParameters || null == instanceParameters)
         throw new IllegalArgumentException("neither parameters may be null");
      
      while (templateParameters.hasNext() && instanceParameters.hasNext())
      {
         Object templateObj = templateParameters.next();
         Object instanceObj = instanceParameters.next();
         if (templateObj instanceof IPSParameter && 
            instanceObj instanceof IPSParameter)
         {
            IPSParameter templateParameter = (IPSParameter) templateObj;
            IPSParameter instanceParameter = (IPSParameter) instanceObj;
            
            IPSReplacementValue templateValue = templateParameter.getValue();
            String macroName = extractMacroName( templateValue );
            if (macroName != null)
            {
               // template has a macro; does instance have its value?
               String macroValue = resolveMacro( context, macroName );
               IPSReplacementValue instanceValue = instanceParameter.getValue();
               if (instanceValue instanceof IPSMutatableReplacementValue && 
                   macroValue != null )
               {
                  IPSMutatableReplacementValue mutatableValue = 
                     (IPSMutatableReplacementValue) instanceValue;
                  
                  if ( mutatableValue.getValueText().equals( macroValue ))
                     mutatableValue.setValueText( 
                        MACRO_PREFIX + macroName + MACRO_SUFFIX );
               }             
            }
         }
         else
            throw new IllegalArgumentException( 
               "Only IPSParameters may be supplied as parameters" );
      }
   }
   
   /**
    * Searches <code>value</code> for a macro name and returns the name.
    * 
    * @param value object that may contain macro; asssumed not <code>null</code>
    * @return name of macro (without prefix or suffix), or <code>null</code>
    * if no macro found.
    */ 
   private static String extractMacroName(IPSReplacementValue value)
   {
      String macroName = null;
      String valueText = value.getValueText();
      if (valueText != null && valueText.startsWith( MACRO_PREFIX ) && 
         valueText.endsWith( MACRO_SUFFIX ))
         macroName = valueText.substring( MACRO_PREFIX.length(), 
            valueText.length() - MACRO_SUFFIX.length() );
      return macroName;
   }
   
   
   /**
    * Determines a macro's value using the given <code>context</code>.  
    * 
    * @param context component that contains the dependency; assumed not 
    * <code>null</code>.
    * @param macroName name of the macro to resolve (without prefix or suffix);
    * assumed not <code>null</code>.
    * 
    * @return the value of the macro in the given context, or <code>null</code>
    * if the macro could not be resolved.
    */ 
   private static String resolveMacro(PSDisplayMapping context, 
      String macroName)
   {
      String macroValue = null;
      
      if (macroName.equals("fieldName") || 
         macroName.equals("simpleChildFieldSet"))
      {
         macroValue = context.getFieldRef();
      }
      else if (macroName.equals(("simpleChildField")))
      {
         PSDisplayMapper mapper = context.getDisplayMapper();
         if (mapper != null)
         {
            Iterator mappings = mapper.iterator();
            while (mappings.hasNext())
            {
               /*
                * A simple child will always only have one field mapped. That
                * is the one we need to extract.
                */
               PSDisplayMapping mapping = (PSDisplayMapping) mappings.next();
               macroValue = mapping.getFieldRef();
            }
         }
         else
         {
            /*
             * The workbench did already resolve the correct mapping for us.
             */
            macroValue = context.getFieldRef();
         }
      }
      else
      {
         // an unknown macro was specified -- do nothing
      }      
      return macroValue;
   }

   /** Prefix for all macro names */
   private static final String MACRO_PREFIX = "$(";

   /** Suffix for all macro names */
   private static final String MACRO_SUFFIX = ")";
}

