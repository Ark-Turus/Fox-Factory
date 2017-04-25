package ninja.invisiblecode.foxfactory.exceptions;

public class MissingAnnotationException extends RuntimeException {

	public MissingAnnotationException(String message) {
		super(message);
	}

	public MissingAnnotationException(Throwable t) {
		super(t);
	}

	public MissingAnnotationException(String message, Throwable t) {
		super(message, t);
	}

}
