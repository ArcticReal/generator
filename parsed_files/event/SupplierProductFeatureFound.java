package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SupplierProductFeatureFound implements Event{

	private List<SupplierProductFeature> supplierProductFeatures;

	public SupplierProductFeatureFound(List<SupplierProductFeature> supplierProductFeatures) {
		this.setSupplierProductFeatures(supplierProductFeatures);
	}

	public List<SupplierProductFeature> getSupplierProductFeatures()	{
		return supplierProductFeatures;
	}

	public void setSupplierProductFeatures(List<SupplierProductFeature> supplierProductFeatures)	{
		this.supplierProductFeatures = supplierProductFeatures;
	}
}
