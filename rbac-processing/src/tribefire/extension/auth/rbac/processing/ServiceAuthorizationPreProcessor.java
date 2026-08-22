package tribefire.extension.auth.rbac.processing;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

import com.braintribe.cfg.Required;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.security.reason.Forbidden;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.service.api.ReasonedServicePreProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.common.context.UserSessionAspect;
import com.braintribe.model.service.api.AuthorizedRequest;
import com.braintribe.model.usersession.UserSession;

public class ServiceAuthorizationPreProcessor implements ReasonedServicePreProcessor<AuthorizedRequest> {
	private static final Logger logger = Logger.getLogger(ServiceAuthorizationPreProcessor.class);
	private ServiceAuthorizationContext authorizationContext;
	private Function<String, CmdResolver> mdResolverLookup;
	private final Set<String> bypassRoles = Collections.emptySet();
	
	@Required
	public void setAuthorizationContext(ServiceAuthorizationContext authorizationContext) {
		this.authorizationContext = authorizationContext;
	}
	@Override
	public Maybe<? extends AuthorizedRequest> processReasoned(ServiceRequestContext requestContext, AuthorizedRequest request) {
		StatefulServiceAuthorization serviceAuthorization = new StatefulServiceAuthorization(requestContext, request);
		return serviceAuthorization.process();
	}
	
	private record RequestPriviledging(String domainId, AuthorizedRequest request, Set<String> allowRoles, Set<String> denyRoles) {
		public boolean isPriviledged() {
			return !allowRoles.isEmpty() || !denyRoles.isEmpty();
		}
		
		public String implicitAllowRole() {
			return "@allow-" + domainId + "-"+ request().entityType().getTypeSignature();
		}
		
		public String implicitDenyRole() {
			return "@deny-" + domainId + "-" + request().entityType().getTypeSignature();
		}
	}
	
	private class StatefulServiceAuthorization {
		private final ServiceRequestContext context;
		private final AuthorizedRequest request;
		private UserSession userSession;
		
		public StatefulServiceAuthorization(ServiceRequestContext context, AuthorizedRequest request) {
			this.context = context;
			this.request = request;
		}
		
		public UserSession getUserSession() {
			if (userSession == null) {
				userSession = context.getAttribute(UserSessionAspect.class);
			}

			return userSession;
		}
		
		private Set<String> getEffectiveRoles() {
			return getUserSession().getEffectiveRoles();
		}
		
		private String getUserId() {
			return getUserSession().getUser().getId();
		}
		
		public Maybe<? extends AuthorizedRequest> process() {
			String domainId = context.getDomainId();
			ServiceDomainAuthorizationResolver resolver = authorizationContext.getDomainResolver(domainId);
			ServiceAuthorization authorization =  resolver.resolve(request.entityType());
			Set<String> effectiveRoles = getEffectiveRoles();
			
			ServiceAccess determineAccess = authorization.determineAccess(effectiveRoles);
			String requestInfo = "user: " + getUserId() + ", request: " + request.entityType().getTypeSignature() + ", domain: " + domainId;
			
			switch (determineAccess) {
			case ALLOWED_PUBLIC:
				logger.debug("ACCESS ALLOWED [role-agnostic] " + requestInfo);
				return Maybe.complete(request);
			case ALLOWED_SUPERUSER:
				logger.debug("SUPERUSER ACCESS ALLOWED [role-governed] " + requestInfo);
				return Maybe.complete(request);
			case ALLOWED:
				logger.debug("ACCESS ALLOWED [role-governed] " + requestInfo);
				return Maybe.complete(request);
			case DENIED:
			default:
				logger.warn("ACCESS DENIED [role-governed] " + requestInfo + ", ip: " + context.getRequestorAddress());
				return Reasons.build(Forbidden.T).text("Insufficient priviledges to execute this request.").toMaybe();
			}
		}
	}
}