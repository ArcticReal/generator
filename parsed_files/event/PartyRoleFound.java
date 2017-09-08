package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PartyRoleFound implements Event{

	private List<PartyRole> partyRoles;

	public PartyRoleFound(List<PartyRole> partyRoles) {
		this.setPartyRoles(partyRoles);
	}

	public List<PartyRole> getPartyRoles()	{
		return partyRoles;
	}

	public void setPartyRoles(List<PartyRole> partyRoles)	{
		this.partyRoles = partyRoles;
	}
}
