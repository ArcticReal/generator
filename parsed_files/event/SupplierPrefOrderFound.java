package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SupplierPrefOrderFound implements Event{

	private List<SupplierPrefOrder> supplierPrefOrders;

	public SupplierPrefOrderFound(List<SupplierPrefOrder> supplierPrefOrders) {
		this.setSupplierPrefOrders(supplierPrefOrders);
	}

	public List<SupplierPrefOrder> getSupplierPrefOrders()	{
		return supplierPrefOrders;
	}

	public void setSupplierPrefOrders(List<SupplierPrefOrder> supplierPrefOrders)	{
		this.supplierPrefOrders = supplierPrefOrders;
	}
}
