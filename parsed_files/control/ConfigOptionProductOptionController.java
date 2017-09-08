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
import com.skytala.eCommerce.command.AddConfigOptionProductOption;
import com.skytala.eCommerce.command.DeleteConfigOptionProductOption;
import com.skytala.eCommerce.command.UpdateConfigOptionProductOption;
import com.skytala.eCommerce.entity.ConfigOptionProductOption;
import com.skytala.eCommerce.entity.ConfigOptionProductOptionMapper;
import com.skytala.eCommerce.event.ConfigOptionProductOptionAdded;
import com.skytala.eCommerce.event.ConfigOptionProductOptionDeleted;
import com.skytala.eCommerce.event.ConfigOptionProductOptionFound;
import com.skytala.eCommerce.event.ConfigOptionProductOptionUpdated;
import com.skytala.eCommerce.query.FindConfigOptionProductOptionsBy;

@RestController
@RequestMapping("/api/configOptionProductOption")
public class ConfigOptionProductOptionController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ConfigOptionProductOption>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ConfigOptionProductOptionController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ConfigOptionProductOption
	 * @return a List with the ConfigOptionProductOptions
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ConfigOptionProductOption> findConfigOptionProductOptionsBy(@RequestParam Map<String, String> allRequestParams) {

		FindConfigOptionProductOptionsBy query = new FindConfigOptionProductOptionsBy(allRequestParams);

		int usedTicketId;

		synchronized (ConfigOptionProductOptionController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ConfigOptionProductOptionFound.class,
				event -> sendConfigOptionProductOptionsFoundMessage(((ConfigOptionProductOptionFound) event).getConfigOptionProductOptions(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendConfigOptionProductOptionsFoundMessage(List<ConfigOptionProductOption> configOptionProductOptions, int usedTicketId) {
		queryReturnVal.put(usedTicketId, configOptionProductOptions);
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
	public boolean createConfigOptionProductOption(HttpServletRequest request) {

		ConfigOptionProductOption configOptionProductOptionToBeAdded = new ConfigOptionProductOption();
		try {
			configOptionProductOptionToBeAdded = ConfigOptionProductOptionMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createConfigOptionProductOption(configOptionProductOptionToBeAdded);

	}

	/**
	 * creates a new ConfigOptionProductOption entry in the ofbiz database
	 * 
	 * @param configOptionProductOptionToBeAdded
	 *            the ConfigOptionProductOption thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createConfigOptionProductOption(ConfigOptionProductOption configOptionProductOptionToBeAdded) {

		AddConfigOptionProductOption com = new AddConfigOptionProductOption(configOptionProductOptionToBeAdded);
		int usedTicketId;

		synchronized (ConfigOptionProductOptionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ConfigOptionProductOptionAdded.class,
				event -> sendConfigOptionProductOptionChangedMessage(((ConfigOptionProductOptionAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateConfigOptionProductOption(HttpServletRequest request) {

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

		ConfigOptionProductOption configOptionProductOptionToBeUpdated = new ConfigOptionProductOption();

		try {
			configOptionProductOptionToBeUpdated = ConfigOptionProductOptionMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateConfigOptionProductOption(configOptionProductOptionToBeUpdated);

	}

	/**
	 * Updates the ConfigOptionProductOption with the specific Id
	 * 
	 * @param configOptionProductOptionToBeUpdated the ConfigOptionProductOption thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateConfigOptionProductOption(ConfigOptionProductOption configOptionProductOptionToBeUpdated) {

		UpdateConfigOptionProductOption com = new UpdateConfigOptionProductOption(configOptionProductOptionToBeUpdated);

		int usedTicketId;

		synchronized (ConfigOptionProductOptionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ConfigOptionProductOptionUpdated.class,
				event -> sendConfigOptionProductOptionChangedMessage(((ConfigOptionProductOptionUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ConfigOptionProductOption from the database
	 * 
	 * @param configOptionProductOptionId:
	 *            the id of the ConfigOptionProductOption thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteconfigOptionProductOptionById(@RequestParam(value = "configOptionProductOptionId") String configOptionProductOptionId) {

		DeleteConfigOptionProductOption com = new DeleteConfigOptionProductOption(configOptionProductOptionId);

		int usedTicketId;

		synchronized (ConfigOptionProductOptionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ConfigOptionProductOptionDeleted.class,
				event -> sendConfigOptionProductOptionChangedMessage(((ConfigOptionProductOptionDeleted) event).isSuccess(), usedTicketId));

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

	public void sendConfigOptionProductOptionChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/configOptionProductOption/\" plus one of the following: "
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
