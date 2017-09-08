package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductKeywordFound implements Event{

	private List<ProductKeyword> productKeywords;

	public ProductKeywordFound(List<ProductKeyword> productKeywords) {
		this.setProductKeywords(productKeywords);
	}

	public List<ProductKeyword> getProductKeywords()	{
		return productKeywords;
	}

	public void setProductKeywords(List<ProductKeyword> productKeywords)	{
		this.productKeywords = productKeywords;
	}
}
