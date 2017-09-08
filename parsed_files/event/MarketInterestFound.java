package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class MarketInterestFound implements Event{

	private List<MarketInterest> marketInterests;

	public MarketInterestFound(List<MarketInterest> marketInterests) {
		this.setMarketInterests(marketInterests);
	}

	public List<MarketInterest> getMarketInterests()	{
		return marketInterests;
	}

	public void setMarketInterests(List<MarketInterest> marketInterests)	{
		this.marketInterests = marketInterests;
	}
}
