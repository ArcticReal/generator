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
import com.skytala.eCommerce.command.AddTrainingClassType;
import com.skytala.eCommerce.command.DeleteTrainingClassType;
import com.skytala.eCommerce.command.UpdateTrainingClassType;
import com.skytala.eCommerce.entity.TrainingClassType;
import com.skytala.eCommerce.entity.TrainingClassTypeMapper;
import com.skytala.eCommerce.event.TrainingClassTypeAdded;
import com.skytala.eCommerce.event.TrainingClassTypeDeleted;
import com.skytala.eCommerce.event.TrainingClassTypeFound;
import com.skytala.eCommerce.event.TrainingClassTypeUpdated;
import com.skytala.eCommerce.query.FindTrainingClassTypesBy;

@RestController
@RequestMapping("/api/trainingClassType")
public class TrainingClassTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<TrainingClassType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public TrainingClassTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a TrainingClassType
	 * @return a List with the TrainingClassTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<TrainingClassType> findTrainingClassTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindTrainingClassTypesBy query = new FindTrainingClassTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (TrainingClassTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TrainingClassTypeFound.class,
				event -> sendTrainingClassTypesFoundMessage(((TrainingClassTypeFound) event).getTrainingClassTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendTrainingClassTypesFoundMessage(List<TrainingClassType> trainingClassTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, trainingClassTypes);
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
	public boolean createTrainingClassType(HttpServletRequest request) {

		TrainingClassType trainingClassTypeToBeAdded = new TrainingClassType();
		try {
			trainingClassTypeToBeAdded = TrainingClassTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createTrainingClassType(trainingClassTypeToBeAdded);

	}

	/**
	 * creates a new TrainingClassType entry in the ofbiz database
	 * 
	 * @param trainingClassTypeToBeAdded
	 *            the TrainingClassType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createTrainingClassType(TrainingClassType trainingClassTypeToBeAdded) {

		AddTrainingClassType com = new AddTrainingClassType(trainingClassTypeToBeAdded);
		int usedTicketId;

		synchronized (TrainingClassTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TrainingClassTypeAdded.class,
				event -> sendTrainingClassTypeChangedMessage(((TrainingClassTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateTrainingClassType(HttpServletRequest request) {

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

		TrainingClassType trainingClassTypeToBeUpdated = new TrainingClassType();

		try {
			trainingClassTypeToBeUpdated = TrainingClassTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateTrainingClassType(trainingClassTypeToBeUpdated);

	}

	/**
	 * Updates the TrainingClassType with the specific Id
	 * 
	 * @param trainingClassTypeToBeUpdated the TrainingClassType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateTrainingClassType(TrainingClassType trainingClassTypeToBeUpdated) {

		UpdateTrainingClassType com = new UpdateTrainingClassType(trainingClassTypeToBeUpdated);

		int usedTicketId;

		synchronized (TrainingClassTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TrainingClassTypeUpdated.class,
				event -> sendTrainingClassTypeChangedMessage(((TrainingClassTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a TrainingClassType from the database
	 * 
	 * @param trainingClassTypeId:
	 *            the id of the TrainingClassType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletetrainingClassTypeById(@RequestParam(value = "trainingClassTypeId") String trainingClassTypeId) {

		DeleteTrainingClassType com = new DeleteTrainingClassType(trainingClassTypeId);

		int usedTicketId;

		synchronized (TrainingClassTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TrainingClassTypeDeleted.class,
				event -> sendTrainingClassTypeChangedMessage(((TrainingClassTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendTrainingClassTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/trainingClassType/\" plus one of the following: "
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
