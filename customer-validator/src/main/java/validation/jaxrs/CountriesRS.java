package validation.jaxrs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import validation.validator.Countries2Validator;

@Path("/validator")
public interface CountriesRS {

    @Path("/{countryCode}/{anotherValue}")
    @GET
    @Countries2Validator
    public Response getCountryByCode2(@PathParam("countryCode") String countryCode, @PathParam("anotherValue") String anotherValue);
}
