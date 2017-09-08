package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PostalAddressBoundaryFound implements Event{

	private List<PostalAddressBoundary> postalAddressBoundarys;

	public PostalAddressBoundaryFound(List<PostalAddressBoundary> postalAddressBoundarys) {
		this.setPostalAddressBoundarys(postalAddressBoundarys);
	}

	public List<PostalAddressBoundary> getPostalAddressBoundarys()	{
		return postalAddressBoundarys;
	}

	public void setPostalAddressBoundarys(List<PostalAddressBoundary> postalAddressBoundarys)	{
		this.postalAddressBoundarys = postalAddressBoundarys;
	}
}
