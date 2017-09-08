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
import com.skytala.eCommerce.command.AddProductStoreVendorPayment;
import com.skytala.eCommerce.command.DeleteProductStoreVendorPayment;
import com.skytala.eCommerce.command.UpdateProductStoreVendorPayment;
import com.skytala.eCommerce.entity.ProductStoreVendorPayment;
import com.skytala.eCommerce.entity.ProductStoreVendorPaymentMapper;
import com.skytala.eCommerce.event.ProductStoreVendorPaymentAdded;
import com.skytala.eCommerce.event.ProductStoreVendorPaymentDeleted;
import com.skytala.eCommerce.event.ProductStoreVendorPaymentFound;
import com.skytala.eCommerce.event.ProductStoreVendorPaymentUpdated;
import com.skytala.eCommerce.query.FindProductStoreVendorPaymentsBy;

@RestController
@RequestMapping("/api/productStoreVendorPayment")
public class ProductStoreVendorPaymentController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductStoreVendorPayment>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductStoreVendorPaymentController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductStoreVendorPayment
	 * @return a List with the ProductStoreVendorPayments
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductStoreVendorPayment> findProductStoreVendorPaymentsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductStoreVendorPaymentsBy query = new FindProductStoreVendorPaymentsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductStoreVendorPaymentController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreVendorPaymentFound.class,
				event -> sendProductStoreVendorPaymentsFoundMessage(((ProductStoreVendorPaymentFound) event).getProductStoreVendorPayments(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductStoreVendorPaymentsFoundMessage(List<ProductStoreVendorPayment> productStoreVendorPayments, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productStoreVendorPayments);
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
	public boolean createProductStoreVendorPayment(HttpServletRequest request) {

		ProductStoreVendorPayment productStoreVendorPaymentToBeAdded = new ProductStoreVendorPayment();
		try {
			productStoreVendorPaymentToBeAdded = ProductStoreVendorPaymentMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductStoreVendorPayment(productStoreVendorPaymentToBeAdded);

	}

	/**
	 * creates a new ProductStoreVendorPayment entry in the ofbiz database
	 * 
	 * @param productStoreVendorPaymentToBeAdded
	 *            the ProductStoreVendorPayment thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductStoreVendorPayment(ProductStoreVendorPayment productStoreVendorPaymentToBeAdded) {

		AddProductStoreVendorPayment com = new AddProductStoreVendorPayment(productStoreVendorPaymentToBeAdded);
		int usedTicketId;

		synchronized (ProductStoreVendorPaymentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreVendorPaymentAdded.class,
				event -> sendProductStoreVendorPaymentChangedMessage(((ProductStoreVendorPaymentAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductStoreVendorPayment(HttpServletRequest request) {

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

		ProductStoreVendorPayment productStoreVendorPaymentToBeUpdated = new ProductStoreVendorPayment();

		try {
			productStoreVendorPaymentToBeUpdated = ProductStoreVendorPaymentMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductStoreVendorPayment(productStoreVendorPaymentToBeUpdated);

	}

	/**
	 * Updates the ProductStoreVendorPayment with the specific Id
	 * 
	 * @param productStoreVendorPaymentToBeUpdated the ProductStoreVendorPayment thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductStoreVendorPayment(ProductStoreVendorPayment productStoreVendorPaymentToBeUpdated) {

		UpdateProductStoreVendorPayment com = new UpdateProductStoreVendorPayment(productStoreVendorPaymentToBeUpdated);

		int usedTicketId;

		synchronized (ProductStoreVendorPaymentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreVendorPaymentUpdated.class,
				event -> sendProductStoreVendorPaymentChangedMessage(((ProductStoreVendorPaymentUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductStoreVendorPayment from the database
	 * 
	 * @param productStoreVendorPaymentId:
	 *            the id of the ProductStoreVendorPayment thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductStoreVendorPaymentById(@RequestParam(value = "productStoreVendorPaymentId") String productStoreVendorPaymentId) {

		DeleteProductStoreVendorPayment com = new DeleteProductStoreVendorPayment(productStoreVendorPaymentId);

		int usedTicketId;

		synchronized (ProductStoreVendorPaymentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreVendorPaymentDeleted.class,
				event -> sendProductStoreVendorPaymentChangedMessage(((ProductStoreVendorPaymentDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductStoreVendorPaymentChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productStoreVendorPayment/\" plus one of the following: "
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
