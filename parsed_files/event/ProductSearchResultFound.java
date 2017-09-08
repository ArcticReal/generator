package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductSearchResultFound implements Event{

	private List<ProductSearchResult> productSearchResults;

	public ProductSearchResultFound(List<ProductSearchResult> productSearchResults) {
		this.setProductSearchResults(productSearchResults);
	}

	public List<ProductSearchResult> getProductSearchResults()	{
		return productSearchResults;
	}

	public void setProductSearchResults(List<ProductSearchResult> productSearchResults)	{
		this.productSearchResults = productSearchResults;
	}
}
