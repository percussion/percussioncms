/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.rx.audit;

import com.percussion.rx.audit.PSDesignObjectAuditor.PSAuditData;
import com.percussion.services.audit.data.PSAuditLogEntry.AuditTypes;
import com.percussion.services.catalog.IPSCatalogIdentifier;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Unit test for the {@link PSDesignObjectAuditor} class.
 */
public class PSDesignObjectAuditorTest extends TestCase
{
   /**
    * Test the logic of the {@link PSDesignObjectAuditor} for a method name and
    * argument.  Does not test actual AOP joinpoint processing or persisting of
    * audit data.
    */
   public void testExtractAuditData()
   {
      PSDesignObjectAuditor auditor = new PSDesignObjectAuditor();
   
      // test non-audited inputs
      assertTrue(auditor.createAuditData(null, null).isEmpty());
      assertTrue(auditor.createAuditData("", null).isEmpty());
      assertTrue(auditor.createAuditData("findSomething", null).isEmpty());
      assertTrue(auditor.createAuditData("deleteSomething", null).isEmpty());
      assertTrue(auditor.createAuditData("deleteSomething", "Test").isEmpty());
      assertTrue(auditor.createAuditData("saveSomething", "Test").isEmpty());
      
      IPSGuid guid = new PSGuid(PSTypeEnum.CONTENT_LIST, 301);
      
      assertTrue(auditor.createAuditData("findSomething", guid).isEmpty());
      
      // test delete with guid and identifier
      Collection<PSAuditData> auditData = auditor.createAuditData(
         "deleteSomething", guid);
      assertFalse(auditData.isEmpty());
      PSAuditData data = auditData.iterator().next();
      assertNotNull(data);
      assertEquals(guid, data.getGuid());
      assertEquals(AuditTypes.DELETE, data.getAction());
      
      PSMockCatalogIdentifier id = new PSMockCatalogIdentifier();
      id.mi_guid = guid;
      id.mi_version = null;
      auditData = auditor.createAuditData("deleteSomething", id);
      assertFalse(auditData.isEmpty());
      data = auditData.iterator().next();
      assertNotNull(data);
      assertEquals(guid, data.getGuid());
      assertEquals(AuditTypes.DELETE, data.getAction());
      
      
      // test update
      id.mi_version = new Integer(2);
      auditData = auditor.createAuditData("saveSomething", id);
      assertFalse(auditData.isEmpty());
      data = auditData.iterator().next();
      assertNotNull(data);
      assertEquals(AuditTypes.SAVE, data.getAction());
      
      // test collections
      List<Object> coll = new ArrayList<Object>();
      
      PSMockCatalogIdentifier id1 = new PSMockCatalogIdentifier();
      PSMockCatalogIdentifier id2 = new PSMockCatalogIdentifier();
      PSMockCatalogIdentifier id3 = new PSMockCatalogIdentifier();
            
      id1.mi_guid = new PSGuid(PSTypeEnum.CONTENT_LIST, 301);
      id1.mi_version = 0;
      id2.mi_guid = new PSGuid(PSTypeEnum.CONTENT_LIST, 302);
      id2.mi_version = 1;
      id3.mi_guid = new PSGuid(PSTypeEnum.CONTENT_LIST, 303);
      id3.mi_version = 0;
      
      coll.add(id1);
      coll.add(id2);
      coll.add(id3);
      
      auditData = auditor.createAuditData("saveSomething", coll);
      assertEquals(coll.size(), auditData.size());
      validateAuditedCollection(coll, auditData);
      
      coll.add("An object that is not audited");
      auditData = auditor.createAuditData("saveSomething", coll);
      assertEquals(coll.size() - 1, auditData.size());
      validateAuditedCollection(coll, auditData);
      
      // test mix for delete
      coll.clear();
      coll.add(id1.mi_guid);
      coll.add(id2.mi_guid);
      id3.mi_version = null;
      coll.add(id3);
      auditData = auditor.createAuditData("deleteSomething", coll);
      assertEquals(coll.size(), auditData.size());
      validateAuditedCollection(coll, auditData);
   }

   /**
    * Validates that each object in the collection that should be audited 
    * has a corresponding and correct result in the audit data.  Assumes the 
    * method call was a "save".
    * 
    * @param coll The collection of objects that may or may not be instances of
    * {@link PSMockCatalogIdentifier} or {@link IPSGuid}, assumed not 
    * <code>null</code>.  
    * @param auditData The resulting audit data to validate, assumed not 
    * <code>null</code>.
    */
   private void validateAuditedCollection(List<Object> coll, 
      Collection<PSAuditData> auditData)
   {
      PSMockCatalogIdentifier id;
      Map<IPSGuid, PSAuditData> resultMap = new HashMap<IPSGuid, PSAuditData>();
      for (PSAuditData result : auditData)
      {
         resultMap.put(result.getGuid(), result);
      }
      
      for (Object object : coll)
      {
         if (object instanceof PSMockCatalogIdentifier)
         {
            id = (PSMockCatalogIdentifier) object;
            PSAuditData result = resultMap.get(id.getGUID());
            assertNotNull(result);
            Integer version = id.getVersion();
            if (version == null)
               assertEquals(AuditTypes.DELETE, result.getAction());
            else
               assertEquals(AuditTypes.SAVE, result.getAction());
         }
         else if (object instanceof IPSGuid)
         {
            PSAuditData result = resultMap.get(object);
            assertNotNull(result);
            assertEquals(AuditTypes.DELETE, result.getAction());
         }
      }
   }

   /**
    * Mock implementation of the {@link IPSCatalogIdentifier} interface that 
    * also provides a <code>getVersion()</code> method.
    */
   public class PSMockCatalogIdentifier implements IPSCatalogIdentifier
   {
      /**
       * The guid of the identifier, may be <code>null</code>.
       */
      private IPSGuid mi_guid;
      
      /**
       * The version of the identifier, may be <code>null</code>, used to 
       * provide a value for the {@link #getVersion()} method.  A <code>null</code>
       * is used by the test to expect a delete.
       */
      private Integer mi_version;
      
      /**
       * Get the guid of this object
       * 
       * @return The guid, may be <code>null</code>.
       */
      public IPSGuid getGUID()
      {
         return mi_guid;
      }
      
      /**
       * Get the Hibernate version of this object.
       * 
       * @return The version, may be <code>null</code>.
       */
      public Integer getVersion()
      {
         return mi_version;
      }
   }
}

