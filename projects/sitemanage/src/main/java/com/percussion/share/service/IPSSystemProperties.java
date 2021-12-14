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
package com.percussion.share.service;

/**
 * Exposes system properties to sitemanage services
 * 
 * @author JaySeletz
 *
 */
public interface IPSSystemProperties
{
    /**
     * Name of the property defining the max number of pages to catalog for a site. 
     */
    public static final String CATALOG_PAGE_MAX = "catalogPageMax";
    
    /**
     * Name of the property defining the max number of pages to import for a site.
     */
    public static final String IMPORT_PAGE_MAX = "importPageMax";
    
    /**
     * Name of the properties defining the number of seconds to set as the connect and retrieve
     * connection timeouts when importing content.  
     */
    public static final String IMPORT_TIME_OUT = "importTimeOut";
    
    /**
     * Users in one of the roles can only access the accessibility enabled pages. 
     */
    public static final String ACCESSIBILITY_ROLES = "accessibilityRoles";
    
    /**
     * Name of the property defining the number of seconds after which content activity queries should abort and return an error.
     */
    public static final String CONTENT_ACTIVITY_TIME_OUT = "contentActivityTimeOut";
    
    /**
     * Get the value of a known system property
     * 
     * @param name The name of the property, not <code>null<code/> or empty.
     * 
     * @return The property value, may be <code>null<code/> or empty.
     */
    public String getProperty(String name);
}
