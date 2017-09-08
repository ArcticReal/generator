package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductFeatureIactnTypeFound implements Event{

	private List<ProductFeatureIactnType> productFeatureIactnTypes;

	public ProductFeatureIactnTypeFound(List<ProductFeatureIactnType> productFeatureIactnTypes) {
		this.setProductFeatureIactnTypes(productFeatureIactnTypes);
	}

	public List<ProductFeatureIactnType> getProductFeatureIactnTypes()	{
		return productFeatureIactnTypes;
	}

	public void setProductFeatureIactnTypes(List<ProductFeatureIactnType> productFeatureIactnTypes)	{
		this.productFeatureIactnTypes = productFeatureIactnTypes;
	}
}
