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

package com.percussion.delivery.comments.data;

public interface IPSDefaultModerationState {

	/* (non-Javadoc)
	 * @see com.percussion.delivery.comments.service.rdbms.IPSDefaultModerationState#getSite()
	 */
	public abstract String getSite();

	/* (non-Javadoc)
	 * @see com.percussion.delivery.comments.service.rdbms.IPSDefaultModerationState#setSite(java.lang.String)
	 */
	public abstract void setSite(String site);

	/* (non-Javadoc)
	 * @see com.percussion.delivery.comments.service.rdbms.IPSDefaultModerationState#getDefaultState()
	 */
	public abstract String getDefaultState();

	/* (non-Javadoc)
	 * @see com.percussion.delivery.comments.service.rdbms.IPSDefaultModerationState#setDefaultState(java.lang.String)
	 */
	public abstract void setDefaultState(String defaultState);

}
