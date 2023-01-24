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
package com.percussion.share.service;

import java.util.List;

import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.service.exception.PSParametersValidationException;

//import static java.util.Arrays.*;
//import static org.hamcrest.CoreMatchers.*;
//import static org.junit.matchers.JUnitMatchers.*;

/**
 * Scenario description: 
 * @author adamgent, Oct 8, 2009
 */
@RunWith(JMock.class)
public class PSAbstractFullDataServiceTest
{

    Mockery context = new JUnit4Mockery();

    @SuppressWarnings("unchecked")
    PSAbstractFullDataService sut;

    @SuppressWarnings("unchecked")
    IPSGenericDao dao;
    IPSDataItemSummaryService dataItemSummaryService;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception
    {
        
        dao = context.mock(IPSGenericDao.class);
        dataItemSummaryService = context.mock(IPSDataItemSummaryService.class);
        sut = new TestFullDataService(dataItemSummaryService, dao);
    }
    
    @Test(expected=PSParametersValidationException.class)
    public void shouldThrowValidationExceptionOnInvalidFindParameter() throws PSDataServiceException {
        sut.find(null);
    }
    
    @SuppressWarnings("unchecked")
    @Test(expected=PSParametersValidationException.class)
    public void shouldThrowValidationExceptionOnInvalidLoadParameter() throws IPSDataService.DataServiceLoadException, PSValidationException, IPSDataService.DataServiceNotFoundException {
        sut.load(null);
    }
    
    @SuppressWarnings("unchecked")
    @Test(expected=PSParametersValidationException.class)
    public void shouldThrowValidationExceptionOnInvalidDeleteParameter() throws PSDataServiceException {
        sut.delete(null);
    }
    
    
    public static class TestFullDataService extends PSAbstractFullDataService<Object, IPSItemSummary> {

        public TestFullDataService(IPSDataItemSummaryService itemSummaryService, IPSGenericDao<Object, String> dao)
        {
            super(itemSummaryService, dao);
        }

        @Override
        protected IPSItemSummary createSummary(String id)
        {
            throw new UnsupportedOperationException("createSummary is not yet supported");
        }

        public List<IPSItemSummary> findAll()
                throws com.percussion.share.service.IPSDataService.DataServiceLoadException,
                com.percussion.share.service.IPSDataService.DataServiceNotFoundException
        {
            throw new UnsupportedOperationException("findAll is not yet supported");
        }
    }

}

