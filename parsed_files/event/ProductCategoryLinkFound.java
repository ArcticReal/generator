package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductCategoryLinkFound implements Event{

	private List<ProductCategoryLink> productCategoryLinks;

	public ProductCategoryLinkFound(List<ProductCategoryLink> productCategoryLinks) {
		this.setProductCategoryLinks(productCategoryLinks);
	}

	public List<ProductCategoryLink> getProductCategoryLinks()	{
		return productCategoryLinks;
	}

	public void setProductCategoryLinks(List<ProductCategoryLink> productCategoryLinks)	{
		this.productCategoryLinks = productCategoryLinks;
	}
}
