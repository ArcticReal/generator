package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductConfigProductFound implements Event{

	private List<ProductConfigProduct> productConfigProducts;

	public ProductConfigProductFound(List<ProductConfigProduct> productConfigProducts) {
		this.setProductConfigProducts(productConfigProducts);
	}

	public List<ProductConfigProduct> getProductConfigProducts()	{
		return productConfigProducts;
	}

	public void setProductConfigProducts(List<ProductConfigProduct> productConfigProducts)	{
		this.productConfigProducts = productConfigProducts;
	}
}
