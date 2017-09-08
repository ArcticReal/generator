package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductFeatureIactnFound implements Event{

	private List<ProductFeatureIactn> productFeatureIactns;

	public ProductFeatureIactnFound(List<ProductFeatureIactn> productFeatureIactns) {
		this.setProductFeatureIactns(productFeatureIactns);
	}

	public List<ProductFeatureIactn> getProductFeatureIactns()	{
		return productFeatureIactns;
	}

	public void setProductFeatureIactns(List<ProductFeatureIactn> productFeatureIactns)	{
		this.productFeatureIactns = productFeatureIactns;
	}
}
