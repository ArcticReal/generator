package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class MarketingCampaignRoleAdded implements Event{

	private boolean success;

	public MarketingCampaignRoleAdded(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
