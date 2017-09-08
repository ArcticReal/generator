package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductSubscriptionResourceFound implements Event{

	private List<ProductSubscriptionResource> productSubscriptionResources;

	public ProductSubscriptionResourceFound(List<ProductSubscriptionResource> productSubscriptionResources) {
		this.setProductSubscriptionResources(productSubscriptionResources);
	}

	public List<ProductSubscriptionResource> getProductSubscriptionResources()	{
		return productSubscriptionResources;
	}

	public void setProductSubscriptionResources(List<ProductSubscriptionResource> productSubscriptionResources)	{
		this.productSubscriptionResources = productSubscriptionResources;
	}
}
