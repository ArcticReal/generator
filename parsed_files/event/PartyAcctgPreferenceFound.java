package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PartyAcctgPreferenceFound implements Event{

	private List<PartyAcctgPreference> partyAcctgPreferences;

	public PartyAcctgPreferenceFound(List<PartyAcctgPreference> partyAcctgPreferences) {
		this.setPartyAcctgPreferences(partyAcctgPreferences);
	}

	public List<PartyAcctgPreference> getPartyAcctgPreferences()	{
		return partyAcctgPreferences;
	}

	public void setPartyAcctgPreferences(List<PartyAcctgPreference> partyAcctgPreferences)	{
		this.partyAcctgPreferences = partyAcctgPreferences;
	}
}
