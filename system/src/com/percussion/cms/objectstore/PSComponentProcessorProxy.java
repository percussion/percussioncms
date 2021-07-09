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
package com.percussion.cms.objectstore;

import com.percussion.cms.PSCmsException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

/**
 * This class presents the interfaces supported by the processor classes, but
 * doesn't actually perform any work. It has a configuration file that is read
 * to find the appropriate processor given a component type. That processor is
 * instantiated and is used to perform the heavy lifting. Once the processor
 * type is known, the config is searched for associated properties for the
 * supplied component and procesor. These properties are made available to the
 * real processor. The default processors support web
 * services and direct server access both locally and remotely.
 * <p>See the config schema, sys_CmsProcessorConfig.xsd for details on the
 * format of the configuration file.
 * <p>To provide an idea of how it will work, let's look at what happens when an
 * operation is performed on a remote client using web services, say creating a
 * new instance of some type.
 * <ol>
 *    <li>A new instance of the object would be created locally using the
 *       standard technique (e.g., new PSFolder("foo")).</li>
 *    <li>The object would be manipulated until it reached the desired state.
 *       </li>
 *    <li>The object would then be passed to the {@link #save(IPSDbComponent[])
 *       save} method of this class. </li>
 *    <li>The proxy would look up the real processor and pass it the
 *       properties from the config file, which would include the name of the
 *       web service to use.</li>
 *    <li>The real processor would call toXml on the component and pass this
 *       element as the document when making the web service request. This
 *       request would be received by the web services handler at the server.
 *       </li>
 *    <li>The handler would re-create the original object, passing the supplied
 *       xml to the component's fromXml method.</li>
 *    <li>This object would then be passed to the local proxy processor's save
 *       method.</li>
 *    <li>The local proxy would look up the real processor and pass the
 *       config information. By default this processor will generate a new key
 *       and call toDbXml on the component and pass the generated fragment to
 *       the resource defined in the processor's config</li>
 *    <li>After this is successful, setPersisted is called on the component.
 *       </li>
 *    <li>Success is returned to the web service caller.</li>
 *    <li>The WS processor will call setPersisted on the remote object.</li>
 * </ol>
 * When the methods of a processor are called, a component type must be supplied
 * (or it is obtained from the supplied component). In general, this should be
 * the base name of the component (but for the relationship processor methods,
 * it is an arbitrary name, usually based on the type of relationship being
 * processed). This name is used to find the 'real' processor class name and a
 * set of properties appropriate for this type of component. Most components
 * will use the generic processor, which performs an internal request to a
 * resource as described above.
 *
 * @author Paul Howard
 * @version 1.0
 */
public class PSComponentProcessorProxy  extends PSProcessorProxy
   implements IPSComponentProcessor
{
   // TODO Take a closer look for problems in a multi-thread environment

   /**
    * Creates a proxy for a specific type of processor. Simply delegates to the
    * base  class.
    *
    * @param type  The type of processor for which this class is acting as a
    *    proxy. See {@link PSProcessorProxy version of the constructor} for more
    *    details
    *
    * @param ctx A context object appropriate for the processor type,
    * may be <code>null</code> if the processor does not require one.
    *
    * @throws PSCmsException If the xml document is not well-formed and
    *    conformant to its schema.
    */
   public PSComponentProcessorProxy(String type, Object ctx)
      throws PSCmsException
   {
      super(type, ctx);
   }

   //see interface for description
   public PSSaveResults save(IPSDbComponent [] components)
      throws PSCmsException
   {
      if (null == components)
         throw new IllegalArgumentException("Supplied array cannot be null.");

      Map procGroups = createComponentProcessorGroups(components);

      PSProcessingStatistics totals = new PSProcessingStatistics(0,0);
      List resultComps = new ArrayList();
      Iterator iter = procGroups.keySet().iterator();
      while (iter.hasNext())
      {
         PSProcessorCommon proc = (PSProcessorCommon) iter.next();
         Collection coll = (Collection) procGroups.get(proc);
         IPSDbComponent[] comps = new IPSDbComponent[coll.size()];
         coll.toArray(comps);
         PSSaveResults results = proc.save(comps);
         PSProcessingStatistics stats = results.getResultStats();
         totals = new PSProcessingStatistics(
               totals.getInsertedCount() + stats.getInsertedCount(),
               totals.getUpdatedCount() + stats.getUpdatedCount(),
               totals.getDeletedCount() + stats.getDeletedCount(),
               totals.getSkippedCount() + stats.getSkippedCount(),
               totals.getErroredCount() + stats.getErroredCount());
         resultComps.addAll(Arrays.asList(results.getResults()));
      }
      IPSDbComponent[] res = new IPSDbComponent[resultComps.size()];
      resultComps.toArray(res);
      return new PSSaveResults(res, totals);
   }

   //see interface for description
   public Element [] load(String componentType, PSKey [] locators)
      throws PSCmsException
   {
      IPSComponentProcessor proc = getProcessor(componentType);
      return proc.load(componentType, locators);
   }


   //see interface for description
   public int delete(String componentType, PSKey [] locators)
      throws PSCmsException
   {
      IPSComponentProcessor proc = getProcessor(componentType);
      return proc.delete(componentType, locators);
   }


   //see interface for description
   public int delete(IPSDbComponent[] components)
      throws PSCmsException
   {
      if (null == components || null == components[0])
      {
         throw new IllegalArgumentException(
               "Component array and members cannot be null.");
      }

      Map procGroups = createComponentProcessorGroups(components);

      int total = 0;
      Iterator iter = procGroups.keySet().iterator();
      while (iter.hasNext())
      {
         PSProcessorCommon proc = (PSProcessorCommon) iter.next();
         Collection coll = (Collection) procGroups.get(proc);
         IPSDbComponent[] comps = new IPSDbComponent[coll.size()];
         coll.toArray(comps);
         total += proc.delete(comps);
      }
      return total;
   }


   //see interface for description
   public int delete(IPSDbComponent comp)
      throws PSCmsException
   {
      return delete(new IPSDbComponent[] {comp});
   }

   //see interface for description
   public void reorder(int insertAt, List comp)
      throws PSCmsException
   {

   }
   /**
    * Helper method that overrides the base class version to return a specfic
    * type of processor (component processor) object from the configuration.
    * @param componentType component type for the processor assumed not
    * <code>null</code>.
    * @return IPSRelationshipProcessor object never <code>null</code>
    */
   public IPSComponentProcessor getProcessor(String componentType)
      throws PSCmsException
   {
      return (IPSComponentProcessor)m_processorConfig.getProcessor(
         componentType);
   }
}
