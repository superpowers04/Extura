package org.figuramc.figura.lua.api;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaTypeDoc;

@LuaWhitelist
@LuaTypeDoc(
		name = "disabledapi",
		value = "disabled"
)
public class DisabledAPI {
	public String api = "";
	public String reason = "";
    public DisabledAPI(String APIName,String Reason) {
    	api = APIName;
    	reason = Reason;
    }
    @LuaWhitelist
	@LuaMethodDoc("disabled.real")
    public void anyAccess(){
    	if(reason != null && reason != "") throw new LuaError(api+reason);
    	throw new LuaError(api+" is disabled.");
    }
    @LuaWhitelist
	@LuaMethodDoc("disabled.fake")
    public Object __index(String arg) {
    	if(arg.contains("allowed")){
    		return this;
    	}
    	anyAccess();
    	return null;
    }
    public boolean __call(){
    	return false;
    }
    public boolean isNetworkingAllowed() {
    	return false;
    }
    public boolean isLinkAllowed(Object meow) {
    	return false;
    }
    public boolean isPathAllowed(Object meow) {
    	return false;
    }
    @LuaWhitelist
	@LuaMethodDoc("disabled.i_like_women")
    public void __newindex(String key, boolean value) {
    	anyAccess();
    }

    @Override
    public String toString() {
        return api;
    }
}
