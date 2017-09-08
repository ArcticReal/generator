package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductPromoFound implements Event{

	private List<ProductPromo> productPromos;

	public ProductPromoFound(List<ProductPromo> productPromos) {
		this.setProductPromos(productPromos);
	}

	public List<ProductPromo> getProductPromos()	{
		return productPromos;
	}

	public void setProductPromos(List<ProductPromo> productPromos)	{
		this.productPromos = productPromos;
	}
}
