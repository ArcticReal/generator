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
import com.skytala.eCommerce.command.AddContentRevisionItem;
import com.skytala.eCommerce.command.DeleteContentRevisionItem;
import com.skytala.eCommerce.command.UpdateContentRevisionItem;
import com.skytala.eCommerce.entity.ContentRevisionItem;
import com.skytala.eCommerce.entity.ContentRevisionItemMapper;
import com.skytala.eCommerce.event.ContentRevisionItemAdded;
import com.skytala.eCommerce.event.ContentRevisionItemDeleted;
import com.skytala.eCommerce.event.ContentRevisionItemFound;
import com.skytala.eCommerce.event.ContentRevisionItemUpdated;
import com.skytala.eCommerce.query.FindContentRevisionItemsBy;

@RestController
@RequestMapping("/api/contentRevisionItem")
public class ContentRevisionItemController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ContentRevisionItem>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ContentRevisionItemController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ContentRevisionItem
	 * @return a List with the ContentRevisionItems
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ContentRevisionItem> findContentRevisionItemsBy(@RequestParam Map<String, String> allRequestParams) {

		FindContentRevisionItemsBy query = new FindContentRevisionItemsBy(allRequestParams);

		int usedTicketId;

		synchronized (ContentRevisionItemController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentRevisionItemFound.class,
				event -> sendContentRevisionItemsFoundMessage(((ContentRevisionItemFound) event).getContentRevisionItems(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendContentRevisionItemsFoundMessage(List<ContentRevisionItem> contentRevisionItems, int usedTicketId) {
		queryReturnVal.put(usedTicketId, contentRevisionItems);
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
	public boolean createContentRevisionItem(HttpServletRequest request) {

		ContentRevisionItem contentRevisionItemToBeAdded = new ContentRevisionItem();
		try {
			contentRevisionItemToBeAdded = ContentRevisionItemMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createContentRevisionItem(contentRevisionItemToBeAdded);

	}

	/**
	 * creates a new ContentRevisionItem entry in the ofbiz database
	 * 
	 * @param contentRevisionItemToBeAdded
	 *            the ContentRevisionItem thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createContentRevisionItem(ContentRevisionItem contentRevisionItemToBeAdded) {

		AddContentRevisionItem com = new AddContentRevisionItem(contentRevisionItemToBeAdded);
		int usedTicketId;

		synchronized (ContentRevisionItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentRevisionItemAdded.class,
				event -> sendContentRevisionItemChangedMessage(((ContentRevisionItemAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateContentRevisionItem(HttpServletRequest request) {

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

		ContentRevisionItem contentRevisionItemToBeUpdated = new ContentRevisionItem();

		try {
			contentRevisionItemToBeUpdated = ContentRevisionItemMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateContentRevisionItem(contentRevisionItemToBeUpdated);

	}

	/**
	 * Updates the ContentRevisionItem with the specific Id
	 * 
	 * @param contentRevisionItemToBeUpdated the ContentRevisionItem thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateContentRevisionItem(ContentRevisionItem contentRevisionItemToBeUpdated) {

		UpdateContentRevisionItem com = new UpdateContentRevisionItem(contentRevisionItemToBeUpdated);

		int usedTicketId;

		synchronized (ContentRevisionItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentRevisionItemUpdated.class,
				event -> sendContentRevisionItemChangedMessage(((ContentRevisionItemUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ContentRevisionItem from the database
	 * 
	 * @param contentRevisionItemId:
	 *            the id of the ContentRevisionItem thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecontentRevisionItemById(@RequestParam(value = "contentRevisionItemId") String contentRevisionItemId) {

		DeleteContentRevisionItem com = new DeleteContentRevisionItem(contentRevisionItemId);

		int usedTicketId;

		synchronized (ContentRevisionItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentRevisionItemDeleted.class,
				event -> sendContentRevisionItemChangedMessage(((ContentRevisionItemDeleted) event).isSuccess(), usedTicketId));

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

	public void sendContentRevisionItemChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/contentRevisionItem/\" plus one of the following: "
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
