package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class GlResourceTypeFound implements Event{

	private List<GlResourceType> glResourceTypes;

	public GlResourceTypeFound(List<GlResourceType> glResourceTypes) {
		this.setGlResourceTypes(glResourceTypes);
	}

	public List<GlResourceType> getGlResourceTypes()	{
		return glResourceTypes;
	}

	public void setGlResourceTypes(List<GlResourceType> glResourceTypes)	{
		this.glResourceTypes = glResourceTypes;
	}
}
