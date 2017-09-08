package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductFeatureFound implements Event{

	private List<ProductFeature> productFeatures;

	public ProductFeatureFound(List<ProductFeature> productFeatures) {
		this.setProductFeatures(productFeatures);
	}

	public List<ProductFeature> getProductFeatures()	{
		return productFeatures;
	}

	public void setProductFeatures(List<ProductFeature> productFeatures)	{
		this.productFeatures = productFeatures;
	}
}
