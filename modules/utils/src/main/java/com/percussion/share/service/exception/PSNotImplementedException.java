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

package com.percussion.share.service.exception;


/**
 * Should be used when a method is not yet implemented
 * 
 * @author natechadwick
 *
 */
public class PSNotImplementedException extends RuntimeException {

	public PSNotImplementedException(String message) {
		super(message);
	}

	public PSNotImplementedException(Throwable cause) {
		super(cause);
	}

	public PSNotImplementedException(String message, Throwable cause) {
		super(message, cause);
	}

}
