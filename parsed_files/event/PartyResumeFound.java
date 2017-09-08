package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PartyResumeFound implements Event{

	private List<PartyResume> partyResumes;

	public PartyResumeFound(List<PartyResume> partyResumes) {
		this.setPartyResumes(partyResumes);
	}

	public List<PartyResume> getPartyResumes()	{
		return partyResumes;
	}

	public void setPartyResumes(List<PartyResume> partyResumes)	{
		this.partyResumes = partyResumes;
	}
}
