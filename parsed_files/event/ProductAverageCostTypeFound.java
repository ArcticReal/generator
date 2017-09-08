package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductAverageCostTypeFound implements Event{

	private List<ProductAverageCostType> productAverageCostTypes;

	public ProductAverageCostTypeFound(List<ProductAverageCostType> productAverageCostTypes) {
		this.setProductAverageCostTypes(productAverageCostTypes);
	}

	public List<ProductAverageCostType> getProductAverageCostTypes()	{
		return productAverageCostTypes;
	}

	public void setProductAverageCostTypes(List<ProductAverageCostType> productAverageCostTypes)	{
		this.productAverageCostTypes = productAverageCostTypes;
	}
}
