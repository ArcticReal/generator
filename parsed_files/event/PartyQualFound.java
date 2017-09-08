package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PartyQualFound implements Event{

	private List<PartyQual> partyQuals;

	public PartyQualFound(List<PartyQual> partyQuals) {
		this.setPartyQuals(partyQuals);
	}

	public List<PartyQual> getPartyQuals()	{
		return partyQuals;
	}

	public void setPartyQuals(List<PartyQual> partyQuals)	{
		this.partyQuals = partyQuals;
	}
}
