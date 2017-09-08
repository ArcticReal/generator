package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductConfigOptionIactnFound implements Event{

	private List<ProductConfigOptionIactn> productConfigOptionIactns;

	public ProductConfigOptionIactnFound(List<ProductConfigOptionIactn> productConfigOptionIactns) {
		this.setProductConfigOptionIactns(productConfigOptionIactns);
	}

	public List<ProductConfigOptionIactn> getProductConfigOptionIactns()	{
		return productConfigOptionIactns;
	}

	public void setProductConfigOptionIactns(List<ProductConfigOptionIactn> productConfigOptionIactns)	{
		this.productConfigOptionIactns = productConfigOptionIactns;
	}
}
