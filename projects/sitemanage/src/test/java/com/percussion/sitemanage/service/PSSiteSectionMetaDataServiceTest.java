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
package com.percussion.sitemanage.service;

import static java.util.Arrays.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.dao.MockItemSummary;
import com.percussion.share.dao.IPSFolderHelper.PathTarget;
import com.percussion.share.dao.impl.PSFolderHelper;
import com.percussion.share.data.IPSFolderPath;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.sitemanage.service.impl.PSSiteSectionMetaDataService;
import com.percussion.webservices.content.IPSContentWs;

//import static java.util.Arrays.*;
//import static org.hamcrest.CoreMatchers.*;
//import static org.junit.matchers.JUnitMatchers.*;

/**
 * Scenario description: Expect the site section meta data service to use its
 * templateService the {@link IPSFolderHelper}
 * 
 * @author adamgent, October 13, 2009
 */
@RunWith(JMock.class)
public class PSSiteSectionMetaDataServiceTest
{

    Mockery context = new JUnit4Mockery();

    PSSiteSectionMetaDataService sut;

    PSFolderHelper fh = new PSFolderHelper(null, null, null, null, null, null, null, null, null,null);

    IPSFolderHelper collaborator;
    IPSContentWs contentWs;

    @Before
    public void setUp() throws Exception
    {

        collaborator = context.mock(IPSFolderHelper.class);
        contentWs = context.mock(IPSContentWs.class);
        sut = new PSSiteSectionMetaDataService(collaborator, contentWs);
        // TODO !!! wire the templateService to the SUT ie call the setter !!!

    }

    @Test
    public void shouldAddItem() throws Exception
    {

        expectPathSeparator();
        expectConcat("//Sites/Test", ".system", "Templates");
        expectAddItem("//Sites/Test/.system/Templates", "1");

        /*
         * When:
         */

        sut.addItem(fp("//Sites/Test"), "Templates", "1");
    }

    @Test
    public void shouldRemoveItem() throws Exception
    {

        expectPathSeparator();
        expectConcat("//Sites/Test", ".system", "Templates");
        expectRemoveItem("//Sites/Test/.system/Templates", "1");

        /*
         * When:
         */

        sut.removeItem(fp("//Sites/Test"), "Templates", "1");
    }

    @Test
    public void shouldFindSections() throws Exception
    {

        String s1 = "//Sites/Test1/.system/Templates";
        String s2 = "//Sites/Test2/.system/Templates";
        String s3 = "//Folders/$System$/Templates";
        expectPathSeparator();
        expectConcat(".system", "Templates");
        expectFindPaths("1", asList(s1, s2, s3));

        /*
         * When:
         */

        List<IPSFolderPath> paths = sut.findSections("Templates", "1");
        assertEquals("Two paths", 2, paths.size());
        assertEquals("//Sites/Test1", paths.get(0).getFolderPath());
    }

    @Test
    public void shouldFindItems() throws Exception
    {

        expectPathSeparator();
        expectConcat("//Sites/Test1", ".system", "Templates");
        expectFindItems("//Sites/Test1/.system/Templates", "1", "2");
        expectDoesFolderExist("//Sites/Test1/.system/Templates", true);

        /*
         * When:
         */

        List<IPSItemSummary> items = sut.findItems(fp("//Sites/Test1"), "Templates");
        assertEquals("Two Items", 2, items.size());
    }

    private IPSFolderPath fp(String path)
    {
        return new MockFolderPath(path);
    }

    public static class MockFolderPath implements IPSFolderPath
    {

        private String folderPath;

        public MockFolderPath(String folderPath)
        {
            super();
            this.folderPath = folderPath;
        }

        public String getFolderPath()
        {
            return folderPath;
        }

        public void setFolderPath(String folderPath)
        {
            this.folderPath = folderPath;
        }

    }

    public void expectDoesFolderExist(final String path, final boolean exist) throws Exception
    {
        context.checking(new Expectations()
        {
            {
                PathTarget p;
                if (exist)
                {
                    p = new PathTarget(path, new MockItemSummary()
                    {

                        @Override
                        public boolean isFolder()
                        {
                            return true;
                        }

                    });
                }
                else
                {
                    p = new PathTarget(path);
                }
                one(collaborator).pathTarget(path);
                will(returnValue(p));
            }
        });
    }

    public void expectFindItems(final String path, String... items) throws Exception
    {
        final List<IPSItemSummary> sums = new ArrayList<IPSItemSummary>();
        for (String item : items)
        {
            sums.add(context.mock(IPSItemSummary.class, IPSItemSummary.class.getSimpleName() + item));
        }
        context.checking(new Expectations()
        {
            {
                one(collaborator).findItems(path);
                will(returnValue(sums));
            }
        });
    }

    public void expectParentPath(final String path) throws Exception
    {
        context.checking(new Expectations()
        {
            {
                one(collaborator).parentPath(path);
                will(returnValue(fh.parentPath(path)));
            }
        });
    }

    public void expectFindPaths(final String id, final List<String> paths) throws Exception
    {
        context.checking(new Expectations()
        {
            {
                one(collaborator).findPaths(id);
                will(returnValue(paths));
            }
        });

    }

    public void expectRemoveItem(final String path, final String id) throws Exception
    {
        context.checking(new Expectations()
        {
            {
                one(collaborator).removeItem(path, id, false);
            }
        });

    }

    public void expectAddItem(final String path, final String id) throws Exception
    {
        context.checking(new Expectations()
        {
            {
                one(collaborator).addItem(path, id);
            }
        });

    }

    public void expectPathSeparator()
    {
        context.checking(new Expectations()
        {
            {
                allowing(collaborator).pathSeparator();
                will(returnValue(fh.pathSeparator()));
            }
        });
    }

    public void expectConcat(final String path, final String... paths)
    {

        context.checking(new Expectations()
        {
            {
                allowing(collaborator).concatPath(path, paths);
                will(returnValue(fh.concatPath(path, paths)));
            }
        });

    }

}
