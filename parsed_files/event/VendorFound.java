package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class VendorFound implements Event{

	private List<Vendor> vendors;

	public VendorFound(List<Vendor> vendors) {
		this.setVendors(vendors);
	}

	public List<Vendor> getVendors()	{
		return vendors;
	}

	public void setVendors(List<Vendor> vendors)	{
		this.vendors = vendors;
	}
}
