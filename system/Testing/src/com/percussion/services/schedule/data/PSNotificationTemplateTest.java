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
package com.percussion.services.schedule.data;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.schedule.data.PSNotificationTemplate.ByLabelComparator;
import com.percussion.utils.guid.IPSGuid;

import junit.framework.TestCase;

/**
 * @author Andriy Palamarchuk
 */
public class PSNotificationTemplateTest extends TestCase
{
   public void testSetId()
   {
      final PSNotificationTemplate template = new PSNotificationTemplate();
      template.setId(createTemplateGuid());
      
      // null id
      try
      {
         template.setId(null);
         fail();
      }
      catch (NullPointerException expected) {}
      
      // wrong id guid type
      try
      {
         template.setId(new PSGuid(UUID, PSTypeEnum.SCHEDULED_TASK, HOST_ID));
         fail();
      }
      catch (IllegalArgumentException expected) {}
   }

   public void testSetLabelSubject()
   {
      final PSNotificationTemplate template = new PSNotificationTemplate();
      try
      {
         template.setName("  \t\n\r");
         fail();
      }
      catch (IllegalArgumentException expected) {}

      try
      {
         template.setSubject("  \t\n\r");
         fail();
      }
      catch (IllegalArgumentException expected) {}
}
   
   public void testByLabelComparator()
   {
      final ByLabelComparator c = new ByLabelComparator();
      final PSNotificationTemplate t1 = new PSNotificationTemplate();
      t1.setName(LABEL1);
      assertEquals(0, c.compare(t1, t1));

      final PSNotificationTemplate t2 = new PSNotificationTemplate();
      t2.setName(LABEL2);
      assertEquals(-1, c.compare(t1, t2));
      assertEquals(1, c.compare(t2, t1));
   }

   public void testEqualsHashCode()
   {
      IPSGuid id = createTemplateGuid();
      final PSNotificationTemplate t1 = createTemplate(id, LABEL1, SUBJECT1,
            TEMPLATE1);
      final PSNotificationTemplate t2 = createTemplate(id, LABEL1, SUBJECT1,
            TEMPLATE1);
      final PSNotificationTemplate t3 = createTemplate(id, LABEL2, SUBJECT2,
            TEMPLATE2);

      assertTrue(t1.equals(t2));
      assertTrue(t1.hashCode() == t2.hashCode());
      
      assertFalse(t1.equals(t3));
      assertFalse(t1.hashCode() == t3.hashCode());
   }
   
   /**
    * Creates a template with the given properties.
    * 
    * @param id the ID of the template, assumed not <code>null</code>.
    * @param label the label of the template, assumed not blank.
    * @param subject the subject of the template, assumed not blank.
    * @param template the template, may be blank.
    * 
    * @return the notification template object, never <code>null</code>.
    */
   private PSNotificationTemplate createTemplate(IPSGuid id, 
         String label, String subject, String template)
   {
      PSNotificationTemplate t = new PSNotificationTemplate();
      t.setId(id);
      t.setName(label);
      t.setSubject(subject);
      t.setTemplate(template);
      
      return t;
   }
   
   /**
    * Creates a sample schedule notification template GUID. 
    */
   private PSGuid createTemplateGuid()
   {
      return new PSGuid(
            UUID, PSTypeEnum.SCHEDULE_NOTIFICATION_TEMPLATE, HOST_ID);
   }

   /**
    * Sample GUID UUID.
    */
   private static final int UUID = 123;

   /**
    * Sample GUID host id.
    */
   private static final int HOST_ID = 10;

   /**
    * Sample label.
    */
   private static final String LABEL1 = "Label 1";

   /**
    * Sample label.
    */
   private static final String LABEL2 = "Label 2";

   /**
    * Sample subject.
    */
   private static final String SUBJECT1 = "'Subject 1'";

   /**
    * Sample subject.
    */
   private static final String SUBJECT2 = "'Subject 2'";

   /**
    * Sample tempate.
    */
   private static final String TEMPLATE1 = "template 1";

   /**
    * Sample tempate.
    */
   private static final String TEMPLATE2 = "template 2";

}
