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
package com.percussion.cms.objectstore;

import com.percussion.cms.IPSCmsErrors;
import com.percussion.cms.PSCmsException;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;



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
 *    <li>The object would then be passed to the 
 *       {@link IPSComponentProcessor#save(IPSDbComponent[])
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
public abstract class PSProcessorProxy
{
   /**
    * Processors are associated with component types by a type (or category)
    * name. The types correspond generically to how the processor accomplishes
    * its work. These are the defaults, any other arbitrary name is allowed.
    * Not every component type is necessarily supported by every processor
    * type. Processor types are case-insensitive.
    * <p>This type of processor should be used on remote clients if you want
    * to interface to the server with Web Services.
    */
   public static final String PROCTYPE_WEBSERVICE = "webservice";


   /**
    * See {@link #PROCTYPE_WEBSERVICE} for more details.
    * <p>This type of processor should be used on remote clients for direct
    * access to support apps. It is nearly identical to the {@link
    * #PROCTYPE_SERVERLOCAL}, except it makes http requests rather than
    * internal requests.
    */
   public static final String PROCTYPE_REMOTE = "remote";


   /**
    * See {@link #PROCTYPE_WEBSERVICE} for more4 details.
    * <p>This type of processor should be used when operating in the same VM
    * as the Rhythmyx server. It uses internal requests to accomplish its work.
    */
   public static final String PROCTYPE_SERVERLOCAL = "local";

   /**
    * Creates a proxy for a specific type of processor. The config file is
    * obtained from the default location (in the jar containing this class).
    * It is assumed the file name is CmsProcessorConfig.xml.
    *
    * @param type  The type of processor for which this class is acting as a
    *    proxy. This is a general category such as "webservice", "remote" or
    *    "local". Several predefined ones are included with the PROCTYPE_xxx
    *    constants. See the config file for all possibilities.
    *    Never empty or <code>null</code>. This name will be used to find
    *    the processor's entry in the config document when the methods of this
    *    class are called.
    * @param ctx A context object appropriate for the processor type,
    *    may be <code>null</code> if the processor does not require one.
    * @throws PSCmsException for all errors constucting this processor.
    */
   public PSProcessorProxy(String type, Object ctx)
      throws PSCmsException
   {
      if (type == null)
         throw new IllegalArgumentException("type cannot be null");
         
      type = type.trim();
      if (type.length() == 0)
         throw new IllegalArgumentException("type cannot be empty");
         
      String configName = "CmsProcessorConfig.xml";
      try
      {
         InputStream is = getClass().getResourceAsStream(configName);
         if ( null == is )
         {
            String[] args =
            {
               configName
            };
            throw new PSCmsException(IPSCmsErrors.PROCESSOR_CONFIG_MISSING,
                  args);
         }

         Document props = PSXmlDocumentBuilder.createXmlDocument(is, false);
         PSXmlTreeWalker walker = new PSXmlTreeWalker(props);
         Element root = props.getDocumentElement();
         String rootName = "CmsComponents";
         if (null == root || !root.getNodeName().equals(rootName))
         {
            //use error codes for PSUnknownDocTypeException
            String[] args =
            {
               rootName,
               root.getNodeName()
            };
            throw new PSCmsException(
                  IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
         }

         ProcessorConfig pc = null;
         do
         {
            pc = processComponentConfig(type, walker, m_processorConfig);
            if ( null != pc )
               m_processorConfig = pc;
         }
         while (null != pc);

         if (ctx!=null)
            setProcessorContext(ctx);
      }
      catch (PSUnknownNodeTypeException unte)
      {
         throw new PSCmsException(unte.getErrorCode(),
               unte.getErrorArguments());
      }
      catch (SAXException se)
      {
         Exception e = se.getException();
         String[] args =
         {
            configName,
            se.getMessage(),
            null == e ? "none" : e.getLocalizedMessage()
         };
         throw new PSCmsException(IPSCmsErrors.XML_PARSING_ERROR, args);
      }
      catch (IOException ioe)
      {
         throw new PSCmsException(IPSCmsErrors.PROCESSOR_CONFIG_IO_ERROR,
            ioe.toString());
      }
   }


   /**
    * Reads the next component node in the walker, parses it into a set of
    * properties for each processor and adds the results to the supplied
    * map.
    *
    * @param procType The type of processor to extract. Assumed not
    *    <code>null</code>.
    *
    * @param walker Assumed not <code>null</code>. The first time this method
    *    is called, it should be at the root. Each successive time it should
    *    be where this method left it.
    *
    * @param cfg  The storage location for the extracted properties. If
    *    <code>null</code>, a new one is created.
    *
    * @return If a component node is found, the ProcessorConfig is returned.
    *    If cfg was supplied as <code>null</code>, a new config is created
    *    and returned, otherwise the one supplied is returned. If no
    *    component node is found, <code>null</code> is returned.
    *
    * @throws PSUnknownNodeTypeException If the document is not properly formed.
    */
   private ProcessorConfig processComponentConfig(String procType,
         PSXmlTreeWalker walker, ProcessorConfig cfg)
      throws PSUnknownNodeTypeException
   {
      final String COMPONENT_NODE = "CmsComponent";
      Map result = new HashMap();
      Element e = walker.getNextElement(COMPONENT_NODE,
            PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS
            | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT);
      if (null == e)
      {
         // Needed for 1st call to this method
         e = walker.getNextElement(COMPONENT_NODE,
            PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS
            | PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
         if ( null == e )
            return null;
      }

      String compType = PSXMLDomUtil.checkAttribute(e, "type", true);
      PSXmlTreeWalker ewalker = new PSXmlTreeWalker(e);
      Element generalProps = ewalker.getNextElement("Properties",
            PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN
            | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT);
      if (null != generalProps)
      {
         //todo: process them
      }

      boolean foundProc = false;
      final String PROCESSOR_NODENAME = "Processor";
      final String PROPERTY_NODENAME = "Property";

      Element processorEl = ewalker.getNextElement(PROCESSOR_NODENAME);
      while (null != processorEl)
      {
         String foundProcType =
               PSXMLDomUtil.checkAttribute(processorEl, "type", true);
         Node curProc = ewalker.getCurrent();
         if (procType.equalsIgnoreCase(foundProcType))
         {
            String procClass =
                  PSXMLDomUtil.checkAttribute(processorEl, "className", true);
            foundProc = true;
            Map processorProps = new HashMap();
            if (null == cfg)
            {
               cfg = new ProcessorConfig(procType, procClass);
            }

            Element propertyEl = ewalker.getNextElement(PROPERTY_NODENAME,
                  PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
            while (null != propertyEl)
            {
               Node curProp = ewalker.getCurrent();
               String propName = PSXMLDomUtil.checkAttribute(
                     propertyEl, "name", true).toLowerCase();
               Element propValue = ewalker.getNextElement(
                     PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
               if (processorProps.containsKey(propName))
               {
                  String[] args =
                  {
                     procType,
                     compType,
                     propName
                  };
                  throw new PSUnknownNodeTypeException(
                        IPSCmsErrors.DUPLICATE_PROCESSOR_PROPERTY, args);
               }
               if (propValue.getNodeName().equals("Value"))
               {
                  processorProps.put(propName,
                        PSXmlTreeWalker.getElementData(propValue));
               }
               else
                  processorProps.put(propName, propValue);

               ewalker.setCurrent(curProp);
               propertyEl = ewalker.getNextElement(PROPERTY_NODENAME,
                     PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
            }

            if (cfg.addComponentPropertySet(compType, processorProps, procClass))
            {
               String[] args =
               {
                  procType,
                  compType
               };
               throw new PSUnknownNodeTypeException(
                     IPSCmsErrors.DUPLICATE_PROCESSOR_ENTRY, args);
            }
            ewalker.setCurrent(curProc);
         }
         processorEl = ewalker.getNextElement(PROCESSOR_NODENAME,
               PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      }
      return cfg;
   }

   /**
    * Creates a proxy for a specific type of processor.
    *
    * @param configXml  The source containing the configuration file for
    *    the processors. The content must conform to the sys_CmsProcessorConfig
    *    schema. Never <code>null</code>. Takes ownership and closes the stream
    *    when finished.
    *
    * @param type  The type of processor for which this class is acting as a
    *    proxy. This is a general category such as "webservice", "remote" or
    *    "local". Several predefined ones are included with the PROCTYPE_xxx
    *    constants. See the config file for all possibilities.
    *    Never empty or <code>null</code>. This name will be used to find
    *    the processor's entry in the config document when the methods of this
    *    class are called.
    *
    * @throws IOException If any problems occur reading data from the config.
    */
   public PSProcessorProxy(Reader configXml, String type)
      throws IOException
   {
      throw new UnsupportedOperationException("Not implemented yet");
   }


   /**
    * Each processor has a specified context which is required for the
    * processor to perform the requested operations. The supplied object
    * must match the type required by the processor type specified in the
    * ctor. If one is required and not supplied, all requests will fail.
    * <p>This method may need to be called more than once during the life of
    * the proxy, see the description for the processor type for details.
    * <p>Each time this method is called, a new processor will be instantiated,
    * otherwise, each instantiated processor is cached and reused across
    * multiple operations.
    *
    * @param ctx An object appropriate for the processor type. May be
    *    <code>null</code> if the processor does not require one.
    */
   public void setProcessorContext( Object ctx )
   {
      m_processorConfig.flushCache();
      m_context = ctx;
   }

   /**
    * Looks up the processor for every supplied component and creates a
    * collection for those that have the same processor.
    *
    * @param components Assumed not <code>null</code> and that no entries are
    *    <code>null</code>.
    *
    * @return Never <code>null</code>. Each entry has a processor as the key
    *    and a Collection as the value. The collection contains all the
    *    components in the supplied array that use that processor.
    *
    * @throws PSCmsException  If the processor cannot be found or instantiated
    *    for any of the components.
    */
   protected Map createComponentProcessorGroups(IPSDbComponent[] components)
      throws PSCmsException
   {
      Map procGroups = new HashMap();
      for (int i=0; i < components.length; i++)
      {
         IPSDbComponent c = components[i];
         if ( null == c )
            continue;
         Object proc = m_processorConfig.getProcessor(c);
         Collection coll;
         if ( procGroups.containsKey(proc))
            coll = (Collection) procGroups.get(proc);
         else
         {
            coll = new ArrayList();
            procGroups.put(proc, coll);
         }
         coll.add(c);
      }
      return procGroups;
   }


   /**
    * For every component in the supplied array that has an unassigned key,
    * the key is assigned. This is equivalent to what a processor will do
    * when it is saving a component with an unassigned key.
    * <p>This method is not supported by all processors.
    *
    * @param comps Never <code>null</code> and no entry can be <code>
    *    null</code>.
    *
    * @param parentKey If the supplied components are children of another
    *    component, then the key of the parent must be supplied. If supplied,
    *    it must be assigned.
    *
    * @throws PSCmsException If any errors occur while allocating the ids.
    *
    * @throws UnsupportedOperationException if the processor can't fulfill the
    *    request.
    */
   public void assignKey(IPSDbComponent comps[], PSKey parentKey)
      throws PSCmsException
   {
      if (null == comps)
         throw new IllegalArgumentException("Component array cannot be null.");
      for (int i=0; i < comps.length; i++)
      {
         if (comps[i] == null)
         {
            throw new IllegalArgumentException(
                  "Component entry cannot be null.");
         }
      }

      Map procGroups = createComponentProcessorGroups(comps);

      int total = 0;
      Iterator iter = procGroups.keySet().iterator();
      while (iter.hasNext())
      {
         PSProcessorCommon proc = (PSProcessorCommon) iter.next();
         Collection coll = (Collection) procGroups.get(proc);
         proc.setNextAllocationSize(coll.size());
         Iterator it = coll.iterator();
         while (it.hasNext())
         {
            IPSDbComponent c = (IPSDbComponent) it.next();
            c.assignKey(proc, parentKey);
         }
      }
   }

   /**
    * The component type for relationship type operations, such as
    * getChildren(), getParents() for a specified relationship.
    */
   public final static String RELATIONSHIP_COMPTYPE = "Relationship";

   /**
    * <code>null</code> until the first component entry is found in the
    * config, then never <code>null</code> after that. After ctor is finished,
    * never changes after that.
    */
   protected ProcessorConfig m_processorConfig = null;

   /**
    * Passed to the processor when the various proxy methods are called.
    * Set by the setProcessorContext method. May be <code>null</code>. What
    * and whether this is needed is defined by the processors.
    */
   protected Object m_context = null;


   /**
    * A simple class to manage the properties associated with a single
    * processor.
    *
    * @author Paul Howard
    * @version 1.0
    */
   protected class ProcessorConfig
   {
      /**
       * Create the processor w/ no properties.
       *
       * @param processorType Assumed not <code>null</code> or empty.
       *
       * @param className  The fully qualified class name of the processor,
       *    assumed not <code>null</code> or empty.
       */
      public ProcessorConfig(String processorType, String className)
      {
         m_type = processorType;
      }

      /**
       * Adds all the properties associated with a particular component type.
       *
       * @param componentType  Assumed not <code>null</code> or empty.
       *
       * @param props Assumed not <code>null</code>.
       *
       * @param procClassName  The fully qualified name of the processor
       *    to instantiate for this component. Assumed not <code>null</code>
       *    or empty.
       *
       * @return <code>true</code> if the property already existed in this
       *    configuration, <code>false</code> otherwise.
       */
      public boolean addComponentPropertySet(String componentType, Map props,
            String procClassName)
      {
         String type = componentType.toLowerCase();
         boolean present = m_componentProps.containsKey(type);
         m_componentProps.put(type, props);
         m_procClassNames.put(type, procClassName);
         return present;
      }

      /**
       * Returns the class implementing the processor as specified by the
       * className property in the ctor. The first time this method is
       * called, the class is instantiated, it is then cached.
       *
       * @param compType Assumed not <code>null</code> or empty;
       *
       * @return The cached copy. If no processor can be found for the
       *    supplied type, an exception is thrown.
       *
       * @throws PSCmsException If the class cannot be found, there are
       *    any problems instantiating it, or it doesn't extend PSProcessorCommon.
       */
      public Object getProcessor(String compType)
         throws PSCmsException
      {
         String name = null;
         try
         {
            name = (String) m_procClassNames.get(compType.toLowerCase());
            if (null == name)
            {
               String[] args =
               {
                  compType,
                  m_type
               };
               throw new PSCmsException(IPSCmsErrors.NO_PROCESSOR_ENTRY,
                     args);
            }

            if (m_cachedProcs.containsKey(name))
               return m_cachedProcs.get(name);

            Class cl = Class.forName(name);

            try
            {
               Method instanceMethod = cl.getMethod("getInstance");
               Object o = instanceMethod.invoke(null);
               m_cachedProcs.put(name, o);
               
               return o;
            }
            catch (Exception e)
            {
               // Ignore
            }
            
            
            Constructor[] ctors = cl.getConstructors();
            //find the one w/ the Map param
            String paramType = "java.util.Map";
            boolean found = false;
            int i=0;
            int paramIndex = m_context == null ? 0 : 1;
            for (; i < ctors.length && !found; i++)
            {
               /* If no context, look for ctor(Map), otherwise look for
                  ctor(m_context.getClass(), Map) */
               Constructor ctor = ctors[i];
               Class[] paramTypes = ctor.getParameterTypes();
               if (paramTypes.length == 2
                     && paramTypes[paramIndex].getName().equals(paramType)
                     && (m_context == null
                        || paramTypes[0].isAssignableFrom(
                           m_context.getClass())))
               {
                  found = true;
               }
            }

            if (!found)
            {
               String className1;
               String className2;
               if (m_context == null)
               {
                  className1 = paramType;
                  className2 = "";
               }
               else
               {
                  className1 = m_context.getClass().getName();
                  className2 = paramType;
               }
               String[] args =
               {
                  name,
                  ""+(paramIndex+1),
                  className1 + (className2.length() > 0 ? "," : ""),
                  className2
               };
               throw new PSCmsException(IPSCmsErrors.PROCESSOR_NO_SUCH_METHOD,
                     args);
            }

            Object o = ctors[i-1].newInstance(
                  new Object[] {m_context, m_componentProps});

            m_cachedProcs.put(name, o);

            return o;
         }
         catch (ClassNotFoundException cnfe)
         {
            String[] args =
            {
               name,
               compType,
               cnfe.getLocalizedMessage()
            };
            throw new PSCmsException(IPSCmsErrors.PROCESSOR_INSTANTIATION_ERROR,
                  args);
         }
         catch (InstantiationException ie)
         {
            String[] args =
            {
               name,
               compType,
               ie.getLocalizedMessage()
            };
            throw new PSCmsException(IPSCmsErrors.PROCESSOR_INSTANTIATION_ERROR,
                  args);
         }
         catch (IllegalAccessException iae)
         {
            String[] args =
            {
               name,
               compType,
               iae.getLocalizedMessage()
            };
            throw new PSCmsException(IPSCmsErrors.PROCESSOR_INSTANTIATION_ERROR,
                  args);
         }
         catch (InvocationTargetException ite)
         {
            Throwable origException = ite.getTargetException();
            String msg = origException.getLocalizedMessage();
            String[] args =
            {
               name,
               compType,
               origException.getClass().getName() + ": " + msg
            };
            throw new PSCmsException(IPSCmsErrors.PROCESSOR_INSTANTIATION_ERROR,
                  args);
         }
         catch (IllegalArgumentException iae)
         {
            //this should never happen because we checked ahead of time
            throw new RuntimeException(
                  "Ctor parameter count changed unexpectedly.");
         }
      }

      /**
       * Returns the class implementing the processor as specified by the
       * className property in the ctor. The first time this method is
       * called, the class is instantiated, it is then cached.
       *
       * @param comp The component whose processor type defines the processor
       * returned.  Assumed not <code>null</code>.  If an instance of a
       * <code>PSDbComponentCollection</code> or <code>PSDbComponentList</code>
       * is supplied (but not a derived class instance), the member processor
       * type is used.
       *
       * @return The cached copy. If no processor can be found for the
       *    supplied type, an exception is thrown.
       *
       * @throws PSCmsException If the class cannot be found, there are
       *    any problems instantiating it, or it doesn't extend PSProcessorCommon.
       */
      public Object getProcessor(IPSDbComponent comp)
         throws PSCmsException
      {
         String compType = comp.getComponentType();

         // if we have an actual instance of a list or collection, get the
         // member type
         if (compType.equals(PSDbComponent.getComponentType(
            PSDbComponentCollection.class)))
         {
            PSDbComponentCollection compColl = (PSDbComponentCollection)comp;
            compType = compColl.getMemberComponentType();
         }
         else if (compType.equals(PSDbComponent.getComponentType(
            PSDbComponentList.class)))
         {
            PSDbComponentList compList = (PSDbComponentList)comp;
            compType = compList.getMemberComponentType();
         }

         return getProcessor(compType);
      }

      /**
       * Removes all processor entries that had been previously cached. This
       * should be called whenever the context of the proxy changes.
       */
      public void flushCache()
      {
         m_cachedProcs.clear();
      }

      /**
       * Get the map that contains all properties for all component types.
       *
       * @return The returned map has String component types as keys and
       *    maps as values. The maps contain name/value pairs. The value
       *    is either a String or an Element. This should be treated read-
       *    only by the caller.
       */
      public Map getComponentPropertySets()
      {
         return m_componentProps;
      }

      /**
       * This processor's type (e.g., local, webservice). Set in ctor,
       * then never <code>null</code> or empty after that (assumed).
       */
      private String m_type;

      /**
       * See {@link #getComponentPropertySets()} for description of contents.
       * Never <code>null</code>.
       */
      private Map m_componentProps = new HashMap();

      /**
       * Never <code>null</code>. Each time {@link #getProcessor(String)}
       * instantiates a new processor, it will be added to this cache.
       * The key is the class name, the value is the processor instance.
       */
      private Map m_cachedProcs = new HashMap();

      /**
       * Never <code>null</code>. Contains 1 entry for every component added
       * to this config. The key is the component name, the value is the
       * processor class name. Assumed that entries are never <code>null</code>
       * or empty.
       */
      private Map m_procClassNames = new HashMap();
   }
}
