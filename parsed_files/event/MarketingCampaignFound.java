package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class MarketingCampaignFound implements Event{

	private List<MarketingCampaign> marketingCampaigns;

	public MarketingCampaignFound(List<MarketingCampaign> marketingCampaigns) {
		this.setMarketingCampaigns(marketingCampaigns);
	}

	public List<MarketingCampaign> getMarketingCampaigns()	{
		return marketingCampaigns;
	}

	public void setMarketingCampaigns(List<MarketingCampaign> marketingCampaigns)	{
		this.marketingCampaigns = marketingCampaigns;
	}
}
