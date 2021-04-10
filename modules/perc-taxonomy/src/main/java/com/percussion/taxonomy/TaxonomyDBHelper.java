/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

package com.percussion.taxonomy;

import org.hibernate.SessionFactory;

import com.percussion.services.PSBaseServiceLocator;

public class TaxonomyDBHelper {

	public static SessionFactory getSessionFactory(){
		
		return (SessionFactory) PSBaseServiceLocator.getBean("sys_sessionFactory");
		
		// look at all the examples below !!!
		
//		Configuration config = new Configuration();
//
//		// basic config options
//		config.addClass(com.percussion.taxonomy.domain.Attribute.class);
//		config.addClass(com.percussion.taxonomy.domain.Attribute_lang.class);
//		config.addClass(com.percussion.taxonomy.domain.Language.class);
//		config.addClass(com.percussion.taxonomy.domain.Node.class);
//		config.addClass(com.percussion.taxonomy.domain.Taxonomy.class);
//		config.addClass(com.percussion.taxonomy.domain.Value.class);
//		config.addClass(com.percussion.taxonomy.domain.Related_node.class);
//		config.addClass(com.percussion.taxonomy.domain.Relationship_type.class);
//		config.addClass(com.percussion.taxonomy.domain.Node_status.class);
//		config.addClass(com.percussion.taxonomy.domain.Node_editor.class);
//
//		// this is how to set manually
//		// config.setProperty("hibernate.dialect",
//		// "org.hibernate.dialect.Oracle10gDialect");
//		// config.setProperty("hibernate.connection.driver_class",
//		// "oracle.jdbc.OracleDriver");
//		// config.setProperty("hibernate.connection.url",
//		// "jdbc:oracle:thin:@192.168.1.83:1521:XE");
//		// config.setProperty("hibernate.connection.username", "percussion");
//		// config.setProperty("hibernate.connection.password", "password");
//
//		// this is how to use jndi
//		config.setProperty("hibernate.connection.datasource", "java:jdbc/RhythmyxData");
//
//		// this is how we we could create from existing connection
//		// SessionFactory factory = config.buildSessionFactory();
//		// Session session =
//		// factory.openSession(PSConnectionHelper.getDbConnection());
//
//		// or we could have skipped all the above with:
//		//
//		// SessionFactory factory = new
//		// Configuration().configure("taxonomy.hibernate.cfg.xml").buildSessionFactory();
//		// Session session = factory.openSession();
//
//		//return config.buildSessionFactory();
		
		
		

		
		
	}
	
}
