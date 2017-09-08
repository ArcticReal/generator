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
import com.skytala.eCommerce.command.AddSupplierProductFeature;
import com.skytala.eCommerce.command.DeleteSupplierProductFeature;
import com.skytala.eCommerce.command.UpdateSupplierProductFeature;
import com.skytala.eCommerce.entity.SupplierProductFeature;
import com.skytala.eCommerce.entity.SupplierProductFeatureMapper;
import com.skytala.eCommerce.event.SupplierProductFeatureAdded;
import com.skytala.eCommerce.event.SupplierProductFeatureDeleted;
import com.skytala.eCommerce.event.SupplierProductFeatureFound;
import com.skytala.eCommerce.event.SupplierProductFeatureUpdated;
import com.skytala.eCommerce.query.FindSupplierProductFeaturesBy;

@RestController
@RequestMapping("/api/supplierProductFeature")
public class SupplierProductFeatureController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SupplierProductFeature>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SupplierProductFeatureController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SupplierProductFeature
	 * @return a List with the SupplierProductFeatures
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SupplierProductFeature> findSupplierProductFeaturesBy(@RequestParam Map<String, String> allRequestParams) {

		FindSupplierProductFeaturesBy query = new FindSupplierProductFeaturesBy(allRequestParams);

		int usedTicketId;

		synchronized (SupplierProductFeatureController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SupplierProductFeatureFound.class,
				event -> sendSupplierProductFeaturesFoundMessage(((SupplierProductFeatureFound) event).getSupplierProductFeatures(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSupplierProductFeaturesFoundMessage(List<SupplierProductFeature> supplierProductFeatures, int usedTicketId) {
		queryReturnVal.put(usedTicketId, supplierProductFeatures);
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
	public boolean createSupplierProductFeature(HttpServletRequest request) {

		SupplierProductFeature supplierProductFeatureToBeAdded = new SupplierProductFeature();
		try {
			supplierProductFeatureToBeAdded = SupplierProductFeatureMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSupplierProductFeature(supplierProductFeatureToBeAdded);

	}

	/**
	 * creates a new SupplierProductFeature entry in the ofbiz database
	 * 
	 * @param supplierProductFeatureToBeAdded
	 *            the SupplierProductFeature thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSupplierProductFeature(SupplierProductFeature supplierProductFeatureToBeAdded) {

		AddSupplierProductFeature com = new AddSupplierProductFeature(supplierProductFeatureToBeAdded);
		int usedTicketId;

		synchronized (SupplierProductFeatureController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SupplierProductFeatureAdded.class,
				event -> sendSupplierProductFeatureChangedMessage(((SupplierProductFeatureAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSupplierProductFeature(HttpServletRequest request) {

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

		SupplierProductFeature supplierProductFeatureToBeUpdated = new SupplierProductFeature();

		try {
			supplierProductFeatureToBeUpdated = SupplierProductFeatureMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSupplierProductFeature(supplierProductFeatureToBeUpdated);

	}

	/**
	 * Updates the SupplierProductFeature with the specific Id
	 * 
	 * @param supplierProductFeatureToBeUpdated the SupplierProductFeature thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSupplierProductFeature(SupplierProductFeature supplierProductFeatureToBeUpdated) {

		UpdateSupplierProductFeature com = new UpdateSupplierProductFeature(supplierProductFeatureToBeUpdated);

		int usedTicketId;

		synchronized (SupplierProductFeatureController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SupplierProductFeatureUpdated.class,
				event -> sendSupplierProductFeatureChangedMessage(((SupplierProductFeatureUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SupplierProductFeature from the database
	 * 
	 * @param supplierProductFeatureId:
	 *            the id of the SupplierProductFeature thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesupplierProductFeatureById(@RequestParam(value = "supplierProductFeatureId") String supplierProductFeatureId) {

		DeleteSupplierProductFeature com = new DeleteSupplierProductFeature(supplierProductFeatureId);

		int usedTicketId;

		synchronized (SupplierProductFeatureController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SupplierProductFeatureDeleted.class,
				event -> sendSupplierProductFeatureChangedMessage(((SupplierProductFeatureDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSupplierProductFeatureChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/supplierProductFeature/\" plus one of the following: "
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
