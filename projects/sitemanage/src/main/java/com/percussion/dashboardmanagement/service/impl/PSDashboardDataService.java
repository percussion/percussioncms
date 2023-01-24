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
package com.percussion.dashboardmanagement.service.impl;

import com.percussion.dashboardmanagement.dao.IPSDashboardDao;
import com.percussion.dashboardmanagement.data.PSDashboard;
import com.percussion.dashboardmanagement.service.IPSDashboardDataService;
import com.percussion.share.service.PSAbstractSimpleDataService;
import com.percussion.util.PSSiteManageBean;
import org.springframework.beans.factory.annotation.Autowired;

@PSSiteManageBean("dashboardDataService")
public class PSDashboardDataService extends 
	PSAbstractSimpleDataService<PSDashboard, String> implements IPSDashboardDataService {

	@Autowired
	public PSDashboardDataService(IPSDashboardDao dao) {
		super(dao);
	}


}
