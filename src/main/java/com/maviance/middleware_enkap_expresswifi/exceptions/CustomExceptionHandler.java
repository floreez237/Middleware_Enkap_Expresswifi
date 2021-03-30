package com.maviance.middleware_enkap_expresswifi.exceptions;


import com.maviance.middleware_enkap_expresswifi.enums.ExpessWifiStatus;
import com.maviance.middleware_enkap_expresswifi.model.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@ResponseBody
public class CustomExceptionHandler {
    @ExceptionHandler(ExpressWifiException.class)
    public ErrorResponse handleExpressWifiException(ExpressWifiException exception) {
        return exception.getErrorResponse();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidMethodArgument(MethodArgumentNotValidException exception) {
        final FieldError fieldError = exception.getBindingResult().getFieldError();
        assert fieldError != null;
        String fieldName = fieldError.getField();
        ErrorResponse.ErrorObject errorObject;
        if (fieldName.equals("hmac")) {
            errorObject = new ErrorResponse.ErrorObject("The timestamp parameter is required",
                    "MissingTimestampException", 101, "APnIyP3Av3DcI1zH5Vam-GL");
        } else if (fieldName.equals("timestamp")) {
            errorObject = new ErrorResponse.ErrorObject("The hmac parameter is required",
                    "MissingHmacException", 100, "A1lgQj37a-Mp22Y3PgBu5dG");
        } else if (fieldName.equals("paymentId")) {
            errorObject = new ErrorResponse.ErrorObject("The payment ID parameter is required",
                    "MissingHPaymentIdException", 102, "sdsdsasdgBu5dG");
        } else {
            errorObject = new ErrorResponse.ErrorObject(fieldError.getDefaultMessage(),
                    "Invalid Method Argument", 106, "fbgenexception");
        }
        ErrorResponse response = new ErrorResponse(errorObject);
        response.setExpessWifiStatus(ExpessWifiStatus.FAILURE);
        return response;
    }

    @ExceptionHandler(ParameterNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleParameterNotFound() {
        return "An Error has occurred";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus
    public ErrorResponse handleUnpredictedException(Exception exception) {
        ErrorResponse.ErrorObject errorObject = new ErrorResponse.ErrorObject(exception.getMessage(), "Internal Server Error", 500, "fbunpredicted");
        ErrorResponse response = new ErrorResponse(errorObject);
        response.setExpessWifiStatus(ExpessWifiStatus.FAILURE);
        return response;
    }


}
