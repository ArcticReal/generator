package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductConfigItemFound implements Event{

	private List<ProductConfigItem> productConfigItems;

	public ProductConfigItemFound(List<ProductConfigItem> productConfigItems) {
		this.setProductConfigItems(productConfigItems);
	}

	public List<ProductConfigItem> getProductConfigItems()	{
		return productConfigItems;
	}

	public void setProductConfigItems(List<ProductConfigItem> productConfigItems)	{
		this.productConfigItems = productConfigItems;
	}
}
