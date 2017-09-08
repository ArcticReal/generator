package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SupplierRatingTypeFound implements Event{

	private List<SupplierRatingType> supplierRatingTypes;

	public SupplierRatingTypeFound(List<SupplierRatingType> supplierRatingTypes) {
		this.setSupplierRatingTypes(supplierRatingTypes);
	}

	public List<SupplierRatingType> getSupplierRatingTypes()	{
		return supplierRatingTypes;
	}

	public void setSupplierRatingTypes(List<SupplierRatingType> supplierRatingTypes)	{
		this.supplierRatingTypes = supplierRatingTypes;
	}
}
