package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PartyFixedAssetAssignmentFound implements Event{

	private List<PartyFixedAssetAssignment> partyFixedAssetAssignments;

	public PartyFixedAssetAssignmentFound(List<PartyFixedAssetAssignment> partyFixedAssetAssignments) {
		this.setPartyFixedAssetAssignments(partyFixedAssetAssignments);
	}

	public List<PartyFixedAssetAssignment> getPartyFixedAssetAssignments()	{
		return partyFixedAssetAssignments;
	}

	public void setPartyFixedAssetAssignments(List<PartyFixedAssetAssignment> partyFixedAssetAssignments)	{
		this.partyFixedAssetAssignments = partyFixedAssetAssignments;
	}
}
