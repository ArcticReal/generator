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
import com.skytala.eCommerce.command.AddContentPurpose;
import com.skytala.eCommerce.command.DeleteContentPurpose;
import com.skytala.eCommerce.command.UpdateContentPurpose;
import com.skytala.eCommerce.entity.ContentPurpose;
import com.skytala.eCommerce.entity.ContentPurposeMapper;
import com.skytala.eCommerce.event.ContentPurposeAdded;
import com.skytala.eCommerce.event.ContentPurposeDeleted;
import com.skytala.eCommerce.event.ContentPurposeFound;
import com.skytala.eCommerce.event.ContentPurposeUpdated;
import com.skytala.eCommerce.query.FindContentPurposesBy;

@RestController
@RequestMapping("/api/contentPurpose")
public class ContentPurposeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ContentPurpose>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ContentPurposeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ContentPurpose
	 * @return a List with the ContentPurposes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ContentPurpose> findContentPurposesBy(@RequestParam Map<String, String> allRequestParams) {

		FindContentPurposesBy query = new FindContentPurposesBy(allRequestParams);

		int usedTicketId;

		synchronized (ContentPurposeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentPurposeFound.class,
				event -> sendContentPurposesFoundMessage(((ContentPurposeFound) event).getContentPurposes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendContentPurposesFoundMessage(List<ContentPurpose> contentPurposes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, contentPurposes);
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
	public boolean createContentPurpose(HttpServletRequest request) {

		ContentPurpose contentPurposeToBeAdded = new ContentPurpose();
		try {
			contentPurposeToBeAdded = ContentPurposeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createContentPurpose(contentPurposeToBeAdded);

	}

	/**
	 * creates a new ContentPurpose entry in the ofbiz database
	 * 
	 * @param contentPurposeToBeAdded
	 *            the ContentPurpose thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createContentPurpose(ContentPurpose contentPurposeToBeAdded) {

		AddContentPurpose com = new AddContentPurpose(contentPurposeToBeAdded);
		int usedTicketId;

		synchronized (ContentPurposeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentPurposeAdded.class,
				event -> sendContentPurposeChangedMessage(((ContentPurposeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateContentPurpose(HttpServletRequest request) {

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

		ContentPurpose contentPurposeToBeUpdated = new ContentPurpose();

		try {
			contentPurposeToBeUpdated = ContentPurposeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateContentPurpose(contentPurposeToBeUpdated);

	}

	/**
	 * Updates the ContentPurpose with the specific Id
	 * 
	 * @param contentPurposeToBeUpdated the ContentPurpose thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateContentPurpose(ContentPurpose contentPurposeToBeUpdated) {

		UpdateContentPurpose com = new UpdateContentPurpose(contentPurposeToBeUpdated);

		int usedTicketId;

		synchronized (ContentPurposeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentPurposeUpdated.class,
				event -> sendContentPurposeChangedMessage(((ContentPurposeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ContentPurpose from the database
	 * 
	 * @param contentPurposeId:
	 *            the id of the ContentPurpose thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecontentPurposeById(@RequestParam(value = "contentPurposeId") String contentPurposeId) {

		DeleteContentPurpose com = new DeleteContentPurpose(contentPurposeId);

		int usedTicketId;

		synchronized (ContentPurposeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentPurposeDeleted.class,
				event -> sendContentPurposeChangedMessage(((ContentPurposeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendContentPurposeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/contentPurpose/\" plus one of the following: "
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
