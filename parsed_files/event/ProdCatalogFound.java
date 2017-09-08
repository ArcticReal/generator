package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProdCatalogFound implements Event{

	private List<ProdCatalog> prodCatalogs;

	public ProdCatalogFound(List<ProdCatalog> prodCatalogs) {
		this.setProdCatalogs(prodCatalogs);
	}

	public List<ProdCatalog> getProdCatalogs()	{
		return prodCatalogs;
	}

	public void setProdCatalogs(List<ProdCatalog> prodCatalogs)	{
		this.prodCatalogs = prodCatalogs;
	}
}