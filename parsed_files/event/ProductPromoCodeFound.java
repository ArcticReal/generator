package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductPromoCodeFound implements Event{

	private List<ProductPromoCode> productPromoCodes;

	public ProductPromoCodeFound(List<ProductPromoCode> productPromoCodes) {
		this.setProductPromoCodes(productPromoCodes);
	}

	public List<ProductPromoCode> getProductPromoCodes()	{
		return productPromoCodes;
	}

	public void setProductPromoCodes(List<ProductPromoCode> productPromoCodes)	{
		this.productPromoCodes = productPromoCodes;
	}
}
