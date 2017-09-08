package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class MarketingCampaignPriceFound implements Event{

	private List<MarketingCampaignPrice> marketingCampaignPrices;

	public MarketingCampaignPriceFound(List<MarketingCampaignPrice> marketingCampaignPrices) {
		this.setMarketingCampaignPrices(marketingCampaignPrices);
	}

	public List<MarketingCampaignPrice> getMarketingCampaignPrices()	{
		return marketingCampaignPrices;
	}

	public void setMarketingCampaignPrices(List<MarketingCampaignPrice> marketingCampaignPrices)	{
		this.marketingCampaignPrices = marketingCampaignPrices;
	}
}
