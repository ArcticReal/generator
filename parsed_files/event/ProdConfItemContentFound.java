package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProdConfItemContentFound implements Event{

	private List<ProdConfItemContent> prodConfItemContents;

	public ProdConfItemContentFound(List<ProdConfItemContent> prodConfItemContents) {
		this.setProdConfItemContents(prodConfItemContents);
	}

	public List<ProdConfItemContent> getProdConfItemContents()	{
		return prodConfItemContents;
	}

	public void setProdConfItemContents(List<ProdConfItemContent> prodConfItemContents)	{
		this.prodConfItemContents = prodConfItemContents;
	}
}
