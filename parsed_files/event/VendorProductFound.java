package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class VendorProductFound implements Event{

	private List<VendorProduct> vendorProducts;

	public VendorProductFound(List<VendorProduct> vendorProducts) {
		this.setVendorProducts(vendorProducts);
	}

	public List<VendorProduct> getVendorProducts()	{
		return vendorProducts;
	}

	public void setVendorProducts(List<VendorProduct> vendorProducts)	{
		this.vendorProducts = vendorProducts;
	}
}
