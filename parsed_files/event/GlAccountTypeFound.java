package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class GlAccountTypeFound implements Event{

	private List<GlAccountType> glAccountTypes;

	public GlAccountTypeFound(List<GlAccountType> glAccountTypes) {
		this.setGlAccountTypes(glAccountTypes);
	}

	public List<GlAccountType> getGlAccountTypes()	{
		return glAccountTypes;
	}

	public void setGlAccountTypes(List<GlAccountType> glAccountTypes)	{
		this.glAccountTypes = glAccountTypes;
	}
}
