package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PartyRateFound implements Event{

	private List<PartyRate> partyRates;

	public PartyRateFound(List<PartyRate> partyRates) {
		this.setPartyRates(partyRates);
	}

	public List<PartyRate> getPartyRates()	{
		return partyRates;
	}

	public void setPartyRates(List<PartyRate> partyRates)	{
		this.partyRates = partyRates;
	}
}
