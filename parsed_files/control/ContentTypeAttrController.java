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
import com.skytala.eCommerce.command.AddContentTypeAttr;
import com.skytala.eCommerce.command.DeleteContentTypeAttr;
import com.skytala.eCommerce.command.UpdateContentTypeAttr;
import com.skytala.eCommerce.entity.ContentTypeAttr;
import com.skytala.eCommerce.entity.ContentTypeAttrMapper;
import com.skytala.eCommerce.event.ContentTypeAttrAdded;
import com.skytala.eCommerce.event.ContentTypeAttrDeleted;
import com.skytala.eCommerce.event.ContentTypeAttrFound;
import com.skytala.eCommerce.event.ContentTypeAttrUpdated;
import com.skytala.eCommerce.query.FindContentTypeAttrsBy;

@RestController
@RequestMapping("/api/contentTypeAttr")
public class ContentTypeAttrController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ContentTypeAttr>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ContentTypeAttrController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ContentTypeAttr
	 * @return a List with the ContentTypeAttrs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ContentTypeAttr> findContentTypeAttrsBy(@RequestParam Map<String, String> allRequestParams) {

		FindContentTypeAttrsBy query = new FindContentTypeAttrsBy(allRequestParams);

		int usedTicketId;

		synchronized (ContentTypeAttrController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentTypeAttrFound.class,
				event -> sendContentTypeAttrsFoundMessage(((ContentTypeAttrFound) event).getContentTypeAttrs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendContentTypeAttrsFoundMessage(List<ContentTypeAttr> contentTypeAttrs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, contentTypeAttrs);
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
	public boolean createContentTypeAttr(HttpServletRequest request) {

		ContentTypeAttr contentTypeAttrToBeAdded = new ContentTypeAttr();
		try {
			contentTypeAttrToBeAdded = ContentTypeAttrMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createContentTypeAttr(contentTypeAttrToBeAdded);

	}

	/**
	 * creates a new ContentTypeAttr entry in the ofbiz database
	 * 
	 * @param contentTypeAttrToBeAdded
	 *            the ContentTypeAttr thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createContentTypeAttr(ContentTypeAttr contentTypeAttrToBeAdded) {

		AddContentTypeAttr com = new AddContentTypeAttr(contentTypeAttrToBeAdded);
		int usedTicketId;

		synchronized (ContentTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentTypeAttrAdded.class,
				event -> sendContentTypeAttrChangedMessage(((ContentTypeAttrAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateContentTypeAttr(HttpServletRequest request) {

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

		ContentTypeAttr contentTypeAttrToBeUpdated = new ContentTypeAttr();

		try {
			contentTypeAttrToBeUpdated = ContentTypeAttrMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateContentTypeAttr(contentTypeAttrToBeUpdated);

	}

	/**
	 * Updates the ContentTypeAttr with the specific Id
	 * 
	 * @param contentTypeAttrToBeUpdated the ContentTypeAttr thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateContentTypeAttr(ContentTypeAttr contentTypeAttrToBeUpdated) {

		UpdateContentTypeAttr com = new UpdateContentTypeAttr(contentTypeAttrToBeUpdated);

		int usedTicketId;

		synchronized (ContentTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentTypeAttrUpdated.class,
				event -> sendContentTypeAttrChangedMessage(((ContentTypeAttrUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ContentTypeAttr from the database
	 * 
	 * @param contentTypeAttrId:
	 *            the id of the ContentTypeAttr thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecontentTypeAttrById(@RequestParam(value = "contentTypeAttrId") String contentTypeAttrId) {

		DeleteContentTypeAttr com = new DeleteContentTypeAttr(contentTypeAttrId);

		int usedTicketId;

		synchronized (ContentTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentTypeAttrDeleted.class,
				event -> sendContentTypeAttrChangedMessage(((ContentTypeAttrDeleted) event).isSuccess(), usedTicketId));

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

	public void sendContentTypeAttrChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/contentTypeAttr/\" plus one of the following: "
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
