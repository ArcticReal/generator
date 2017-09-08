package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductPromoContentFound implements Event{

	private List<ProductPromoContent> productPromoContents;

	public ProductPromoContentFound(List<ProductPromoContent> productPromoContents) {
		this.setProductPromoContents(productPromoContents);
	}

	public List<ProductPromoContent> getProductPromoContents()	{
		return productPromoContents;
	}

	public void setProductPromoContents(List<ProductPromoContent> productPromoContents)	{
		this.productPromoContents = productPromoContents;
	}
}
