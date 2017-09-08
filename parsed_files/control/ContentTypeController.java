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
import com.skytala.eCommerce.command.AddContentType;
import com.skytala.eCommerce.command.DeleteContentType;
import com.skytala.eCommerce.command.UpdateContentType;
import com.skytala.eCommerce.entity.ContentType;
import com.skytala.eCommerce.entity.ContentTypeMapper;
import com.skytala.eCommerce.event.ContentTypeAdded;
import com.skytala.eCommerce.event.ContentTypeDeleted;
import com.skytala.eCommerce.event.ContentTypeFound;
import com.skytala.eCommerce.event.ContentTypeUpdated;
import com.skytala.eCommerce.query.FindContentTypesBy;

@RestController
@RequestMapping("/api/contentType")
public class ContentTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ContentType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ContentTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ContentType
	 * @return a List with the ContentTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ContentType> findContentTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindContentTypesBy query = new FindContentTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (ContentTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentTypeFound.class,
				event -> sendContentTypesFoundMessage(((ContentTypeFound) event).getContentTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendContentTypesFoundMessage(List<ContentType> contentTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, contentTypes);
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
	public boolean createContentType(HttpServletRequest request) {

		ContentType contentTypeToBeAdded = new ContentType();
		try {
			contentTypeToBeAdded = ContentTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createContentType(contentTypeToBeAdded);

	}

	/**
	 * creates a new ContentType entry in the ofbiz database
	 * 
	 * @param contentTypeToBeAdded
	 *            the ContentType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createContentType(ContentType contentTypeToBeAdded) {

		AddContentType com = new AddContentType(contentTypeToBeAdded);
		int usedTicketId;

		synchronized (ContentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentTypeAdded.class,
				event -> sendContentTypeChangedMessage(((ContentTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateContentType(HttpServletRequest request) {

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

		ContentType contentTypeToBeUpdated = new ContentType();

		try {
			contentTypeToBeUpdated = ContentTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateContentType(contentTypeToBeUpdated);

	}

	/**
	 * Updates the ContentType with the specific Id
	 * 
	 * @param contentTypeToBeUpdated the ContentType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateContentType(ContentType contentTypeToBeUpdated) {

		UpdateContentType com = new UpdateContentType(contentTypeToBeUpdated);

		int usedTicketId;

		synchronized (ContentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentTypeUpdated.class,
				event -> sendContentTypeChangedMessage(((ContentTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ContentType from the database
	 * 
	 * @param contentTypeId:
	 *            the id of the ContentType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecontentTypeById(@RequestParam(value = "contentTypeId") String contentTypeId) {

		DeleteContentType com = new DeleteContentType(contentTypeId);

		int usedTicketId;

		synchronized (ContentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentTypeDeleted.class,
				event -> sendContentTypeChangedMessage(((ContentTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendContentTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/contentType/\" plus one of the following: "
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
