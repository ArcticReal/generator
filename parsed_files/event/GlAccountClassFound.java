package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class GlAccountClassFound implements Event{

	private List<GlAccountClass> glAccountClasss;

	public GlAccountClassFound(List<GlAccountClass> glAccountClasss) {
		this.setGlAccountClasss(glAccountClasss);
	}

	public List<GlAccountClass> getGlAccountClasss()	{
		return glAccountClasss;
	}

	public void setGlAccountClasss(List<GlAccountClass> glAccountClasss)	{
		this.glAccountClasss = glAccountClasss;
	}
}
