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

package com.percussion.deployer.server.dependencies;

import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSIdMap;
import com.percussion.deployer.objectstore.PSIdMapping;
import com.percussion.deployer.server.PSArchiveHandler;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.deployer.server.PSImportCtx;
import com.percussion.error.IPSDeploymentErrors;
import com.percussion.error.PSDeployException;
import com.percussion.security.PSSecurityToken;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public interface IPSDependencyHandler {

    /**
     * Gets a handler instance using the supplied def
     *
     * @param def The def for which the appropriate handler type should be
     *            returned.  May not be <code>null</code>.
     * @param map The dependency map, may not be <code>null</code>.
     * @return The handler, never <code>null</code>.
     * @throws IllegalArgumentException if <code>def</code> is <code>null</code>.
     * @throws PSDeployException        if there are any errors.
     */
    static PSDependencyHandler getHandlerInstance(PSDependencyDef def,
                                                  PSDependencyMap map) throws PSDeployException {
        if (def == null)
            throw new IllegalArgumentException("def may not be null");

        if (map == null)
            throw new IllegalArgumentException("map may not be null");

        String className = def.getHandlerClassName();
        PSDependencyHandler handler = null;

        try {
            Class handlerClass = Class.forName(className);
            Constructor handlerCtor = handlerClass.getConstructor(new Class[]
                    {PSDependencyDef.class, PSDependencyMap.class});
            handler = (PSDependencyHandler) handlerCtor.newInstance(
                    new Object[]{def, map});

            return handler;
        } catch (ClassNotFoundException cnfe) {
            Object[] args = {className, cnfe.getLocalizedMessage()};
            throw new PSDeployException(
                    IPSDeploymentErrors.DEPENDENCY_HANDLER_INIT, args);
        } catch (InstantiationException ie) {
            Object[] args = {className, ie.getLocalizedMessage()};
            throw new PSDeployException(
                    IPSDeploymentErrors.DEPENDENCY_HANDLER_INIT, args);
        } catch (IllegalAccessException iae) {
            Object[] args = {className, iae.getLocalizedMessage()};
            throw new PSDeployException(
                    IPSDeploymentErrors.DEPENDENCY_HANDLER_INIT, args);
        } catch (InvocationTargetException ite) {
            Throwable origException = ite.getTargetException();
            String msg = origException.getLocalizedMessage();
            Object[] args = {className, origException.getClass().getName() + ": " +
                    msg};
            throw new PSDeployException(
                    IPSDeploymentErrors.DEPENDENCY_HANDLER_INIT, args);
        } catch (NoSuchMethodException nsme) {
            Object[] args = {className, nsme.getLocalizedMessage()};
            throw new PSDeployException(
                    IPSDeploymentErrors.DEPENDENCY_HANDLER_INIT, args);
        } catch (IllegalArgumentException iae) {
            //this should never happen because we checked ahead of time
            throw new RuntimeException("Ctor args failed validation: " +
                    iae.getLocalizedMessage());
        }
    }

    boolean isChildTypeSupported(PSDependency child);

    /**
     * Gets all dependencies that are child dependecies of the supplied
     * dependency.
     * Note: Add IDType dependencies this method
     *
     * @param tok The security token to use if objectstore access is required,
     *            may not be <code>null</code>.
     * @param dep A dependency of the type defined by this handler, may not be
     *            <code>null</code>.
     * @return iterator over zero or more <code>PSDependency</code> objects,
     * never <code>null</code>, may be empty.
     * @throws IllegalArgumentException if dep is invalid.
     * @throws PSDeployException        if there are any errors.
     */
    Iterator getChildDependencies(PSSecurityToken tok,
                                  PSDependency dep)
            throws PSDeployException, PSNotFoundException;

    Iterator getDependencyFiles(PSSecurityToken tok, PSDependency dep)
            throws PSDeployException, PSNotFoundException;

    void installDependencyFiles(PSSecurityToken tok,
                                PSArchiveHandler archive, PSDependency dep, PSImportCtx ctx)
            throws PSDeployException, PSAssemblyException, PSNotFoundException;

    /**
     * Gets all dependencies of this type that exist on the Rhythmyx server.
     *
     * @param tok The security token to use if objectstore access is required,
     *            may not be <code>null</code>.
     * @return An iterator over zero or more <code>PSDependency</code> objects.
     * @throws IllegalArgumentException if <code>tok</code> is invalid.
     * @throws PSDeployException        if there are any errors.
     */
    Iterator<PSDependency> getDependencies(PSSecurityToken tok)
            throws PSDeployException, PSNotFoundException;

    Iterator getDependencies(PSSecurityToken tok, String parentType,
                             String parentId) throws PSDeployException;

    PSDependency getDependency(PSSecurityToken tok, String id)
            throws PSDeployException, PSNotFoundException;

    PSDependency getDependency(PSSecurityToken tok, String id,
                               String parentType, String parentId)
            throws PSDeployException;

    void addAclDependency(PSSecurityToken tok, PSTypeEnum key,
                          PSDependency dep, Collection<PSDependency> childDeps)
            throws PSDeployException, PSNotFoundException;

    /**
     * Derived classes must override this method to provide the list of child
     * dependency types they can discover.
     *
     * @return An iterator over zero or more types as <code>String</code>
     * objects, never <code>null</code>, does not contain <code>null</code> or
     * empty entries.
     */
    Iterator getChildTypes();

    /**
     * Must be overriden by derived classes to supply the correct type.
     *
     * @return the type of dependency supported by this handler, never
     * <code>null</code> or empty.
     */
    String getType();

    String getParentType();

    boolean doesDependencyExist(PSSecurityToken tok, String id)
            throws PSDeployException, PSNotFoundException;

    boolean doesDependencyExist(PSSecurityToken tok, String id,
                                String parentId) throws PSDeployException;

    void reserveNewId(PSDependency dep, PSIdMap idMap)
            throws PSDeployException;

    boolean shouldDeferInstallation();

    boolean delegatesIdMapping();

    String getIdMappingType();

    String getParentIdMappingType();

    boolean isRequiredChild(String type);

    boolean overwritesOnInstall();

    String getTargetId(PSIdMapping mapping, String id)
            throws PSDeployException;

    List getExternalDbmsInfoList(PSSecurityToken tok, PSDependency dep)
            throws PSDeployException;

    PSIdMapping getIdMapping(PSIdMap idMap, String id, String type,
                             String parentId, String parentType)
            throws PSDeployException;

    void addTransactionLogEntryByGuidType(PSDependency dep,
                                          PSImportCtx ctx, PSTypeEnum type, boolean isNew) throws PSDeployException;

    void addTransactionLogEntry(PSDependency dep, PSImportCtx ctx,
                                String elementName, String elementType, int action)
            throws PSDeployException;


}
