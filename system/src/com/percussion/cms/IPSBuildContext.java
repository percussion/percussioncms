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
package com.percussion.cms;


import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class is used to share information between a sequence of steps that
 * are executed polymorphically. The context is used while building a result
 * document for a query request. Typically, the build step should request the
 * document from the context and use it to create the nodes it needs while
 * building the DisplayField element for the output document. It should then
 * add the field using one of the other methods in the interface. It is
 * possible that a build step will not add a node at all.
 *
 * @see IPSBuildStep
 */
public interface IPSBuildContext
{
   /**
    * To create elements, a document is needed.
    *
    * @return The document object to which all fields added to this object
    *    will eventually be added. This object should be treated read-only.
    */
   public Document getResultDocument();

   /**
    * If a DisplayField element created by a build step should be hidden, it
    * should be added using this method after the node is created.
    *
    * @param dispNode A DisplayField element that should be visible when
    *    the editor is rendered. Must not be <code>null</code>.
    *
    * @param controlName The name of the control that should render this node.
    *    Must not be empty.
    *
    * @throws IllegalArgumentException if dispNode is <code>null</code> or
    *    controlName is <code>null</code> or empty.
    */
   public void addHiddenField( Element dispNode, String controlName );



   /**
    * If a DisplayField element created by a build step should be visible when
    * rendered, it should be added using this method after the node is created.
    *
    * @param dispNode A DisplayField element that should be visible when
    *    the editor is rendered. Must not be <code>null</code>.
    *
    * @param controlName The name of the control that should render this node.
    *    Must not be empty.
    *
    * @throws IllegalArgumentException if dispNode is <code>null</code> or
    *    controlName is <code>null</code> or empty.
    */
   public void addVisibleField( Element dispNode, String controlName );
}


