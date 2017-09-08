package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PartyInvitationGroupAssocFound implements Event{

	private List<PartyInvitationGroupAssoc> partyInvitationGroupAssocs;

	public PartyInvitationGroupAssocFound(List<PartyInvitationGroupAssoc> partyInvitationGroupAssocs) {
		this.setPartyInvitationGroupAssocs(partyInvitationGroupAssocs);
	}

	public List<PartyInvitationGroupAssoc> getPartyInvitationGroupAssocs()	{
		return partyInvitationGroupAssocs;
	}

	public void setPartyInvitationGroupAssocs(List<PartyInvitationGroupAssoc> partyInvitationGroupAssocs)	{
		this.partyInvitationGroupAssocs = partyInvitationGroupAssocs;
	}
}
