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
import com.skytala.eCommerce.command.AddProductStoreShipmentMeth;
import com.skytala.eCommerce.command.DeleteProductStoreShipmentMeth;
import com.skytala.eCommerce.command.UpdateProductStoreShipmentMeth;
import com.skytala.eCommerce.entity.ProductStoreShipmentMeth;
import com.skytala.eCommerce.entity.ProductStoreShipmentMethMapper;
import com.skytala.eCommerce.event.ProductStoreShipmentMethAdded;
import com.skytala.eCommerce.event.ProductStoreShipmentMethDeleted;
import com.skytala.eCommerce.event.ProductStoreShipmentMethFound;
import com.skytala.eCommerce.event.ProductStoreShipmentMethUpdated;
import com.skytala.eCommerce.query.FindProductStoreShipmentMethsBy;

@RestController
@RequestMapping("/api/productStoreShipmentMeth")
public class ProductStoreShipmentMethController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductStoreShipmentMeth>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductStoreShipmentMethController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductStoreShipmentMeth
	 * @return a List with the ProductStoreShipmentMeths
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductStoreShipmentMeth> findProductStoreShipmentMethsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductStoreShipmentMethsBy query = new FindProductStoreShipmentMethsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductStoreShipmentMethController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreShipmentMethFound.class,
				event -> sendProductStoreShipmentMethsFoundMessage(((ProductStoreShipmentMethFound) event).getProductStoreShipmentMeths(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductStoreShipmentMethsFoundMessage(List<ProductStoreShipmentMeth> productStoreShipmentMeths, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productStoreShipmentMeths);
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
	public boolean createProductStoreShipmentMeth(HttpServletRequest request) {

		ProductStoreShipmentMeth productStoreShipmentMethToBeAdded = new ProductStoreShipmentMeth();
		try {
			productStoreShipmentMethToBeAdded = ProductStoreShipmentMethMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductStoreShipmentMeth(productStoreShipmentMethToBeAdded);

	}

	/**
	 * creates a new ProductStoreShipmentMeth entry in the ofbiz database
	 * 
	 * @param productStoreShipmentMethToBeAdded
	 *            the ProductStoreShipmentMeth thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductStoreShipmentMeth(ProductStoreShipmentMeth productStoreShipmentMethToBeAdded) {

		AddProductStoreShipmentMeth com = new AddProductStoreShipmentMeth(productStoreShipmentMethToBeAdded);
		int usedTicketId;

		synchronized (ProductStoreShipmentMethController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreShipmentMethAdded.class,
				event -> sendProductStoreShipmentMethChangedMessage(((ProductStoreShipmentMethAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductStoreShipmentMeth(HttpServletRequest request) {

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

		ProductStoreShipmentMeth productStoreShipmentMethToBeUpdated = new ProductStoreShipmentMeth();

		try {
			productStoreShipmentMethToBeUpdated = ProductStoreShipmentMethMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductStoreShipmentMeth(productStoreShipmentMethToBeUpdated);

	}

	/**
	 * Updates the ProductStoreShipmentMeth with the specific Id
	 * 
	 * @param productStoreShipmentMethToBeUpdated the ProductStoreShipmentMeth thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductStoreShipmentMeth(ProductStoreShipmentMeth productStoreShipmentMethToBeUpdated) {

		UpdateProductStoreShipmentMeth com = new UpdateProductStoreShipmentMeth(productStoreShipmentMethToBeUpdated);

		int usedTicketId;

		synchronized (ProductStoreShipmentMethController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreShipmentMethUpdated.class,
				event -> sendProductStoreShipmentMethChangedMessage(((ProductStoreShipmentMethUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductStoreShipmentMeth from the database
	 * 
	 * @param productStoreShipmentMethId:
	 *            the id of the ProductStoreShipmentMeth thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductStoreShipmentMethById(@RequestParam(value = "productStoreShipmentMethId") String productStoreShipmentMethId) {

		DeleteProductStoreShipmentMeth com = new DeleteProductStoreShipmentMeth(productStoreShipmentMethId);

		int usedTicketId;

		synchronized (ProductStoreShipmentMethController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreShipmentMethDeleted.class,
				event -> sendProductStoreShipmentMethChangedMessage(((ProductStoreShipmentMethDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductStoreShipmentMethChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productStoreShipmentMeth/\" plus one of the following: "
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
