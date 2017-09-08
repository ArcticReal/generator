package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductOrderItemFound implements Event{

	private List<ProductOrderItem> productOrderItems;

	public ProductOrderItemFound(List<ProductOrderItem> productOrderItems) {
		this.setProductOrderItems(productOrderItems);
	}

	public List<ProductOrderItem> getProductOrderItems()	{
		return productOrderItems;
	}

	public void setProductOrderItems(List<ProductOrderItem> productOrderItems)	{
		this.productOrderItems = productOrderItems;
	}
}
