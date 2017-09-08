package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductFeaturePriceFound implements Event{

	private List<ProductFeaturePrice> productFeaturePrices;

	public ProductFeaturePriceFound(List<ProductFeaturePrice> productFeaturePrices) {
		this.setProductFeaturePrices(productFeaturePrices);
	}

	public List<ProductFeaturePrice> getProductFeaturePrices()	{
		return productFeaturePrices;
	}

	public void setProductFeaturePrices(List<ProductFeaturePrice> productFeaturePrices)	{
		this.productFeaturePrices = productFeaturePrices;
	}
}
