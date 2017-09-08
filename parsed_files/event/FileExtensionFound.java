package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FileExtensionFound implements Event{

	private List<FileExtension> fileExtensions;

	public FileExtensionFound(List<FileExtension> fileExtensions) {
		this.setFileExtensions(fileExtensions);
	}

	public List<FileExtension> getFileExtensions()	{
		return fileExtensions;
	}

	public void setFileExtensions(List<FileExtension> fileExtensions)	{
		this.fileExtensions = fileExtensions;
	}
}
