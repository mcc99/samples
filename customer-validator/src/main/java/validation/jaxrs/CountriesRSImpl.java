package validation.jaxrs;

import org.springframework.stereotype.Service;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import validation.validator.Countries2Validator;


@Service
public class CountriesRSImpl implements CountriesRS {

    @Path("/{countryCode}/{anotherValue}")
    @GET
    @Countries2Validator
    public Response getCountryByCode2(String countryCode, String anotherValue) {
        return Response.ok("OK").build();
    }

}
