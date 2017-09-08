package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProdCatalogInvFacilityFound implements Event{

	private List<ProdCatalogInvFacility> prodCatalogInvFacilitys;

	public ProdCatalogInvFacilityFound(List<ProdCatalogInvFacility> prodCatalogInvFacilitys) {
		this.setProdCatalogInvFacilitys(prodCatalogInvFacilitys);
	}

	public List<ProdCatalogInvFacility> getProdCatalogInvFacilitys()	{
		return prodCatalogInvFacilitys;
	}

	public void setProdCatalogInvFacilitys(List<ProdCatalogInvFacility> prodCatalogInvFacilitys)	{
		this.prodCatalogInvFacilitys = prodCatalogInvFacilitys;
	}
}
