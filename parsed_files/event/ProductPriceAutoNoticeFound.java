package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductPriceAutoNoticeFound implements Event{

	private List<ProductPriceAutoNotice> productPriceAutoNotices;

	public ProductPriceAutoNoticeFound(List<ProductPriceAutoNotice> productPriceAutoNotices) {
		this.setProductPriceAutoNotices(productPriceAutoNotices);
	}

	public List<ProductPriceAutoNotice> getProductPriceAutoNotices()	{
		return productPriceAutoNotices;
	}

	public void setProductPriceAutoNotices(List<ProductPriceAutoNotice> productPriceAutoNotices)	{
		this.productPriceAutoNotices = productPriceAutoNotices;
	}
}
