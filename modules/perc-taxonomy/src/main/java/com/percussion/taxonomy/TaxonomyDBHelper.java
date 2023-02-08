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
