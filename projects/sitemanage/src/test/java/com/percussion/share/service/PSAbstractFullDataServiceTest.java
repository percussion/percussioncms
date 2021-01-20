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
package com.percussion.share.service;

import java.util.List;

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
    public void shouldThrowValidationExceptionOnInvalidFindParameter()
    {
        sut.find(null);
    }
    
    @SuppressWarnings("unchecked")
    @Test(expected=PSParametersValidationException.class)
    public void shouldThrowValidationExceptionOnInvalidLoadParameter()
    {
        sut.load(null);
    }
    
    @SuppressWarnings("unchecked")
    @Test(expected=PSParametersValidationException.class)
    public void shouldThrowValidationExceptionOnInvalidDeleteParameter()
    {
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

