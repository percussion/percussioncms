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

package com.percussion.server.compare;

import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;

import com.percussion.server.IPSLoadableRequestHandler;
import com.percussion.server.PSRequest;

/**
 * This class implements a loadable request handler interface
 * <code>IPSLoadableRequestHandler</code> for comparing the two contentitems or
 * two different revisions of a contentitem. When the request handler is called
 * with proper contentid, revision and variantids for the tow items, it returns
 * the compared document using the comparision engine DocuComp.
 * The values for DocuComp options can be supplied through the compare.xml file.
 * The process method first gets the assembly pages as strings for
 * the documents that need to be compared and then runs docucomp engines docuRun
 * to compare these strings.
 *
 */
public class PSCompareRequestHandler implements IPSLoadableRequestHandler
{
   // see the IPSLoadableRequestHandler interface for method javadocs
   @SuppressWarnings("rawtypes")
   public void init(Collection requestRoots, InputStream cfgFileIn)
   {
      this.m_requestRoots = requestRoots;
   }

   // see IPSRootedHandler for documentation
   public String getName()
   {
      return HANDLER;
   }

   // see IPSRootedHandler for documentation
   @SuppressWarnings("rawtypes")
   public Iterator getRequestRoots()
   {
      return m_requestRoots.iterator();
   }

   /**
    * Performs the document comparision using the docucomp engine. Creates two
    * <code>PSCompare</code> objects one for each document which needs to be
    * compared. Gets the assembly page for each document and compares them. The
    * result then will be send through response object.
    *
    * @param request the request to process, must not be <code>null</code>.
    */
   public void processRequest(PSRequest request)
   {
      throw new UnsupportedOperationException("Compare is not supported at this time " +
      "and probably never will be.");
   }



   // see interface for method javadoc
   public void shutdown()
   {
      // nothing to do
   }

   /**
    * Name of the subsystem used to dump messages to server console.
    */
   public static final String HANDLER = "Compare";
   
   /**
    * Storage for the request roots, initialized in the <code>init</code>
    * method, never <code>null</code> or empty after. Contains
    * <code>String</code> objects.
    */
   private Collection<?> m_requestRoots = null;

}
