package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PartyCarrierAccountFound implements Event{

	private List<PartyCarrierAccount> partyCarrierAccounts;

	public PartyCarrierAccountFound(List<PartyCarrierAccount> partyCarrierAccounts) {
		this.setPartyCarrierAccounts(partyCarrierAccounts);
	}

	public List<PartyCarrierAccount> getPartyCarrierAccounts()	{
		return partyCarrierAccounts;
	}

	public void setPartyCarrierAccounts(List<PartyCarrierAccount> partyCarrierAccounts)	{
		this.partyCarrierAccounts = partyCarrierAccounts;
	}
}
