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
import com.skytala.eCommerce.command.AddReorderGuideline;
import com.skytala.eCommerce.command.DeleteReorderGuideline;
import com.skytala.eCommerce.command.UpdateReorderGuideline;
import com.skytala.eCommerce.entity.ReorderGuideline;
import com.skytala.eCommerce.entity.ReorderGuidelineMapper;
import com.skytala.eCommerce.event.ReorderGuidelineAdded;
import com.skytala.eCommerce.event.ReorderGuidelineDeleted;
import com.skytala.eCommerce.event.ReorderGuidelineFound;
import com.skytala.eCommerce.event.ReorderGuidelineUpdated;
import com.skytala.eCommerce.query.FindReorderGuidelinesBy;

@RestController
@RequestMapping("/api/reorderGuideline")
public class ReorderGuidelineController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ReorderGuideline>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ReorderGuidelineController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ReorderGuideline
	 * @return a List with the ReorderGuidelines
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ReorderGuideline> findReorderGuidelinesBy(@RequestParam Map<String, String> allRequestParams) {

		FindReorderGuidelinesBy query = new FindReorderGuidelinesBy(allRequestParams);

		int usedTicketId;

		synchronized (ReorderGuidelineController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReorderGuidelineFound.class,
				event -> sendReorderGuidelinesFoundMessage(((ReorderGuidelineFound) event).getReorderGuidelines(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendReorderGuidelinesFoundMessage(List<ReorderGuideline> reorderGuidelines, int usedTicketId) {
		queryReturnVal.put(usedTicketId, reorderGuidelines);
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
	public boolean createReorderGuideline(HttpServletRequest request) {

		ReorderGuideline reorderGuidelineToBeAdded = new ReorderGuideline();
		try {
			reorderGuidelineToBeAdded = ReorderGuidelineMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createReorderGuideline(reorderGuidelineToBeAdded);

	}

	/**
	 * creates a new ReorderGuideline entry in the ofbiz database
	 * 
	 * @param reorderGuidelineToBeAdded
	 *            the ReorderGuideline thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createReorderGuideline(ReorderGuideline reorderGuidelineToBeAdded) {

		AddReorderGuideline com = new AddReorderGuideline(reorderGuidelineToBeAdded);
		int usedTicketId;

		synchronized (ReorderGuidelineController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReorderGuidelineAdded.class,
				event -> sendReorderGuidelineChangedMessage(((ReorderGuidelineAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateReorderGuideline(HttpServletRequest request) {

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

		ReorderGuideline reorderGuidelineToBeUpdated = new ReorderGuideline();

		try {
			reorderGuidelineToBeUpdated = ReorderGuidelineMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateReorderGuideline(reorderGuidelineToBeUpdated);

	}

	/**
	 * Updates the ReorderGuideline with the specific Id
	 * 
	 * @param reorderGuidelineToBeUpdated the ReorderGuideline thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateReorderGuideline(ReorderGuideline reorderGuidelineToBeUpdated) {

		UpdateReorderGuideline com = new UpdateReorderGuideline(reorderGuidelineToBeUpdated);

		int usedTicketId;

		synchronized (ReorderGuidelineController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReorderGuidelineUpdated.class,
				event -> sendReorderGuidelineChangedMessage(((ReorderGuidelineUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ReorderGuideline from the database
	 * 
	 * @param reorderGuidelineId:
	 *            the id of the ReorderGuideline thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletereorderGuidelineById(@RequestParam(value = "reorderGuidelineId") String reorderGuidelineId) {

		DeleteReorderGuideline com = new DeleteReorderGuideline(reorderGuidelineId);

		int usedTicketId;

		synchronized (ReorderGuidelineController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReorderGuidelineDeleted.class,
				event -> sendReorderGuidelineChangedMessage(((ReorderGuidelineDeleted) event).isSuccess(), usedTicketId));

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

	public void sendReorderGuidelineChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/reorderGuideline/\" plus one of the following: "
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
