package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PartyDataSourceFound implements Event{

	private List<PartyDataSource> partyDataSources;

	public PartyDataSourceFound(List<PartyDataSource> partyDataSources) {
		this.setPartyDataSources(partyDataSources);
	}

	public List<PartyDataSource> getPartyDataSources()	{
		return partyDataSources;
	}

	public void setPartyDataSources(List<PartyDataSource> partyDataSources)	{
		this.partyDataSources = partyDataSources;
	}
}
