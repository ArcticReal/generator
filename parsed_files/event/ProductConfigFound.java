package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductConfigFound implements Event{

	private List<ProductConfig> productConfigs;

	public ProductConfigFound(List<ProductConfig> productConfigs) {
		this.setProductConfigs(productConfigs);
	}

	public List<ProductConfig> getProductConfigs()	{
		return productConfigs;
	}

	public void setProductConfigs(List<ProductConfig> productConfigs)	{
		this.productConfigs = productConfigs;
	}
}
