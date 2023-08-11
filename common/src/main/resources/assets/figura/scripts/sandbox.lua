-- sandbox --

_VERSION = "Lua 5.2 - Figura"

-- yeet FileIO and gc globals
if not isHost or not expose_sensitive_libraries then
	debug = nil
	dofile = nil
	loadfile = nil
	collectgarbage = nil
end
expose_sensitive_libraries = nil

-- GS easter egg
_GS = _G