package com.embeddedmicro.mojo.boards;

public class MojoV2 extends Board {

	@Override
	public String getFPGAName() {
		return "xc6slx25-3ftg256";
	}

	@Override
	public String getName() {
		return "miniSpartan6+";
	}

	@Override
	public String getBaseProjectName() {
		return "miniSpartan6+";
	}

}
