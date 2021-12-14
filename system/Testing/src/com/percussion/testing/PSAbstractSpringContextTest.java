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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.testing;

import com.percussion.services.PSBaseServiceLocator;
import com.percussion.utils.annotations.IgnoreInWebAppSpringContext;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.utils.testing.SpringContextTest;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.context.WebApplicationContext;

import javax.naming.Context;
import javax.naming.InitialContext;

//TODO: The spring tests need moved to a new jar.

@Category({IntegrationTest.class, SpringContextTest.class})
//@WebAppConfiguration("file:../modules/perc-distribution-tree/target/distribution/jetty/base/webapps/Rhythmyx/WEB-INF")
@ContextConfiguration(classes = {PSSpringContextTestConfig.class})
@IgnoreInWebAppSpringContext
public class PSAbstractSpringContextTest {

    @Autowired
    protected WebApplicationContext wac;

    @Autowired
    protected ConfigurableApplicationContext ctx;

    @Before
    public  void setContext(){
        PSBaseServiceLocator.setCtx(ctx);
    }
    @BeforeClass
    public static void setupJndi() throws Exception {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.osjava.sj.memory.MemoryContextFactory");
        System.setProperty("org.osjava.sj.jndi.shared", "true");
        InitialContext ic = new InitialContext();

        ic.createSubcontext("java:/comp/env/jdbc");
        ic.createSubcontext("java:comp/env/jms");
        ic.createSubcontext("java:comp/env/queue");
      //  ic.bind("java:comp/env/jms/ConnectionFactory", PSMockJmsConnectionFactoryHelper.getMs_mockFactory());
    }
}
