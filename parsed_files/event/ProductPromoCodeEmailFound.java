package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductPromoCodeEmailFound implements Event{

	private List<ProductPromoCodeEmail> productPromoCodeEmails;

	public ProductPromoCodeEmailFound(List<ProductPromoCodeEmail> productPromoCodeEmails) {
		this.setProductPromoCodeEmails(productPromoCodeEmails);
	}

	public List<ProductPromoCodeEmail> getProductPromoCodeEmails()	{
		return productPromoCodeEmails;
	}

	public void setProductPromoCodeEmails(List<ProductPromoCodeEmail> productPromoCodeEmails)	{
		this.productPromoCodeEmails = productPromoCodeEmails;
	}
}
