package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductPromoProductFound implements Event{

	private List<ProductPromoProduct> productPromoProducts;

	public ProductPromoProductFound(List<ProductPromoProduct> productPromoProducts) {
		this.setProductPromoProducts(productPromoProducts);
	}

	public List<ProductPromoProduct> getProductPromoProducts()	{
		return productPromoProducts;
	}

	public void setProductPromoProducts(List<ProductPromoProduct> productPromoProducts)	{
		this.productPromoProducts = productPromoProducts;
	}
}
