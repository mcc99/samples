Fails to execute validation as expected.  After Maven install, use Jetty > run to start the server.
Use Postman or another utility to hit http://localhost:9086/validation/validator/US/USA
The problem is that an error should get returned since US and USA are diff. values.  I would
expect it to return OK only if for example the URL was http://localhost:9086/validation/validator/USA/USA

Please reply to the Jetty Users list with comments or to mcc99@hotmail.com.  TIA.