package roomescape.support.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.domain.Member;
import roomescape.dto.response.MemberResponse;
import roomescape.service.MemberLoginService;

@Component
public class MemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final MemberLoginService memberLoginService;
    private final AuthorizationExtractor authorizationExtractor;

    public MemberArgumentResolver(MemberLoginService memberLoginService,
                                  AuthorizationExtractor authorizationExtractor) {
        this.memberLoginService = memberLoginService;
        this.authorizationExtractor = authorizationExtractor;
    }

    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return parameter.getParameterType().equals(Member.class);
    }

    @Override
    public Object resolveArgument(final MethodParameter parameter, final ModelAndViewContainer mavContainer,
                                  final NativeWebRequest webRequest, final WebDataBinderFactory binderFactory) {
        final HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        final String token = authorizationExtractor.extract(request);
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("인증 정보가 없습니다.");
        }
        final MemberResponse member = memberLoginService.findByToken(token);
        return new Member(member.id(), member.name(), member.email(), member.memberRole());
    }
}
