package org.figuramc.figura.backend2;

import org.figuramc.figura.backend2.FSB;
import org.figuramc.figura.backend2.NetworkStuff;

public enum Destination {
	BACKEND,
	FSB,
	BOTH,
	FSB_OR_BACKEND,
	NONE;

	public boolean allowBackend() {
		return switch (this) {
			case BACKEND, BOTH -> NetworkStuff.isConnected();
			case FSB, NONE -> false;
			case FSB_OR_BACKEND -> !org.figuramc.figura.backend2.FSB.instance().connected();
		};
	}

	public boolean allowFSB() {
		return switch (this) {
			case FSB, BOTH -> org.figuramc.figura.backend2.FSB.instance().connected();
			case BACKEND, NONE -> false;
			case FSB_OR_BACKEND -> org.figuramc.figura.backend2.FSB.instance().connected();
		};
	}
	public Destination setBackend(boolean b){
		if(b){
			return switch (this) {
				case FSB -> BOTH;
				case NONE -> BACKEND;
				default -> this;
			};
		}
		return switch (this) {
			case BOTH, FSB_OR_BACKEND -> FSB;
			case BACKEND -> NONE;
			default -> this;
		};
	}
	public Destination setFSB(boolean b){
		if(b){
			return switch (this) {
				case BACKEND -> BOTH;
				case NONE -> FSB;
				default -> this;
			};
		}
		return switch (this) {
			case BOTH, FSB_OR_BACKEND -> BACKEND;
			case FSB -> NONE;
			default -> this;
		};
	}
	public static Destination fromBool(boolean b,boolean f){
		return (
			b && f ? BOTH :
			b ? BACKEND :
			f ? FSB :
			NONE
		);
	}
}