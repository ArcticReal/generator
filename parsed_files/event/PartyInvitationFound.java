package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PartyInvitationFound implements Event{

	private List<PartyInvitation> partyInvitations;

	public PartyInvitationFound(List<PartyInvitation> partyInvitations) {
		this.setPartyInvitations(partyInvitations);
	}

	public List<PartyInvitation> getPartyInvitations()	{
		return partyInvitations;
	}

	public void setPartyInvitations(List<PartyInvitation> partyInvitations)	{
		this.partyInvitations = partyInvitations;
	}
}
