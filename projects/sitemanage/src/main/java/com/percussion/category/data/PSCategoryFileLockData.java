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

package com.percussion.category.data;

import java.nio.channels.FileLock;

import java.time.LocalDateTime;

public class PSCategoryFileLockData {
	
	private FileLock lock;
	private LocalDateTime creationTime;
	
	public PSCategoryFileLockData() {
		super();
	}
	
	public PSCategoryFileLockData(FileLock lock, LocalDateTime creationTime) {
		super();
		this.lock = lock;
		this.creationTime = creationTime;
	}

	public FileLock getLock() {
		return lock;
	}

	public void setLock(FileLock lock) {
		this.lock = lock;
	}

	public LocalDateTime getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(LocalDateTime creationTime) {
		this.creationTime = creationTime;
	}

	@Override
	public String toString() {
		return "PSCategoryFileLockData [lock=" + lock + ", creationTime="
				+ creationTime + "]";
	}
}
