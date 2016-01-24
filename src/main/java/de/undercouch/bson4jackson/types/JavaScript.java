// Copyright 2010-2011 Michel Kraemer
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package de.undercouch.bson4jackson.types;

import java.util.Map;

/**
 * Embedded JavaScript code with an optional scope (i.e. an embedded BSON document)
 * @author Michel Kraemer
 */
public class JavaScript {
	/**
	 * The actual code
	 */
	protected final String _code;
	
	/**
	 * The scope (may be null)
	 */
	protected final Map<String, Object> _scope;
	
	/**
	 * Constructs a new JavaScript object 
	 * @param code the actual code
	 */
	public JavaScript(String code) {
		this(code, null);
	}
	
	/**
	 * Constructs a new JavaScript object 
	 * @param code the actual code
	 * @param scope the scope (may be null)
	 */
	public JavaScript(String code, Map<String, Object> scope) {
		_code = code;
		_scope = scope;
	}
	
	/**
	 * @return the actual code
	 */
	public String getCode() {
		return _code;
	}
	
	/**
	 * @return the scope (may be null)
	 */
	public Map<String, Object> getScope() {
		return _scope;
	}
}
