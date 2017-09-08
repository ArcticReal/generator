package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductFeatureCategoryApplFound implements Event{

	private List<ProductFeatureCategoryAppl> productFeatureCategoryAppls;

	public ProductFeatureCategoryApplFound(List<ProductFeatureCategoryAppl> productFeatureCategoryAppls) {
		this.setProductFeatureCategoryAppls(productFeatureCategoryAppls);
	}

	public List<ProductFeatureCategoryAppl> getProductFeatureCategoryAppls()	{
		return productFeatureCategoryAppls;
	}

	public void setProductFeatureCategoryAppls(List<ProductFeatureCategoryAppl> productFeatureCategoryAppls)	{
		this.productFeatureCategoryAppls = productFeatureCategoryAppls;
	}
}
