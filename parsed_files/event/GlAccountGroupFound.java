package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class GlAccountGroupFound implements Event{

	private List<GlAccountGroup> glAccountGroups;

	public GlAccountGroupFound(List<GlAccountGroup> glAccountGroups) {
		this.setGlAccountGroups(glAccountGroups);
	}

	public List<GlAccountGroup> getGlAccountGroups()	{
		return glAccountGroups;
	}

	public void setGlAccountGroups(List<GlAccountGroup> glAccountGroups)	{
		this.glAccountGroups = glAccountGroups;
	}
}
