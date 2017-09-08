package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductConfigOptionFound implements Event{

	private List<ProductConfigOption> productConfigOptions;

	public ProductConfigOptionFound(List<ProductConfigOption> productConfigOptions) {
		this.setProductConfigOptions(productConfigOptions);
	}

	public List<ProductConfigOption> getProductConfigOptions()	{
		return productConfigOptions;
	}

	public void setProductConfigOptions(List<ProductConfigOption> productConfigOptions)	{
		this.productConfigOptions = productConfigOptions;
	}
}
