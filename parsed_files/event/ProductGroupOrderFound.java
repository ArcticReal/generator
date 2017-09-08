package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductGroupOrderFound implements Event{

	private List<ProductGroupOrder> productGroupOrders;

	public ProductGroupOrderFound(List<ProductGroupOrder> productGroupOrders) {
		this.setProductGroupOrders(productGroupOrders);
	}

	public List<ProductGroupOrder> getProductGroupOrders()	{
		return productGroupOrders;
	}

	public void setProductGroupOrders(List<ProductGroupOrder> productGroupOrders)	{
		this.productGroupOrders = productGroupOrders;
	}
}
