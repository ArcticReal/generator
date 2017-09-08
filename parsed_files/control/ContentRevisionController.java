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
import com.skytala.eCommerce.command.AddContentRevision;
import com.skytala.eCommerce.command.DeleteContentRevision;
import com.skytala.eCommerce.command.UpdateContentRevision;
import com.skytala.eCommerce.entity.ContentRevision;
import com.skytala.eCommerce.entity.ContentRevisionMapper;
import com.skytala.eCommerce.event.ContentRevisionAdded;
import com.skytala.eCommerce.event.ContentRevisionDeleted;
import com.skytala.eCommerce.event.ContentRevisionFound;
import com.skytala.eCommerce.event.ContentRevisionUpdated;
import com.skytala.eCommerce.query.FindContentRevisionsBy;

@RestController
@RequestMapping("/api/contentRevision")
public class ContentRevisionController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ContentRevision>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ContentRevisionController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ContentRevision
	 * @return a List with the ContentRevisions
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ContentRevision> findContentRevisionsBy(@RequestParam Map<String, String> allRequestParams) {

		FindContentRevisionsBy query = new FindContentRevisionsBy(allRequestParams);

		int usedTicketId;

		synchronized (ContentRevisionController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentRevisionFound.class,
				event -> sendContentRevisionsFoundMessage(((ContentRevisionFound) event).getContentRevisions(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendContentRevisionsFoundMessage(List<ContentRevision> contentRevisions, int usedTicketId) {
		queryReturnVal.put(usedTicketId, contentRevisions);
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
	public boolean createContentRevision(HttpServletRequest request) {

		ContentRevision contentRevisionToBeAdded = new ContentRevision();
		try {
			contentRevisionToBeAdded = ContentRevisionMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createContentRevision(contentRevisionToBeAdded);

	}

	/**
	 * creates a new ContentRevision entry in the ofbiz database
	 * 
	 * @param contentRevisionToBeAdded
	 *            the ContentRevision thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createContentRevision(ContentRevision contentRevisionToBeAdded) {

		AddContentRevision com = new AddContentRevision(contentRevisionToBeAdded);
		int usedTicketId;

		synchronized (ContentRevisionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentRevisionAdded.class,
				event -> sendContentRevisionChangedMessage(((ContentRevisionAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateContentRevision(HttpServletRequest request) {

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

		ContentRevision contentRevisionToBeUpdated = new ContentRevision();

		try {
			contentRevisionToBeUpdated = ContentRevisionMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateContentRevision(contentRevisionToBeUpdated);

	}

	/**
	 * Updates the ContentRevision with the specific Id
	 * 
	 * @param contentRevisionToBeUpdated the ContentRevision thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateContentRevision(ContentRevision contentRevisionToBeUpdated) {

		UpdateContentRevision com = new UpdateContentRevision(contentRevisionToBeUpdated);

		int usedTicketId;

		synchronized (ContentRevisionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentRevisionUpdated.class,
				event -> sendContentRevisionChangedMessage(((ContentRevisionUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ContentRevision from the database
	 * 
	 * @param contentRevisionId:
	 *            the id of the ContentRevision thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecontentRevisionById(@RequestParam(value = "contentRevisionId") String contentRevisionId) {

		DeleteContentRevision com = new DeleteContentRevision(contentRevisionId);

		int usedTicketId;

		synchronized (ContentRevisionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentRevisionDeleted.class,
				event -> sendContentRevisionChangedMessage(((ContentRevisionDeleted) event).isSuccess(), usedTicketId));

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

	public void sendContentRevisionChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/contentRevision/\" plus one of the following: "
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
