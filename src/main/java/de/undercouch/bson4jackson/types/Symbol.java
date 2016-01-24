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

/**
 * A distinct string
 * @author Michel Kraemer
 */
public class Symbol {
	/**
	 * The actual symbol
	 */
	protected final String _symbol;
	
	/**
	 * Constructs a new symbol
	 * @param symbol the actual symbol
	 */
	public Symbol(String symbol) {
		_symbol = symbol;
	}
	
	/**
	 * @return the actual symbol
	 */
	public String getSymbol() {
		return _symbol;
	}
	
	@Override
	public String toString() {
		return _symbol;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (o instanceof String) {
			return _symbol.equals((String)o);
		}
		if (o instanceof Symbol) {
			return _symbol.equals(((Symbol)o)._symbol);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_symbol == null) ? 0 : _symbol.hashCode());
		return result;
	}
}
