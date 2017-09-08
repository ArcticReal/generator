package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class GlAccountRoleFound implements Event{

	private List<GlAccountRole> glAccountRoles;

	public GlAccountRoleFound(List<GlAccountRole> glAccountRoles) {
		this.setGlAccountRoles(glAccountRoles);
	}

	public List<GlAccountRole> getGlAccountRoles()	{
		return glAccountRoles;
	}

	public void setGlAccountRoles(List<GlAccountRole> glAccountRoles)	{
		this.glAccountRoles = glAccountRoles;
	}
}
