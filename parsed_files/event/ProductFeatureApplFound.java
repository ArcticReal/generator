package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductFeatureApplFound implements Event{

	private List<ProductFeatureAppl> productFeatureAppls;

	public ProductFeatureApplFound(List<ProductFeatureAppl> productFeatureAppls) {
		this.setProductFeatureAppls(productFeatureAppls);
	}

	public List<ProductFeatureAppl> getProductFeatureAppls()	{
		return productFeatureAppls;
	}

	public void setProductFeatureAppls(List<ProductFeatureAppl> productFeatureAppls)	{
		this.productFeatureAppls = productFeatureAppls;
	}
}
