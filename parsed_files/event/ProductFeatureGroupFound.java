package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductFeatureGroupFound implements Event{

	private List<ProductFeatureGroup> productFeatureGroups;

	public ProductFeatureGroupFound(List<ProductFeatureGroup> productFeatureGroups) {
		this.setProductFeatureGroups(productFeatureGroups);
	}

	public List<ProductFeatureGroup> getProductFeatureGroups()	{
		return productFeatureGroups;
	}

	public void setProductFeatureGroups(List<ProductFeatureGroup> productFeatureGroups)	{
		this.productFeatureGroups = productFeatureGroups;
	}
}
