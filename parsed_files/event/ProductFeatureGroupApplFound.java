package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductFeatureGroupApplFound implements Event{

	private List<ProductFeatureGroupAppl> productFeatureGroupAppls;

	public ProductFeatureGroupApplFound(List<ProductFeatureGroupAppl> productFeatureGroupAppls) {
		this.setProductFeatureGroupAppls(productFeatureGroupAppls);
	}

	public List<ProductFeatureGroupAppl> getProductFeatureGroupAppls()	{
		return productFeatureGroupAppls;
	}

	public void setProductFeatureGroupAppls(List<ProductFeatureGroupAppl> productFeatureGroupAppls)	{
		this.productFeatureGroupAppls = productFeatureGroupAppls;
	}
}
