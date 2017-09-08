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
import com.skytala.eCommerce.command.AddRateAmount;
import com.skytala.eCommerce.command.DeleteRateAmount;
import com.skytala.eCommerce.command.UpdateRateAmount;
import com.skytala.eCommerce.entity.RateAmount;
import com.skytala.eCommerce.entity.RateAmountMapper;
import com.skytala.eCommerce.event.RateAmountAdded;
import com.skytala.eCommerce.event.RateAmountDeleted;
import com.skytala.eCommerce.event.RateAmountFound;
import com.skytala.eCommerce.event.RateAmountUpdated;
import com.skytala.eCommerce.query.FindRateAmountsBy;

@RestController
@RequestMapping("/api/rateAmount")
public class RateAmountController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<RateAmount>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public RateAmountController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a RateAmount
	 * @return a List with the RateAmounts
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<RateAmount> findRateAmountsBy(@RequestParam Map<String, String> allRequestParams) {

		FindRateAmountsBy query = new FindRateAmountsBy(allRequestParams);

		int usedTicketId;

		synchronized (RateAmountController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RateAmountFound.class,
				event -> sendRateAmountsFoundMessage(((RateAmountFound) event).getRateAmounts(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendRateAmountsFoundMessage(List<RateAmount> rateAmounts, int usedTicketId) {
		queryReturnVal.put(usedTicketId, rateAmounts);
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
	public boolean createRateAmount(HttpServletRequest request) {

		RateAmount rateAmountToBeAdded = new RateAmount();
		try {
			rateAmountToBeAdded = RateAmountMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createRateAmount(rateAmountToBeAdded);

	}

	/**
	 * creates a new RateAmount entry in the ofbiz database
	 * 
	 * @param rateAmountToBeAdded
	 *            the RateAmount thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createRateAmount(RateAmount rateAmountToBeAdded) {

		AddRateAmount com = new AddRateAmount(rateAmountToBeAdded);
		int usedTicketId;

		synchronized (RateAmountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RateAmountAdded.class,
				event -> sendRateAmountChangedMessage(((RateAmountAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateRateAmount(HttpServletRequest request) {

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

		RateAmount rateAmountToBeUpdated = new RateAmount();

		try {
			rateAmountToBeUpdated = RateAmountMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateRateAmount(rateAmountToBeUpdated);

	}

	/**
	 * Updates the RateAmount with the specific Id
	 * 
	 * @param rateAmountToBeUpdated the RateAmount thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateRateAmount(RateAmount rateAmountToBeUpdated) {

		UpdateRateAmount com = new UpdateRateAmount(rateAmountToBeUpdated);

		int usedTicketId;

		synchronized (RateAmountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RateAmountUpdated.class,
				event -> sendRateAmountChangedMessage(((RateAmountUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a RateAmount from the database
	 * 
	 * @param rateAmountId:
	 *            the id of the RateAmount thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleterateAmountById(@RequestParam(value = "rateAmountId") String rateAmountId) {

		DeleteRateAmount com = new DeleteRateAmount(rateAmountId);

		int usedTicketId;

		synchronized (RateAmountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RateAmountDeleted.class,
				event -> sendRateAmountChangedMessage(((RateAmountDeleted) event).isSuccess(), usedTicketId));

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

	public void sendRateAmountChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/rateAmount/\" plus one of the following: "
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