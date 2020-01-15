package top.cloudli.gateway.filter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Jwt token 验证过滤器
 */
@Slf4j
public class JwtVerifyFilter extends GenericFilterBean {

    private ObjectMapper objectMapper = new ObjectMapper();

    {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws ServletException, IOException {
        // 获取 Header 中的 jwt token
        String jwtToken = ((HttpServletRequest) servletRequest).getHeader("token");

        if (jwtToken == null) {
            // 没有 token 时交给 Spring Security 去处理，会返回 403
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            try {
                Claims claims = Jwts.parser()
                        .setSigningKey("cloudli.top")
                        .parseClaimsJws(jwtToken)
                        .getBody();

                String username = claims.getSubject();

                log.info("验证用户 {} Token.", username);

                List<GrantedAuthority> authorities = AuthorityUtils
                        .commaSeparatedStringToAuthorityList((String) claims.get("authorities"));

                UsernamePasswordAuthenticationToken token =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(token);

                filterChain.doFilter(servletRequest, servletResponse);
            } catch (JwtException e) {
                // Jwt 解析异常的处理
                log.error(e.getMessage());
                ((HttpServletResponse)servletResponse).setStatus(401);
            }
        }
    }
}