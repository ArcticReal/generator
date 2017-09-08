package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SupplierProductFound implements Event{

	private List<SupplierProduct> supplierProducts;

	public SupplierProductFound(List<SupplierProduct> supplierProducts) {
		this.setSupplierProducts(supplierProducts);
	}

	public List<SupplierProduct> getSupplierProducts()	{
		return supplierProducts;
	}

	public void setSupplierProducts(List<SupplierProduct> supplierProducts)	{
		this.supplierProducts = supplierProducts;
	}
}
