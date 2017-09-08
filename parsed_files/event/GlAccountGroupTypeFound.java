package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class GlAccountGroupTypeFound implements Event{

	private List<GlAccountGroupType> glAccountGroupTypes;

	public GlAccountGroupTypeFound(List<GlAccountGroupType> glAccountGroupTypes) {
		this.setGlAccountGroupTypes(glAccountGroupTypes);
	}

	public List<GlAccountGroupType> getGlAccountGroupTypes()	{
		return glAccountGroupTypes;
	}

	public void setGlAccountGroupTypes(List<GlAccountGroupType> glAccountGroupTypes)	{
		this.glAccountGroupTypes = glAccountGroupTypes;
	}
}
