package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PartyContactMechPurposeFound implements Event{

	private List<PartyContactMechPurpose> partyContactMechPurposes;

	public PartyContactMechPurposeFound(List<PartyContactMechPurpose> partyContactMechPurposes) {
		this.setPartyContactMechPurposes(partyContactMechPurposes);
	}

	public List<PartyContactMechPurpose> getPartyContactMechPurposes()	{
		return partyContactMechPurposes;
	}

	public void setPartyContactMechPurposes(List<PartyContactMechPurpose> partyContactMechPurposes)	{
		this.partyContactMechPurposes = partyContactMechPurposes;
	}
}
