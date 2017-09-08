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
import com.skytala.eCommerce.command.AddWorkEffortReview;
import com.skytala.eCommerce.command.DeleteWorkEffortReview;
import com.skytala.eCommerce.command.UpdateWorkEffortReview;
import com.skytala.eCommerce.entity.WorkEffortReview;
import com.skytala.eCommerce.entity.WorkEffortReviewMapper;
import com.skytala.eCommerce.event.WorkEffortReviewAdded;
import com.skytala.eCommerce.event.WorkEffortReviewDeleted;
import com.skytala.eCommerce.event.WorkEffortReviewFound;
import com.skytala.eCommerce.event.WorkEffortReviewUpdated;
import com.skytala.eCommerce.query.FindWorkEffortReviewsBy;

@RestController
@RequestMapping("/api/workEffortReview")
public class WorkEffortReviewController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WorkEffortReview>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WorkEffortReviewController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WorkEffortReview
	 * @return a List with the WorkEffortReviews
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WorkEffortReview> findWorkEffortReviewsBy(@RequestParam Map<String, String> allRequestParams) {

		FindWorkEffortReviewsBy query = new FindWorkEffortReviewsBy(allRequestParams);

		int usedTicketId;

		synchronized (WorkEffortReviewController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortReviewFound.class,
				event -> sendWorkEffortReviewsFoundMessage(((WorkEffortReviewFound) event).getWorkEffortReviews(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWorkEffortReviewsFoundMessage(List<WorkEffortReview> workEffortReviews, int usedTicketId) {
		queryReturnVal.put(usedTicketId, workEffortReviews);
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
	public boolean createWorkEffortReview(HttpServletRequest request) {

		WorkEffortReview workEffortReviewToBeAdded = new WorkEffortReview();
		try {
			workEffortReviewToBeAdded = WorkEffortReviewMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWorkEffortReview(workEffortReviewToBeAdded);

	}

	/**
	 * creates a new WorkEffortReview entry in the ofbiz database
	 * 
	 * @param workEffortReviewToBeAdded
	 *            the WorkEffortReview thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWorkEffortReview(WorkEffortReview workEffortReviewToBeAdded) {

		AddWorkEffortReview com = new AddWorkEffortReview(workEffortReviewToBeAdded);
		int usedTicketId;

		synchronized (WorkEffortReviewController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortReviewAdded.class,
				event -> sendWorkEffortReviewChangedMessage(((WorkEffortReviewAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWorkEffortReview(HttpServletRequest request) {

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

		WorkEffortReview workEffortReviewToBeUpdated = new WorkEffortReview();

		try {
			workEffortReviewToBeUpdated = WorkEffortReviewMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWorkEffortReview(workEffortReviewToBeUpdated);

	}

	/**
	 * Updates the WorkEffortReview with the specific Id
	 * 
	 * @param workEffortReviewToBeUpdated the WorkEffortReview thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWorkEffortReview(WorkEffortReview workEffortReviewToBeUpdated) {

		UpdateWorkEffortReview com = new UpdateWorkEffortReview(workEffortReviewToBeUpdated);

		int usedTicketId;

		synchronized (WorkEffortReviewController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortReviewUpdated.class,
				event -> sendWorkEffortReviewChangedMessage(((WorkEffortReviewUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WorkEffortReview from the database
	 * 
	 * @param workEffortReviewId:
	 *            the id of the WorkEffortReview thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteworkEffortReviewById(@RequestParam(value = "workEffortReviewId") String workEffortReviewId) {

		DeleteWorkEffortReview com = new DeleteWorkEffortReview(workEffortReviewId);

		int usedTicketId;

		synchronized (WorkEffortReviewController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortReviewDeleted.class,
				event -> sendWorkEffortReviewChangedMessage(((WorkEffortReviewDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWorkEffortReviewChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/workEffortReview/\" plus one of the following: "
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
