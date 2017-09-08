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
import com.skytala.eCommerce.command.AddBudgetReview;
import com.skytala.eCommerce.command.DeleteBudgetReview;
import com.skytala.eCommerce.command.UpdateBudgetReview;
import com.skytala.eCommerce.entity.BudgetReview;
import com.skytala.eCommerce.entity.BudgetReviewMapper;
import com.skytala.eCommerce.event.BudgetReviewAdded;
import com.skytala.eCommerce.event.BudgetReviewDeleted;
import com.skytala.eCommerce.event.BudgetReviewFound;
import com.skytala.eCommerce.event.BudgetReviewUpdated;
import com.skytala.eCommerce.query.FindBudgetReviewsBy;

@RestController
@RequestMapping("/api/budgetReview")
public class BudgetReviewController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<BudgetReview>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public BudgetReviewController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a BudgetReview
	 * @return a List with the BudgetReviews
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<BudgetReview> findBudgetReviewsBy(@RequestParam Map<String, String> allRequestParams) {

		FindBudgetReviewsBy query = new FindBudgetReviewsBy(allRequestParams);

		int usedTicketId;

		synchronized (BudgetReviewController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetReviewFound.class,
				event -> sendBudgetReviewsFoundMessage(((BudgetReviewFound) event).getBudgetReviews(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendBudgetReviewsFoundMessage(List<BudgetReview> budgetReviews, int usedTicketId) {
		queryReturnVal.put(usedTicketId, budgetReviews);
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
	public boolean createBudgetReview(HttpServletRequest request) {

		BudgetReview budgetReviewToBeAdded = new BudgetReview();
		try {
			budgetReviewToBeAdded = BudgetReviewMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createBudgetReview(budgetReviewToBeAdded);

	}

	/**
	 * creates a new BudgetReview entry in the ofbiz database
	 * 
	 * @param budgetReviewToBeAdded
	 *            the BudgetReview thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createBudgetReview(BudgetReview budgetReviewToBeAdded) {

		AddBudgetReview com = new AddBudgetReview(budgetReviewToBeAdded);
		int usedTicketId;

		synchronized (BudgetReviewController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetReviewAdded.class,
				event -> sendBudgetReviewChangedMessage(((BudgetReviewAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateBudgetReview(HttpServletRequest request) {

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

		BudgetReview budgetReviewToBeUpdated = new BudgetReview();

		try {
			budgetReviewToBeUpdated = BudgetReviewMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateBudgetReview(budgetReviewToBeUpdated);

	}

	/**
	 * Updates the BudgetReview with the specific Id
	 * 
	 * @param budgetReviewToBeUpdated the BudgetReview thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateBudgetReview(BudgetReview budgetReviewToBeUpdated) {

		UpdateBudgetReview com = new UpdateBudgetReview(budgetReviewToBeUpdated);

		int usedTicketId;

		synchronized (BudgetReviewController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetReviewUpdated.class,
				event -> sendBudgetReviewChangedMessage(((BudgetReviewUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a BudgetReview from the database
	 * 
	 * @param budgetReviewId:
	 *            the id of the BudgetReview thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletebudgetReviewById(@RequestParam(value = "budgetReviewId") String budgetReviewId) {

		DeleteBudgetReview com = new DeleteBudgetReview(budgetReviewId);

		int usedTicketId;

		synchronized (BudgetReviewController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetReviewDeleted.class,
				event -> sendBudgetReviewChangedMessage(((BudgetReviewDeleted) event).isSuccess(), usedTicketId));

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

	public void sendBudgetReviewChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/budgetReview/\" plus one of the following: "
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
