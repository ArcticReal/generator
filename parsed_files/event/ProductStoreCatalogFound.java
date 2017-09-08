package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductStoreCatalogFound implements Event{

	private List<ProductStoreCatalog> productStoreCatalogs;

	public ProductStoreCatalogFound(List<ProductStoreCatalog> productStoreCatalogs) {
		this.setProductStoreCatalogs(productStoreCatalogs);
	}

	public List<ProductStoreCatalog> getProductStoreCatalogs()	{
		return productStoreCatalogs;
	}

	public void setProductStoreCatalogs(List<ProductStoreCatalog> productStoreCatalogs)	{
		this.productStoreCatalogs = productStoreCatalogs;
	}
}
