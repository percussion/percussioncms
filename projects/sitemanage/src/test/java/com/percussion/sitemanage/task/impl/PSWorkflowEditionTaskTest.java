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
package com.percussion.sitemanage.task.impl;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.itemmanagement.service.IPSWorkflowHelper;
import com.percussion.itemmanagement.service.impl.PSAbstractWorkflowExtension;
import com.percussion.itemmanagement.service.impl.PSAbstractWorkflowExtension.WorkflowItem.AssetType;
import com.percussion.itemmanagement.service.impl.PSAbstractWorkflowExtension.WorkflowItem.ItemStatus;
import com.percussion.services.contentchange.IPSContentChangeService;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.publisher.IPSPubItemStatus;
import com.percussion.services.publisher.IPSSiteItem.Operation;
import com.percussion.services.publisher.IPSSiteItem.Status;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.system.IPSSystemWs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Scenario description: Test the workflow edition task
 * by testing its workflow worker inline class.
 * 
 * @author adamgent, Feb 2, 2010
 */
@RunWith(JMock.class)
public class PSWorkflowEditionTaskTest
{

    Mockery context = new JUnit4Mockery();

    TestWorkflowEditionTask sut;

    IPSSystemWs systemWs;
    IPSWorkflowHelper workflowHelper;
    IPSGuidManager guidManager;
    IPSContentChangeService changeService;
    
    PSAbstractWorkflowExtension.WorkflowItemWorker worker;
    PSAbstractWorkflowExtension.WorkflowItem currentWorkflowItem;
    IPSGuid pageItemId;
    IPSGuid localItemId;
    IPSGuid sharedItemId;
    HashMap<String, String> params = new HashMap<String, String>();
    IPSPubItemStatus item;
    MockComponentSummary itemSummary = new MockComponentSummary();
    
    
    
    @Before
    public void setUp() throws Exception
    {
        sut = new TestWorkflowEditionTask();
        currentWorkflowItem = new PSWorkflowEditionTask.WorkflowItem();
        pageItemId = context.mock(IPSGuid.class, "pageItemId");
        localItemId = context.mock(IPSGuid.class, "localItemId");
        sharedItemId = context.mock(IPSGuid.class, "sharedItemId");
        workflowHelper = context.mock(IPSWorkflowHelper.class);
        changeService = context.mock(IPSContentChangeService.class);
        
        currentWorkflowItem.assetType = AssetType.PAGE;
        currentWorkflowItem.guid = pageItemId;
        currentWorkflowItem.itemSummary = itemSummary;
        currentWorkflowItem.itemSummary.setCurrentLocator(new PSLocator(1,1));
        
        params.put("state","Pending");
        params.put("trigger","forcetolive");
        worker = sut.getWorker(params);
        systemWs = context.mock(IPSSystemWs.class);
        guidManager = context.mock(IPSGuidManager.class);

        sut.setGuidManager(guidManager);
        sut.setSystemWs(systemWs);
        sut.setWorkflowHelper(workflowHelper);
        sut.setContentChangeService(changeService);
        
    }
    
    public class TestWorkflowEditionTask extends PSWorkflowEditionTask {

        @Override
        public WorkflowItemWorker getWorker(Map<String, String> p)
        {
            return new PSWorkflowEditionTask.WorkflowItemWorker(p) {

                @Override
                protected WorkflowItem getWorkflowItem(@SuppressWarnings("unused") IPSPubItemStatus it)
                {
                    return currentWorkflowItem;
                }

                @Override
                protected List<WorkflowItem> getLocalAssetWorkflowItems(@SuppressWarnings("unused") WorkflowItem page)
                {
                    return emptyList();
                }

                @Override
                protected List<WorkflowItem> getSharedAssetWorkflowItems(@SuppressWarnings("unused") WorkflowItem page)
                {
                    return emptyList();
                }
            
            };
        }
        
    }
    
    @Test
    public void shouldProcessValidItem()
    {

        item = new DefaultPubItemStatus();
        currentWorkflowItem.checkedOutUserName = "";
        currentWorkflowItem.state = "Pending";
        

        expectTransition();

        worker.processItem(item);
        assertThat(currentWorkflowItem.status, equalTo(ItemStatus.PROCESSED));
        assertNull(currentWorkflowItem.error);
    }
    
    @Test
    public void shouldProcessValidResource()
    {

        item = new DefaultPubItemStatus();
        currentWorkflowItem.checkedOutUserName = "";
        currentWorkflowItem.state = "Pending";
        currentWorkflowItem.assetType = AssetType.RESOURCE;

        expectTransition();

        worker.processItem(item);
        assertThat(currentWorkflowItem.status, equalTo(ItemStatus.PROCESSED));
        assertNull(currentWorkflowItem.error);
    }

    private void expectTransition()
    {
        context.checking(new Expectations()
        {
            {
                one(systemWs).transitionItems(asList(pageItemId), "forcetolive");
                one(workflowHelper).transitionRelatedNavigationItem(with(any(IPSGuid.class)), with(not((String)null)));
            }
        });
    }
    
    @Test
    public void shouldNotProcessItemIfCheckedOut()
    {

        item = new DefaultPubItemStatus();
        currentWorkflowItem.checkedOutUserName = "CheckedOutByJoe";
        currentWorkflowItem.state = "Pending";

        //no expectations

        worker.processItem(item);
        assertThat(currentWorkflowItem.status, equalTo(ItemStatus.FAILED));
    }
    
    @Test
    public void shouldNotProcessItemIfAlreadyWorkflowed()
    {

        item = new DefaultPubItemStatus();
        currentWorkflowItem.checkedOutUserName = "";
        currentWorkflowItem.state = "Pending";
        expectTransition();

        //no expectations

        worker.processItem(item);
        assertThat(currentWorkflowItem.status, equalTo(ItemStatus.PROCESSED));
        //Try processing again.
        worker.processItem(item);
        assertThat(currentWorkflowItem.status, equalTo(ItemStatus.IGNORED));
        
    }

    @Test
    public void shouldNotProcessItemIfInWrongState()
    {

        item = new DefaultPubItemStatus();
        currentWorkflowItem.checkedOutUserName = null;
        //Bad state
        currentWorkflowItem.state = "Blah";

        //no expectations

        worker.processItem(item);
    }
    
    
    public static class DefaultPubItemStatus extends MockPubItemStatus {

        @Override
        public Status getStatus()
        {
            return Status.SUCCESS;
        }

        @Override
        public Operation getOperation()
        {
            return Operation.PUBLISH;
        }

        @Override
        public int getContentId()
        {
            return 1;
        }

        @Override
        public int getRevisionId()
        {
            return 1;
        }
    
    }
    
    @SuppressWarnings("serial")
    public static class MockComponentSummary extends PSComponentSummary {

        @Override
        public int getContentStateId()
        {
            return 1;
        }
    
        
    }

  
}
