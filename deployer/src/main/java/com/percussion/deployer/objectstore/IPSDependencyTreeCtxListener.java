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
package com.percussion.deployer.objectstore;

/**
 * Implemented by classes that are to be notified of changes to the 
 * {@link PSDependencyTreeContext} in case the ui representing that context
 * needs to be updated.
 */
public interface IPSDependencyTreeCtxListener
{
   /**
    * Informs the listener that the supplied context has been modified and that
    * any nodes represented by that context may need to be updated in the ui.
    * 
    * @param ctx The modified context, never <code>null</code>.
    */
   public void ctxChanged(PSDependencyContext ctx);
   
   /**
    * Determine if the listener is listening for changes on the supplied
    * package.  Used to remove listeners when a pacakge's tree is removed.
    * 
    * @param pkg The pacakge to check, never <code>null</code>.
    * 
    * @return <code>true</code> if the listener is listening for changes to the
    * supplied package's tree, <code>false</code> if not.
    */
   public boolean listensForChanges(PSDeployableElement pkg);
}
