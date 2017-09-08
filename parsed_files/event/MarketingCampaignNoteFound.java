package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class MarketingCampaignNoteFound implements Event{

	private List<MarketingCampaignNote> marketingCampaignNotes;

	public MarketingCampaignNoteFound(List<MarketingCampaignNote> marketingCampaignNotes) {
		this.setMarketingCampaignNotes(marketingCampaignNotes);
	}

	public List<MarketingCampaignNote> getMarketingCampaignNotes()	{
		return marketingCampaignNotes;
	}

	public void setMarketingCampaignNotes(List<MarketingCampaignNote> marketingCampaignNotes)	{
		this.marketingCampaignNotes = marketingCampaignNotes;
	}
}
