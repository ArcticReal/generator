package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class MarketingCampaignPromoFound implements Event{

	private List<MarketingCampaignPromo> marketingCampaignPromos;

	public MarketingCampaignPromoFound(List<MarketingCampaignPromo> marketingCampaignPromos) {
		this.setMarketingCampaignPromos(marketingCampaignPromos);
	}

	public List<MarketingCampaignPromo> getMarketingCampaignPromos()	{
		return marketingCampaignPromos;
	}

	public void setMarketingCampaignPromos(List<MarketingCampaignPromo> marketingCampaignPromos)	{
		this.marketingCampaignPromos = marketingCampaignPromos;
	}
}
