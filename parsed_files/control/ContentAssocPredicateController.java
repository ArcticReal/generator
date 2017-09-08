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
import com.skytala.eCommerce.command.AddContentAssocPredicate;
import com.skytala.eCommerce.command.DeleteContentAssocPredicate;
import com.skytala.eCommerce.command.UpdateContentAssocPredicate;
import com.skytala.eCommerce.entity.ContentAssocPredicate;
import com.skytala.eCommerce.entity.ContentAssocPredicateMapper;
import com.skytala.eCommerce.event.ContentAssocPredicateAdded;
import com.skytala.eCommerce.event.ContentAssocPredicateDeleted;
import com.skytala.eCommerce.event.ContentAssocPredicateFound;
import com.skytala.eCommerce.event.ContentAssocPredicateUpdated;
import com.skytala.eCommerce.query.FindContentAssocPredicatesBy;

@RestController
@RequestMapping("/api/contentAssocPredicate")
public class ContentAssocPredicateController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ContentAssocPredicate>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ContentAssocPredicateController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ContentAssocPredicate
	 * @return a List with the ContentAssocPredicates
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ContentAssocPredicate> findContentAssocPredicatesBy(@RequestParam Map<String, String> allRequestParams) {

		FindContentAssocPredicatesBy query = new FindContentAssocPredicatesBy(allRequestParams);

		int usedTicketId;

		synchronized (ContentAssocPredicateController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentAssocPredicateFound.class,
				event -> sendContentAssocPredicatesFoundMessage(((ContentAssocPredicateFound) event).getContentAssocPredicates(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendContentAssocPredicatesFoundMessage(List<ContentAssocPredicate> contentAssocPredicates, int usedTicketId) {
		queryReturnVal.put(usedTicketId, contentAssocPredicates);
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
	public boolean createContentAssocPredicate(HttpServletRequest request) {

		ContentAssocPredicate contentAssocPredicateToBeAdded = new ContentAssocPredicate();
		try {
			contentAssocPredicateToBeAdded = ContentAssocPredicateMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createContentAssocPredicate(contentAssocPredicateToBeAdded);

	}

	/**
	 * creates a new ContentAssocPredicate entry in the ofbiz database
	 * 
	 * @param contentAssocPredicateToBeAdded
	 *            the ContentAssocPredicate thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createContentAssocPredicate(ContentAssocPredicate contentAssocPredicateToBeAdded) {

		AddContentAssocPredicate com = new AddContentAssocPredicate(contentAssocPredicateToBeAdded);
		int usedTicketId;

		synchronized (ContentAssocPredicateController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentAssocPredicateAdded.class,
				event -> sendContentAssocPredicateChangedMessage(((ContentAssocPredicateAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateContentAssocPredicate(HttpServletRequest request) {

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

		ContentAssocPredicate contentAssocPredicateToBeUpdated = new ContentAssocPredicate();

		try {
			contentAssocPredicateToBeUpdated = ContentAssocPredicateMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateContentAssocPredicate(contentAssocPredicateToBeUpdated);

	}

	/**
	 * Updates the ContentAssocPredicate with the specific Id
	 * 
	 * @param contentAssocPredicateToBeUpdated the ContentAssocPredicate thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateContentAssocPredicate(ContentAssocPredicate contentAssocPredicateToBeUpdated) {

		UpdateContentAssocPredicate com = new UpdateContentAssocPredicate(contentAssocPredicateToBeUpdated);

		int usedTicketId;

		synchronized (ContentAssocPredicateController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentAssocPredicateUpdated.class,
				event -> sendContentAssocPredicateChangedMessage(((ContentAssocPredicateUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ContentAssocPredicate from the database
	 * 
	 * @param contentAssocPredicateId:
	 *            the id of the ContentAssocPredicate thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecontentAssocPredicateById(@RequestParam(value = "contentAssocPredicateId") String contentAssocPredicateId) {

		DeleteContentAssocPredicate com = new DeleteContentAssocPredicate(contentAssocPredicateId);

		int usedTicketId;

		synchronized (ContentAssocPredicateController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentAssocPredicateDeleted.class,
				event -> sendContentAssocPredicateChangedMessage(((ContentAssocPredicateDeleted) event).isSuccess(), usedTicketId));

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

	public void sendContentAssocPredicateChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/contentAssocPredicate/\" plus one of the following: "
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
