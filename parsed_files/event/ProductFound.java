package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductFound implements Event{

	private List<Product> products;

	public ProductFound(List<Product> products) {
		this.setProducts(products);
	}

	public List<Product> getProducts()	{
		return products;
	}

	public void setProducts(List<Product> products)	{
		this.products = products;
	}
}
