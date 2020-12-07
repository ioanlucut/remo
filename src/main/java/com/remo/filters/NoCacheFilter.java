package com.remo.filters;

import javax.faces.application.ResourceHandler;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This class is used to always revalidate the page when back button in the browser is
 * called. This way the back button will send a fullworthy HTTP request which should
 * recreate the view state and result in a page with a form with the proper view state
 * hidden field value.
 */
@WebFilter(servletNames = {"Faces Servlet"}, dispatcherTypes = {DispatcherType.REQUEST, DispatcherType.FORWARD})
public class NoCacheFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
        ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpRes = (HttpServletResponse) response;

        // Skip JSF resources (CSS/JS/Images/etc)
        if (!httpReq.getRequestURI().startsWith(httpReq.getContextPath() + ResourceHandler.RESOURCE_IDENTIFIER)) {
            // HTTP 1.1.
            httpRes.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            // HTTP 1.0.
            httpRes.setHeader("Pragma", "no-cache");
            // Proxies.
            httpRes.setDateHeader("Expires", 0);
        }

        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

}
