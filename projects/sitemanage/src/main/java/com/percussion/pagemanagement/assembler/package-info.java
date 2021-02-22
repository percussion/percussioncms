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
/**
 * The rendering system renders pages and templates which 
 * are composed of widgets.
 * 
 * <h2>Widgets</h2>
 * <p>
 * The main extension point in the rendering system is through the creation of a widget.
 * <p>
 * A widget is defined through an XML file (see {@link com.percussion.pagemanagement.data.PSWidgetDefinition} ).
 * The XML file that defines the widget is stored in the directory: <code>ROOT/rxconfig/Widgets</code>
 * and the file name of the XML file is the definition id of the widget 
 * ( {@link com.percussion.pagemanagement.data.PSWidgetDefinition#getId()} ). 
 * <p>
 * A widget file can be added at runtime and any changes made during runtime are reloaded much like JSPs.
 * 
 * <h3>Widget Schema</h3>
 * 
 * 
 */
package com.percussion.pagemanagement.assembler;