package com.example.employeemanagement.config;

import jakarta.servlet.Filter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class WebConfigTests {

    @Test
    void corsFilterAllowsDeployedFrontendWhenConfiguredOriginsOverrideDefaults() throws Exception {
        WebConfig webConfig = new WebConfig("http://localhost:5173/");
        MockHttpServletResponse response = preflight(webConfig, "https://employee-management-frontends.onrender.com");

        assertThat(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)).isEqualTo("https://employee-management-frontends.onrender.com");
        assertThat(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS)).contains("POST");
        assertThat(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS)).contains("content-type");
    }

    @Test
    void corsFilterAllowsRenderFrontendPreviewDomains() throws Exception {
        WebConfig webConfig = new WebConfig("");
        MockHttpServletResponse response = preflight(webConfig, "https://some-preview-name.onrender.com");

        assertThat(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)).isEqualTo("https://some-preview-name.onrender.com");
    }

    private MockHttpServletResponse preflight(WebConfig webConfig, String origin) throws Exception {
        FilterRegistrationBean<?> registration = webConfig.corsFilter();
        Filter filter = registration.getFilter();

        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/auth/login");
        request.addHeader(HttpHeaders.ORIGIN, origin);
        request.addHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST");
        request.addHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type");

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }
}
