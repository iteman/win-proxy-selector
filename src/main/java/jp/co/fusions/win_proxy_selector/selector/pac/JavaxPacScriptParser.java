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
		try {
			// for IPv6
			return evaluate(url, host, "FindProxyForURLEx");
		} catch (ProxyEvaluationException e) {
			Logger.log(getClass(), LogLevel.DEBUG, "FindProxyForURLEx failed. Trying FindProxyForURL. \n{0}\n{1}", e.getScript(), e);
			return evaluate(url, host, "FindProxyForURL");
		}
	}

	private String evaluate(String url, String host, String findProxyFunctionName) throws ProxyEvaluationException {
		StringBuilder script = null;
		try {
			script = new StringBuilder(this.source.getScriptContent());
			String evalMethod = " ;" + findProxyFunctionName + " (\"" + url + "\",\"" + host + "\")";
			script.append(evalMethod);
			Object result = this.engine.eval(script.toString());
			return (String) result;
		} catch (Exception e) {
			String script2 = (script == null) ? "" : script.toString();
			throw new ProxyEvaluationException("Error while executing PAC script: " + e.getMessage(), e, script2);
		}

	}

}
