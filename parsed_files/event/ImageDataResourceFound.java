package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ImageDataResourceFound implements Event{

	private List<ImageDataResource> imageDataResources;

	public ImageDataResourceFound(List<ImageDataResource> imageDataResources) {
		this.setImageDataResources(imageDataResources);
	}

	public List<ImageDataResource> getImageDataResources()	{
		return imageDataResources;
	}

	public void setImageDataResources(List<ImageDataResource> imageDataResources)	{
		this.imageDataResources = imageDataResources;
	}
}
