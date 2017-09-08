package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductStoreFound implements Event{

	private List<ProductStore> productStores;

	public ProductStoreFound(List<ProductStore> productStores) {
		this.setProductStores(productStores);
	}

	public List<ProductStore> getProductStores()	{
		return productStores;
	}

	public void setProductStores(List<ProductStore> productStores)	{
		this.productStores = productStores;
	}
}