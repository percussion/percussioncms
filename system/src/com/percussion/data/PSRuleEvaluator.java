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
package com.percussion.data;

import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSRule;
import com.percussion.error.PSEvaluationException;
import com.percussion.extension.IPSExtension;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionRef;
import com.percussion.server.PSServer;
import com.percussion.util.PSCollection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A new concept known as a PSRule was added when the Content Editors were
 * first created. A rule is either a collection of conditionals or an
 * extension set. Rules can be combined with boolean 'and' and 'or' operators
 * to create complex expressions. Any extension in a rule must return a
 * value that can be interpreted as <code>true</code> or <code>false</code>,
 * Which means that the extension must implement the <code>IPSUdfProcessor</code>
 * interface.
 * This class constructs an appropriate representation of the definitions
 * that can be executed repeatedly at run time using the {@link
 * #isMatch(PSExecutionData) isMatch} method.
 */
public class PSRuleEvaluator extends PSConditionalEvaluator
{
   /*
    * Constructs a rule evaluator, parsing the conditionals and building the
    * appropriate internal representation or creating an executable object
    * from the extension either of which is ready for run-time execution.
    * For extension-based rules, the extensions must return a
    * <code>java.lang.Boolean</code> which will be used to determine if the
    * rule has passed or failed.  If the extension returns anything else, the
    * rule will fail, assuming <code>false</code>.
    *
    * @param rule The rule, if <code>null</code>, this evaluator will always
    *    indicate a match.
    *
    * @throws PSNotFoundException If a specified extension cannot be found.
    *
    * @throws PSExtensionException If any errors occur while preparing a
    *    runnable version of the extension.
    */
   public PSRuleEvaluator( PSRule rule )
         throws PSNotFoundException, PSExtensionException
   {
      super(rule == null ? null : rule.getConditionalRulesCollection());

      if ((rule.getConditionalRulesCollection().size() == 0) &&
         (rule.getExtensionRules().size() > 0))
      {
         /* if the rule has an extensioncallset, use that instead of the
            default behavior (conditional evaluator with no conditions)
          */
         m_extensions = new ArrayList();
         loadExtensions(rule.getExtensionRules(),
            IPSUdfProcessor.class.getName(), m_extensions);
      }
   }

   /**
    * Prepares extension instances for each of the given extension calls
    * whose extension implements the given class or interface, storing
    * each prepared extension in the given collection.
    *
    * @param extCalls A collection of extension calls. Will not be modified.
    * Can be <CODE>null</CODE>, in which case this method will do nothing.
    *
    * @param interfaceName The fully qualified classname of the interface
    * that determines which of the referenced extensions will be prepared.
    * Assumed not <code>null</code>.
    *
    * @param instances The List into which prepared extensions will be put.
    */
   private void loadExtensions(
         PSCollection extCalls,
         String interfaceName,
         List instances
         )
      throws PSNotFoundException, PSExtensionException
   {
      try
      {
         IPSExtensionManager extMgr = PSServer.getExtensionManager(null);
         final int size = (extCalls == null) ? 0 : extCalls.size();
         for (int i = 0; i < size; i++)
         {
            PSExtensionCall call = (PSExtensionCall)extCalls.get(i);

            PSExtensionRef ref = call.getExtensionRef();

            if (extensionImplementsInterface(ref, interfaceName))
            {
              IPSExtension ext = extMgr.prepareExtension(ref,
               null);
              instances.add(PSExtensionRunner.createRunner(call, ext));
            } else
            {
               if (interfaceName == null)
                  throw new IllegalArgumentException(
                     "Interface name must be supplied.");
               else
                  throw new IllegalArgumentException(ref.getExtensionName() +
                  " does not implement "+interfaceName);
            }
         }
      } catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }
   }

   /**
    * Returns <CODE>true</CODE> if and only if the named extension
    * implements the given interface, <CODE>false</CODE> otherwise.
    *
    * @param ref The extension name. Must not be <CODE>null</CODE>.
    *
    * @param interfaceName The fully qualified Java classname of the
    * interface to test for.
    *
    * @return <CODE>true</CODE> if the referenced extension implements
    * <CODE>interfaceName</CODE>.
    */
   public static boolean extensionImplementsInterface(PSExtensionRef ref,
      String interfaceName)
      throws PSExtensionException, PSNotFoundException
   {
      IPSExtensionManager extMgr = PSServer.getExtensionManager(null);

      IPSExtensionDef def = extMgr.getExtensionDef(ref);
      try
      {
         Class toTest = Class.forName(interfaceName);
         for (Iterator i = def.getInterfaces(); i.hasNext();)
         {
            String iface = (String) (i.next());
            Class clazz = Class.forName(iface);
            if (toTest.isAssignableFrom(clazz))
            {
               return true;
            }
         }
      }
      catch (ClassNotFoundException e)
      {
         //This should not happen as these are predefined Interfaces.
         throw new RuntimeException(e.getLocalizedMessage());
      }
      return false;
   }

   /**
    * The extension runners for this rule.
    * Can be <code>null</code> after construction, but not empty
    */
   private ArrayList m_extensions = null;

   /**
    * Checks the conditionals or extensions using the specified data. Tokens
    * representing variables are substituted with their run-time values before
    * performing the check.
    * <p>
    * This evaluator can use the request context hash tables, the input
    * XML document and the result set(s) for processing.
    * <P>
    * Conditionals within the rule are executed
    * according to {@link PSConditionalEvaluator#isMatch(PSExecutionData) isMatch}
    * in the super class.
    * <B>
    * Extension calls within a rule are executed as if they
    * were a list of AND-ed conditions, that is, the first to return
    * <code>false</code> will cause this method to return
    * <code>false</code> immediately.
    * <P>
    *
    * @param data The execution data the evaluator will be applied to.
    *
    * @return  <code>true</code> if the conditional criteria are met or no
    *          criteria are defined for this rule,
    *          <code>false</code> otherwise
    *
    * @throws PSEvaluationException if a data extraction or conversion exception
    *    occurs (for extension-based rules) or if a evaluation exception occurs
    *    in the underlying base class (for conditional-based rules)
    */
   public boolean isMatch( PSExecutionData data )
   {
      if (m_extensions != null)
      {
         try {
            Iterator it = m_extensions.iterator();
            while (it.hasNext())
            {
               PSExtensionRunner udfRunner = (PSExtensionRunner) it.next();
               Object retObj = udfRunner.processUdfCallExtractor(data);
               if (retObj instanceof Boolean)
               {
                  if (!((Boolean) retObj).booleanValue())
                    return false;
               } else
                  return false;
            }
         } catch (PSDataExtractionException extractE)
         {
            throw new PSEvaluationException(extractE.getErrorCode(),
               extractE.getErrorArguments());
         } catch (PSConversionException convE)
         {
            throw new PSEvaluationException(convE.getErrorCode(),
               convE.getErrorArguments());
         }
      } else
      {
         return super.isMatch(data);
      }

      return true;
   }
}
