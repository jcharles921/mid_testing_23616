package filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization logic if any
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        // URI for login, signup, and user creation endpoints
        String loginURI = req.getContextPath() + "/index.html";
        String loginPageURI = req.getContextPath() + "/login"; 
        String signupURI = req.getContextPath() + "/signup.html"; 
        String createUserURI = req.getContextPath() + "/createUser"; 

        // Check if the user is logged in by verifying session attribute
        boolean loggedIn = (session != null && session.getAttribute("userId") != null);

        // Check if the current request is for login, signup, createUser, or static resources
        boolean loginRequest = req.getRequestURI().equals(loginURI) || req.getRequestURI().equals(loginPageURI);
        boolean signupRequest = req.getRequestURI().equals(signupURI);
        boolean createUserRequest = req.getRequestURI().equals(createUserURI);
        boolean resourceRequest = req.getRequestURI().endsWith(".css") || req.getRequestURI().endsWith(".js") ||
                                  req.getRequestURI().endsWith(".jpg") || req.getRequestURI().endsWith(".png");

        // Allow access if logged in, or if it's a login/signup/createUser page or resource request
        if (loggedIn || loginRequest || signupRequest || createUserRequest || resourceRequest) {
            chain.doFilter(request, response); // Continue processing the request
        } else {
            res.sendRedirect(loginURI); // Redirect to login page if not logged in
        }
    }


    @Override
    public void destroy() {
        // Cleanup logic if any
    }
}
