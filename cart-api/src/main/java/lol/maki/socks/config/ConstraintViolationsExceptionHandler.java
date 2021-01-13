package lol.maki.socks.config;

import java.util.Date;
import java.util.List;
import java.util.Map;

import am.ik.yavi.core.ConstraintViolationsException;
import am.ik.yavi.core.ViolationDetail;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ConstraintViolationsExceptionHandler extends ResponseEntityExceptionHandler {
	final HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

	@ExceptionHandler(ConstraintViolationsException.class)
	public ResponseEntity<Object> handleConstraintViolationsException(ConstraintViolationsException e) {
		final List<ViolationDetail> details = e.violations().details();
		return ResponseEntity.badRequest()
				.body(Map.of(
						"status", httpStatus.value(),
						"error", httpStatus.getReasonPhrase(),
						"message", e.getMessage(),
						"timestamp", new Date(),
						"details", details));
	}
}
