package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProdConfItemContentTypeFound implements Event{

	private List<ProdConfItemContentType> prodConfItemContentTypes;

	public ProdConfItemContentTypeFound(List<ProdConfItemContentType> prodConfItemContentTypes) {
		this.setProdConfItemContentTypes(prodConfItemContentTypes);
	}

	public List<ProdConfItemContentType> getProdConfItemContentTypes()	{
		return prodConfItemContentTypes;
	}

	public void setProdConfItemContentTypes(List<ProdConfItemContentType> prodConfItemContentTypes)	{
		this.prodConfItemContentTypes = prodConfItemContentTypes;
	}
}
