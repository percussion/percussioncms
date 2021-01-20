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
package com.percussion.webservices.transformation.converter;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.workflow.data.PSNotification;
import com.percussion.services.workflow.data.PSTransition;
import com.percussion.utils.string.PSStringUtils;
import com.percussion.webservices.transformation.impl.PSTransformerFactory;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.beanutils.Converter;
/**
 * Test the {@link PSTransitionConverter}
 */
public class PSTransitionConverterTest extends TestCase
{
   /**
    * Test the converter
    * 
    * @throws Exception if the test fails.
    */
   public void testConverter() throws Exception
   {
      PSTransformerFactory factory = PSTransformerFactory.getInstance();
      
      // convert server to client object
      Converter converter = factory.getConverter(PSTransition.class);
      
      PSTransition src = new PSTransition();
      src.setAllowAllRoles(true);
      src.setApprovals(3);
      src.setDefaultTransition(true);
      src.setDescription("desc");
      src.setGUID(new PSGuid(PSTypeEnum.WORKFLOW_TRANSITION, 123));
      src.setLabel("label");
      src.setRequiresComment(PSTransition.PSWorkflowCommentEnum.DO_NOT_SHOW);
      src.setTransitionAction("transAction");
      src.setTrigger("trigger");
      List<PSNotification> notificationList = new ArrayList<PSNotification>();
      notificationList.add(new PSNotification());
      notificationList.add(new PSNotification());
      src.setNotifications(notificationList);
      
      com.percussion.webservices.system.PSTransition tgt = 
         (com.percussion.webservices.system.PSTransition) 
         converter.convert(
            com.percussion.webservices.system.PSTransition.class, src);
      
      assertEquals(src.getDescription(), tgt.getDescription());
      assertEquals(src.getLabel(), tgt.getLabel());
      assertEquals(src.getTransitionAction(), tgt.getTransitionAction());
      assertEquals(src.getTrigger(), tgt.getTrigger());
      assertEquals(src.getApprovals(), tgt.getApprovals());
      assertEquals(src.getGUID().longValue(), tgt.getId().longValue());
      assertEquals(src.getNotifications().size(), 
         tgt.getNotifications().length);
      assertEquals(PSStringUtils.toCamelCase(src.getRequiresComment().name()), 
         tgt.getComment().toString());
      assertEquals(src.isAllowAllRoles(), tgt.isAllowAllRoles());
      assertEquals(src.isDefaultTransition(), tgt.isDefaultTransition());
   }
}

