package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProtectedViewFound implements Event{

	private List<ProtectedView> protectedViews;

	public ProtectedViewFound(List<ProtectedView> protectedViews) {
		this.setProtectedViews(protectedViews);
	}

	public List<ProtectedView> getProtectedViews()	{
		return protectedViews;
	}

	public void setProtectedViews(List<ProtectedView> protectedViews)	{
		this.protectedViews = protectedViews;
	}
}
