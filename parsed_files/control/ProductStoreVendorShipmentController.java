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
import com.skytala.eCommerce.command.AddProductStoreVendorShipment;
import com.skytala.eCommerce.command.DeleteProductStoreVendorShipment;
import com.skytala.eCommerce.command.UpdateProductStoreVendorShipment;
import com.skytala.eCommerce.entity.ProductStoreVendorShipment;
import com.skytala.eCommerce.entity.ProductStoreVendorShipmentMapper;
import com.skytala.eCommerce.event.ProductStoreVendorShipmentAdded;
import com.skytala.eCommerce.event.ProductStoreVendorShipmentDeleted;
import com.skytala.eCommerce.event.ProductStoreVendorShipmentFound;
import com.skytala.eCommerce.event.ProductStoreVendorShipmentUpdated;
import com.skytala.eCommerce.query.FindProductStoreVendorShipmentsBy;

@RestController
@RequestMapping("/api/productStoreVendorShipment")
public class ProductStoreVendorShipmentController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductStoreVendorShipment>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductStoreVendorShipmentController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductStoreVendorShipment
	 * @return a List with the ProductStoreVendorShipments
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductStoreVendorShipment> findProductStoreVendorShipmentsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductStoreVendorShipmentsBy query = new FindProductStoreVendorShipmentsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductStoreVendorShipmentController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreVendorShipmentFound.class,
				event -> sendProductStoreVendorShipmentsFoundMessage(((ProductStoreVendorShipmentFound) event).getProductStoreVendorShipments(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductStoreVendorShipmentsFoundMessage(List<ProductStoreVendorShipment> productStoreVendorShipments, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productStoreVendorShipments);
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
	public boolean createProductStoreVendorShipment(HttpServletRequest request) {

		ProductStoreVendorShipment productStoreVendorShipmentToBeAdded = new ProductStoreVendorShipment();
		try {
			productStoreVendorShipmentToBeAdded = ProductStoreVendorShipmentMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductStoreVendorShipment(productStoreVendorShipmentToBeAdded);

	}

	/**
	 * creates a new ProductStoreVendorShipment entry in the ofbiz database
	 * 
	 * @param productStoreVendorShipmentToBeAdded
	 *            the ProductStoreVendorShipment thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductStoreVendorShipment(ProductStoreVendorShipment productStoreVendorShipmentToBeAdded) {

		AddProductStoreVendorShipment com = new AddProductStoreVendorShipment(productStoreVendorShipmentToBeAdded);
		int usedTicketId;

		synchronized (ProductStoreVendorShipmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreVendorShipmentAdded.class,
				event -> sendProductStoreVendorShipmentChangedMessage(((ProductStoreVendorShipmentAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductStoreVendorShipment(HttpServletRequest request) {

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

		ProductStoreVendorShipment productStoreVendorShipmentToBeUpdated = new ProductStoreVendorShipment();

		try {
			productStoreVendorShipmentToBeUpdated = ProductStoreVendorShipmentMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductStoreVendorShipment(productStoreVendorShipmentToBeUpdated);

	}

	/**
	 * Updates the ProductStoreVendorShipment with the specific Id
	 * 
	 * @param productStoreVendorShipmentToBeUpdated the ProductStoreVendorShipment thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductStoreVendorShipment(ProductStoreVendorShipment productStoreVendorShipmentToBeUpdated) {

		UpdateProductStoreVendorShipment com = new UpdateProductStoreVendorShipment(productStoreVendorShipmentToBeUpdated);

		int usedTicketId;

		synchronized (ProductStoreVendorShipmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreVendorShipmentUpdated.class,
				event -> sendProductStoreVendorShipmentChangedMessage(((ProductStoreVendorShipmentUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductStoreVendorShipment from the database
	 * 
	 * @param productStoreVendorShipmentId:
	 *            the id of the ProductStoreVendorShipment thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductStoreVendorShipmentById(@RequestParam(value = "productStoreVendorShipmentId") String productStoreVendorShipmentId) {

		DeleteProductStoreVendorShipment com = new DeleteProductStoreVendorShipment(productStoreVendorShipmentId);

		int usedTicketId;

		synchronized (ProductStoreVendorShipmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreVendorShipmentDeleted.class,
				event -> sendProductStoreVendorShipmentChangedMessage(((ProductStoreVendorShipmentDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductStoreVendorShipmentChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productStoreVendorShipment/\" plus one of the following: "
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
