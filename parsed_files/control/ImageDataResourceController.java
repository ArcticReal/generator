package com.skytala.eCommerce.control;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Splitter;
import com.skytala.eCommerce.command.AddImageDataResource;
import com.skytala.eCommerce.command.DeleteImageDataResource;
import com.skytala.eCommerce.command.UpdateImageDataResource;
import com.skytala.eCommerce.entity.ImageDataResource;
import com.skytala.eCommerce.entity.ImageDataResourceMapper;
import com.skytala.eCommerce.event.ImageDataResourceAdded;
import com.skytala.eCommerce.event.ImageDataResourceDeleted;
import com.skytala.eCommerce.event.ImageDataResourceFound;
import com.skytala.eCommerce.event.ImageDataResourceUpdated;
import com.skytala.eCommerce.query.FindImageDataResourcesBy;

@RestController
@RequestMapping("/api/imageDataResource")
public class ImageDataResourceController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ImageDataResource>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ImageDataResourceController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ImageDataResource
	 * @return a List with the ImageDataResources
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ImageDataResource> findImageDataResourcesBy(@RequestParam Map<String, String> allRequestParams) {

		FindImageDataResourcesBy query = new FindImageDataResourcesBy(allRequestParams);

		int usedTicketId;

		synchronized (ImageDataResourceController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ImageDataResourceFound.class,
				event -> sendImageDataResourcesFoundMessage(((ImageDataResourceFound) event).getImageDataResources(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendImageDataResourcesFoundMessage(List<ImageDataResource> imageDataResources, int usedTicketId) {
		queryReturnVal.put(usedTicketId, imageDataResources);
	}

	/**
	 * 
	 * this method will only be called by Springs DispatcherServlet
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @return true on success; false on fail
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/add", consumes = "application/x-www-form-urlencoded")
	public boolean createImageDataResource(HttpServletRequest request) {

		ImageDataResource imageDataResourceToBeAdded = new ImageDataResource();
		try {
			imageDataResourceToBeAdded = ImageDataResourceMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createImageDataResource(imageDataResourceToBeAdded);

	}

	/**
	 * creates a new ImageDataResource entry in the ofbiz database
	 * 
	 * @param imageDataResourceToBeAdded
	 *            the ImageDataResource thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createImageDataResource(ImageDataResource imageDataResourceToBeAdded) {

		AddImageDataResource com = new AddImageDataResource(imageDataResourceToBeAdded);
		int usedTicketId;

		synchronized (ImageDataResourceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ImageDataResourceAdded.class,
				event -> sendImageDataResourceChangedMessage(((ImageDataResourceAdded) event).isSuccess(), usedTicketId));

		try {
			Scheduler.instance().schedule(com).executeNext();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		while (!commandReturnVal.containsKey(usedTicketId)) {
		}

		return commandReturnVal.remove(usedTicketId);

	}

	/**
	 * this method will only be called by Springs DispatcherServlet
	 * 
	 * @param request HttpServletRequest object
	 * @return true on success, false on fail
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "/update", consumes = "application/x-www-form-urlencoded")
	public boolean updateImageDataResource(HttpServletRequest request) {

		BufferedReader br;
		String data = null;
		Map<String, String> dataMap = null;

		try {
			br = new BufferedReader(new InputStreamReader(request.getInputStream()));
			if (br != null) {
				data = java.net.URLDecoder.decode(br.readLine(), "UTF-8");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}

		dataMap = Splitter.on('&').trimResults().withKeyValueSeparator(Splitter.on('=').limit(2).trimResults())
				.split(data);

		ImageDataResource imageDataResourceToBeUpdated = new ImageDataResource();

		try {
			imageDataResourceToBeUpdated = ImageDataResourceMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateImageDataResource(imageDataResourceToBeUpdated);

	}

	/**
	 * Updates the ImageDataResource with the specific Id
	 * 
	 * @param imageDataResourceToBeUpdated the ImageDataResource thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateImageDataResource(ImageDataResource imageDataResourceToBeUpdated) {

		UpdateImageDataResource com = new UpdateImageDataResource(imageDataResourceToBeUpdated);

		int usedTicketId;

		synchronized (ImageDataResourceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ImageDataResourceUpdated.class,
				event -> sendImageDataResourceChangedMessage(((ImageDataResourceUpdated) event).isSuccess(), usedTicketId));

		try {
			Scheduler.instance().schedule(com).executeNext();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		while (!commandReturnVal.containsKey(usedTicketId)) {
		}

		return commandReturnVal.remove(usedTicketId);
	}

	/**
	 * removes a ImageDataResource from the database
	 * 
	 * @param imageDataResourceId:
	 *            the id of the ImageDataResource thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteimageDataResourceById(@RequestParam(value = "imageDataResourceId") String imageDataResourceId) {

		DeleteImageDataResource com = new DeleteImageDataResource(imageDataResourceId);

		int usedTicketId;

		synchronized (ImageDataResourceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ImageDataResourceDeleted.class,
				event -> sendImageDataResourceChangedMessage(((ImageDataResourceDeleted) event).isSuccess(), usedTicketId));

		try {
			Scheduler.instance().schedule(com).executeNext();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		while (!commandReturnVal.containsKey(usedTicketId)) {
		}

		return commandReturnVal.remove(usedTicketId);
	}

	public void sendImageDataResourceChangedMessage(boolean success, int usedTicketId) {
		commandReturnVal.put(usedTicketId, success);
	}

	@RequestMapping(value = (" * "))
	public String returnErrorPage(HttpServletRequest request) {

		String usedUri = request.getRequestURI();
		String[] splittedString = usedUri.split("/");

		String usedRequest = splittedString[splittedString.length - 1];

		if (validRequests.containsKey(usedRequest)) {
			return "Error: request method " + request.getMethod() + " not allowed for \"" + usedUri + "\"!\n"
					+ "Please use " + validRequests.get(usedRequest) + "!";

		}

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/imageDataResource/\" plus one of the following: "
				+ "";

		Set<String> keySet = validRequests.keySet();
		Iterator<String> it = keySet.iterator();

		while (it.hasNext()) {
			returnVal += "\"" + it.next() + "\"";
			if (it.hasNext())
				returnVal += ", ";
		}

		returnVal += "!";

		return returnVal;

	}
}
