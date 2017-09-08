package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SkillTypeFound implements Event{

	private List<SkillType> skillTypes;

	public SkillTypeFound(List<SkillType> skillTypes) {
		this.setSkillTypes(skillTypes);
	}

	public List<SkillType> getSkillTypes()	{
		return skillTypes;
	}

	public void setSkillTypes(List<SkillType> skillTypes)	{
		this.skillTypes = skillTypes;
	}
}
