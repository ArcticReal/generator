package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class GlAccountTypeDefaultFound implements Event{

	private List<GlAccountTypeDefault> glAccountTypeDefaults;

	public GlAccountTypeDefaultFound(List<GlAccountTypeDefault> glAccountTypeDefaults) {
		this.setGlAccountTypeDefaults(glAccountTypeDefaults);
	}

	public List<GlAccountTypeDefault> getGlAccountTypeDefaults()	{
		return glAccountTypeDefaults;
	}

	public void setGlAccountTypeDefaults(List<GlAccountTypeDefault> glAccountTypeDefaults)	{
		this.glAccountTypeDefaults = glAccountTypeDefaults;
	}
}
