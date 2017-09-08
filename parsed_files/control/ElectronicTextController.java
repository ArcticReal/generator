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
import com.skytala.eCommerce.command.AddElectronicText;
import com.skytala.eCommerce.command.DeleteElectronicText;
import com.skytala.eCommerce.command.UpdateElectronicText;
import com.skytala.eCommerce.entity.ElectronicText;
import com.skytala.eCommerce.entity.ElectronicTextMapper;
import com.skytala.eCommerce.event.ElectronicTextAdded;
import com.skytala.eCommerce.event.ElectronicTextDeleted;
import com.skytala.eCommerce.event.ElectronicTextFound;
import com.skytala.eCommerce.event.ElectronicTextUpdated;
import com.skytala.eCommerce.query.FindElectronicTextsBy;

@RestController
@RequestMapping("/api/electronicText")
public class ElectronicTextController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ElectronicText>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ElectronicTextController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ElectronicText
	 * @return a List with the ElectronicTexts
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ElectronicText> findElectronicTextsBy(@RequestParam Map<String, String> allRequestParams) {

		FindElectronicTextsBy query = new FindElectronicTextsBy(allRequestParams);

		int usedTicketId;

		synchronized (ElectronicTextController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ElectronicTextFound.class,
				event -> sendElectronicTextsFoundMessage(((ElectronicTextFound) event).getElectronicTexts(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendElectronicTextsFoundMessage(List<ElectronicText> electronicTexts, int usedTicketId) {
		queryReturnVal.put(usedTicketId, electronicTexts);
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
	public boolean createElectronicText(HttpServletRequest request) {

		ElectronicText electronicTextToBeAdded = new ElectronicText();
		try {
			electronicTextToBeAdded = ElectronicTextMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createElectronicText(electronicTextToBeAdded);

	}

	/**
	 * creates a new ElectronicText entry in the ofbiz database
	 * 
	 * @param electronicTextToBeAdded
	 *            the ElectronicText thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createElectronicText(ElectronicText electronicTextToBeAdded) {

		AddElectronicText com = new AddElectronicText(electronicTextToBeAdded);
		int usedTicketId;

		synchronized (ElectronicTextController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ElectronicTextAdded.class,
				event -> sendElectronicTextChangedMessage(((ElectronicTextAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateElectronicText(HttpServletRequest request) {

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

		ElectronicText electronicTextToBeUpdated = new ElectronicText();

		try {
			electronicTextToBeUpdated = ElectronicTextMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateElectronicText(electronicTextToBeUpdated);

	}

	/**
	 * Updates the ElectronicText with the specific Id
	 * 
	 * @param electronicTextToBeUpdated the ElectronicText thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateElectronicText(ElectronicText electronicTextToBeUpdated) {

		UpdateElectronicText com = new UpdateElectronicText(electronicTextToBeUpdated);

		int usedTicketId;

		synchronized (ElectronicTextController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ElectronicTextUpdated.class,
				event -> sendElectronicTextChangedMessage(((ElectronicTextUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ElectronicText from the database
	 * 
	 * @param electronicTextId:
	 *            the id of the ElectronicText thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteelectronicTextById(@RequestParam(value = "electronicTextId") String electronicTextId) {

		DeleteElectronicText com = new DeleteElectronicText(electronicTextId);

		int usedTicketId;

		synchronized (ElectronicTextController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ElectronicTextDeleted.class,
				event -> sendElectronicTextChangedMessage(((ElectronicTextDeleted) event).isSuccess(), usedTicketId));

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

	public void sendElectronicTextChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/electronicText/\" plus one of the following: "
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
