package com.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import eu.europa.ec.taxud.vies.services.checkvat.CheckVatPortType;
import eu.europa.ec.taxud.vies.services.checkvat.CheckVatService;
import org.apache.log4j.Logger;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Handler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private static final Logger LOG = Logger.getLogger(Handler.class);

	// VIES WSDL URL
	private static final String VIES_HTTP_URL = "http://ec.europa.eu/taxation_customs/vies/checkVatService.wsdl";
	private static final String NAMESPACE_URI = "urn:ec.europa.eu:taxud:vies:services:checkVat";
	private static final String CHECK_VAT_SERVICE = "checkVatService";

	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
		LOG.info("received: " + input);

		final Map<String, Object> queryStringParameters = (Map<String, Object>) input.get("queryStringParameters");
		if (queryStringParameters == null) {
			return getApiGatewayResponse(400,
					"{\"error\":\"Missing query string parameters {number} and {country}. " +
							"The parameter {country} holds a two character EU country code, for example BE. " +
							"And {number} holds the VAT number you want to validate.\"");
		}

		String countryCode = (String)queryStringParameters.get("country");
		String vatNumber = (String)queryStringParameters.get("number");

		if (countryCode == null) {
			return getApiGatewayResponse(400,
					"{\"error\":\"Missing query string parameter {country}. " +
							"Country parameter holds a two character EU country code, for example BE.\"");
		}

		if (vatNumber == null) {
			return getApiGatewayResponse(400,
					"{\"error':\"Missing query string parameter {number}. " +
							"The VAT number you want to validate.\"");
		}

		LOG.info("country: " + countryCode);
		LOG.info("vatNumber: " + vatNumber);

		QName qname = new QName(NAMESPACE_URI, CHECK_VAT_SERVICE);

		URL url = null;
		try {
			url = new URL(VIES_HTTP_URL);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		javax.xml.ws.Service service = CheckVatService.create(url, qname);

		Holder<String> countryCodeHolder = new Holder<>(countryCode.trim().toUpperCase());
		Holder<String> vatNumberHolder = new Holder<>(vatNumber.trim());

		Holder<XMLGregorianCalendar> requestDateHolder = new Holder<>();
		Holder<Boolean> isValidHolder = new Holder<>();
		Holder<String> nameHolder = new Holder<>();
		Holder<String> addressHolder = new Holder<>();

		// Calls the SOAP service
		final CheckVatPortType port = service.getPort(CheckVatPortType.class);
		port.checkVat(countryCodeHolder, vatNumberHolder, requestDateHolder, isValidHolder, nameHolder, addressHolder);

		LOG.info("isValid : " + isValidHolder.value);
		LOG.info("name : " + nameHolder.value);
		LOG.info("address : " + addressHolder.value);

		String json = new Gson().toJson(new ResponseValue(isValidHolder.value, nameHolder.value, addressHolder.value));

		return getApiGatewayResponse(200, json);
	}

	private ApiGatewayResponse getApiGatewayResponse(final int statusCode,
													 final String message) {
		return ApiGatewayResponse.builder()
                .setStatusCode(statusCode)
                .setRawBody(message)
                .setHeaders(getHeaders())
                .build();
	}

	private Map<String, String> getHeaders() {
		Map<String, String> headers = new HashMap<>();
		headers.put("X-Powered-By", "Stephan Janssen (sja@devoxx.com) AWS EU VAT Lambda");
		headers.put("Content-Type", "application/json");
		return headers;
	}

	class ResponseValue {
		boolean isValid;
		String name;
		String address;

		ResponseValue(final boolean isValid, final String name, final String address) {
			this.isValid = isValid;
			this.name = name;
			this.address = address;
		}
	}
}
