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
import com.skytala.eCommerce.command.AddPersonTraining;
import com.skytala.eCommerce.command.DeletePersonTraining;
import com.skytala.eCommerce.command.UpdatePersonTraining;
import com.skytala.eCommerce.entity.PersonTraining;
import com.skytala.eCommerce.entity.PersonTrainingMapper;
import com.skytala.eCommerce.event.PersonTrainingAdded;
import com.skytala.eCommerce.event.PersonTrainingDeleted;
import com.skytala.eCommerce.event.PersonTrainingFound;
import com.skytala.eCommerce.event.PersonTrainingUpdated;
import com.skytala.eCommerce.query.FindPersonTrainingsBy;

@RestController
@RequestMapping("/api/personTraining")
public class PersonTrainingController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PersonTraining>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PersonTrainingController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PersonTraining
	 * @return a List with the PersonTrainings
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PersonTraining> findPersonTrainingsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPersonTrainingsBy query = new FindPersonTrainingsBy(allRequestParams);

		int usedTicketId;

		synchronized (PersonTrainingController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PersonTrainingFound.class,
				event -> sendPersonTrainingsFoundMessage(((PersonTrainingFound) event).getPersonTrainings(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPersonTrainingsFoundMessage(List<PersonTraining> personTrainings, int usedTicketId) {
		queryReturnVal.put(usedTicketId, personTrainings);
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
	public boolean createPersonTraining(HttpServletRequest request) {

		PersonTraining personTrainingToBeAdded = new PersonTraining();
		try {
			personTrainingToBeAdded = PersonTrainingMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPersonTraining(personTrainingToBeAdded);

	}

	/**
	 * creates a new PersonTraining entry in the ofbiz database
	 * 
	 * @param personTrainingToBeAdded
	 *            the PersonTraining thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPersonTraining(PersonTraining personTrainingToBeAdded) {

		AddPersonTraining com = new AddPersonTraining(personTrainingToBeAdded);
		int usedTicketId;

		synchronized (PersonTrainingController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PersonTrainingAdded.class,
				event -> sendPersonTrainingChangedMessage(((PersonTrainingAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePersonTraining(HttpServletRequest request) {

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

		PersonTraining personTrainingToBeUpdated = new PersonTraining();

		try {
			personTrainingToBeUpdated = PersonTrainingMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePersonTraining(personTrainingToBeUpdated);

	}

	/**
	 * Updates the PersonTraining with the specific Id
	 * 
	 * @param personTrainingToBeUpdated the PersonTraining thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePersonTraining(PersonTraining personTrainingToBeUpdated) {

		UpdatePersonTraining com = new UpdatePersonTraining(personTrainingToBeUpdated);

		int usedTicketId;

		synchronized (PersonTrainingController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PersonTrainingUpdated.class,
				event -> sendPersonTrainingChangedMessage(((PersonTrainingUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PersonTraining from the database
	 * 
	 * @param personTrainingId:
	 *            the id of the PersonTraining thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepersonTrainingById(@RequestParam(value = "personTrainingId") String personTrainingId) {

		DeletePersonTraining com = new DeletePersonTraining(personTrainingId);

		int usedTicketId;

		synchronized (PersonTrainingController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PersonTrainingDeleted.class,
				event -> sendPersonTrainingChangedMessage(((PersonTrainingDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPersonTrainingChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/personTraining/\" plus one of the following: "
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
