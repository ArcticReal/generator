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
import com.skytala.eCommerce.command.AddContentAssocType;
import com.skytala.eCommerce.command.DeleteContentAssocType;
import com.skytala.eCommerce.command.UpdateContentAssocType;
import com.skytala.eCommerce.entity.ContentAssocType;
import com.skytala.eCommerce.entity.ContentAssocTypeMapper;
import com.skytala.eCommerce.event.ContentAssocTypeAdded;
import com.skytala.eCommerce.event.ContentAssocTypeDeleted;
import com.skytala.eCommerce.event.ContentAssocTypeFound;
import com.skytala.eCommerce.event.ContentAssocTypeUpdated;
import com.skytala.eCommerce.query.FindContentAssocTypesBy;

@RestController
@RequestMapping("/api/contentAssocType")
public class ContentAssocTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ContentAssocType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ContentAssocTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ContentAssocType
	 * @return a List with the ContentAssocTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ContentAssocType> findContentAssocTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindContentAssocTypesBy query = new FindContentAssocTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (ContentAssocTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentAssocTypeFound.class,
				event -> sendContentAssocTypesFoundMessage(((ContentAssocTypeFound) event).getContentAssocTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendContentAssocTypesFoundMessage(List<ContentAssocType> contentAssocTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, contentAssocTypes);
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
	public boolean createContentAssocType(HttpServletRequest request) {

		ContentAssocType contentAssocTypeToBeAdded = new ContentAssocType();
		try {
			contentAssocTypeToBeAdded = ContentAssocTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createContentAssocType(contentAssocTypeToBeAdded);

	}

	/**
	 * creates a new ContentAssocType entry in the ofbiz database
	 * 
	 * @param contentAssocTypeToBeAdded
	 *            the ContentAssocType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createContentAssocType(ContentAssocType contentAssocTypeToBeAdded) {

		AddContentAssocType com = new AddContentAssocType(contentAssocTypeToBeAdded);
		int usedTicketId;

		synchronized (ContentAssocTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentAssocTypeAdded.class,
				event -> sendContentAssocTypeChangedMessage(((ContentAssocTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateContentAssocType(HttpServletRequest request) {

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

		ContentAssocType contentAssocTypeToBeUpdated = new ContentAssocType();

		try {
			contentAssocTypeToBeUpdated = ContentAssocTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateContentAssocType(contentAssocTypeToBeUpdated);

	}

	/**
	 * Updates the ContentAssocType with the specific Id
	 * 
	 * @param contentAssocTypeToBeUpdated the ContentAssocType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateContentAssocType(ContentAssocType contentAssocTypeToBeUpdated) {

		UpdateContentAssocType com = new UpdateContentAssocType(contentAssocTypeToBeUpdated);

		int usedTicketId;

		synchronized (ContentAssocTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentAssocTypeUpdated.class,
				event -> sendContentAssocTypeChangedMessage(((ContentAssocTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ContentAssocType from the database
	 * 
	 * @param contentAssocTypeId:
	 *            the id of the ContentAssocType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecontentAssocTypeById(@RequestParam(value = "contentAssocTypeId") String contentAssocTypeId) {

		DeleteContentAssocType com = new DeleteContentAssocType(contentAssocTypeId);

		int usedTicketId;

		synchronized (ContentAssocTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentAssocTypeDeleted.class,
				event -> sendContentAssocTypeChangedMessage(((ContentAssocTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendContentAssocTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/contentAssocType/\" plus one of the following: "
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
