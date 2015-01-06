package com.embeddedmicro.mojo;

public interface Callback {
	public void onSuccess();
	public void onError(String error);
}
