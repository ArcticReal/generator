package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class GlXbrlClassFound implements Event{

	private List<GlXbrlClass> glXbrlClasss;

	public GlXbrlClassFound(List<GlXbrlClass> glXbrlClasss) {
		this.setGlXbrlClasss(glXbrlClasss);
	}

	public List<GlXbrlClass> getGlXbrlClasss()	{
		return glXbrlClasss;
	}

	public void setGlXbrlClasss(List<GlXbrlClass> glXbrlClasss)	{
		this.glXbrlClasss = glXbrlClasss;
	}
}
