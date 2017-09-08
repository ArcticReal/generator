package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PartySkillFound implements Event{

	private List<PartySkill> partySkills;

	public PartySkillFound(List<PartySkill> partySkills) {
		this.setPartySkills(partySkills);
	}

	public List<PartySkill> getPartySkills()	{
		return partySkills;
	}

	public void setPartySkills(List<PartySkill> partySkills)	{
		this.partySkills = partySkills;
	}
}
