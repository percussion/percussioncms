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

package com.percussion.apibridge;


import com.percussion.cms.objectstore.PSActionParameter;
import com.percussion.cms.objectstore.PSActionParameters;
import com.percussion.cms.objectstore.PSActionProperties;
import com.percussion.cms.objectstore.PSActionProperty;
import com.percussion.cms.objectstore.PSActionVisibilityContext;
import com.percussion.cms.objectstore.PSActionVisibilityContexts;
import com.percussion.cms.objectstore.PSDbComponentCollection;
import com.percussion.cms.objectstore.PSMenuModeContextMapping;
import com.percussion.rest.actions.ActionMenu;
import com.percussion.rest.actions.ActionMenuModeUIContext;
import com.percussion.rest.actions.ActionMenuParameter;
import com.percussion.rest.actions.ActionMenuProperty;
import com.percussion.rest.actions.ActionMenuVisibilityContext;
import com.percussion.rest.actions.IActionMenuAdaptor;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.menus.PSContentTypeActionMenuHelper;
import com.percussion.services.menus.PSTemplateActionMenuHelper;
import com.percussion.util.PSSiteManageBean;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.ui.IPSUiDesignWs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@PSSiteManageBean
@Lazy
public class ActionMenuAdaptor implements IActionMenuAdaptor {

    private IPSUiDesignWs service;


    @Autowired
    public ActionMenuAdaptor(IPSUiDesignWs service){
     this.service = service;
    }

    @Override
    public List<ActionMenu> findMenus(String name, String label, Boolean item, Boolean dynamic, Boolean cascading) throws PSErrorResultsException {

        IPSCmsObjectMgr mgr = PSCmsObjectMgrLocator.getObjectManager();

        return ApiUtils.convertPSActionMenuList(mgr.findActionMenus());

    }

    @Override
    public List<ActionMenu> findAllowedTransitions(Integer[] contentIds, Integer[] assignmentTypeIds) {
        return Collections.emptyList();
    }

    @Override
    public List<ActionMenu> findAllowedContentTypes(Integer[] contentIds) {
        return ApiUtils.convertPSActionMenuList(
                PSContentTypeActionMenuHelper.getInstance().getContentTypeMenus(null));
    }

    @Override
    public List<ActionMenu> findAllowedTemplates(Integer contentId, boolean isAA) {
        return ApiUtils.convertPSActionMenuList(
                PSTemplateActionMenuHelper.getInstance().getTemplateMenus(contentId,isAA,null)
        );
    }

    private ActionMenuVisibilityContext[] copyVisibilityContexts(PSActionVisibilityContexts visibilityContexts) {

        ArrayList<ActionMenuVisibilityContext> ctxs = new ArrayList<>();

        while(visibilityContexts.iterator().hasNext()){
            PSActionVisibilityContext  v = (PSActionVisibilityContext)visibilityContexts.iterator().next();
            ActionMenuVisibilityContext amc = new ActionMenuVisibilityContext();

            ArrayList values = new ArrayList();
            while(v.iterator().hasNext()){
                values.add(v.iterator().next());
            }
            amc.setDescription(v.getDescription());
            amc.setName(v.getName());

        }

        return ctxs.toArray(new ActionMenuVisibilityContext[ctxs.size()]);
    }

    private ActionMenuModeUIContext[] copyUIContexts(PSDbComponentCollection modeUIContexts) {
        ArrayList<ActionMenuModeUIContext> uictx = new ArrayList<>();

        while(modeUIContexts.iterator().hasNext()){
            PSMenuModeContextMapping mode = (PSMenuModeContextMapping)modeUIContexts.iterator().next();

            ActionMenuModeUIContext restMode = new ActionMenuModeUIContext();

            restMode.setContextId(mode.getContextId());
            restMode.setContextName(mode.getContextName());
            restMode.setModeId(mode.getModeId());
            restMode.setModeName(mode.getModeName());
            restMode.setDescription(mode.getDescription());

            uictx.add(restMode);
        }

        return uictx.toArray(new ActionMenuModeUIContext[uictx.size()]);
    }

    private ActionMenuParameter[] copyParameters(PSActionParameters parameters) {
        ArrayList<ActionMenuParameter> ret = new ArrayList<>();

        while(parameters.iterator().hasNext()){
            PSActionParameter  psap = (PSActionParameter)parameters.iterator().next();

            ActionMenuParameter p = new ActionMenuParameter();

            p.setDescription(psap.getDescription());
            p.setName(psap.getName());
            p.setValue(psap.getValue());
            ret.add(p);
        }

        return ret.toArray(new ActionMenuParameter[ret.size()]);
    }

    private ActionMenuProperty[] copyProperties(PSActionProperties properties) {

        ArrayList<ActionMenuProperty> ret = new ArrayList<>();

        while(properties.iterator().hasNext()){
            PSActionProperty p = (PSActionProperty) properties.iterator().next();

            ActionMenuProperty prop = new ActionMenuProperty();
            prop.setDescription(p.getDescription());
            prop.setValue(p.getValue());
            prop.setName(p.getName());
            ret.add(prop);

        }

        return ret.toArray(new ActionMenuProperty[ret.size()]);
    }

}
