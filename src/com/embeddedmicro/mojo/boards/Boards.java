package com.embeddedmicro.mojo.boards;

import java.util.ArrayList;

public class Boards {
	public final static ArrayList<Board> boards = new ArrayList<>();
	
	static {
		boards.add(new MojoV3());
		boards.add(new MojoV2());
	}
	
	public static Board getByName(String name){
		for (Board b : boards){
			if (b.getName().equals(name))
				return b;
		}
		return null;
	}
}
