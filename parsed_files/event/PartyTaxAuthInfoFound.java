package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PartyTaxAuthInfoFound implements Event{

	private List<PartyTaxAuthInfo> partyTaxAuthInfos;

	public PartyTaxAuthInfoFound(List<PartyTaxAuthInfo> partyTaxAuthInfos) {
		this.setPartyTaxAuthInfos(partyTaxAuthInfos);
	}

	public List<PartyTaxAuthInfo> getPartyTaxAuthInfos()	{
		return partyTaxAuthInfos;
	}

	public void setPartyTaxAuthInfos(List<PartyTaxAuthInfo> partyTaxAuthInfos)	{
		this.partyTaxAuthInfos = partyTaxAuthInfos;
	}
}
