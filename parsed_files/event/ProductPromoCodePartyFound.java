package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductPromoCodePartyFound implements Event{

	private List<ProductPromoCodeParty> productPromoCodePartys;

	public ProductPromoCodePartyFound(List<ProductPromoCodeParty> productPromoCodePartys) {
		this.setProductPromoCodePartys(productPromoCodePartys);
	}

	public List<ProductPromoCodeParty> getProductPromoCodePartys()	{
		return productPromoCodePartys;
	}

	public void setProductPromoCodePartys(List<ProductPromoCodeParty> productPromoCodePartys)	{
		this.productPromoCodePartys = productPromoCodePartys;
	}
}
