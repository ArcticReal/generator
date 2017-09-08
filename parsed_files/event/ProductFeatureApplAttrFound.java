package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductFeatureApplAttrFound implements Event{

	private List<ProductFeatureApplAttr> productFeatureApplAttrs;

	public ProductFeatureApplAttrFound(List<ProductFeatureApplAttr> productFeatureApplAttrs) {
		this.setProductFeatureApplAttrs(productFeatureApplAttrs);
	}

	public List<ProductFeatureApplAttr> getProductFeatureApplAttrs()	{
		return productFeatureApplAttrs;
	}

	public void setProductFeatureApplAttrs(List<ProductFeatureApplAttr> productFeatureApplAttrs)	{
		this.productFeatureApplAttrs = productFeatureApplAttrs;
	}
}
