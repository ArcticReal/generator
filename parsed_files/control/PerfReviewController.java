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
import com.skytala.eCommerce.command.AddPerfReview;
import com.skytala.eCommerce.command.DeletePerfReview;
import com.skytala.eCommerce.command.UpdatePerfReview;
import com.skytala.eCommerce.entity.PerfReview;
import com.skytala.eCommerce.entity.PerfReviewMapper;
import com.skytala.eCommerce.event.PerfReviewAdded;
import com.skytala.eCommerce.event.PerfReviewDeleted;
import com.skytala.eCommerce.event.PerfReviewFound;
import com.skytala.eCommerce.event.PerfReviewUpdated;
import com.skytala.eCommerce.query.FindPerfReviewsBy;

@RestController
@RequestMapping("/api/perfReview")
public class PerfReviewController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PerfReview>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PerfReviewController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PerfReview
	 * @return a List with the PerfReviews
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PerfReview> findPerfReviewsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPerfReviewsBy query = new FindPerfReviewsBy(allRequestParams);

		int usedTicketId;

		synchronized (PerfReviewController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PerfReviewFound.class,
				event -> sendPerfReviewsFoundMessage(((PerfReviewFound) event).getPerfReviews(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPerfReviewsFoundMessage(List<PerfReview> perfReviews, int usedTicketId) {
		queryReturnVal.put(usedTicketId, perfReviews);
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
	public boolean createPerfReview(HttpServletRequest request) {

		PerfReview perfReviewToBeAdded = new PerfReview();
		try {
			perfReviewToBeAdded = PerfReviewMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPerfReview(perfReviewToBeAdded);

	}

	/**
	 * creates a new PerfReview entry in the ofbiz database
	 * 
	 * @param perfReviewToBeAdded
	 *            the PerfReview thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPerfReview(PerfReview perfReviewToBeAdded) {

		AddPerfReview com = new AddPerfReview(perfReviewToBeAdded);
		int usedTicketId;

		synchronized (PerfReviewController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PerfReviewAdded.class,
				event -> sendPerfReviewChangedMessage(((PerfReviewAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePerfReview(HttpServletRequest request) {

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

		PerfReview perfReviewToBeUpdated = new PerfReview();

		try {
			perfReviewToBeUpdated = PerfReviewMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePerfReview(perfReviewToBeUpdated);

	}

	/**
	 * Updates the PerfReview with the specific Id
	 * 
	 * @param perfReviewToBeUpdated the PerfReview thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePerfReview(PerfReview perfReviewToBeUpdated) {

		UpdatePerfReview com = new UpdatePerfReview(perfReviewToBeUpdated);

		int usedTicketId;

		synchronized (PerfReviewController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PerfReviewUpdated.class,
				event -> sendPerfReviewChangedMessage(((PerfReviewUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PerfReview from the database
	 * 
	 * @param perfReviewId:
	 *            the id of the PerfReview thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteperfReviewById(@RequestParam(value = "perfReviewId") String perfReviewId) {

		DeletePerfReview com = new DeletePerfReview(perfReviewId);

		int usedTicketId;

		synchronized (PerfReviewController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PerfReviewDeleted.class,
				event -> sendPerfReviewChangedMessage(((PerfReviewDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPerfReviewChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/perfReview/\" plus one of the following: "
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
