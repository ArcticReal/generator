package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class TarpittedLoginViewFound implements Event{

	private List<TarpittedLoginView> tarpittedLoginViews;

	public TarpittedLoginViewFound(List<TarpittedLoginView> tarpittedLoginViews) {
		this.setTarpittedLoginViews(tarpittedLoginViews);
	}

	public List<TarpittedLoginView> getTarpittedLoginViews()	{
		return tarpittedLoginViews;
	}

	public void setTarpittedLoginViews(List<TarpittedLoginView> tarpittedLoginViews)	{
		this.tarpittedLoginViews = tarpittedLoginViews;
	}
}
