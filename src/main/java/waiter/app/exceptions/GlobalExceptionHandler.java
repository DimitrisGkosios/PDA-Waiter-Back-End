package waiter.app.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import waiter.app.Enums.PaymentMethod;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 🔸 Enum conversion errors (π.χ. λάθος payment method)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleEnumConversionError(MethodArgumentTypeMismatchException ex) {
        if (ex.getRequiredType() == PaymentMethod.class) {
            return ResponseEntity
                    .badRequest()
                    .body("❌ Invalid payment method. Allowed values: CARD or CASH.");
        }
        return ResponseEntity
                .badRequest()
                .body("❌ Invalid parameter: " + ex.getName());
    }

    // 🔸 Missing query or form parameters (π.χ. ?status=ABSENT δεν υπάρχει)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> handleMissingParams(MissingServletRequestParameterException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("❌ Missing required parameter: " + ex.getParameterName());
    }

    // ✅ Collect ALL validation errors into a JSON map
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err -> {
            errors.put(err.getField(), err.getDefaultMessage());
        });

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }

    // 🔸 Catch-all (fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllExceptions(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Something went wrong: " + ex.getMessage());
    }
}
