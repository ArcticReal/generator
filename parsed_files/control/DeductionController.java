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
import com.skytala.eCommerce.command.AddDeduction;
import com.skytala.eCommerce.command.DeleteDeduction;
import com.skytala.eCommerce.command.UpdateDeduction;
import com.skytala.eCommerce.entity.Deduction;
import com.skytala.eCommerce.entity.DeductionMapper;
import com.skytala.eCommerce.event.DeductionAdded;
import com.skytala.eCommerce.event.DeductionDeleted;
import com.skytala.eCommerce.event.DeductionFound;
import com.skytala.eCommerce.event.DeductionUpdated;
import com.skytala.eCommerce.query.FindDeductionsBy;

@RestController
@RequestMapping("/api/deduction")
public class DeductionController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<Deduction>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public DeductionController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a Deduction
	 * @return a List with the Deductions
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<Deduction> findDeductionsBy(@RequestParam Map<String, String> allRequestParams) {

		FindDeductionsBy query = new FindDeductionsBy(allRequestParams);

		int usedTicketId;

		synchronized (DeductionController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DeductionFound.class,
				event -> sendDeductionsFoundMessage(((DeductionFound) event).getDeductions(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendDeductionsFoundMessage(List<Deduction> deductions, int usedTicketId) {
		queryReturnVal.put(usedTicketId, deductions);
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
	public boolean createDeduction(HttpServletRequest request) {

		Deduction deductionToBeAdded = new Deduction();
		try {
			deductionToBeAdded = DeductionMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createDeduction(deductionToBeAdded);

	}

	/**
	 * creates a new Deduction entry in the ofbiz database
	 * 
	 * @param deductionToBeAdded
	 *            the Deduction thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createDeduction(Deduction deductionToBeAdded) {

		AddDeduction com = new AddDeduction(deductionToBeAdded);
		int usedTicketId;

		synchronized (DeductionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DeductionAdded.class,
				event -> sendDeductionChangedMessage(((DeductionAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateDeduction(HttpServletRequest request) {

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

		Deduction deductionToBeUpdated = new Deduction();

		try {
			deductionToBeUpdated = DeductionMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateDeduction(deductionToBeUpdated);

	}

	/**
	 * Updates the Deduction with the specific Id
	 * 
	 * @param deductionToBeUpdated the Deduction thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateDeduction(Deduction deductionToBeUpdated) {

		UpdateDeduction com = new UpdateDeduction(deductionToBeUpdated);

		int usedTicketId;

		synchronized (DeductionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DeductionUpdated.class,
				event -> sendDeductionChangedMessage(((DeductionUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a Deduction from the database
	 * 
	 * @param deductionId:
	 *            the id of the Deduction thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletedeductionById(@RequestParam(value = "deductionId") String deductionId) {

		DeleteDeduction com = new DeleteDeduction(deductionId);

		int usedTicketId;

		synchronized (DeductionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DeductionDeleted.class,
				event -> sendDeductionChangedMessage(((DeductionDeleted) event).isSuccess(), usedTicketId));

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

	public void sendDeductionChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/deduction/\" plus one of the following: "
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
