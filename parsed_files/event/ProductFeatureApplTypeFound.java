package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductFeatureApplTypeFound implements Event{

	private List<ProductFeatureApplType> productFeatureApplTypes;

	public ProductFeatureApplTypeFound(List<ProductFeatureApplType> productFeatureApplTypes) {
		this.setProductFeatureApplTypes(productFeatureApplTypes);
	}

	public List<ProductFeatureApplType> getProductFeatureApplTypes()	{
		return productFeatureApplTypes;
	}

	public void setProductFeatureApplTypes(List<ProductFeatureApplType> productFeatureApplTypes)	{
		this.productFeatureApplTypes = productFeatureApplTypes;
	}
}
