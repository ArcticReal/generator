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
import com.skytala.eCommerce.command.AddFixedAssetProduct;
import com.skytala.eCommerce.command.DeleteFixedAssetProduct;
import com.skytala.eCommerce.command.UpdateFixedAssetProduct;
import com.skytala.eCommerce.entity.FixedAssetProduct;
import com.skytala.eCommerce.entity.FixedAssetProductMapper;
import com.skytala.eCommerce.event.FixedAssetProductAdded;
import com.skytala.eCommerce.event.FixedAssetProductDeleted;
import com.skytala.eCommerce.event.FixedAssetProductFound;
import com.skytala.eCommerce.event.FixedAssetProductUpdated;
import com.skytala.eCommerce.query.FindFixedAssetProductsBy;

@RestController
@RequestMapping("/api/fixedAssetProduct")
public class FixedAssetProductController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FixedAssetProduct>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FixedAssetProductController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FixedAssetProduct
	 * @return a List with the FixedAssetProducts
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FixedAssetProduct> findFixedAssetProductsBy(@RequestParam Map<String, String> allRequestParams) {

		FindFixedAssetProductsBy query = new FindFixedAssetProductsBy(allRequestParams);

		int usedTicketId;

		synchronized (FixedAssetProductController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetProductFound.class,
				event -> sendFixedAssetProductsFoundMessage(((FixedAssetProductFound) event).getFixedAssetProducts(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFixedAssetProductsFoundMessage(List<FixedAssetProduct> fixedAssetProducts, int usedTicketId) {
		queryReturnVal.put(usedTicketId, fixedAssetProducts);
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
	public boolean createFixedAssetProduct(HttpServletRequest request) {

		FixedAssetProduct fixedAssetProductToBeAdded = new FixedAssetProduct();
		try {
			fixedAssetProductToBeAdded = FixedAssetProductMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFixedAssetProduct(fixedAssetProductToBeAdded);

	}

	/**
	 * creates a new FixedAssetProduct entry in the ofbiz database
	 * 
	 * @param fixedAssetProductToBeAdded
	 *            the FixedAssetProduct thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFixedAssetProduct(FixedAssetProduct fixedAssetProductToBeAdded) {

		AddFixedAssetProduct com = new AddFixedAssetProduct(fixedAssetProductToBeAdded);
		int usedTicketId;

		synchronized (FixedAssetProductController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetProductAdded.class,
				event -> sendFixedAssetProductChangedMessage(((FixedAssetProductAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFixedAssetProduct(HttpServletRequest request) {

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

		FixedAssetProduct fixedAssetProductToBeUpdated = new FixedAssetProduct();

		try {
			fixedAssetProductToBeUpdated = FixedAssetProductMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFixedAssetProduct(fixedAssetProductToBeUpdated);

	}

	/**
	 * Updates the FixedAssetProduct with the specific Id
	 * 
	 * @param fixedAssetProductToBeUpdated the FixedAssetProduct thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFixedAssetProduct(FixedAssetProduct fixedAssetProductToBeUpdated) {

		UpdateFixedAssetProduct com = new UpdateFixedAssetProduct(fixedAssetProductToBeUpdated);

		int usedTicketId;

		synchronized (FixedAssetProductController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetProductUpdated.class,
				event -> sendFixedAssetProductChangedMessage(((FixedAssetProductUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FixedAssetProduct from the database
	 * 
	 * @param fixedAssetProductId:
	 *            the id of the FixedAssetProduct thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefixedAssetProductById(@RequestParam(value = "fixedAssetProductId") String fixedAssetProductId) {

		DeleteFixedAssetProduct com = new DeleteFixedAssetProduct(fixedAssetProductId);

		int usedTicketId;

		synchronized (FixedAssetProductController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetProductDeleted.class,
				event -> sendFixedAssetProductChangedMessage(((FixedAssetProductDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFixedAssetProductChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/fixedAssetProduct/\" plus one of the following: "
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
