package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductFeatureCategoryFound implements Event{

	private List<ProductFeatureCategory> productFeatureCategorys;

	public ProductFeatureCategoryFound(List<ProductFeatureCategory> productFeatureCategorys) {
		this.setProductFeatureCategorys(productFeatureCategorys);
	}

	public List<ProductFeatureCategory> getProductFeatureCategorys()	{
		return productFeatureCategorys;
	}

	public void setProductFeatureCategorys(List<ProductFeatureCategory> productFeatureCategorys)	{
		this.productFeatureCategorys = productFeatureCategorys;
	}
}
