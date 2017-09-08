package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProdCatalogCategoryFound implements Event{

	private List<ProdCatalogCategory> prodCatalogCategorys;

	public ProdCatalogCategoryFound(List<ProdCatalogCategory> prodCatalogCategorys) {
		this.setProdCatalogCategorys(prodCatalogCategorys);
	}

	public List<ProdCatalogCategory> getProdCatalogCategorys()	{
		return prodCatalogCategorys;
	}

	public void setProdCatalogCategorys(List<ProdCatalogCategory> prodCatalogCategorys)	{
		this.prodCatalogCategorys = prodCatalogCategorys;
	}
}
