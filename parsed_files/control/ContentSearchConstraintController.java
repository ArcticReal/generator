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
import com.skytala.eCommerce.command.AddContentSearchConstraint;
import com.skytala.eCommerce.command.DeleteContentSearchConstraint;
import com.skytala.eCommerce.command.UpdateContentSearchConstraint;
import com.skytala.eCommerce.entity.ContentSearchConstraint;
import com.skytala.eCommerce.entity.ContentSearchConstraintMapper;
import com.skytala.eCommerce.event.ContentSearchConstraintAdded;
import com.skytala.eCommerce.event.ContentSearchConstraintDeleted;
import com.skytala.eCommerce.event.ContentSearchConstraintFound;
import com.skytala.eCommerce.event.ContentSearchConstraintUpdated;
import com.skytala.eCommerce.query.FindContentSearchConstraintsBy;

@RestController
@RequestMapping("/api/contentSearchConstraint")
public class ContentSearchConstraintController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ContentSearchConstraint>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ContentSearchConstraintController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ContentSearchConstraint
	 * @return a List with the ContentSearchConstraints
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ContentSearchConstraint> findContentSearchConstraintsBy(@RequestParam Map<String, String> allRequestParams) {

		FindContentSearchConstraintsBy query = new FindContentSearchConstraintsBy(allRequestParams);

		int usedTicketId;

		synchronized (ContentSearchConstraintController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentSearchConstraintFound.class,
				event -> sendContentSearchConstraintsFoundMessage(((ContentSearchConstraintFound) event).getContentSearchConstraints(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendContentSearchConstraintsFoundMessage(List<ContentSearchConstraint> contentSearchConstraints, int usedTicketId) {
		queryReturnVal.put(usedTicketId, contentSearchConstraints);
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
	public boolean createContentSearchConstraint(HttpServletRequest request) {

		ContentSearchConstraint contentSearchConstraintToBeAdded = new ContentSearchConstraint();
		try {
			contentSearchConstraintToBeAdded = ContentSearchConstraintMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createContentSearchConstraint(contentSearchConstraintToBeAdded);

	}

	/**
	 * creates a new ContentSearchConstraint entry in the ofbiz database
	 * 
	 * @param contentSearchConstraintToBeAdded
	 *            the ContentSearchConstraint thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createContentSearchConstraint(ContentSearchConstraint contentSearchConstraintToBeAdded) {

		AddContentSearchConstraint com = new AddContentSearchConstraint(contentSearchConstraintToBeAdded);
		int usedTicketId;

		synchronized (ContentSearchConstraintController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentSearchConstraintAdded.class,
				event -> sendContentSearchConstraintChangedMessage(((ContentSearchConstraintAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateContentSearchConstraint(HttpServletRequest request) {

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

		ContentSearchConstraint contentSearchConstraintToBeUpdated = new ContentSearchConstraint();

		try {
			contentSearchConstraintToBeUpdated = ContentSearchConstraintMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateContentSearchConstraint(contentSearchConstraintToBeUpdated);

	}

	/**
	 * Updates the ContentSearchConstraint with the specific Id
	 * 
	 * @param contentSearchConstraintToBeUpdated the ContentSearchConstraint thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateContentSearchConstraint(ContentSearchConstraint contentSearchConstraintToBeUpdated) {

		UpdateContentSearchConstraint com = new UpdateContentSearchConstraint(contentSearchConstraintToBeUpdated);

		int usedTicketId;

		synchronized (ContentSearchConstraintController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentSearchConstraintUpdated.class,
				event -> sendContentSearchConstraintChangedMessage(((ContentSearchConstraintUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ContentSearchConstraint from the database
	 * 
	 * @param contentSearchConstraintId:
	 *            the id of the ContentSearchConstraint thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecontentSearchConstraintById(@RequestParam(value = "contentSearchConstraintId") String contentSearchConstraintId) {

		DeleteContentSearchConstraint com = new DeleteContentSearchConstraint(contentSearchConstraintId);

		int usedTicketId;

		synchronized (ContentSearchConstraintController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentSearchConstraintDeleted.class,
				event -> sendContentSearchConstraintChangedMessage(((ContentSearchConstraintDeleted) event).isSuccess(), usedTicketId));

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

	public void sendContentSearchConstraintChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/contentSearchConstraint/\" plus one of the following: "
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
