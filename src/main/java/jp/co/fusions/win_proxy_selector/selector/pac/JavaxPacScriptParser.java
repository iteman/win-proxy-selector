package jp.co.fusions.win_proxy_selector.selector.pac;

import delight.nashornsandbox.NashornSandbox;
import delight.nashornsandbox.NashornSandboxes;
import java.lang.reflect.Method;
import jp.co.fusions.win_proxy_selector.util.Logger;
import jp.co.fusions.win_proxy_selector.util.Logger.LogLevel;

/*****************************************************************************
 * PAC parser using the Nashorn JavaScript engine bundled with Java 1.8<br>
 *
 * More information about PAC can be found there:<br>
 * <a href="http://en.wikipedia.org/wiki/Proxy_auto-config">Proxy_auto-config
 * </a><br>
 * <a href=
 * "http://homepages.tesco.net/~J.deBoynePollard/FGA/web-browser-auto-proxy-configuration.html">
 * web-browser-auto-proxy-configuration</a>
 *
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/
class JavaxPacScriptParser implements PacScriptParser {
	static final String SCRIPT_METHODS_OBJECT = "__pacutil";
	static final String SOURCE_NAME = JavaxPacScriptParser.class.getName();

	private final PacScriptSource source;
	private final NashornSandbox engine;

	/*************************************************************************
	 * Constructor
	 *
	 * @param source
	 *            the source for the PAC script.
	 * @throws ProxyEvaluationException
	 *             on error.
	 ************************************************************************/
	JavaxPacScriptParser(PacScriptSource source) throws ProxyEvaluationException {
		this.source = source;
		this.engine = setupEngine();
	}

	/*************************************************************************
	 * Initializes the JavaScript engine and adds aliases for the functions
	 * defined in ScriptMethods.
	 *
	 * @throws ProxyEvaluationException
	 *             on error.
	 ************************************************************************/
	private NashornSandbox setupEngine() throws ProxyEvaluationException {
		NashornSandbox engine = NashornSandboxes.create();
		engine.inject(SCRIPT_METHODS_OBJECT, new PacScriptMethods());
		// allow String
		engine.allow(String.class);

		Class<?> scriptMethodsClazz = ScriptMethods.class;
		Method[] scriptMethods = scriptMethodsClazz.getMethods();

		for (Method method : scriptMethods) {
			String name = method.getName();
			int args = method.getParameterTypes().length;
			StringBuilder toEval = new StringBuilder(name).append(" = function(");
			for (int i = 0; i < args; i++) {
				if (i > 0) {
					toEval.append(",");
				}
				toEval.append("arg").append(i);
			}
			toEval.append(") {return ");

			String functionCall = buildFunctionCallCode(name, args);

			// If return type is java.lang.String convert it to a JS string
			if (String.class.isAssignableFrom(method.getReturnType())) {
				functionCall = "String(" + functionCall + ")";
			}
			toEval.append(functionCall).append("; }");
			try {
				// Add functions with calls to Java object to global scope 
				engine.eval(toEval.toString());
			} catch (Exception e) {
				Logger.log(getClass(), LogLevel.ERROR, "JS evaluation error when creating alias for " + name + ".", e);
				throw new ProxyEvaluationException("Error setting up script engine", e, null);
			}
		}

		return engine;
	}

	/*************************************************************************
	 * Builds a JavaScript code snippet to call a function that we bind.
	 *
	 * @param functionName
	 *            of the bound function
	 * @param args
	 *            of the bound function
	 * @return the JS code to invoke the method.
	 ************************************************************************/

	private String buildFunctionCallCode(String functionName, int args) {
		StringBuilder functionCall = new StringBuilder();
		functionCall.append(SCRIPT_METHODS_OBJECT).append(".").append(functionName).append("(");
		for (int i = 0; i < args; i++) {
			if (i > 0) {
				functionCall.append(",");
			}
			functionCall.append("arg").append(i);
		}
		functionCall.append(")");
		return functionCall.toString();
	}

	/***************************************************************************
	 * Gets the source of the PAC script used by this parser.
	 *
	 * @return a PacScriptSource.
	 **************************************************************************/
	public PacScriptSource getScriptSource() {
		return this.source;
	}

	/*************************************************************************
	 * Evaluates the given URL and host against the PAC script.
	 *
	 * @param url
	 *            the URL to evaluate.
	 * @param host
	 *            the host name part of the URL.
	 * @return the script result.
	 * @throws ProxyEvaluationException
	 *             on execution error.
	 ************************************************************************/
	public String evaluate(String url, String host) throws ProxyEvaluationException {
		String script = this.source.getScriptContent();
		if (script.contains("FindProxyForURLEx")) {
			// for IPv6
			try {
				return evaluate(url, host, script, "FindProxyForURLEx");
			} catch (ProxyEvaluationException e) {
				Logger.log(getClass(), LogLevel.DEBUG, "FindProxyForURLEx failed. Trying FindProxyForURL. \n{0}\n{1}", e.getScript(), e);
				return evaluate(url, host, script, "FindProxyForURL");
			}
		} else {
			// for IPv4
			return evaluate(url, host, script, "FindProxyForURL");
		}
	}

	private String evaluate(String url, String host, String scriptBody, String findProxyFunctionName) throws ProxyEvaluationException {
		String evalMethod = " ;" + findProxyFunctionName + " (\"" + url + "\",\"" + host + "\")";
		String script = scriptBody + evalMethod;
		Logger.log(getClass(), LogLevel.INFO, "Evaluating PAC script from: {0}\n\n{1}\n", this.source.getName(), script);

		try {
			Object result = this.engine.eval(script);
			Logger.log(getClass(), LogLevel.INFO, "PAC script evaluates to : \"{0}\"", result);
			return (String) result;
		} catch (Exception e) {
			throw new ProxyEvaluationException("Error while executing PAC script: " + e.getMessage(), e, script);
		}

	}

}
