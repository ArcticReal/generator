package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductFeatureTypeFound implements Event{

	private List<ProductFeatureType> productFeatureTypes;

	public ProductFeatureTypeFound(List<ProductFeatureType> productFeatureTypes) {
		this.setProductFeatureTypes(productFeatureTypes);
	}

	public List<ProductFeatureType> getProductFeatureTypes()	{
		return productFeatureTypes;
	}

	public void setProductFeatureTypes(List<ProductFeatureType> productFeatureTypes)	{
		this.productFeatureTypes = productFeatureTypes;
	}
}
