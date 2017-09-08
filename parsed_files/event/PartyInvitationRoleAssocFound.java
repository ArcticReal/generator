package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PartyInvitationRoleAssocFound implements Event{

	private List<PartyInvitationRoleAssoc> partyInvitationRoleAssocs;

	public PartyInvitationRoleAssocFound(List<PartyInvitationRoleAssoc> partyInvitationRoleAssocs) {
		this.setPartyInvitationRoleAssocs(partyInvitationRoleAssocs);
	}

	public List<PartyInvitationRoleAssoc> getPartyInvitationRoleAssocs()	{
		return partyInvitationRoleAssocs;
	}

	public void setPartyInvitationRoleAssocs(List<PartyInvitationRoleAssoc> partyInvitationRoleAssocs)	{
		this.partyInvitationRoleAssocs = partyInvitationRoleAssocs;
	}
}
