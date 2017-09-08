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
import com.skytala.eCommerce.command.AddTrainingRequest;
import com.skytala.eCommerce.command.DeleteTrainingRequest;
import com.skytala.eCommerce.command.UpdateTrainingRequest;
import com.skytala.eCommerce.entity.TrainingRequest;
import com.skytala.eCommerce.entity.TrainingRequestMapper;
import com.skytala.eCommerce.event.TrainingRequestAdded;
import com.skytala.eCommerce.event.TrainingRequestDeleted;
import com.skytala.eCommerce.event.TrainingRequestFound;
import com.skytala.eCommerce.event.TrainingRequestUpdated;
import com.skytala.eCommerce.query.FindTrainingRequestsBy;

@RestController
@RequestMapping("/api/trainingRequest")
public class TrainingRequestController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<TrainingRequest>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public TrainingRequestController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a TrainingRequest
	 * @return a List with the TrainingRequests
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<TrainingRequest> findTrainingRequestsBy(@RequestParam Map<String, String> allRequestParams) {

		FindTrainingRequestsBy query = new FindTrainingRequestsBy(allRequestParams);

		int usedTicketId;

		synchronized (TrainingRequestController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TrainingRequestFound.class,
				event -> sendTrainingRequestsFoundMessage(((TrainingRequestFound) event).getTrainingRequests(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendTrainingRequestsFoundMessage(List<TrainingRequest> trainingRequests, int usedTicketId) {
		queryReturnVal.put(usedTicketId, trainingRequests);
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
	public boolean createTrainingRequest(HttpServletRequest request) {

		TrainingRequest trainingRequestToBeAdded = new TrainingRequest();
		try {
			trainingRequestToBeAdded = TrainingRequestMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createTrainingRequest(trainingRequestToBeAdded);

	}

	/**
	 * creates a new TrainingRequest entry in the ofbiz database
	 * 
	 * @param trainingRequestToBeAdded
	 *            the TrainingRequest thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createTrainingRequest(TrainingRequest trainingRequestToBeAdded) {

		AddTrainingRequest com = new AddTrainingRequest(trainingRequestToBeAdded);
		int usedTicketId;

		synchronized (TrainingRequestController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TrainingRequestAdded.class,
				event -> sendTrainingRequestChangedMessage(((TrainingRequestAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateTrainingRequest(HttpServletRequest request) {

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

		TrainingRequest trainingRequestToBeUpdated = new TrainingRequest();

		try {
			trainingRequestToBeUpdated = TrainingRequestMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateTrainingRequest(trainingRequestToBeUpdated);

	}

	/**
	 * Updates the TrainingRequest with the specific Id
	 * 
	 * @param trainingRequestToBeUpdated the TrainingRequest thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateTrainingRequest(TrainingRequest trainingRequestToBeUpdated) {

		UpdateTrainingRequest com = new UpdateTrainingRequest(trainingRequestToBeUpdated);

		int usedTicketId;

		synchronized (TrainingRequestController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TrainingRequestUpdated.class,
				event -> sendTrainingRequestChangedMessage(((TrainingRequestUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a TrainingRequest from the database
	 * 
	 * @param trainingRequestId:
	 *            the id of the TrainingRequest thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletetrainingRequestById(@RequestParam(value = "trainingRequestId") String trainingRequestId) {

		DeleteTrainingRequest com = new DeleteTrainingRequest(trainingRequestId);

		int usedTicketId;

		synchronized (TrainingRequestController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TrainingRequestDeleted.class,
				event -> sendTrainingRequestChangedMessage(((TrainingRequestDeleted) event).isSuccess(), usedTicketId));

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

	public void sendTrainingRequestChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/trainingRequest/\" plus one of the following: "
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
