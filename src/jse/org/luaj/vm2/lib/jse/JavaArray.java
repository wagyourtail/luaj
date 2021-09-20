/*******************************************************************************
* Copyright (c) 2011 Luaj.org. All rights reserved.
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
******************************************************************************/
package org.luaj.vm2.lib.jse;

import java.lang.reflect.Array;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaUserdata;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

/**
 * LuaValue that represents a Java instance of array type.
 * <p>
 * Can get elements by their integer key index, as well as the length.
 * <p>
 * This class is not used directly.  
 * It is returned by calls to {@link CoerceJavaToLua#coerce(Object)} 
 * when an array is supplied.
 * @see CoerceJavaToLua
 * @see CoerceLuaToJava
 */
class JavaArray extends LuaUserdata {

	private static final class LenFunction extends OneArgFunction {
		public LuaValue call(LuaValue u) {
			return LuaValue.valueOf(Array.getLength(((LuaUserdata)u).m_instance));
		}
	}

	private static final class IPairsFunction extends VarArgFunction {

		@Override
		public Varargs invoke(Varargs args) {

			return varargsOf(new VarArgFunction() {
				@Override
				public Varargs invoke(Varargs args) {
					Object list = (Object) ((LuaUserdata)args.arg1()).m_instance;
					int index = args.arg(2).toint();
					if (index == Array.getLength(list)) return NIL;
					return varargsOf(LuaValue.valueOf(index + 1), CoerceJavaToLua.coerce(Array.get(list, index)));
				}
			}, args.arg1(), LuaValue.valueOf(0));
		}
	}

	static final LuaValue LENGTH = valueOf("length");

	static final LenFunction len_func = new LenFunction();
	static final IPairsFunction ipairs_func = new IPairsFunction();


	JavaArray(Object instance) {
		super(instance);

		LuaTable array_metatable = new LuaTable();
		array_metatable.set(LuaValue.LEN, len_func);
		array_metatable.set(LuaValue.IPAIRS, ipairs_func);
		array_metatable.set(LuaValue.PAIRS, ipairs_func);
		setmetatable(array_metatable);
	}
	
	public LuaValue get(LuaValue key) {
		if ( key.equals(LENGTH) )
			return valueOf(Array.getLength(m_instance));
		if ( key.isint() ) {
			int i = key.toint() - 1;
			return i>=0 && i<Array.getLength(m_instance)?
				CoerceJavaToLua.coerce(Array.get(m_instance,key.toint()-1)):
				NIL;
		}
		return super.get(key);
	}

	public void set(LuaValue key, LuaValue value) {
		if ( key.isint() ) {
			int i = key.toint() - 1;
			if ( i>=0 && i<Array.getLength(m_instance) )
				Array.set(m_instance,i,CoerceLuaToJava.coerce(value, m_instance.getClass().getComponentType()));
			else if ( m_metatable==null || ! settable(this,key,value) )
					error("array index out of bounds");
		}
		else
			super.set(key, value);
	} 	
}
