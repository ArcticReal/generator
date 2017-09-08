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
import com.skytala.eCommerce.command.AddVideoDataResource;
import com.skytala.eCommerce.command.DeleteVideoDataResource;
import com.skytala.eCommerce.command.UpdateVideoDataResource;
import com.skytala.eCommerce.entity.VideoDataResource;
import com.skytala.eCommerce.entity.VideoDataResourceMapper;
import com.skytala.eCommerce.event.VideoDataResourceAdded;
import com.skytala.eCommerce.event.VideoDataResourceDeleted;
import com.skytala.eCommerce.event.VideoDataResourceFound;
import com.skytala.eCommerce.event.VideoDataResourceUpdated;
import com.skytala.eCommerce.query.FindVideoDataResourcesBy;

@RestController
@RequestMapping("/api/videoDataResource")
public class VideoDataResourceController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<VideoDataResource>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public VideoDataResourceController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a VideoDataResource
	 * @return a List with the VideoDataResources
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<VideoDataResource> findVideoDataResourcesBy(@RequestParam Map<String, String> allRequestParams) {

		FindVideoDataResourcesBy query = new FindVideoDataResourcesBy(allRequestParams);

		int usedTicketId;

		synchronized (VideoDataResourceController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(VideoDataResourceFound.class,
				event -> sendVideoDataResourcesFoundMessage(((VideoDataResourceFound) event).getVideoDataResources(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendVideoDataResourcesFoundMessage(List<VideoDataResource> videoDataResources, int usedTicketId) {
		queryReturnVal.put(usedTicketId, videoDataResources);
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
	public boolean createVideoDataResource(HttpServletRequest request) {

		VideoDataResource videoDataResourceToBeAdded = new VideoDataResource();
		try {
			videoDataResourceToBeAdded = VideoDataResourceMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createVideoDataResource(videoDataResourceToBeAdded);

	}

	/**
	 * creates a new VideoDataResource entry in the ofbiz database
	 * 
	 * @param videoDataResourceToBeAdded
	 *            the VideoDataResource thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createVideoDataResource(VideoDataResource videoDataResourceToBeAdded) {

		AddVideoDataResource com = new AddVideoDataResource(videoDataResourceToBeAdded);
		int usedTicketId;

		synchronized (VideoDataResourceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(VideoDataResourceAdded.class,
				event -> sendVideoDataResourceChangedMessage(((VideoDataResourceAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateVideoDataResource(HttpServletRequest request) {

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

		VideoDataResource videoDataResourceToBeUpdated = new VideoDataResource();

		try {
			videoDataResourceToBeUpdated = VideoDataResourceMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateVideoDataResource(videoDataResourceToBeUpdated);

	}

	/**
	 * Updates the VideoDataResource with the specific Id
	 * 
	 * @param videoDataResourceToBeUpdated the VideoDataResource thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateVideoDataResource(VideoDataResource videoDataResourceToBeUpdated) {

		UpdateVideoDataResource com = new UpdateVideoDataResource(videoDataResourceToBeUpdated);

		int usedTicketId;

		synchronized (VideoDataResourceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(VideoDataResourceUpdated.class,
				event -> sendVideoDataResourceChangedMessage(((VideoDataResourceUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a VideoDataResource from the database
	 * 
	 * @param videoDataResourceId:
	 *            the id of the VideoDataResource thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletevideoDataResourceById(@RequestParam(value = "videoDataResourceId") String videoDataResourceId) {

		DeleteVideoDataResource com = new DeleteVideoDataResource(videoDataResourceId);

		int usedTicketId;

		synchronized (VideoDataResourceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(VideoDataResourceDeleted.class,
				event -> sendVideoDataResourceChangedMessage(((VideoDataResourceDeleted) event).isSuccess(), usedTicketId));

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

	public void sendVideoDataResourceChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/videoDataResource/\" plus one of the following: "
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
