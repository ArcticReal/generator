package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class MarketingCampaignRoleFound implements Event{

	private List<MarketingCampaignRole> marketingCampaignRoles;

	public MarketingCampaignRoleFound(List<MarketingCampaignRole> marketingCampaignRoles) {
		this.setMarketingCampaignRoles(marketingCampaignRoles);
	}

	public List<MarketingCampaignRole> getMarketingCampaignRoles()	{
		return marketingCampaignRoles;
	}

	public void setMarketingCampaignRoles(List<MarketingCampaignRole> marketingCampaignRoles)	{
		this.marketingCampaignRoles = marketingCampaignRoles;
	}
}
