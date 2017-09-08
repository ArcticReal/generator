package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class FixedAssetTypeAdded implements Event{

	private boolean success;

	public FixedAssetTypeAdded(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
