package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductStoreKeywordOvrdFound implements Event{

	private List<ProductStoreKeywordOvrd> productStoreKeywordOvrds;

	public ProductStoreKeywordOvrdFound(List<ProductStoreKeywordOvrd> productStoreKeywordOvrds) {
		this.setProductStoreKeywordOvrds(productStoreKeywordOvrds);
	}

	public List<ProductStoreKeywordOvrd> getProductStoreKeywordOvrds()	{
		return productStoreKeywordOvrds;
	}

	public void setProductStoreKeywordOvrds(List<ProductStoreKeywordOvrd> productStoreKeywordOvrds)	{
		this.productStoreKeywordOvrds = productStoreKeywordOvrds;
	}
}
