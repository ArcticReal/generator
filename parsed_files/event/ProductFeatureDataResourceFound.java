package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductFeatureDataResourceFound implements Event{

	private List<ProductFeatureDataResource> productFeatureDataResources;

	public ProductFeatureDataResourceFound(List<ProductFeatureDataResource> productFeatureDataResources) {
		this.setProductFeatureDataResources(productFeatureDataResources);
	}

	public List<ProductFeatureDataResource> getProductFeatureDataResources()	{
		return productFeatureDataResources;
	}

	public void setProductFeatureDataResources(List<ProductFeatureDataResource> productFeatureDataResources)	{
		this.productFeatureDataResources = productFeatureDataResources;
	}
}
