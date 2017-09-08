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
import com.skytala.eCommerce.command.AddDelivery;
import com.skytala.eCommerce.command.DeleteDelivery;
import com.skytala.eCommerce.command.UpdateDelivery;
import com.skytala.eCommerce.entity.Delivery;
import com.skytala.eCommerce.entity.DeliveryMapper;
import com.skytala.eCommerce.event.DeliveryAdded;
import com.skytala.eCommerce.event.DeliveryDeleted;
import com.skytala.eCommerce.event.DeliveryFound;
import com.skytala.eCommerce.event.DeliveryUpdated;
import com.skytala.eCommerce.query.FindDeliverysBy;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<Delivery>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public DeliveryController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a Delivery
	 * @return a List with the Deliverys
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<Delivery> findDeliverysBy(@RequestParam Map<String, String> allRequestParams) {

		FindDeliverysBy query = new FindDeliverysBy(allRequestParams);

		int usedTicketId;

		synchronized (DeliveryController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DeliveryFound.class,
				event -> sendDeliverysFoundMessage(((DeliveryFound) event).getDeliverys(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendDeliverysFoundMessage(List<Delivery> deliverys, int usedTicketId) {
		queryReturnVal.put(usedTicketId, deliverys);
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
	public boolean createDelivery(HttpServletRequest request) {

		Delivery deliveryToBeAdded = new Delivery();
		try {
			deliveryToBeAdded = DeliveryMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createDelivery(deliveryToBeAdded);

	}

	/**
	 * creates a new Delivery entry in the ofbiz database
	 * 
	 * @param deliveryToBeAdded
	 *            the Delivery thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createDelivery(Delivery deliveryToBeAdded) {

		AddDelivery com = new AddDelivery(deliveryToBeAdded);
		int usedTicketId;

		synchronized (DeliveryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DeliveryAdded.class,
				event -> sendDeliveryChangedMessage(((DeliveryAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateDelivery(HttpServletRequest request) {

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

		Delivery deliveryToBeUpdated = new Delivery();

		try {
			deliveryToBeUpdated = DeliveryMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateDelivery(deliveryToBeUpdated);

	}

	/**
	 * Updates the Delivery with the specific Id
	 * 
	 * @param deliveryToBeUpdated the Delivery thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateDelivery(Delivery deliveryToBeUpdated) {

		UpdateDelivery com = new UpdateDelivery(deliveryToBeUpdated);

		int usedTicketId;

		synchronized (DeliveryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DeliveryUpdated.class,
				event -> sendDeliveryChangedMessage(((DeliveryUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a Delivery from the database
	 * 
	 * @param deliveryId:
	 *            the id of the Delivery thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletedeliveryById(@RequestParam(value = "deliveryId") String deliveryId) {

		DeleteDelivery com = new DeleteDelivery(deliveryId);

		int usedTicketId;

		synchronized (DeliveryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DeliveryDeleted.class,
				event -> sendDeliveryChangedMessage(((DeliveryDeleted) event).isSuccess(), usedTicketId));

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

	public void sendDeliveryChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/delivery/\" plus one of the following: "
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
