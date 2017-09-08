package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class VideoDataResourceFound implements Event{

	private List<VideoDataResource> videoDataResources;

	public VideoDataResourceFound(List<VideoDataResource> videoDataResources) {
		this.setVideoDataResources(videoDataResources);
	}

	public List<VideoDataResource> getVideoDataResources()	{
		return videoDataResources;
	}

	public void setVideoDataResources(List<VideoDataResource> videoDataResources)	{
		this.videoDataResources = videoDataResources;
	}
}
