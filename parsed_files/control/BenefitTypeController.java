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
import com.skytala.eCommerce.command.AddBenefitType;
import com.skytala.eCommerce.command.DeleteBenefitType;
import com.skytala.eCommerce.command.UpdateBenefitType;
import com.skytala.eCommerce.entity.BenefitType;
import com.skytala.eCommerce.entity.BenefitTypeMapper;
import com.skytala.eCommerce.event.BenefitTypeAdded;
import com.skytala.eCommerce.event.BenefitTypeDeleted;
import com.skytala.eCommerce.event.BenefitTypeFound;
import com.skytala.eCommerce.event.BenefitTypeUpdated;
import com.skytala.eCommerce.query.FindBenefitTypesBy;

@RestController
@RequestMapping("/api/benefitType")
public class BenefitTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<BenefitType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public BenefitTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a BenefitType
	 * @return a List with the BenefitTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<BenefitType> findBenefitTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindBenefitTypesBy query = new FindBenefitTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (BenefitTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BenefitTypeFound.class,
				event -> sendBenefitTypesFoundMessage(((BenefitTypeFound) event).getBenefitTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendBenefitTypesFoundMessage(List<BenefitType> benefitTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, benefitTypes);
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
	public boolean createBenefitType(HttpServletRequest request) {

		BenefitType benefitTypeToBeAdded = new BenefitType();
		try {
			benefitTypeToBeAdded = BenefitTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createBenefitType(benefitTypeToBeAdded);

	}

	/**
	 * creates a new BenefitType entry in the ofbiz database
	 * 
	 * @param benefitTypeToBeAdded
	 *            the BenefitType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createBenefitType(BenefitType benefitTypeToBeAdded) {

		AddBenefitType com = new AddBenefitType(benefitTypeToBeAdded);
		int usedTicketId;

		synchronized (BenefitTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BenefitTypeAdded.class,
				event -> sendBenefitTypeChangedMessage(((BenefitTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateBenefitType(HttpServletRequest request) {

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

		BenefitType benefitTypeToBeUpdated = new BenefitType();

		try {
			benefitTypeToBeUpdated = BenefitTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateBenefitType(benefitTypeToBeUpdated);

	}

	/**
	 * Updates the BenefitType with the specific Id
	 * 
	 * @param benefitTypeToBeUpdated the BenefitType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateBenefitType(BenefitType benefitTypeToBeUpdated) {

		UpdateBenefitType com = new UpdateBenefitType(benefitTypeToBeUpdated);

		int usedTicketId;

		synchronized (BenefitTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BenefitTypeUpdated.class,
				event -> sendBenefitTypeChangedMessage(((BenefitTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a BenefitType from the database
	 * 
	 * @param benefitTypeId:
	 *            the id of the BenefitType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletebenefitTypeById(@RequestParam(value = "benefitTypeId") String benefitTypeId) {

		DeleteBenefitType com = new DeleteBenefitType(benefitTypeId);

		int usedTicketId;

		synchronized (BenefitTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BenefitTypeDeleted.class,
				event -> sendBenefitTypeChangedMessage(((BenefitTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendBenefitTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/benefitType/\" plus one of the following: "
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
