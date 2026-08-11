package com.gamemate.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationFirewallFilterTest {

    @Test
    void blocksDangerousHttpMethod() throws Exception {
        ApplicationFirewallFilter filter = firewall(100, 10);
        MockHttpServletRequest request = request("TRACE", "/api/game/list", "10.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(405);
    }

    @Test
    void hidesCommonScannerPaths() throws Exception {
        ApplicationFirewallFilter filter = firewall(100, 10);
        MockHttpServletRequest request = request("GET", "/.env", "10.0.0.2");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    void limitsAuthenticationBruteForce() throws Exception {
        ApplicationFirewallFilter filter = firewall(100, 2);

        assertThat(execute(filter, request("POST", "/api/user/login", "10.0.0.3"))).isEqualTo(200);
        assertThat(execute(filter, request("POST", "/api/user/login", "10.0.0.3"))).isEqualTo(200);
        MockHttpServletResponse blocked = executeResponse(filter, request("POST", "/api/user/login", "10.0.0.3"));

        assertThat(blocked.getStatus()).isEqualTo(429);
        assertThat(blocked.getHeader("Retry-After")).isEqualTo("300");
    }

    @Test
    void limitsGeneralRequestFlood() throws Exception {
        ApplicationFirewallFilter filter = firewall(2, 100);

        assertThat(execute(filter, request("GET", "/api/game/list", "10.0.0.4"))).isEqualTo(200);
        assertThat(execute(filter, request("GET", "/api/game/list", "10.0.0.4"))).isEqualTo(200);
        assertThat(execute(filter, request("GET", "/api/game/list", "10.0.0.4"))).isEqualTo(429);
    }

    @Test
    void blocksOversizedJsonBody() throws Exception {
        ApplicationFirewallFilter filter = firewall(100, 100);
        MockHttpServletRequest request = request("POST", "/api/chat/messages", "10.0.0.5");
        request.setContentType("application/json");
        request.setContent(new byte[4097]);
        MockHttpServletResponse response = executeResponse(filter, request);

        assertThat(response.getStatus()).isEqualTo(413);
    }

    private ApplicationFirewallFilter firewall(int generalLimit, int authLimit) {
        return new ApplicationFirewallFilter(true, generalLimit, authLimit, 4096, 8192);
    }

    private MockHttpServletRequest request(String method, String path, String clientIp) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setRemoteAddr("172.20.0.10");
        request.addHeader("X-Real-IP", clientIp);
        return request;
    }

    private int execute(ApplicationFirewallFilter filter, MockHttpServletRequest request) throws Exception {
        return executeResponse(filter, request).getStatus();
    }

    private MockHttpServletResponse executeResponse(ApplicationFirewallFilter filter,
                                                     MockHttpServletRequest request) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }
}
