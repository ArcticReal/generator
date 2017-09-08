package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProdCatalogCategoryTypeFound implements Event{

	private List<ProdCatalogCategoryType> prodCatalogCategoryTypes;

	public ProdCatalogCategoryTypeFound(List<ProdCatalogCategoryType> prodCatalogCategoryTypes) {
		this.setProdCatalogCategoryTypes(prodCatalogCategoryTypes);
	}

	public List<ProdCatalogCategoryType> getProdCatalogCategoryTypes()	{
		return prodCatalogCategoryTypes;
	}

	public void setProdCatalogCategoryTypes(List<ProdCatalogCategoryType> prodCatalogCategoryTypes)	{
		this.prodCatalogCategoryTypes = prodCatalogCategoryTypes;
	}
}
