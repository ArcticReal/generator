package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductAverageCostFound implements Event{

	private List<ProductAverageCost> productAverageCosts;

	public ProductAverageCostFound(List<ProductAverageCost> productAverageCosts) {
		this.setProductAverageCosts(productAverageCosts);
	}

	public List<ProductAverageCost> getProductAverageCosts()	{
		return productAverageCosts;
	}

	public void setProductAverageCosts(List<ProductAverageCost> productAverageCosts)	{
		this.productAverageCosts = productAverageCosts;
	}
}
