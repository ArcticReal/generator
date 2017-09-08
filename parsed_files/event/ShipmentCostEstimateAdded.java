package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class ShipmentCostEstimateAdded implements Event{

	private boolean success;

	public ShipmentCostEstimateAdded(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
