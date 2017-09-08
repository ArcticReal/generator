package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OtherDataResourceFound implements Event{

	private List<OtherDataResource> otherDataResources;

	public OtherDataResourceFound(List<OtherDataResource> otherDataResources) {
		this.setOtherDataResources(otherDataResources);
	}

	public List<OtherDataResource> getOtherDataResources()	{
		return otherDataResources;
	}

	public void setOtherDataResources(List<OtherDataResource> otherDataResources)	{
		this.otherDataResources = otherDataResources;
	}
}
