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
import com.skytala.eCommerce.command.AddSalaryStep;
import com.skytala.eCommerce.command.DeleteSalaryStep;
import com.skytala.eCommerce.command.UpdateSalaryStep;
import com.skytala.eCommerce.entity.SalaryStep;
import com.skytala.eCommerce.entity.SalaryStepMapper;
import com.skytala.eCommerce.event.SalaryStepAdded;
import com.skytala.eCommerce.event.SalaryStepDeleted;
import com.skytala.eCommerce.event.SalaryStepFound;
import com.skytala.eCommerce.event.SalaryStepUpdated;
import com.skytala.eCommerce.query.FindSalaryStepsBy;

@RestController
@RequestMapping("/api/salaryStep")
public class SalaryStepController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SalaryStep>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SalaryStepController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SalaryStep
	 * @return a List with the SalarySteps
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SalaryStep> findSalaryStepsBy(@RequestParam Map<String, String> allRequestParams) {

		FindSalaryStepsBy query = new FindSalaryStepsBy(allRequestParams);

		int usedTicketId;

		synchronized (SalaryStepController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalaryStepFound.class,
				event -> sendSalaryStepsFoundMessage(((SalaryStepFound) event).getSalarySteps(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSalaryStepsFoundMessage(List<SalaryStep> salarySteps, int usedTicketId) {
		queryReturnVal.put(usedTicketId, salarySteps);
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
	public boolean createSalaryStep(HttpServletRequest request) {

		SalaryStep salaryStepToBeAdded = new SalaryStep();
		try {
			salaryStepToBeAdded = SalaryStepMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSalaryStep(salaryStepToBeAdded);

	}

	/**
	 * creates a new SalaryStep entry in the ofbiz database
	 * 
	 * @param salaryStepToBeAdded
	 *            the SalaryStep thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSalaryStep(SalaryStep salaryStepToBeAdded) {

		AddSalaryStep com = new AddSalaryStep(salaryStepToBeAdded);
		int usedTicketId;

		synchronized (SalaryStepController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalaryStepAdded.class,
				event -> sendSalaryStepChangedMessage(((SalaryStepAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSalaryStep(HttpServletRequest request) {

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

		SalaryStep salaryStepToBeUpdated = new SalaryStep();

		try {
			salaryStepToBeUpdated = SalaryStepMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSalaryStep(salaryStepToBeUpdated);

	}

	/**
	 * Updates the SalaryStep with the specific Id
	 * 
	 * @param salaryStepToBeUpdated the SalaryStep thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSalaryStep(SalaryStep salaryStepToBeUpdated) {

		UpdateSalaryStep com = new UpdateSalaryStep(salaryStepToBeUpdated);

		int usedTicketId;

		synchronized (SalaryStepController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalaryStepUpdated.class,
				event -> sendSalaryStepChangedMessage(((SalaryStepUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SalaryStep from the database
	 * 
	 * @param salaryStepId:
	 *            the id of the SalaryStep thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesalaryStepById(@RequestParam(value = "salaryStepId") String salaryStepId) {

		DeleteSalaryStep com = new DeleteSalaryStep(salaryStepId);

		int usedTicketId;

		synchronized (SalaryStepController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalaryStepDeleted.class,
				event -> sendSalaryStepChangedMessage(((SalaryStepDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSalaryStepChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/salaryStep/\" plus one of the following: "
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
