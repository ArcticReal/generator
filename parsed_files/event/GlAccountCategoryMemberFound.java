package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class GlAccountCategoryMemberFound implements Event{

	private List<GlAccountCategoryMember> glAccountCategoryMembers;

	public GlAccountCategoryMemberFound(List<GlAccountCategoryMember> glAccountCategoryMembers) {
		this.setGlAccountCategoryMembers(glAccountCategoryMembers);
	}

	public List<GlAccountCategoryMember> getGlAccountCategoryMembers()	{
		return glAccountCategoryMembers;
	}

	public void setGlAccountCategoryMembers(List<GlAccountCategoryMember> glAccountCategoryMembers)	{
		this.glAccountCategoryMembers = glAccountCategoryMembers;
	}
}
