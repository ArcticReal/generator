package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProdCatalogRoleFound implements Event{

	private List<ProdCatalogRole> prodCatalogRoles;

	public ProdCatalogRoleFound(List<ProdCatalogRole> prodCatalogRoles) {
		this.setProdCatalogRoles(prodCatalogRoles);
	}

	public List<ProdCatalogRole> getProdCatalogRoles()	{
		return prodCatalogRoles;
	}

	public void setProdCatalogRoles(List<ProdCatalogRole> prodCatalogRoles)	{
		this.prodCatalogRoles = prodCatalogRoles;
	}
}
