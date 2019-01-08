# win-proxy-selector
Win Proxy Selector is a ProxySelector implementation for Windows environment.

Win Proxy Selector is a fork of Proxy Vole, which is a Java library to auto detect the platform network proxy settings.
Win Proxy Selector is currently dedicated to the Windows environment.

Note: This library is a fork of [proxy-vole](https://github.com/MarkusBernhardt/proxy-vole/) which in turn is the now dead [proxy-vole](https://code.google.com/p/proxy-vole/) project by Bernd Rosstauscher hosted at Google Code.

## Introduction
The library provides a ProxySelector factory which reads the proxy settings from the system config of Windows, IE config (both on the Windows Registory) and provides you a ready to use proxy selector.

## Why a fork?
* We want more greedy approach in finding the appropriate proxy server than that used in Proxy Vole (eg. Tries provided fixed proxy address if failed to auto-detect one or the auto-detected proxy is not responding).

## Usage

### Using the default strategy to find the settings
```Java
// Instantiate a WinProxySelector giving a fallback proxy selector (usually the system's default proxy selector).

ProxySelector myProxySelector = new WinProxySelector(ProxySelector.getDefault());

// Proxies can be got by invoking select() method with a URI you want to connect to.
List<Proxy> proxies = myProxySelector.select(new URI("http://www.fusions.co.jp"));

// You can also install this ProxySelector as default ProxySelector for all connections.
ProxySelector.setDefault(proxySelector);
```

### How to handle proxy authentication
Some proxy servers request a login from the user before they will allow any connections. Proxy Vole 
has no support to handle this automatically. This needs to be done manually, because there is no way to read 
the login and password. These settings are stored encrypted. You need to install an authenticator in your Java
program manually and e.g. ask the user in a dialog to enter the username and password.
```Java
Authenticator.setDefault(new Authenticator() {
    protected PasswordAuthentication getPasswordAuthentication() {
        if (getRequestorType() == RequestorType.PROXY) {
            return new PasswordAuthentication("proxy-user", "proxy-password".toCharArray());
        } else { 
            return super.getPasswordAuthentication();
        }
    }               
});
```

### Choose the right proxy
Please be aware a Java ProxySelector returns a list of valid proxies for a given URL and sometimes simply 
choosing the first one is not good enough. Very often a check of the supported protocol is necessary.

The following code chooses the first HTTP/S proxy.
```Java
Proxy proxy = Proxy.NO_PROXY;

// Get list of proxies from default ProxySelector available for given URL
List<Proxy> proxies = null;
if (ProxySelector.getDefault() != null) {
    proxies = ProxySelector.getDefault().select(uri);
}

// Find first proxy for HTTP/S. Any DIRECT proxy in the list returned is only second choice
if (proxies != null) {
    loop: for (Proxy p : proxies) {
        switch (p.type()) {
        case HTTP:
            proxy = p;
            break loop;
        case DIRECT:
            proxy = p;
            break;
        }
    }
}
```

### Logging
Win Proxy Selector allows you to use arbitrary logging framework.
Install your logger which would redirect the logging output using Logger.setBackend() like this:
```Java
// Register MyLogger instance 
Logger.setBackend(new MyLogger());
```


