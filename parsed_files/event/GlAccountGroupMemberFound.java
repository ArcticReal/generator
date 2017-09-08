package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class GlAccountGroupMemberFound implements Event{

	private List<GlAccountGroupMember> glAccountGroupMembers;

	public GlAccountGroupMemberFound(List<GlAccountGroupMember> glAccountGroupMembers) {
		this.setGlAccountGroupMembers(glAccountGroupMembers);
	}

	public List<GlAccountGroupMember> getGlAccountGroupMembers()	{
		return glAccountGroupMembers;
	}

	public void setGlAccountGroupMembers(List<GlAccountGroupMember> glAccountGroupMembers)	{
		this.glAccountGroupMembers = glAccountGroupMembers;
	}
}
