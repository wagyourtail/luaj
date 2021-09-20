package org.luaj.vm2.lib.jse;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaUserdata;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

import java.util.List;

class JavaList<T> extends LuaUserdata {

    static final LuaValue LENGTH = valueOf("length");

    public JavaList(List<T> obj) {
        super(obj);

        LuaTable list_metatable = new LuaTable();
        list_metatable.rawset(LuaValue.LEN, new LenFunction());
        list_metatable.rawset(LuaValue.IPAIRS, new ListIPairs());
        list_metatable.rawset(LuaValue.PAIRS, new ListIPairs());
        setmetatable(list_metatable);
    }

    public LuaValue get(LuaValue key) {
        if (key.equals(LENGTH))
            return valueOf(((List<?>) m_instance).size());
        if (key.isint()) {
            int i = key.toint() - 1;
            return i >= 0 && i < ((List<?>) m_instance).size() ?
                CoerceJavaToLua.coerce(((List<?>) m_instance).get(i)) :
                NIL;
        }
        return super.get(key);
    }

    public void set(LuaValue key, LuaValue value) {
        List<T> list = (List<T>) m_instance;
        if (key.isint()) {
            int i = key.toint() - 1;
            if (i >= 0) {
                if (i < list.size())
                    list.set(i, (T) CoerceLuaToJava.coerce(value, Object.class));
                else
                    list.add((T) CoerceLuaToJava.coerce(value, Object.class));
            }
        } else
            super.set(key, value);
    }

    private static final class ListIPairs extends VarArgFunction {

        @Override
        public Varargs invoke(Varargs args) {

            return varargsOf(new VarArgFunction() {

                @Override
                public Varargs invoke(Varargs args) {
                    List<?> list = (List<?>) ((JavaList)args.arg1()).m_instance;
                    int index = args.arg(2).toint();
                    if (index == list.size()) return NIL;
                    return varargsOf(LuaValue.valueOf(index + 1), CoerceJavaToLua.coerce(list.get(index)));
                }

            }, args.arg1(), LuaValue.valueOf(0));
        }

    }

    private static final class LenFunction extends OneArgFunction {

        public LuaValue call(LuaValue u) {
            return LuaValue.valueOf(((List<?>)((LuaUserdata)u).m_instance).size());
        }

    }

}
