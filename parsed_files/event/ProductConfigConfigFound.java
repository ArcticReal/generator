package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductConfigConfigFound implements Event{

	private List<ProductConfigConfig> productConfigConfigs;

	public ProductConfigConfigFound(List<ProductConfigConfig> productConfigConfigs) {
		this.setProductConfigConfigs(productConfigConfigs);
	}

	public List<ProductConfigConfig> getProductConfigConfigs()	{
		return productConfigConfigs;
	}

	public void setProductConfigConfigs(List<ProductConfigConfig> productConfigConfigs)	{
		this.productConfigConfigs = productConfigConfigs;
	}
}
