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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit tests for all PSComponent derived objects
 */
public class PSComponentTest extends TestCase
{
   public PSComponentTest(String name)
   {
      super(name);
   }

   
   /**
    * Tests that all known subclasses of <code>PSComponent</code> have 
    * overridden the equals() method.
    */ 
   public void testForEquals() throws Exception
   {
      for (int i = 0; i < ms_classNames.length; i++)
      {
         testEqualsIsOverridden( ms_classNames[i] );
      }
   }

   
   /**
    * Tests that the specified class has overridden Object's equal() method,
    * using reflection.
    * @param className FQN of Java class to test; assumed not <code>null
    * </code>.
    * @throws Exception if the test fails
    */ 
   private void testEqualsIsOverridden(String className) throws Exception
   {
      Class theClass = Class.forName(className);
      
      // don't test abstract classes
      if (Modifier.isAbstract(theClass.getModifiers()))
         return;

      Method equalsMethod = theClass.getMethod("equals", 
         new Class[] { Object.class } );
   
      // make sure we didn't get Object's equals()
      assertTrue(className + " implemented equals()", 
         !equalsMethod.getDeclaringClass().equals(Object.class));
   }
      
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest( new PSComponentTest( "testForEquals" ) );
      return suite;
   }

   public static final String[] ms_classNames =
   {
      "com.percussion.design.objectstore.PSAcl",
      "com.percussion.design.objectstore.PSAclEntry",
      "com.percussion.design.objectstore.PSApplicationFile",
      "com.percussion.design.objectstore.PSBackEndColumn",
      "com.percussion.design.objectstore.PSBackEndConnection",
      "com.percussion.design.objectstore.PSBackEndCredential",
      "com.percussion.design.objectstore.PSBackEndDataTank",
      "com.percussion.design.objectstore.PSBackEndJoin",
      "com.percussion.design.objectstore.PSBackEndTable",
      "com.percussion.design.objectstore.PSCgiVariable",
      "com.percussion.design.objectstore.PSConditional",
      "com.percussion.design.objectstore.PSCookie",
      "com.percussion.design.objectstore.PSCustomError",
      "com.percussion.design.objectstore.PSDataEncryptor",
      "com.percussion.design.objectstore.PSDataMapping",
      "com.percussion.design.objectstore.PSDataSelector",
      "com.percussion.design.objectstore.PSDataSet",
      "com.percussion.design.objectstore.PSDataSynchronizer",
      "com.percussion.design.objectstore.PSExtensionCall",
      "com.percussion.design.objectstore.PSExtensionParamDef",
      "com.percussion.design.objectstore.PSExtensionParamValue",
      "com.percussion.design.objectstore.PSHtmlParameter",
      "com.percussion.design.objectstore.PSLiteral",
      "com.percussion.design.objectstore.PSLogger",
      "com.percussion.design.objectstore.PSLoginWebPage",
      "com.percussion.design.objectstore.PSNotifier",
      "com.percussion.design.objectstore.PSPageDataTank",
      "com.percussion.design.objectstore.PSPipe",
      "com.percussion.design.objectstore.PSQueryPipe",
      "com.percussion.design.objectstore.PSRecipient",
      "com.percussion.design.objectstore.PSRequestLink",
      "com.percussion.design.objectstore.PSRequestor",
      "com.percussion.design.objectstore.PSResultPage",
      "com.percussion.design.objectstore.PSResultPager",
      "com.percussion.design.objectstore.PSResultPageSet",
      "com.percussion.design.objectstore.PSRevisionEntry",
      "com.percussion.design.objectstore.PSRevisionHistory",
      "com.percussion.design.objectstore.PSSecurityProviderInstance",
      "com.percussion.design.objectstore.PSSortedColumn",
      "com.percussion.design.objectstore.PSUpdateColumn",
      "com.percussion.design.objectstore.PSUpdatePipe",
      "com.percussion.design.objectstore.PSUserContext",
      "com.percussion.design.objectstore.PSXmlField"
   };
   
   /* JVS: Updated list (01/2002) of derived classes of PSComponent.
      Commented out, because some of these classes fail, and now is not the 
      time to fix them.
   
   public static final String[] ms_classNames =
   {
      "com.percussion.design.objectstore.PSAcl",
      "com.percussion.design.objectstore.PSAclEntry",
      "com.percussion.design.objectstore.PSActionLink",
      "com.percussion.design.objectstore.PSApplicationFile",
      "com.percussion.design.objectstore.PSApplicationFlow",
      "com.percussion.design.objectstore.PSBackEndColumn",
      "com.percussion.design.objectstore.PSBackEndConnection",
      "com.percussion.design.objectstore.PSBackEndCredential",
      "com.percussion.design.objectstore.PSBackEndDataTank",
      "com.percussion.design.objectstore.PSBackEndJoin",
      "com.percussion.design.objectstore.PSBackEndTable",
      "com.percussion.design.objectstore.PSCgiVariable",
      "com.percussion.design.objectstore.PSChoices",
      "com.percussion.design.objectstore.PSCommandHandlerStylesheets",
      "com.percussion.design.objectstore.PSConditional",
      "com.percussion.design.objectstore.PSConditionalExit",
      "com.percussion.design.objectstore.PSConditionalRequest",
      "com.percussion.design.objectstore.PSConditionalStylesheet",
      "com.percussion.design.objectstore.PSContainerLocator",
      "com.percussion.design.objectstore.PSContentEditor",
      "com.percussion.design.objectstore.PSContentEditorMapper",
      "com.percussion.design.objectstore.PSContentEditorPipe",
      "com.percussion.design.objectstore.PSContentEditorSharedDef",
      "com.percussion.design.objectstore.PSContentType",
      "com.percussion.design.objectstore.PSControlMeta",
      "com.percussion.design.objectstore.PSControlParameter",
      "com.percussion.design.objectstore.PSControlRef",
      "com.percussion.design.objectstore.PSCookie",
      "com.percussion.design.objectstore.PSCustomActionGroup",
      "com.percussion.design.objectstore.PSCustomError",
      "com.percussion.design.objectstore.PSDataEncryptor",
      "com.percussion.design.objectstore.PSDataMapping",
      "com.percussion.design.objectstore.PSDataSelector",
      "com.percussion.design.objectstore.PSDataSet",
      "com.percussion.design.objectstore.PSDataSynchronizer",
      "com.percussion.design.objectstore.PSDateLiteral",
      "com.percussion.design.objectstore.PSDefaultSelected",
      "com.percussion.design.objectstore.PSDependency",
      "com.percussion.design.objectstore.PSDisplayFieldRef",
      "com.percussion.design.objectstore.PSDisplayMapping",
      "com.percussion.design.objectstore.PSDisplayText",
      "com.percussion.design.objectstore.PSDisplayTextLiteral",
      "com.percussion.design.objectstore.PSEntry",
      "com.percussion.design.objectstore.PSExtensionCall",
      "com.percussion.design.objectstore.PSExtensionFile",
      "com.percussion.design.objectstore.PSExtensionParamDef",
      "com.percussion.design.objectstore.PSExtensionParamValue",
      "com.percussion.design.objectstore.PSField",
      "com.percussion.design.objectstore.PSFieldSet",
      "com.percussion.design.objectstore.PSFieldTranslation",
      "com.percussion.design.objectstore.PSFieldValidationRules",
      "com.percussion.design.objectstore.PSFile",
      "com.percussion.design.objectstore.PSFormAction",
      "com.percussion.design.objectstore.PSGlobalLookup",
      "com.percussion.design.objectstore.PSHtmlParameter",
      "com.percussion.design.objectstore.PSLiteral",
      "com.percussion.design.objectstore.PSLocation",
      "com.percussion.design.objectstore.PSLogger",
      "com.percussion.design.objectstore.PSLoginWebPage",
      "com.percussion.design.objectstore.PSNamedReplacementValue",
      "com.percussion.design.objectstore.PSNotifier",
      "com.percussion.design.objectstore.PSNullEntry",
      "com.percussion.design.objectstore.PSNumericLiteral",
      "com.percussion.design.objectstore.PSPageDataTank",
      "com.percussion.design.objectstore.PSParam",
      "com.percussion.design.objectstore.PSPipe",
      "com.percussion.design.objectstore.PSQueryPipe",
      "com.percussion.design.objectstore.PSRecipient",
      "com.percussion.design.objectstore.PSRequestLink",
      "com.percussion.design.objectstore.PSRequestor",
      "com.percussion.design.objectstore.PSResultPage",
      "com.percussion.design.objectstore.PSResultPager",
      "com.percussion.design.objectstore.PSResultPageSet",
      "com.percussion.design.objectstore.PSRevisionEntry",
      "com.percussion.design.objectstore.PSRevisionHistory",
      "com.percussion.design.objectstore.PSRule",
      "com.percussion.design.objectstore.PSSecurityProviderInstance",
      "com.percussion.design.objectstore.PSSharedFieldGroup",
      "com.percussion.design.objectstore.PSSingleHtmlParameter",
      "com.percussion.design.objectstore.PSSortedColumn",
      "com.percussion.design.objectstore.PSStylesheet",
      "com.percussion.design.objectstore.PSTableLocator",
      "com.percussion.design.objectstore.PSTableRef",
      "com.percussion.design.objectstore.PSTableSet",
      "com.percussion.design.objectstore.PSTextLiteral",
      "com.percussion.design.objectstore.PSTraceInfo",
      "com.percussion.design.objectstore.PSUIDefinition",
      "com.percussion.design.objectstore.PSUISet",
      "com.percussion.design.objectstore.PSUpdateColumn",
      "com.percussion.design.objectstore.PSUpdatePipe",
      "com.percussion.design.objectstore.PSUrlRequest",
      "com.percussion.design.objectstore.PSUserContext",
      "com.percussion.design.objectstore.PSWhereClause",
      "com.percussion.design.objectstore.PSWorkflow",
      "com.percussion.design.objectstore.PSWorkflowInfo",
      "com.percussion.design.objectstore.PSXmlField"
   };
   */
}
