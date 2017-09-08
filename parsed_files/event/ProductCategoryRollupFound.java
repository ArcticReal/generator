package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductCategoryRollupFound implements Event{

	private List<ProductCategoryRollup> productCategoryRollups;

	public ProductCategoryRollupFound(List<ProductCategoryRollup> productCategoryRollups) {
		this.setProductCategoryRollups(productCategoryRollups);
	}

	public List<ProductCategoryRollup> getProductCategoryRollups()	{
		return productCategoryRollups;
	}

	public void setProductCategoryRollups(List<ProductCategoryRollup> productCategoryRollups)	{
		this.productCategoryRollups = productCategoryRollups;
	}
}
