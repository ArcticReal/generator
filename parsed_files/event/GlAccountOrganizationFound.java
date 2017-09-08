package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class GlAccountOrganizationFound implements Event{

	private List<GlAccountOrganization> glAccountOrganizations;

	public GlAccountOrganizationFound(List<GlAccountOrganization> glAccountOrganizations) {
		this.setGlAccountOrganizations(glAccountOrganizations);
	}

	public List<GlAccountOrganization> getGlAccountOrganizations()	{
		return glAccountOrganizations;
	}

	public void setGlAccountOrganizations(List<GlAccountOrganization> glAccountOrganizations)	{
		this.glAccountOrganizations = glAccountOrganizations;
	}
}
