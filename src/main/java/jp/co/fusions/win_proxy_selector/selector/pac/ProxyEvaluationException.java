package jp.co.fusions.win_proxy_selector.selector.pac;

/*****************************************************************************
 * Exception for PAC script errors.
 *
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

public class ProxyEvaluationException extends Exception {

	private static final long serialVersionUID = 1L;

	private final String script;

	/*************************************************************************
	 * Constructor
	 ************************************************************************/

	public ProxyEvaluationException() {
		super();
		this.script = null;
	}

	/*************************************************************************
	 * Constructor
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the causing exception for exception chaining.
	 ************************************************************************/

	public ProxyEvaluationException(String message, Throwable cause, String script) {
		super(message, cause);
		this.script = script;
	}

	/*************************************************************************
	 * Constructor
	 * 
	 * @param message
	 *            the error message.
	 ************************************************************************/

	public ProxyEvaluationException(String message) {
		super(message);
		this.script = null;
	}

	/*************************************************************************
	 * Constructor
	 * 
	 * @param cause
	 *            the causing exception for exception chaining.
	 ************************************************************************/

	public ProxyEvaluationException(Throwable cause) {
		super(cause);
		this.script = null;
	}

	public String getScript() {
		return script;
	}
}
