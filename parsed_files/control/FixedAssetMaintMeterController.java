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
import com.skytala.eCommerce.command.AddFixedAssetMaintMeter;
import com.skytala.eCommerce.command.DeleteFixedAssetMaintMeter;
import com.skytala.eCommerce.command.UpdateFixedAssetMaintMeter;
import com.skytala.eCommerce.entity.FixedAssetMaintMeter;
import com.skytala.eCommerce.entity.FixedAssetMaintMeterMapper;
import com.skytala.eCommerce.event.FixedAssetMaintMeterAdded;
import com.skytala.eCommerce.event.FixedAssetMaintMeterDeleted;
import com.skytala.eCommerce.event.FixedAssetMaintMeterFound;
import com.skytala.eCommerce.event.FixedAssetMaintMeterUpdated;
import com.skytala.eCommerce.query.FindFixedAssetMaintMetersBy;

@RestController
@RequestMapping("/api/fixedAssetMaintMeter")
public class FixedAssetMaintMeterController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FixedAssetMaintMeter>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FixedAssetMaintMeterController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FixedAssetMaintMeter
	 * @return a List with the FixedAssetMaintMeters
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FixedAssetMaintMeter> findFixedAssetMaintMetersBy(@RequestParam Map<String, String> allRequestParams) {

		FindFixedAssetMaintMetersBy query = new FindFixedAssetMaintMetersBy(allRequestParams);

		int usedTicketId;

		synchronized (FixedAssetMaintMeterController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetMaintMeterFound.class,
				event -> sendFixedAssetMaintMetersFoundMessage(((FixedAssetMaintMeterFound) event).getFixedAssetMaintMeters(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFixedAssetMaintMetersFoundMessage(List<FixedAssetMaintMeter> fixedAssetMaintMeters, int usedTicketId) {
		queryReturnVal.put(usedTicketId, fixedAssetMaintMeters);
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
	public boolean createFixedAssetMaintMeter(HttpServletRequest request) {

		FixedAssetMaintMeter fixedAssetMaintMeterToBeAdded = new FixedAssetMaintMeter();
		try {
			fixedAssetMaintMeterToBeAdded = FixedAssetMaintMeterMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFixedAssetMaintMeter(fixedAssetMaintMeterToBeAdded);

	}

	/**
	 * creates a new FixedAssetMaintMeter entry in the ofbiz database
	 * 
	 * @param fixedAssetMaintMeterToBeAdded
	 *            the FixedAssetMaintMeter thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFixedAssetMaintMeter(FixedAssetMaintMeter fixedAssetMaintMeterToBeAdded) {

		AddFixedAssetMaintMeter com = new AddFixedAssetMaintMeter(fixedAssetMaintMeterToBeAdded);
		int usedTicketId;

		synchronized (FixedAssetMaintMeterController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetMaintMeterAdded.class,
				event -> sendFixedAssetMaintMeterChangedMessage(((FixedAssetMaintMeterAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFixedAssetMaintMeter(HttpServletRequest request) {

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

		FixedAssetMaintMeter fixedAssetMaintMeterToBeUpdated = new FixedAssetMaintMeter();

		try {
			fixedAssetMaintMeterToBeUpdated = FixedAssetMaintMeterMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFixedAssetMaintMeter(fixedAssetMaintMeterToBeUpdated);

	}

	/**
	 * Updates the FixedAssetMaintMeter with the specific Id
	 * 
	 * @param fixedAssetMaintMeterToBeUpdated the FixedAssetMaintMeter thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFixedAssetMaintMeter(FixedAssetMaintMeter fixedAssetMaintMeterToBeUpdated) {

		UpdateFixedAssetMaintMeter com = new UpdateFixedAssetMaintMeter(fixedAssetMaintMeterToBeUpdated);

		int usedTicketId;

		synchronized (FixedAssetMaintMeterController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetMaintMeterUpdated.class,
				event -> sendFixedAssetMaintMeterChangedMessage(((FixedAssetMaintMeterUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FixedAssetMaintMeter from the database
	 * 
	 * @param fixedAssetMaintMeterId:
	 *            the id of the FixedAssetMaintMeter thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefixedAssetMaintMeterById(@RequestParam(value = "fixedAssetMaintMeterId") String fixedAssetMaintMeterId) {

		DeleteFixedAssetMaintMeter com = new DeleteFixedAssetMaintMeter(fixedAssetMaintMeterId);

		int usedTicketId;

		synchronized (FixedAssetMaintMeterController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetMaintMeterDeleted.class,
				event -> sendFixedAssetMaintMeterChangedMessage(((FixedAssetMaintMeterDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFixedAssetMaintMeterChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/fixedAssetMaintMeter/\" plus one of the following: "
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
