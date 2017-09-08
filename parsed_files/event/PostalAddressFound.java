package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PostalAddressFound implements Event{

	private List<PostalAddress> postalAddresss;

	public PostalAddressFound(List<PostalAddress> postalAddresss) {
		this.setPostalAddresss(postalAddresss);
	}

	public List<PostalAddress> getPostalAddresss()	{
		return postalAddresss;
	}

	public void setPostalAddresss(List<PostalAddress> postalAddresss)	{
		this.postalAddresss = postalAddresss;
	}
}
