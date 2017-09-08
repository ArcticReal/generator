package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PartyGlAccountFound implements Event{

	private List<PartyGlAccount> partyGlAccounts;

	public PartyGlAccountFound(List<PartyGlAccount> partyGlAccounts) {
		this.setPartyGlAccounts(partyGlAccounts);
	}

	public List<PartyGlAccount> getPartyGlAccounts()	{
		return partyGlAccounts;
	}

	public void setPartyGlAccounts(List<PartyGlAccount> partyGlAccounts)	{
		this.partyGlAccounts = partyGlAccounts;
	}
}
