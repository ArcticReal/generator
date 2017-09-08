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
import com.skytala.eCommerce.command.AddContentPurposeType;
import com.skytala.eCommerce.command.DeleteContentPurposeType;
import com.skytala.eCommerce.command.UpdateContentPurposeType;
import com.skytala.eCommerce.entity.ContentPurposeType;
import com.skytala.eCommerce.entity.ContentPurposeTypeMapper;
import com.skytala.eCommerce.event.ContentPurposeTypeAdded;
import com.skytala.eCommerce.event.ContentPurposeTypeDeleted;
import com.skytala.eCommerce.event.ContentPurposeTypeFound;
import com.skytala.eCommerce.event.ContentPurposeTypeUpdated;
import com.skytala.eCommerce.query.FindContentPurposeTypesBy;

@RestController
@RequestMapping("/api/contentPurposeType")
public class ContentPurposeTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ContentPurposeType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ContentPurposeTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ContentPurposeType
	 * @return a List with the ContentPurposeTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ContentPurposeType> findContentPurposeTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindContentPurposeTypesBy query = new FindContentPurposeTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (ContentPurposeTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentPurposeTypeFound.class,
				event -> sendContentPurposeTypesFoundMessage(((ContentPurposeTypeFound) event).getContentPurposeTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendContentPurposeTypesFoundMessage(List<ContentPurposeType> contentPurposeTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, contentPurposeTypes);
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
	public boolean createContentPurposeType(HttpServletRequest request) {

		ContentPurposeType contentPurposeTypeToBeAdded = new ContentPurposeType();
		try {
			contentPurposeTypeToBeAdded = ContentPurposeTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createContentPurposeType(contentPurposeTypeToBeAdded);

	}

	/**
	 * creates a new ContentPurposeType entry in the ofbiz database
	 * 
	 * @param contentPurposeTypeToBeAdded
	 *            the ContentPurposeType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createContentPurposeType(ContentPurposeType contentPurposeTypeToBeAdded) {

		AddContentPurposeType com = new AddContentPurposeType(contentPurposeTypeToBeAdded);
		int usedTicketId;

		synchronized (ContentPurposeTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentPurposeTypeAdded.class,
				event -> sendContentPurposeTypeChangedMessage(((ContentPurposeTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateContentPurposeType(HttpServletRequest request) {

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

		ContentPurposeType contentPurposeTypeToBeUpdated = new ContentPurposeType();

		try {
			contentPurposeTypeToBeUpdated = ContentPurposeTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateContentPurposeType(contentPurposeTypeToBeUpdated);

	}

	/**
	 * Updates the ContentPurposeType with the specific Id
	 * 
	 * @param contentPurposeTypeToBeUpdated the ContentPurposeType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateContentPurposeType(ContentPurposeType contentPurposeTypeToBeUpdated) {

		UpdateContentPurposeType com = new UpdateContentPurposeType(contentPurposeTypeToBeUpdated);

		int usedTicketId;

		synchronized (ContentPurposeTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentPurposeTypeUpdated.class,
				event -> sendContentPurposeTypeChangedMessage(((ContentPurposeTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ContentPurposeType from the database
	 * 
	 * @param contentPurposeTypeId:
	 *            the id of the ContentPurposeType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecontentPurposeTypeById(@RequestParam(value = "contentPurposeTypeId") String contentPurposeTypeId) {

		DeleteContentPurposeType com = new DeleteContentPurposeType(contentPurposeTypeId);

		int usedTicketId;

		synchronized (ContentPurposeTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentPurposeTypeDeleted.class,
				event -> sendContentPurposeTypeChangedMessage(((ContentPurposeTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendContentPurposeTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/contentPurposeType/\" plus one of the following: "
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
